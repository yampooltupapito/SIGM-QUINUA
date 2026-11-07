-- ====================================================================================
-- SIGM-QUINUA: MIGRACIÓN V60
-- SUBSISTEMA PROFESIONAL DE EMISIÓN DE RECIBOS, COMPROBANTES Y CONSTANCIAS
-- DE PAGO TRIBUTARIO CON PDF, QR Y VERIFICACIÓN DOCUMENTAL
-- Módulo 4: Recaudación Tributaria Municipal - Municipalidad Distrital de Quinua
-- ====================================================================================

-- 1. Tabla de Tipos Documentales de Recaudación Tributaria
CREATE TABLE IF NOT EXISTS tributaria_tipos_documento_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    requiere_numeracion BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_autorizacion BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_tipos_doc_estado ON tributaria_tipos_documento_pago(estado);

-- 2. Tabla de Series y Control Correlativo de Emisión
CREATE TABLE IF NOT EXISTS tributaria_series_documento_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_documento_id UUID NOT NULL REFERENCES tributaria_tipos_documento_pago(id) ON DELETE RESTRICT,
    codigo_serie VARCHAR(20) NOT NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    numero_correlativo_actual BIGINT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT uq_trib_serie_tipo_codigo UNIQUE (tipo_documento_id, codigo_serie)
);

CREATE INDEX IF NOT EXISTS idx_trib_series_tipo ON tributaria_series_documento_pago(tipo_documento_id);
CREATE INDEX IF NOT EXISTS idx_trib_series_estado ON tributaria_series_documento_pago(estado);

-- 3. Tabla Principal de Documentos Emitidos (Recibos y Constancias)
CREATE TABLE IF NOT EXISTS tributaria_documentos_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_documento VARCHAR(60) NOT NULL UNIQUE,
    tipo_documento_id UUID NOT NULL REFERENCES tributaria_tipos_documento_pago(id) ON DELETE RESTRICT,
    serie_id UUID NOT NULL REFERENCES tributaria_series_documento_pago(id) ON DELETE RESTRICT,
    numero_correlativo BIGINT NOT NULL,
    pago_tributario_id UUID NOT NULL REFERENCES tributaria_pagos(id) ON DELETE RESTRICT,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    fecha_emision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_total NUMERIC(14,2) NOT NULL CHECK (importe_total >= 0),
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    estado VARCHAR(30) NOT NULL DEFAULT 'EMITIDO',
    usuario_emisor_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_emisora_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    archivo_documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    nombre_archivo VARCHAR(255),
    hash_documento VARCHAR(64) NOT NULL,
    codigo_verificacion VARCHAR(64) NOT NULL UNIQUE,
    url_verificacion VARCHAR(255) NOT NULL,
    motivo_anulacion TEXT,
    fecha_anulacion TIMESTAMP WITH TIME ZONE,
    usuario_anulacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    documento_sustitutorio_id UUID REFERENCES tributaria_documentos_pago(id) ON DELETE SET NULL,
    numero_reimpresiones INTEGER NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_docs_pago ON tributaria_documentos_pago(pago_tributario_id);
