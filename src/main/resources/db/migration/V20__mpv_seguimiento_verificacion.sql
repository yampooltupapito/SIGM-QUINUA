-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V20 - SEGUIMIENTO CIUDADANO, CONSULTA Y VERIFICACIÓN DOCUMENTAL (PROMPT 17)
-- ==============================================================================

-- 1. Ampliación de Columnas en Expedientes para Código de Seguimiento Seguro
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS codigo_seguimiento VARCHAR(64);
CREATE UNIQUE INDEX IF NOT EXISTS idx_mpv_exp_cod_seg ON mpv_expedientes(codigo_seguimiento) WHERE codigo_seguimiento IS NOT NULL;

-- 2. Tabla de Códigos de Seguimiento Seguro de Alta Entropía
CREATE TABLE IF NOT EXISTS mpv_codigos_seguimiento (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    codigo VARCHAR(64) NOT NULL UNIQUE,
    token_hash VARCHAR(128) NOT NULL,
    fecha_emision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP WITH TIME ZONE,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    max_intentos INT NOT NULL DEFAULT 5,
    bloqueado_hasta TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    motivo_revocacion TEXT,
    ultimo_acceso TIMESTAMP WITH TIME ZONE,
    ip_ultimo_acceso VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mpv_cod_seg_estado CHECK (estado IN ('ACTIVO', 'REVOCADO', 'EXPIRADO', 'BLOQUEADO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_cod_seg_exp ON mpv_codigos_seguimiento(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_cod_seg_codigo ON mpv_codigos_seguimiento(codigo);
CREATE INDEX IF NOT EXISTS idx_mpv_cod_seg_estado ON mpv_codigos_seguimiento(estado);

-- 3. Tabla de Registro de Verificaciones Documentales Institucionales
CREATE TABLE IF NOT EXISTS mpv_verificaciones_documentales (
    id BIGSERIAL PRIMARY KEY,
    codigo_verificacion VARCHAR(64) NOT NULL UNIQUE,
    token_qr VARCHAR(128) NOT NULL UNIQUE,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(100) NOT NULL,
    expediente_id BIGINT REFERENCES mpv_expedientes(id) ON DELETE SET NULL,
    decision_id BIGINT REFERENCES mpv_decisiones(id) ON DELETE SET NULL,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    fecha_emision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entidad_emisora VARCHAR(150) NOT NULL DEFAULT 'Municipalidad Distrital de Quinua',
    autoridad_firmante VARCHAR(150),
    cargo_firmante VARCHAR(100),
    estado_documento VARCHAR(20) NOT NULL DEFAULT 'VIGENTE',
    motivo_anulacion TEXT,
    url_verificacion VARCHAR(255) NOT NULL,
    veces_consultado INT NOT NULL DEFAULT 0,
    ultima_consulta TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mpv_verif_doc_estado CHECK (estado_documento IN ('VIGENTE', 'ANULADO', 'REVOCADO', 'NO_VERIFICABLE'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_verif_doc_codigo ON mpv_verificaciones_documentales(codigo_verificacion);
CREATE INDEX IF NOT EXISTS idx_mpv_verif_doc_token ON mpv_verificaciones_documentales(token_qr);
CREATE INDEX IF NOT EXISTS idx_mpv_verif_doc_num ON mpv_verificaciones_documentales(numero_documento);
CREATE INDEX IF NOT EXISTS idx_mpv_verif_doc_hash ON mpv_verificaciones_documentales(hash_sha256);
CREATE INDEX IF NOT EXISTS idx_mpv_verif_doc_exp ON mpv_verificaciones_documentales(expediente_id);

-- 4. Tabla de Auditoría y Rate Limiting de Consultas de Seguimiento y Verificación
CREATE TABLE IF NOT EXISTS mpv_consultas_seguimiento (
    id BIGSERIAL PRIMARY KEY,
    tipo_consulta VARCHAR(30) NOT NULL,
    criterio_busqueda VARCHAR(100),
    expediente_id BIGINT REFERENCES mpv_expedientes(id) ON DELETE SET NULL,
    ip_origen VARCHAR(45) NOT NULL,
    user_agent VARCHAR(255),
    resultado VARCHAR(30) NOT NULL,
    fecha_consulta TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tiempo_respuesta_ms INT,
    CONSTRAINT chk_mpv_cons_tipo CHECK (tipo_consulta IN ('SEGUIMIENTO_EXPEDIENTE', 'VERIFICACION_DOCUMENTAL')),
    CONSTRAINT chk_mpv_cons_res CHECK (resultado IN ('EXITOSA', 'NO_ENCONTRADO', 'CODIGO_INVALIDO', 'BLOQUEADO', 'RATE_LIMITED'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_cons_ip_fecha ON mpv_consultas_seguimiento(ip_origen, fecha_consulta);
CREATE INDEX IF NOT EXISTS idx_mpv_cons_criterio ON mpv_consultas_seguimiento(criterio_busqueda);

-- 5. Sembrado de Permisos Granulares de Seguimiento y Verificación
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_SEGUIMIENTO_CIUDADANO', 'Consultar seguimiento integral de expedientes propios', 'VIEW', 'Permite a los administrados consultar el avance y trazabilidad de sus solicitudes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DOCUMENTO_VERIFICAR', 'Verificar autenticidad de documentos emitidos', 'VIEW', 'Permite verificar documentos oficiales emitidos por la entidad'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de Permisos a Roles Institucionales
-- Ciudadano
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_CIUDADANO'
  AND p.codigo IN ('MESAPARTES_SEGUIMIENTO_CIUDADANO', 'MESAPARTES_DOCUMENTO_VERIFICAR')
ON CONFLICT DO NOTHING;

-- Operador, Jefe de Oficina, Admin y Superadmin
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_MESA_PARTES_OPERADOR', 'ROLE_JEFE_OFICINA', 'ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN ('MESAPARTES_SEGUIMIENTO_CIUDADANO', 'MESAPARTES_DOCUMENTO_VERIFICAR')
ON CONFLICT DO NOTHING;
