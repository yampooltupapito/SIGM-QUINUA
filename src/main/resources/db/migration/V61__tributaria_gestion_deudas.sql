-- ============================================================================
-- Módulo 4: Recaudación Tributaria Municipal - Municipalidad Distrital de Quinua
-- Prompt 60: Gestión, Clasificación, Control y Seguimiento de Deudas Tributarias
-- ============================================================================

-- 1. Tabla de Parámetros Normativos de Intereses Moratorios y Compensatorios
CREATE TABLE IF NOT EXISTS tributaria_parametros_interes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    concepto_id UUID REFERENCES tributaria_conceptos(id) ON DELETE SET NULL,
    tipo_interes VARCHAR(30) NOT NULL,
    tasa NUMERIC(8,4) NOT NULL CHECK (tasa >= 0),
    unidad_temporal VARCHAR(20) NOT NULL DEFAULT 'MENSUAL',
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    base_legal VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_param_interes_concepto ON tributaria_parametros_interes(concepto_id);
CREATE INDEX IF NOT EXISTS idx_trib_param_interes_vigencia ON tributaria_parametros_interes(fecha_inicio_vigencia, fecha_fin_vigencia);
CREATE INDEX IF NOT EXISTS idx_trib_param_interes_estado ON tributaria_parametros_interes(estado);