CREATE INDEX IF NOT EXISTS idx_trib_docs_contrib ON tributaria_documentos_pago(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_docs_tipo ON tributaria_documentos_pago(tipo_documento_id);
CREATE INDEX IF NOT EXISTS idx_trib_docs_serie ON tributaria_documentos_pago(serie_id);
CREATE INDEX IF NOT EXISTS idx_trib_docs_estado ON tributaria_documentos_pago(estado);
CREATE INDEX IF NOT EXISTS idx_trib_docs_fec_emi ON tributaria_documentos_pago(fecha_emision);
CREATE INDEX IF NOT EXISTS idx_trib_docs_cod_verif ON tributaria_documentos_pago(codigo_verificacion);

-- Control de unicidad de recibo emitido activo por pago y tipo
CREATE UNIQUE INDEX IF NOT EXISTS uq_trib_doc_pago_tipo_emitido
    ON tributaria_documentos_pago (pago_tributario_id, tipo_documento_id)
    WHERE estado = 'EMITIDO';

-- Control de idempotencia
CREATE UNIQUE INDEX IF NOT EXISTS uq_trib_doc_idempotency
    ON tributaria_documentos_pago (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- 4. Historial / Auditoría de Eventos Documentales
CREATE TABLE IF NOT EXISTS tributaria_historial_documento_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL REFERENCES tributaria_documentos_pago(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    ip_origen VARCHAR(50),
    user_agent VARCHAR(255),
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_doc ON tributaria_historial_documento_pago(documento_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_fecha ON tributaria_historial_documento_pago(fecha_evento);

-- 5. Carga de Tipos Documentales Iniciales
INSERT INTO tributaria_tipos_documento_pago (id, codigo, nombre, descripcion, requiere_numeracion, requiere_autorizacion, estado, created_by)
VALUES 
    ('a0000001-0000-0000-0000-000000000001', 'RECIBO_PAGO', 'Recibo de Caja Municipal', 'Recibo oficial de caja que acredita la recepción de fondos por tributos municipales', TRUE, FALSE, 'ACTIVO', 'SYSTEM'),
    ('a0000001-0000-0000-0000-000000000002', 'CONSTANCIA_PAGO', 'Constancia de Pago Tributario', 'Certificación institucional de cancelación o pago tributario expedida al contribuyente', TRUE, TRUE, 'ACTIVO', 'SYSTEM')
ON CONFLICT (codigo) DO NOTHING;

-- 6. Carga de Series Documentales Iniciales para Quinua (Año 2026)
INSERT INTO tributaria_series_documento_pago (id, tipo_documento_id, codigo_serie, oficina_id, numero_correlativo_actual, estado, fecha_inicio, created_by)
SELECT 
    'b0000001-0000-0000-0000-000000000001', 
    t.id, 
    'REC01', 
    (SELECT id FROM oficinas WHERE codigo = 'RENTAS' LIMIT 1), 
    0, 
    'ACTIVA', 
    CURRENT_DATE, 
    'SYSTEM'
FROM tributaria_tipos_documento_pago t WHERE t.codigo = 'RECIBO_PAGO'
ON CONFLICT (tipo_documento_id, codigo_serie) DO NOTHING;

INSERT INTO tributaria_series_documento_pago (id, tipo_documento_id, codigo_serie, oficina_id, numero_correlativo_actual, estado, fecha_inicio, created_by)
SELECT 
    'b0000001-0000-0000-0000-000000000002', 
    t.id, 
    'CON01', 
    (SELECT id FROM oficinas WHERE codigo = 'RENTAS' LIMIT 1), 
    0, 
    'ACTIVA', 
    CURRENT_DATE, 
    'SYSTEM'
FROM tributaria_tipos_documento_pago t WHERE t.codigo = 'CONSTANCIA_PAGO'
ON CONFLICT (tipo_documento_id, codigo_serie) DO NOTHING;

-- 7. Registro de Permisos de Seguridad RBAC (Cumpliendo chk_permiso_accion)
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_VER', 'Ver Documentos Tributarios', 'VIEW',
       'Permite consultar recibos, constancias y comprobantes de recaudación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_CONFIGURAR', 'Configurar Tipos y Series', 'MANAGE',
       'Permite administrar el catálogo de tipos documentales y series correlativas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_EMITIR', 'Emitir Recibos y Constancias', 'CREATE',
       'Permite generar recibos y constancias a partir de pagos tributarios confirmados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_DESCARGAR', 'Descargar Documentos PDF', 'VIEW',
       'Permite descargar el archivo PDF original de un documento tributario', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_REIMPRIMIR', 'Reimprimir Documentos', 'VIEW',
       'Permite reimprimir copias autorizadas de recibos sin alterar numeración', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_ANULAR', 'Anular Documentos Tributarios', 'DELETE',
       'Permite anular administrativamente documentos emitidos con sustento obligatorio', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_SUSTITUIR', 'Sustituir Documentos Tributarios', 'UPDATE',
       'Permite emitir documentos sustitutorios vinculados a un documento original', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_VER_HISTORIAL', 'Ver Historial de Documentos', 'VIEW',
       'Permite auditar la trazabilidad de emisiones, reimpresiones y consultas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_EXPORTAR', 'Exportar Documentos Tributarios', 'EXPORT',
       'Permite exportar listados de recibos y constancias a Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DOCUMENTOS_REPORTES', 'Reportes de Emisión Documental', 'VIEW',
       'Permite consultar estadísticas de emisión, correlativos e incidencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 8. Asignación a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_DOCUMENTOS_VER',
      'TRIBUTARIA_DOCUMENTOS_CONFIGURAR',
      'TRIBUTARIA_DOCUMENTOS_EMITIR',
      'TRIBUTARIA_DOCUMENTOS_DESCARGAR',
      'TRIBUTARIA_DOCUMENTOS_REIMPRIMIR',
      'TRIBUTARIA_DOCUMENTOS_ANULAR',
      'TRIBUTARIA_DOCUMENTOS_SUSTITUIR',
      'TRIBUTARIA_DOCUMENTOS_VER_HISTORIAL',
      'TRIBUTARIA_DOCUMENTOS_EXPORTAR',
      'TRIBUTARIA_DOCUMENTOS_REPORTES'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_CAJERO'
  AND p.codigo IN (
      'TRIBUTARIA_DOCUMENTOS_VER',
      'TRIBUTARIA_DOCUMENTOS_EMITIR',
      'TRIBUTARIA_DOCUMENTOS_DESCARGAR',
      'TRIBUTARIA_DOCUMENTOS_REIMPRIMIR'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_TRIBUTACION'
  AND p.codigo IN (
      'TRIBUTARIA_DOCUMENTOS_VER',
      'TRIBUTARIA_DOCUMENTOS_EMITIR',
      'TRIBUTARIA_DOCUMENTOS_DESCARGAR',
      'TRIBUTARIA_DOCUMENTOS_REIMPRIMIR',
      'TRIBUTARIA_DOCUMENTOS_ANULAR',
      'TRIBUTARIA_DOCUMENTOS_SUSTITUIR',
      'TRIBUTARIA_DOCUMENTOS_VER_HISTORIAL',
      'TRIBUTARIA_DOCUMENTOS_EXPORTAR',
      'TRIBUTARIA_DOCUMENTOS_REPORTES'
  )
ON CONFLICT DO NOTHING;
