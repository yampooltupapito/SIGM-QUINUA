-- ==============================================================================
-- SIGM-QUINUA: Sistema Integral de Gestión Municipal
-- Municipalidad Distrital de Quinua (Huamanga - Ayacucho)
-- MÓDULO 4: RECAUDACIÓN TRIBUTARIA MUNICIPAL
-- V65: Centro de Reportes, Inteligencia Tributaria, Estadísticas y Análisis Financiero
-- ==============================================================================

-- 1. Catálogo Institucional de Definiciones de Reportes Tributarios
CREATE TABLE IF NOT EXISTS tributaria_definiciones_reportes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(40) NOT NULL,
    permiso_requerido VARCHAR(100) NOT NULL,
    formato_permitido VARCHAR(50) NOT NULL DEFAULT 'PDF,EXCEL,CSV',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_cat_reporte CHECK (categoria IN (
        'RECAUDACION', 'OBLIGACIONES', 'DEUDAS', 'MOROSIDAD', 'CONTRIBUYENTES', 
        'PREDIOS', 'CAJA', 'FRACCIONAMIENTOS', 'NOTIFICACIONES', 'DOCUMENTOS', 'INDICADORES'
    ))
);

CREATE INDEX IF NOT EXISTS idx_def_rep_trib_cat ON tributaria_definiciones_reportes(categoria);
CREATE INDEX IF NOT EXISTS idx_def_rep_trib_cod ON tributaria_definiciones_reportes(codigo);

-- 2. Metas de Recaudación Tributaria Presupuestal e Institucional
CREATE TABLE IF NOT EXISTS tributaria_metas_recaudacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ejercicio INTEGER NOT NULL CHECK (ejercicio >= 2000 AND ejercicio <= 2100),
    periodo INTEGER NOT NULL DEFAULT 0 CHECK (periodo >= 0 AND periodo <= 12), -- 0 = Meta Global Anual, 1..12 = Mensual
    concepto_id UUID REFERENCES tributaria_conceptos(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    importe_meta NUMERIC(14, 2) NOT NULL CHECK (importe_meta > 0),
    documento_sustento_id UUID,
    aprobado BOOLEAN NOT NULL DEFAULT FALSE,
    aprobado_por BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_aprobacion TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    observacion VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_meta_estado CHECK (estado IN ('BORRADOR', 'VIGENTE', 'MODIFICADA', 'CERRADA', 'ANULADA'))
);

CREATE INDEX IF NOT EXISTS idx_metas_trib_ejercicio ON tributaria_metas_recaudacion(ejercicio, periodo);
CREATE INDEX IF NOT EXISTS idx_metas_trib_concepto ON tributaria_metas_recaudacion(concepto_id);

-- 3. Preferencias de Filtros Guardados por Usuario
CREATE TABLE IF NOT EXISTS tributaria_preferencias_reportes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    reporte_codigo VARCHAR(50) NOT NULL,
    nombre_configuracion VARCHAR(100) NOT NULL,
    filtros_json TEXT NOT NULL,
    es_predeterminado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_pref_rep_trib_usr ON tributaria_preferencias_reportes(usuario_id, reporte_codigo);

-- 4. Historial y Auditoría de Exportaciones de Reportes Tributarios
CREATE TABLE IF NOT EXISTS tributaria_historial_exportaciones_reporte (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporte_codigo VARCHAR(50) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP WITH TIME ZONE,
    formato VARCHAR(10) NOT NULL CHECK (formato IN ('PDF', 'EXCEL', 'CSV')),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'COMPLETADO', 'FALLIDO')),
    filtros_aplicados TEXT,
    tamanio_bytes BIGINT,
    nombre_archivo VARCHAR(200),
    checksum_sha256 VARCHAR(64),
    mensaje_error VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_hist_exp_trib_usr ON tributaria_historial_exportaciones_reporte(usuario_id, fecha_solicitud);
CREATE INDEX IF NOT EXISTS idx_hist_exp_trib_rep ON tributaria_historial_exportaciones_reporte(reporte_codigo);

