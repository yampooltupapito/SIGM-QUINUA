-- ============================================================================
-- SIGM-QUINUA: Migración V36 — Reportes, Estadísticas y Documentos de Mercados
-- ============================================================================

-- 1. Tabla de Historial de Exportaciones de Mercados
CREATE TABLE IF NOT EXISTS mercados_historial_exportaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    tipo_reporte VARCHAR(50) NOT NULL,
    formato VARCHAR(20) NOT NULL,
    mercado_id UUID,
    filtros_aplicados TEXT,
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'COMPLETADO',
    cantidad_registros INTEGER NOT NULL DEFAULT 0,
    nombre_archivo VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamanio_bytes BIGINT NOT NULL DEFAULT 0,
    documento_id BIGINT,
    ip_origen VARCHAR(45),
    user_agent VARCHAR(255),
    error_detalle TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mercados_hist_exp_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mercados_hist_exp_mercado FOREIGN KEY (mercado_id) REFERENCES mercados_mercados(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_mercados_hist_exp_usuario ON mercados_historial_exportaciones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_mercados_hist_exp_fecha ON mercados_historial_exportaciones(fecha_generacion DESC);
CREATE INDEX IF NOT EXISTS idx_mercados_hist_exp_tipo ON mercados_historial_exportaciones(tipo_reporte);

-- 2. Tabla de Configuraciones y Definiciones de Reportes
CREATE TABLE IF NOT EXISTS mercados_reporte_configuraciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    descripcion TEXT,
    tipo_fuente VARCHAR(50) NOT NULL DEFAULT 'INTERNO',
    configuracion_columnas JSONB,
    configuracion_filtros JSONB,
    formato_permitido VARCHAR(50) NOT NULL DEFAULT 'AMBOS',
    permiso_requerido VARCHAR(100),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_mercados_rep_conf_cat ON mercados_reporte_configuraciones(categoria);

-- 3. Inserción de Catálogo de Reportes Preconfigurados
INSERT INTO mercados_reporte_configuraciones (codigo, nombre, categoria, descripcion, tipo_fuente, formato_permitido, permiso_requerido)
VALUES
    ('REP_MERC_EJECUTIVO', 'Dashboard Ejecutivo Consolidado', 'EJECUTIVO', 'Resumen integral de ocupación, comerciantes, recaudación y fiscalización', 'AGREGADO', 'AMBOS', 'MERCADOS_REPORTES_EJECUTIVO'),
    ('REP_MERC_OCUPACION', 'Análisis de Ocupación Espacial', 'INFRAESTRUCTURA', 'Distribución de puestos ocupados, disponibles y restringidos por mercado y sector', 'TABULAR', 'AMBOS', 'MERCADOS_REPORTES_OCUPACION'),
    ('REP_MERC_PUESTOS', 'Padrón Maestro de Puestos Comerciales', 'INFRAESTRUCTURA', 'Ficha técnica de puestos comerciales, dimensiones, ubicación y estado', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_VER'),
    ('REP_MERC_COMERCIANTES', 'Padrón Oficial de Comerciantes', 'PADRON', 'Comerciantes empadronados, titularidades, actividades y asignaciones activas', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_COMERCIANTES'),
    ('REP_MERC_ASIGNACIONES', 'Historial de Asignaciones de Puestos', 'PADRON', 'Resoluciones y actos administrativos de asignación vigente e histórica', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_VER'),
    ('REP_MERC_CONTRATOS', 'Contratos y Actos de Ocupación', 'LEGAL', 'Relación de contratos de ocupación, fechas de suscripción y vigencias', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_CONTRATOS'),
    ('REP_MERC_AUTORIZACIONES', 'Autorizaciones y Licencias Comerciales', 'LEGAL', 'Permisos formales de funcionamiento y giros autorizados con código QR', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_VER'),
    ('REP_MERC_VENCIMIENTOS', 'Vencimientos de Instrumentos Legales', 'LEGAL', 'Alertas preventivas de contratos, autorizaciones y asignaciones por vencer', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_VER'),
    ('REP_MERC_OBLIGACIONES', 'Obligaciones y Liquidaciones de Merced', 'FINANZAS', 'Cargos de merced conductiva, tarifas de espacio y servicios de mercado', 'FINANCIERO', 'AMBOS', 'MERCADOS_REPORTES_FINANZAS'),
    ('REP_MERC_RECAUDACION', 'Recaudación y Amortizaciones Confirmadas', 'FINANZAS', 'Ingresos municipales netos por concepto, medio de pago y sesión de caja', 'FINANCIERO', 'AMBOS', 'MERCADOS_REPORTES_FINANZAS'),
    ('REP_MERC_DEUDAS', 'Estado de Deuda y Antigüedad de Saldos', 'FINANZAS', 'Saldos pendientes clasificados por antigüedad de vencimiento', 'FINANCIERO', 'AMBOS', 'MERCADOS_REPORTES_FINANZAS'),
    ('REP_MERC_INSPECCIONES', 'Inspecciones Sanitarias y de Seguridad', 'FISCALIZACION', 'Actas de inspección, resultados de verificación y calificaciones técnicas', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_INSPECCIONES'),
    ('REP_MERC_INCIDENCIAS', 'Reportes de Incidencias Operativas', 'FISCALIZACION', 'Incidencias edilicias y sanitarias registradas con tiempos de atención', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_INCIDENCIAS'),
    ('REP_MERC_RENOVACIONES', 'Trámites de Renovación y Cambio de Titular', 'GESTION', 'Expedientes de solicitudes de renovación de ocupación y transferencias', 'DETALLE', 'AMBOS', 'MERCADOS_REPORTES_VER')
ON CONFLICT (codigo) DO NOTHING;

-- 4. Catálogo de Permisos RBAC para el Centro de Reportes
-- Asegurando que 'accion' pertenezca a ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'ASSIGN', 'EXPORT', 'MANAGE')
INSERT INTO permisos (codigo, nombre, accion, descripcion, modulo_id)
SELECT p.codigo, p.nombre, p.accion, p.descripcion, m.id
FROM (
    VALUES
        ('MERCADOS_REPORTES_VER', 'Ver Centro de Reportes de Mercados', 'VIEW', 'Acceder al centro general de reportes y consultas del módulo de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_EJECUTIVO', 'Ver Dashboard Ejecutivo de Mercados', 'VIEW', 'Consultar tablero de indicadores consolidados de infraestructura y gestión', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_OCUPACION', 'Consultar Reportes de Ocupación', 'VIEW', 'Analizar estadísticas y porcentajes de ocupación de puestos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_COMERCIANTES', 'Consultar Reportes de Comerciantes', 'VIEW', 'Consultar padrón analítico de comerciantes y padrón de titulares', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_CONTRATOS', 'Consultar Reportes de Contratos', 'VIEW', 'Analizar instrumentos contractuales y autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_FINANZAS', 'Consultar Reportes Financieros', 'VIEW', 'Acceder a recaudación, obligaciones, deuda y antigüedad de saldos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_INSPECCIONES', 'Consultar Reportes de Inspecciones', 'VIEW', 'Analizar actas de inspección, hallazgos y medidas correctivas', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_INCIDENCIAS', 'Consultar Reportes de Incidencias', 'VIEW', 'Consultar bitácora de incidencias, prioridades y tiempos de atención', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_EXPORTAR_EXCEL', 'Exportar Reportes a Microsoft Excel', 'EXPORT', 'Generar libros de cálculo XLSX con protección contra inyección', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_EXPORTAR_PDF', 'Exportar Reportes a Formato PDF', 'EXPORT', 'Generar documentos institucionales y reportes analíticos en PDF', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_CONFIGURAR', 'Configurar Plantillas de Reportes', 'MANAGE', 'Administrar el catálogo de reportes y parámetros analíticos del módulo', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_REPORTES_HISTORIAL_VER', 'Ver Historial de Exportaciones', 'VIEW', 'Consultar la bitácora histórica de exportaciones generadas en el módulo', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- 5. Asignación de Permisos a Roles Institucionales
-- Asignar a ROLE_ADMIN y ROLE_SUPERADMIN
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'MERCADOS_REPORTES_%'
  AND NOT EXISTS (
      SELECT 1 FROM rol_permisos rp
      WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
