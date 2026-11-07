-- ==============================================================================
-- SIGM-QUINUA - V43__serenazgo_gestion_alertas_ciudadanas.sql
-- Módulo 3: Seguridad Ciudadana y Serenazgo
-- Prompt 42: Gestión de Alertas y Reportes Ciudadanos
-- ==============================================================================

-- 1. Secuencia para correlativo institucional de Alertas Ciudadanas
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_alerta START WITH 1 INCREMENT BY 1;

-- 2. Ajuste y enriquecimiento de serenazgo_alertas_ciudadanas
ALTER TABLE serenazgo_alertas_ciudadanas DROP CONSTRAINT IF EXISTS chk_serenazgo_alerta_estado;
ALTER TABLE serenazgo_alertas_ciudadanas ADD CONSTRAINT chk_serenazgo_alerta_estado 
    CHECK (estado IN ('RECIBIDA', 'PENDIENTE', 'EN_REVISION', 'EN_PROCESO', 'REQUIERE_INFORMACION', 'CLASIFICADA', 'VINCULADA', 'CONVERTIDA_EN_INCIDENCIA', 'CERRADA', 'DESCARTADA'));

ALTER TABLE serenazgo_alertas_ciudadanas
    ADD COLUMN IF NOT EXISTS titulo VARCHAR(200) NOT NULL DEFAULT 'Reporte de Seguridad Ciudadana',
    ADD COLUMN IF NOT EXISTS tipo_reporte_id UUID REFERENCES serenazgo_tipos_incidencia(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS fecha_ocurrencia TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS prioridad VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
    ADD COLUMN IF NOT EXISTS sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS reportante_nombre VARCHAR(150),
    ADD COLUMN IF NOT EXISTS reportante_email VARCHAR(120),
    ADD COLUMN IF NOT EXISTS clave_consulta_hash VARCHAR(120),
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS nivel_confidencialidad VARCHAR(30) NOT NULL DEFAULT 'ORDINARIO',
    ADD COLUMN IF NOT EXISTS observaciones_internas TEXT,
    ADD COLUMN IF NOT EXISTS motivo_cierre TEXT,
    ADD COLUMN IF NOT EXISTS resultado_ciudadano TEXT,
    ADD COLUMN IF NOT EXISTS motivo_descarte TEXT,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- 3. Tabla de Historial y Trazabilidad de Alertas Ciudadanas
CREATE TABLE IF NOT EXISTS serenazgo_alerta_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alerta_id UUID NOT NULL REFERENCES serenazgo_alertas_ciudadanas(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50),
    motivo TEXT,
    es_visible_ciudadano BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_alerta_hist_alerta ON serenazgo_alerta_historial(alerta_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alerta_hist_fecha ON serenazgo_alerta_historial(created_at);

-- 4. Tabla de Documentos y Fotografías de Alertas Ciudadanas
CREATE TABLE IF NOT EXISTS serenazgo_alerta_documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alerta_id UUID NOT NULL REFERENCES serenazgo_alertas_ciudadanas(id) ON DELETE CASCADE,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    ruta_archivo VARCHAR(500),
    contenido_base64 TEXT,
    es_fotografia BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_alerta_docs_alerta ON serenazgo_alerta_documentos(alerta_id);

-- 5. Tabla de Solicitudes de Información Complementaria al Ciudadano
CREATE TABLE IF NOT EXISTS serenazgo_alerta_solicitudes_info (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alerta_id UUID NOT NULL REFERENCES serenazgo_alertas_ciudadanas(id) ON DELETE CASCADE,
    solicitud TEXT NOT NULL,
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_solicita_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    respuesta_ciudadano TEXT,
    fecha_respuesta TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_solicitud_info_estado CHECK (estado IN ('PENDIENTE', 'RESPONDIDA', 'EXPIRADA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_alerta_sol_alerta ON serenazgo_alerta_solicitudes_info(alerta_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alerta_sol_estado ON serenazgo_alerta_solicitudes_info(estado);

-- 6. Índices adicionales de búsqueda y rendimiento
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_sector ON serenazgo_alertas_ciudadanas(sector_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_tipo ON serenazgo_alertas_ciudadanas(tipo_reporte_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_prioridad ON serenazgo_alertas_ciudadanas(prioridad);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_incidencia ON serenazgo_alertas_ciudadanas(incidencia_id);

-- 7. Registro de Permisos Institucionales
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_VER', 'Ver Alertas Ciudadanas', 'VIEW', 'Consulta de alertas y reportes ciudadanos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_CREAR', 'Crear Alerta Ciudadana', 'CREATE', 'Registro de alertas ciudadanas por operadores o portal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_REVISAR', 'Revisar Alerta Ciudadana', 'VIEW', 'Revisión y examen de contenido de reportes ciudadanos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_CLASIFICAR', 'Clasificar Alerta Ciudadana', 'UPDATE', 'Asignación de catálogo y tipo a reportes ciudadanos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_PRIORIZAR', 'Priorizar Alerta Ciudadana', 'UPDATE', 'Modificación de prioridad administrativa de alertas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_CAMBIAR_ESTADO', 'Cambiar Estado Alerta', 'UPDATE', 'Transiciones en el ciclo de vida de alertas ciudadanas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_SOLICITAR_INFORMACION', 'Solicitar Información a Ciudadano', 'CREATE', 'Requerir aclaraciones o datos adicionales al ciudadano', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_VINCULAR', 'Vincular Alerta con Incidencia', 'UPDATE', 'Asociar alerta con incidencia de seguridad ciudadana', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_CERRAR', 'Cerrar Alerta Ciudadana', 'MANAGE', 'Cierre administrativo o descarte formal de reportes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_EXPORTAR', 'Exportar Alertas Ciudadanas', 'EXPORT', 'Exportación de reportes de alertas ciudadanas en PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 8. Asignación de Permisos a Roles Institucionales
-- Rol Administrador y Superadmin
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMIN_SISTEMA')
  AND p.codigo IN (
      'SERENAZGO_ALERTAS_VER', 'SERENAZGO_ALERTAS_CREAR', 'SERENAZGO_ALERTAS_REVISAR',
      'SERENAZGO_ALERTAS_CLASIFICAR', 'SERENAZGO_ALERTAS_PRIORIZAR', 'SERENAZGO_ALERTAS_CAMBIAR_ESTADO',
      'SERENAZGO_ALERTAS_SOLICITAR_INFORMACION', 'SERENAZGO_ALERTAS_VINCULAR',
      'SERENAZGO_ALERTAS_CERRAR', 'SERENAZGO_ALERTAS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Rol Supervisor Serenazgo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENAZGO_SUPERVISOR')
  AND p.codigo IN (
      'SERENAZGO_ALERTAS_VER', 'SERENAZGO_ALERTAS_CREAR', 'SERENAZGO_ALERTAS_REVISAR',
      'SERENAZGO_ALERTAS_CLASIFICAR', 'SERENAZGO_ALERTAS_PRIORIZAR', 'SERENAZGO_ALERTAS_CAMBIAR_ESTADO',
      'SERENAZGO_ALERTAS_SOLICITAR_INFORMACION', 'SERENAZGO_ALERTAS_VINCULAR',
      'SERENAZGO_ALERTAS_CERRAR', 'SERENAZGO_ALERTAS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Rol Operador Serenazgo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_OPERADOR_SERENAZGO', 'ROLE_SERENAZGO_OPERADOR')
  AND p.codigo IN (
      'SERENAZGO_ALERTAS_VER', 'SERENAZGO_ALERTAS_CREAR', 'SERENAZGO_ALERTAS_REVISAR',
      'SERENAZGO_ALERTAS_CLASIFICAR', 'SERENAZGO_ALERTAS_PRIORIZAR', 'SERENAZGO_ALERTAS_CAMBIAR_ESTADO',
      'SERENAZGO_ALERTAS_SOLICITAR_INFORMACION', 'SERENAZGO_ALERTAS_VINCULAR', 'SERENAZGO_ALERTAS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Rol Sereno
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_SERENO', 'SERENO')
  AND p.codigo IN ('SERENAZGO_ALERTAS_VER', 'SERENAZGO_ALERTAS_REVISAR')
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