-- 5. Semillas de Permisos RBAC Institucionales para Reportes Tributarios
-- Cumpliendo con el constraint chk_permiso_accion: ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'ASSIGN', 'EXPORT', 'MANAGE')
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_ACCEDER', 'Acceso al Centro de Reportes Tributarios', 'VIEW',
       'Permite ingresar al Centro de Reportes, Inteligencia y Estadísticas Tributarias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_RECAUDACION', 'Ver Reportes de Recaudación', 'VIEW',
       'Permite consultar recaudación confirmada por períodos, conceptos y medios de pago', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_OBLIGACIONES', 'Ver Reportes de Obligaciones', 'VIEW',
       'Permite consultar emisión y evolución de obligaciones tributarias aprobadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_DEUDAS', 'Ver Reportes de Cartera y Deudas', 'VIEW',
       'Permite analizar cartera pendiente, saldos vencidos y exigibles', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_MOROSIDAD', 'Ver Análisis de Morosidad', 'VIEW',
       'Permite evaluar índices de morosidad, recuperación y tramos de antigüedad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_CONTRIBUYENTES', 'Ver Estadísticas de Contribuyentes', 'VIEW',
       'Permite analizar universo de contribuyentes, distribución y cumplimiento', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_PREDIOS', 'Ver Estadísticas de Predios', 'VIEW',
       'Permite consultar distribución catastral y tributaria de predios urbanos y rústicos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_CAJA', 'Ver Reportes de Cajas Municipales', 'VIEW',
       'Permite consultar sesiones de caja, arqueos y conciliaciones de recaudación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_FRACCIONAMIENTOS', 'Ver Reportes de Fraccionamientos', 'VIEW',
       'Permite evaluar convenios de fraccionamiento, cuotas y cumplimiento', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_NOTIFICACIONES', 'Ver Estadísticas de Notificaciones', 'VIEW',
       'Permite monitorear diligencias de notificación, entrega y validez legal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_DOCUMENTOS', 'Ver Reportes de Documentos Emitidos', 'VIEW',
       'Permite auditar recibos, constancias y comprobantes emitidos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_CONFIGURAR_METAS', 'Configurar Metas de Recaudación', 'MANAGE',
       'Permite registrar y aprobar metas institucionales de recaudación tributaria', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_EXPORTAR_PDF', 'Exportar Reportes a PDF', 'EXPORT',
       'Permite generar y descargar reportes oficiales en formato PDF institucional', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_EXPORTAR_EXCEL', 'Exportar Reportes a Excel', 'EXPORT',
       'Permite exportar hojas de cálculo en formato XLSX con fórmulas protegidas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER_HISTORIAL', 'Ver Historial de Exportaciones', 'VIEW',
       'Permite consultar la bitácora de reportes y descargas realizadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignar Permisos a Roles Institucionales Relevantes
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ADMINISTRADOR_SISTEMA', 'SUBGERENTE_RENTAS', 'ESPECIALISTA_TRIBUTARIO')
  AND p.codigo IN (
      'TRIBUTARIA_REPORTES_ACCEDER', 'TRIBUTARIA_REPORTES_VER_RECAUDACION',
      'TRIBUTARIA_REPORTES_VER_OBLIGACIONES', 'TRIBUTARIA_REPORTES_VER_DEUDAS',
      'TRIBUTARIA_REPORTES_VER_MOROSIDAD', 'TRIBUTARIA_REPORTES_VER_CONTRIBUYENTES',
      'TRIBUTARIA_REPORTES_VER_PREDIOS', 'TRIBUTARIA_REPORTES_VER_CAJA',
      'TRIBUTARIA_REPORTES_VER_FRACCIONAMIENTOS', 'TRIBUTARIA_REPORTES_VER_NOTIFICACIONES',
      'TRIBUTARIA_REPORTES_VER_DOCUMENTOS', 'TRIBUTARIA_REPORTES_CONFIGURAR_METAS',
      'TRIBUTARIA_REPORTES_EXPORTAR_PDF', 'TRIBUTARIA_REPORTES_EXPORTAR_EXCEL',
      'TRIBUTARIA_REPORTES_VER_HISTORIAL'
  )
