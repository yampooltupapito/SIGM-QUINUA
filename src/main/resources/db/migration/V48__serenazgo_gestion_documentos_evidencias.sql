-- ==============================================================================
-- SIGM-QUINUA: MÓDULO 3 - SEGURIDAD CIUDADANA Y SERENAZGO
-- V48: GESTIÓN INTEGRAL DE EVIDENCIAS, FOTOGRAFÍAS, VIDEOS Y DOCUMENTOS
-- ==============================================================================

-- 1. Tabla Principal de Documentos y Evidencias de Seguridad Ciudadana
CREATE TABLE IF NOT EXISTS serenazgo_documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_almacenado VARCHAR(255) NOT NULL,
    extension VARCHAR(20) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    fecha_hora_carga TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_carga_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    origen_documento VARCHAR(50) NOT NULL DEFAULT 'ADMINISTRATIVO',
    descripcion TEXT,
    estado VARCHAR(50) NOT NULL DEFAULT 'REGISTRADO',
    nivel_confidencialidad VARCHAR(30) NOT NULL DEFAULT 'CONFIDENCIAL',
    fecha_hora_validacion TIMESTAMP WITH TIME ZONE,
    usuario_validador_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT,
    motivo_restriccion TEXT,
    motivo_archivado TEXT,
    version_actual INT NOT NULL DEFAULT 1,
    documento_padre_id UUID REFERENCES serenazgo_documentos(id) ON DELETE SET NULL,
    incidencia_id UUID REFERENCES serenazgo_incidencias(id) ON DELETE SET NULL,
    alerta_id UUID REFERENCES serenazgo_alertas_ciudadanas(id) ON DELETE SET NULL,
    atencion_id UUID REFERENCES serenazgo_atenciones_incidencia(id) ON DELETE SET NULL,
    intervencion_id UUID REFERENCES serenazgo_intervenciones(id) ON DELETE SET NULL,
    patrullaje_id UUID REFERENCES serenazgo_patrullajes(id) ON DELETE SET NULL,
    fecha_captura TIMESTAMP WITH TIME ZONE,
    ubicacion_captura VARCHAR(255),
    latitud NUMERIC(10, 7),
    longitud NUMERIC(10, 7),
    autoria_declarada VARCHAR(200),
    ultimo_resultado_verificacion VARCHAR(50) NOT NULL DEFAULT 'NO_VERIFICADO',
    fecha_ultima_verificacion TIMESTAMP WITH TIME ZONE,
    usuario_ultima_verificacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_serenazgo_doc_tipo CHECK (tipo_documento IN ('FOTOGRAFIA', 'VIDEO', 'INFORME', 'ACTA_ADMINISTRATIVA', 'COMUNICACION_INSTITUCIONAL', 'DOCUMENTO_RESPALDO', 'OTRO')),
    CONSTRAINT chk_serenazgo_doc_estado CHECK (estado IN ('REGISTRADO', 'EN_REVISION', 'VALIDADO_ADMINISTRATIVAMENTE', 'OBSERVADO', 'RESTRINGIDO', 'ARCHIVADO')),
    CONSTRAINT chk_serenazgo_doc_origen CHECK (origen_documento IN ('CIUDADANO', 'ADMINISTRATIVO', 'SISTEMA', 'CANAL_INSTITUCIONAL')),
    CONSTRAINT chk_serenazgo_doc_confidencialidad CHECK (nivel_confidencialidad IN ('PUBLICO', 'INTERNO', 'CONFIDENCIAL', 'RESTRINGIDO')),
    CONSTRAINT chk_serenazgo_doc_verif CHECK (ultimo_resultado_verificacion IN ('COINCIDENTE', 'NO_COINCIDENTE', 'ARCHIVO_NO_DISPONIBLE', 'ERROR_LECTURA', 'NO_VERIFICADO'))
);