-- 2. Tabla de Seguimiento Administrativo de Deudas Tributarias
CREATE TABLE IF NOT EXISTS tributaria_seguimiento_deudas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    obligacion_id UUID NOT NULL UNIQUE REFERENCES tributaria_obligaciones(id) ON DELETE CASCADE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    clasificacion VARCHAR(40) NOT NULL,
    estado_seguimiento VARCHAR(30) NOT NULL DEFAULT 'EN_SEGUIMIENTO',
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_revision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_seg_deuda_obligacion ON tributaria_seguimiento_deudas(obligacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_seg_deuda_contribuyente ON tributaria_seguimiento_deudas(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_seg_deuda_clasif ON tributaria_seguimiento_deudas(clasificacion);
CREATE INDEX IF NOT EXISTS idx_trib_seg_deuda_estado ON tributaria_seguimiento_deudas(estado_seguimiento);
CREATE INDEX IF NOT EXISTS idx_trib_seg_deuda_responsable ON tributaria_seguimiento_deudas(responsable_id);

-- 3. Tabla de Gestiones Administrativas de Seguimiento
CREATE TABLE IF NOT EXISTS tributaria_gestiones_deuda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seguimiento_id UUID NOT NULL REFERENCES tributaria_seguimiento_deudas(id) ON DELETE CASCADE,
    tipo_gestion VARCHAR(40) NOT NULL,
    fecha_gestion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    resultado VARCHAR(150) NOT NULL,
    observaciones TEXT,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_gest_deuda_seguimiento ON tributaria_gestiones_deuda(seguimiento_id);
CREATE INDEX IF NOT EXISTS idx_trib_gest_deuda_fecha ON tributaria_gestiones_deuda(fecha_gestion);

-- 4. Tabla de Compromisos Informativos de Pago
CREATE TABLE IF NOT EXISTS tributaria_compromisos_deuda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seguimiento_id UUID NOT NULL REFERENCES tributaria_seguimiento_deudas(id) ON DELETE CASCADE,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_comprometida DATE NOT NULL,
    monto_comprometido NUMERIC(14,2) CHECK (monto_comprometido >= 0),
    observaciones TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_comp_deuda_seguimiento ON tributaria_compromisos_deuda(seguimiento_id);
CREATE INDEX IF NOT EXISTS idx_trib_comp_deuda_fecha ON tributaria_compromisos_deuda(fecha_comprometida);
CREATE INDEX IF NOT EXISTS idx_trib_comp_deuda_estado ON tributaria_compromisos_deuda(estado);

-- 5. Semilla de Parámetro Oficial de Tasa de Interés Moratorio (TIM) 2026
INSERT INTO tributaria_parametros_interes (
    id, codigo, nombre, concepto_id, tipo_interes, tasa, unidad_temporal,
    fecha_inicio_vigencia, fecha_fin_vigencia, base_legal, estado, version, created_by
)
VALUES (
    'c0000001-0000-0000-0000-000000000001',
    'TIM-2026-SUNAT',
    'Tasa de Interés Moratorio Mensual 2026 (0.90% mensual)',
    NULL,
    'TIM_MENSUAL',
    0.9000,
    'MENSUAL',
    '2026-01-01',
    NULL,
    'Resolución de Superintendencia N° 044-2021/SUNAT y Art. 33 del TUO del Código Tributario (D.S. 133-2013-EF)',
    TRUE,
    0,
    'SYSTEM'
)
ON CONFLICT (codigo) DO NOTHING;

-- 6. Registro de Permisos de Seguridad RBAC
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_VER', 'Ver Cartera de Deudas Tributarias', 'VIEW',
       'Permite consultar el listado y métricas de cartera tributaria y saldos pendientes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_VER_DETALLE', 'Ver Detalle y Ficha de Deuda', 'VIEW',
       'Permite ver la ficha detallada de una deuda, sus obligaciones y movimientos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_CLASIFICAR', 'Clasificar Cartera de Deudas', 'UPDATE',
       'Permite clasificar administrativamente una obligación pendiente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_ASIGNAR', 'Asignar Responsable de Seguimiento', 'UPDATE',
       'Permite asignar o reasignar personal responsable para el seguimiento de la deuda', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_REGISTRAR_GESTION', 'Registrar Gestión Administrativa', 'CREATE',
       'Permite registrar gestiones de contacto, revisión y atención presencial', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO', 'Registrar Compromiso Informativo', 'CREATE',
       'Permite asentar compromisos informativos de pago declarados por el contribuyente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_CONFIGURAR_INTERESES', 'Configurar Tasas de Interés', 'MANAGE',
       'Permite configurar los parámetros legales y vigencias de tasas de interés moratorio', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_CALCULAR_INTERESES', 'Simular Cálculo de Intereses', 'VIEW',
       'Permite simular el cálculo de intereses moratorios a una fecha de corte', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_REVISAR_INTERESES', 'Revisar Parámetros de Interés', 'VIEW',
       'Permite consultar la vigencia e historial normativo de tasas de interés', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_VER_HISTORIAL', 'Ver Historial de Seguimiento', 'VIEW',
       'Permite consultar la trazabilidad y eventos de seguimiento de deudas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_EXPORTAR', 'Exportar Cartera y Reportes de Deuda', 'EXPORT',
       'Permite exportar información de cartera de deudas en formatos oficiales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DEUDAS_REPORTES', 'Consultar Reportes de Cartera', 'VIEW',
       'Permite generar reportes consolidados por concepto, período y tramos de antigüedad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 7. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'TRIBUTARIA_DEUDAS_%'
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_TRIBUTACION'
  AND p.codigo IN (
      'TRIBUTARIA_DEUDAS_VER',
      'TRIBUTARIA_DEUDAS_VER_DETALLE',
      'TRIBUTARIA_DEUDAS_CLASIFICAR',
      'TRIBUTARIA_DEUDAS_ASIGNAR',
      'TRIBUTARIA_DEUDAS_REGISTRAR_GESTION',
      'TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO',
      'TRIBUTARIA_DEUDAS_CALCULAR_INTERESES',
      'TRIBUTARIA_DEUDAS_REVISAR_INTERESES',
      'TRIBUTARIA_DEUDAS_VER_HISTORIAL',
      'TRIBUTARIA_DEUDAS_EXPORTAR',
      'TRIBUTARIA_DEUDAS_REPORTES'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_CAJERO'
  AND p.codigo IN (
      'TRIBUTARIA_DEUDAS_VER',
      'TRIBUTARIA_DEUDAS_VER_DETALLE',
      'TRIBUTARIA_DEUDAS_CALCULAR_INTERESES',
      'TRIBUTARIA_DEUDAS_REGISTRAR_COMPROMISO'
  )
ON CONFLICT DO NOTHING;