ON CONFLICT DO NOTHING;

-- 7. Semilla de Catálogo de Reportes
INSERT INTO tributaria_definiciones_reportes (codigo, nombre, descripcion, categoria, permiso_requerido) VALUES
('REP_REC_CONSOLIDADA', 'Recaudación Consolidada por Período', 'Resumen ejecutivo de ingresos confirmados por fechas y ejercicios', 'RECAUDACION', 'TRIBUTARIA_REPORTES_VER_RECAUDACION'),
('REP_REC_CONCEPTO', 'Recaudación por Concepto Tributario', 'Detalle de pagos aplicados a Impuesto Predial, Arbitrios y otros derechos', 'RECAUDACION', 'TRIBUTARIA_REPORTES_VER_RECAUDACION'),
('REP_REC_MEDIOS', 'Recaudación por Medio de Pago y Canal', 'Distribución de ingresos en efectivo, tarjeta POS, transferencias y depósitos', 'RECAUDACION', 'TRIBUTARIA_REPORTES_VER_RECAUDACION'),
('REP_REC_DIARIA', 'Recaudación Diaria de Caja', 'Corte cronológico diario de pagos ingresados y confirmados en el ejercicio', 'RECAUDACION', 'TRIBUTARIA_REPORTES_VER_RECAUDACION'),
('REP_OBL_EMISION', 'Emisión y Aprobación de Obligaciones', 'Estadísticas de valores y cuotas tributarias emitidas en el ejercicio fiscal', 'OBLIGACIONES', 'TRIBUTARIA_REPORTES_VER_OBLIGACIONES'),
('REP_CARTERA_SALDOS', 'Cartera Pendiente y Saldos Exigibles', 'Análisis financiero de deudas corrientes, vencidas y en exigibilidad', 'DEUDAS', 'TRIBUTARIA_REPORTES_VER_DEUDAS'),
('REP_MOROSIDAD_TRAMOS', 'Morosidad por Tramos de Antigüedad', 'Distribución de deuda por tramos temporales (0-30, 31-90, 91-180, 181-365, >1 año)', 'MOROSIDAD', 'TRIBUTARIA_REPORTES_VER_MOROSIDAD'),
('REP_CAJA_SESIONES', 'Arqueos y Sesiones de Cajas Municipales', 'Control de aperturas, cierres, cobros y diferencias en ventanilla municipal', 'CAJA', 'TRIBUTARIA_REPORTES_VER_CAJA'),
('REP_CONT_PADRON', 'Padrón y Desempeño de Contribuyentes', 'Estadísticas de cumplimiento, puntualidad y universo de contribuyentes de Quinua', 'CONTRIBUYENTES', 'TRIBUTARIA_REPORTES_VER_CONTRIBUYENTES'),
('REP_PREDIOS_CATASTRO', 'Padrón Predial y Distribución Territorial', 'Consolidado de predios urbanos y rústicos, autovalúos y cobertura catastral', 'PREDIOS', 'TRIBUTARIA_REPORTES_VER_PREDIOS'),
('REP_FRACC_CONVENIOS', 'Convenios de Fraccionamiento y Recuperación', 'Seguimiento de convenios suscritos, cuotas amortizadas y morosidad fraccionada', 'FRACCIONAMIENTOS', 'TRIBUTARIA_REPORTES_VER_FRACCIONAMIENTOS'),
('REP_NOTIF_ACTUACIONES', 'Actuaciones y Notificaciones Tributarias', 'Efectividad de entrega, diligencias por medio y validez legal según TUO C.T.', 'NOTIFICACIONES', 'TRIBUTARIA_REPORTES_VER_NOTIFICACIONES')
ON CONFLICT (codigo) DO NOTHING;