-- Índices de consulta y rendimiento
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_codigo ON serenazgo_documentos(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_tipo ON serenazgo_documentos(tipo_documento);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_estado ON serenazgo_documentos(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_origen ON serenazgo_documentos(origen_documento);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_incidencia ON serenazgo_documentos(incidencia_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_alerta ON serenazgo_documentos(alerta_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_atencion ON serenazgo_documentos(atencion_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_intervencion ON serenazgo_documentos(intervencion_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_patrullaje ON serenazgo_documentos(patrullaje_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_fecha_carga ON serenazgo_documentos(fecha_hora_carga);
CREATE INDEX IF NOT EXISTS idx_serenazgo_docs_hash ON serenazgo_documentos(hash_sha256);

-- 2. Asociaciones Múltiples de Documentos con Registros Institucionales
CREATE TABLE IF NOT EXISTS serenazgo_documentos_relaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_seguridad_id UUID NOT NULL REFERENCES serenazgo_documentos(id) ON DELETE CASCADE,
    tipo_entidad VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    rol_relacion VARCHAR(50) NOT NULL DEFAULT 'RESPALDO',
    fecha_asociacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_asocia_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT chk_serenazgo_doc_rel_tipo CHECK (tipo_entidad IN ('INCIDENCIA', 'ALERTA', 'ATENCION', 'INTERVENCION', 'PATRULLAJE')),
    CONSTRAINT chk_serenazgo_doc_rel_rol CHECK (rol_relacion IN ('RESPALDO', 'EVIDENCIA_PRIMARIA', 'ANEXO', 'INFORME_FINAL'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_doc_rel_doc ON serenazgo_documentos_relaciones(documento_seguridad_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_doc_rel_entidad ON serenazgo_documentos_relaciones(tipo_entidad, entidad_id);

-- 3. Historial de Verificaciones de Integridad Digital (Recálculo SHA-256)
CREATE TABLE IF NOT EXISTS serenazgo_documentos_verificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_seguridad_id UUID NOT NULL REFERENCES serenazgo_documentos(id) ON DELETE CASCADE,
    hash_registrado VARCHAR(64) NOT NULL,
    hash_calculado VARCHAR(64),
    resultado VARCHAR(50) NOT NULL,
    fecha_verificacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    detalle_tecnico TEXT,
    CONSTRAINT chk_serenazgo_doc_verif_res CHECK (resultado IN ('COINCIDENTE', 'NO_COINCIDENTE', 'ARCHIVO_NO_DISPONIBLE', 'ERROR_LECTURA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_doc_verif_doc ON serenazgo_documentos_verificaciones(documento_seguridad_id);

-- 4. Trazabilidad e Historial de Operaciones sobre Documentos
CREATE TABLE IF NOT EXISTS serenazgo_documentos_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_seguridad_id UUID NOT NULL REFERENCES serenazgo_documentos(id) ON DELETE CASCADE,
    accion VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    ip_origen VARCHAR(50),
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_doc_hist_doc ON serenazgo_documentos_historial(documento_seguridad_id);

-- 5. Sembrado de Permisos de Seguridad Ciudadana
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_VER', 'Ver acervo de evidencias y documentos', 'VIEW',
       'Permite consultar el catálogo y fichas de evidencias y documentos de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_CREAR', 'Cargar y registrar documentos y evidencias', 'CREATE',
       'Permite subir archivos y registrar evidencias con cálculo criptográfico SHA-256', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_ADJUNTAR', 'Vincular y asociar documentos con actuaciones', 'CREATE',
       'Permite asociar documentos a incidencias, alertas, atenciones, intervenciones y patrullajes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_DESCARGAR', 'Descargar archivos de evidencia y sustento', 'VIEW',
       'Permite la descarga protegida del archivo digital físico', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_VERIFICAR', 'Ejecutar comprobación de integridad digital (SHA-256)', 'VIEW',
       'Permite recalcular el hash SHA-256 y comprobar la no alteración del archivo físico', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_VERSIONAR', 'Registrar nuevas versiones de documentos', 'CREATE',
       'Permite cargar versiones sustitutorias conservando la inmutabilidad y trazabilidad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_REVISAR', 'Revisar y observar documentos de seguridad', 'APPROVE',
       'Permite cambiar estados a EN_REVISION y registrar observaciones técnicas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_VALIDAR', 'Validar administrativamente documentos de serenazgo', 'APPROVE',
       'Permite validar la conformidad documental como sustento municipal (Ley N° 31297)', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_RESTRINGIR', 'Restringir confidencialidad de documentos', 'UPDATE',
       'Permite restringir el acceso a documentos por datos personales o sensibilidad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_ARCHIVAR', 'Archivar documentos de seguridad ciudadana', 'DELETE',
       'Permite transferir documentos al archivo pasivo institucional', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_DOCUMENTOS_EXPORTAR', 'Exportar reportes y fichas técnicas de evidencias', 'EXPORT',
       'Permite exportar listados a Excel y fichas técnicas en PDF con descargo legal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignación de Permisos a Roles Institucionales
-- Administrador y Superadministrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'SERENAZGO_DOCUMENTOS_%'
ON CONFLICT DO NOTHING;

-- Supervisor de Serenazgo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SUPERVISOR_SERENAZGO'
  AND p.codigo IN (
      'SERENAZGO_DOCUMENTOS_VER', 'SERENAZGO_DOCUMENTOS_CREAR', 'SERENAZGO_DOCUMENTOS_ADJUNTAR',
      'SERENAZGO_DOCUMENTOS_DESCARGAR', 'SERENAZGO_DOCUMENTOS_VERIFICAR', 'SERENAZGO_DOCUMENTOS_VERSIONAR',
      'SERENAZGO_DOCUMENTOS_REVISAR', 'SERENAZGO_DOCUMENTOS_VALIDAR', 'SERENAZGO_DOCUMENTOS_EXPORTAR'
  )
ON CONFLICT DO NOTHING;

-- Sereno Operativo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_DOCUMENTOS_VER', 'SERENAZGO_DOCUMENTOS_CREAR', 'SERENAZGO_DOCUMENTOS_ADJUNTAR',
      'SERENAZGO_DOCUMENTOS_DESCARGAR', 'SERENAZGO_DOCUMENTOS_VERIFICAR'
  )
ON CONFLICT DO NOTHING;

-- Operador de Central / Monitoreo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR_CC'
  AND p.codigo IN (
      'SERENAZGO_DOCUMENTOS_VER', 'SERENAZGO_DOCUMENTOS_CREAR', 'SERENAZGO_DOCUMENTOS_ADJUNTAR',
      'SERENAZGO_DOCUMENTOS_DESCARGAR', 'SERENAZGO_DOCUMENTOS_VERIFICAR'
  )
ON CONFLICT DO NOTHING;

