-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V14: Gestión Documental Centralizada, Versiones, Integridad y Archivos
-- ==============================================================================

-- 1. Tabla Principal de Documentos
CREATE TABLE IF NOT EXISTS documentos (
    id BIGSERIAL PRIMARY KEY,
    identificador_publico UUID NOT NULL UNIQUE,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_interno VARCHAR(255) NOT NULL UNIQUE,
    extension VARCHAR(20) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    categoria VARCHAR(50) NOT NULL DEFAULT 'OTRO',
    clasificacion_acceso VARCHAR(30) NOT NULL DEFAULT 'INTERNO',
    descripcion VARCHAR(500),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    version_actual INT NOT NULL DEFAULT 1,
    bloqueo_eliminacion BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_eliminacion VARCHAR(500),
    usuario_propietario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    modulo_origen VARCHAR(50) DEFAULT 'SISTEMA',
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_eliminacion_logica TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_documento_estado CHECK (estado IN ('ACTIVO', 'ARCHIVADO', 'ELIMINADO_LOGICO')),
    CONSTRAINT chk_documento_clasificacion CHECK (clasificacion_acceso IN ('PUBLICO', 'INTERNO', 'CONFIDENCIAL', 'RESTRINGIDO'))
);

CREATE INDEX IF NOT EXISTS idx_documentos_uuid ON documentos(identificador_publico);
CREATE INDEX IF NOT EXISTS idx_documentos_hash ON documentos(hash_sha256);
CREATE INDEX IF NOT EXISTS idx_documentos_propietario ON documentos(usuario_propietario_id);
CREATE INDEX IF NOT EXISTS idx_documentos_oficina ON documentos(oficina_responsable_id);
CREATE INDEX IF NOT EXISTS idx_documentos_categoria ON documentos(categoria);
CREATE INDEX IF NOT EXISTS idx_documentos_estado ON documentos(estado);
CREATE INDEX IF NOT EXISTS idx_documentos_fecha ON documentos(fecha_creacion DESC);

-- 2. Historial de Versiones de Documentos
CREATE TABLE IF NOT EXISTS documento_versiones (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    numero_version INT NOT NULL,
    nombre_interno VARCHAR(255) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    motivo_cambio VARCHAR(500) NOT NULL,
    usuario_responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_doc_version UNIQUE (documento_id, numero_version)
);

CREATE INDEX IF NOT EXISTS idx_doc_versiones_doc ON documento_versiones(documento_id);

-- 3. Relaciones Documentales con Módulos Municipales
CREATE TABLE IF NOT EXISTS documento_relaciones (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    modulo VARCHAR(50) NOT NULL,
    tipo_entidad VARCHAR(50) NOT NULL,
    entidad_id VARCHAR(100) NOT NULL,
    rol_documento VARCHAR(50) NOT NULL DEFAULT 'PRINCIPAL',
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_doc_relaciones_doc ON documento_relaciones(documento_id);
CREATE INDEX IF NOT EXISTS idx_doc_relaciones_entidad ON documento_relaciones(modulo, tipo_entidad, entidad_id);

-- 4. Verificaciones de Integridad SHA-256
CREATE TABLE IF NOT EXISTS documento_verificaciones (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    version_evaluada INT NOT NULL,
    hash_esperado VARCHAR(64) NOT NULL,
    hash_calculado VARCHAR(64) NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    verificado_por_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_verificacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detalle VARCHAR(500),
    CONSTRAINT chk_doc_verif_resultado CHECK (resultado IN ('INTEGRO', 'ALTERADO', 'NO_ENCONTRADO'))
);

CREATE INDEX IF NOT EXISTS idx_doc_verif_doc ON documento_verificaciones(documento_id);

-- 5. Accesos y Auditoría Documental
CREATE TABLE IF NOT EXISTS documento_accesos (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    accion VARCHAR(50) NOT NULL,
    direccion_ip VARCHAR(45),
    user_agent VARCHAR(255),
    fecha_acceso TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO'
);

CREATE INDEX IF NOT EXISTS idx_doc_accesos_doc ON documento_accesos(documento_id);
CREATE INDEX IF NOT EXISTS idx_doc_accesos_usuario ON documento_accesos(usuario_id);

-- 6. Registro de Permisos de Gestión Documental
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'DOCUMENTO_VER', 'Ver documentos institucionales y públicos', 'VIEW', 'Permite visualizar y consultar documentos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'DOCUMENTO_CREAR', 'Subir y registrar documentos', 'CREATE', 'Permite subir archivos a la plataforma institucional'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'DOCUMENTO_EDITAR', 'Versionar y clasificar documentos', 'UPDATE', 'Permite subir nuevas versiones y actualizar metadatos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'DOCUMENTO_ELIMINAR', 'Archivar y eliminación lógica documental', 'DELETE', 'Permite archivar o eliminar lógicamente documentos con justificación'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'DOCUMENTO_ADMIN', 'Administración y conservación documental', 'MANAGE', 'Permite gestionar políticas de conservación, auditoría e integridad total'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignar permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN ('DOCUMENTO_VER', 'DOCUMENTO_CREAR', 'DOCUMENTO_EDITAR', 'DOCUMENTO_ELIMINAR', 'DOCUMENTO_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_JEFE_OFICINA', 'ROLE_OPERADOR', 'ROLE_MESA_PARTES')
  AND p.codigo IN ('DOCUMENTO_VER', 'DOCUMENTO_CREAR', 'DOCUMENTO_EDITAR')
ON CONFLICT DO NOTHING;
