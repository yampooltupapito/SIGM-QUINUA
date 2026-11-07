-- =====================================================================
-- MIGRACIÓN FLYWAY: V45__serenazgo_registro_documentacion_seguimiento_intervenciones.sql
-- Módulo 3: Seguridad Ciudadana y Serenazgo - SIGM-QUINUA
-- Municipalidad Distrital de Quinua - Huamanga - Ayacucho
-- Prompt 44 de 150: Registro, Documentación y Seguimiento de Intervenciones
-- =====================================================================

-- 1. Secuencia para correlativo institucional de intervenciones (INT-YYYY-00000)
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_intervencion
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 2. Modificación de restricción de estado en serenazgo_intervenciones
ALTER TABLE serenazgo_intervenciones
    DROP CONSTRAINT IF EXISTS chk_serenazgo_interv_estado;

ALTER TABLE serenazgo_intervenciones
    ADD CONSTRAINT chk_serenazgo_interv_estado
    CHECK (estado IN ('BORRADOR', 'REGISTRADA', 'EN_REVISION', 'VALIDADA', 'OBSERVADA', 'ARCHIVADA', 'INICIADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA'));

-- 3. Ampliación de columnas en serenazgo_intervenciones
ALTER TABLE serenazgo_intervenciones
    ADD COLUMN IF NOT EXISTS atencion_id UUID REFERENCES serenazgo_atenciones_incidencia(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS tipo_intervencion VARCHAR(50) NOT NULL DEFAULT 'PATRULLAJE_PREVENTIVO',
    ADD COLUMN IF NOT EXISTS sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS ubicacion_descripcion TEXT,
    ADD COLUMN IF NOT EXISTS latitud DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS longitud DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS nivel_confidencialidad VARCHAR(30) NOT NULL DEFAULT 'PUBLICO',
    ADD COLUMN IF NOT EXISTS fecha_validacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS usuario_validador_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS motivo_observacion TEXT,
    ADD COLUMN IF NOT EXISTS motivo_archivo TEXT,
    ADD COLUMN IF NOT EXISTS registro_retrospectivo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS motivo_registro_retrospectivo TEXT,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Índices de consulta sobre serenazgo_intervenciones
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_atencion ON serenazgo_intervenciones(atencion_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_estado ON serenazgo_intervenciones(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_tipo ON serenazgo_intervenciones(tipo_intervencion);
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_sector ON serenazgo_intervenciones(sector_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_fecha_ini ON serenazgo_intervenciones(fecha_inicio);

-- 4. Tabla de Actuaciones Operativas dentro de la Intervención
CREATE TABLE IF NOT EXISTS serenazgo_intervencion_actuaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    intervencion_id UUID NOT NULL REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    tipo_actuacion VARCHAR(60) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    personal_responsable_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_actuaciones_interv ON serenazgo_intervencion_actuaciones(intervencion_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_actuaciones_fecha ON serenazgo_intervencion_actuaciones(fecha_hora);

-- 5. Tabla de Comunicaciones Institucionales y Apoyo Externo
CREATE TABLE IF NOT EXISTS serenazgo_intervencion_comunicaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    intervencion_id UUID NOT NULL REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    entidad_destinataria VARCHAR(60) NOT NULL,
    motivo TEXT NOT NULL,
    fecha_comunicacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    canal VARCHAR(50) NOT NULL DEFAULT 'RADIO_VHF',
    personal_comunicador_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    numero_referencia VARCHAR(100),
    estado_comunicacion VARCHAR(40) NOT NULL DEFAULT 'ENVIADA',
    confirmado_por VARCHAR(150),
    resultado_comunicacion TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT chk_serenazgo_comunicacion_estado CHECK (estado_comunicacion IN ('PREPARADA', 'ENVIADA', 'RECEPCION_CONFIRMADA', 'RESPUESTA_RECIBIDA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_comunic_interv ON serenazgo_intervencion_comunicaciones(intervencion_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_comunic_entidad ON serenazgo_intervencion_comunicaciones(entidad_destinataria);

-- 6. Tabla de Equipamiento Utilizado en la Intervención
CREATE TABLE IF NOT EXISTS serenazgo_intervencion_equipamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    intervencion_id UUID NOT NULL REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    equipamiento_id UUID NOT NULL REFERENCES serenazgo_equipamiento(id) ON DELETE RESTRICT,
    cantidad INT NOT NULL DEFAULT 1,
    estado_uso VARCHAR(50) NOT NULL DEFAULT 'UTILIZADO_CONFORME',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_equip_interv ON serenazgo_intervencion_equipamiento(intervencion_id);

-- 7. Tabla de Documentos y Anexos Administrativos
CREATE TABLE IF NOT EXISTS serenazgo_intervencion_documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    intervencion_id UUID NOT NULL REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    tipo_documento VARCHAR(50) NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100),
    tamanio_bytes BIGINT,
    descripcion TEXT,
    visible_ciudadano BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_docs_interv ON serenazgo_intervencion_documentos(intervencion_id);

-- 8. Tabla de Historial Administrativo y Trazabilidad (Auditoría Operativa)
CREATE TABLE IF NOT EXISTS serenazgo_intervencion_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    intervencion_id UUID NOT NULL REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    accion VARCHAR(60) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    motivo TEXT,
    usuario VARCHAR(100) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_hist_interv ON serenazgo_intervencion_historial(intervencion_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_hist_fecha ON serenazgo_intervencion_historial(fecha);

-- 9. Inserción de Permisos RBAC (Cumpliendo con chk_permiso_accion: VIEW, CREATE, UPDATE, DELETE, APPROVE, ASSIGN, EXPORT, MANAGE)
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_CREAR', 'Crear Intervención Operativa', 'CREATE',
       'Permite registrar una nueva intervención preventiva de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_EDITAR', 'Editar Intervención Operativa', 'UPDATE',
       'Permite editar datos de una intervención en estado borrador u observada', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION', 'Registrar Actuación en Intervención', 'UPDATE',
       'Permite registrar actuaciones preventivas específicas en la intervención', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_GESTIONAR_PARTICIPANTES', 'Gestionar Participantes de Intervención', 'MANAGE',
       'Permite asignar o actualizar el personal interviniente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_ADJUNTAR', 'Adjuntar Documentos a Intervención', 'CREATE',
       'Permite adjuntar actas, partes y fotografías autorizadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_REVISAR', 'Revisar Intervenciones de Serenazgo', 'VIEW',
       'Permite acceder a la bandeja de intervenciones para supervisión', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_OBSERVAR', 'Observar Intervención Operativa', 'UPDATE',
       'Permite a un supervisor observar una intervención con inconsistencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_VALIDAR', 'Validar Intervención Operativa', 'APPROVE',
       'Permite a un supervisor aprobar y validar administrativamente la intervención', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_ARCHIVAR', 'Archivar Intervención Operativa', 'MANAGE',
       'Permite archivar documentalmente una intervención concluida', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_EXPORTAR', 'Exportar Intervenciones de Serenazgo', 'EXPORT',
       'Permite exportar reportes de intervenciones en PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 10. Asignación a Roles del Sistema
-- Administradores: Todos los permisos de intervenciones
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'SERENAZGO_INTERVENCIONES_%'
ON CONFLICT DO NOTHING;

-- Supervisores de Serenazgo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SUPERVISOR_SERENAZGO'
  AND p.codigo IN (
      'SERENAZGO_INTERVENCIONES_VER',
      'SERENAZGO_INTERVENCIONES_CREAR',
      'SERENAZGO_INTERVENCIONES_EDITAR',
      'SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION',
      'SERENAZGO_INTERVENCIONES_GESTIONAR_PARTICIPANTES',
      'SERENAZGO_INTERVENCIONES_ADJUNTAR',
      'SERENAZGO_INTERVENCIONES_REVISAR',
      'SERENAZGO_INTERVENCIONES_OBSERVAR',
      'SERENAZGO_INTERVENCIONES_VALIDAR',
      'SERENAZGO_INTERVENCIONES_ARCHIVAR',
      'SERENAZGO_INTERVENCIONES_EXPORTAR'
  )
ON CONFLICT DO NOTHING;

-- Operadores de Central
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR_CENTRAL'
  AND p.codigo IN (
      'SERENAZGO_INTERVENCIONES_VER',
      'SERENAZGO_INTERVENCIONES_CREAR',
      'SERENAZGO_INTERVENCIONES_EDITAR',
      'SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION',
      'SERENAZGO_INTERVENCIONES_ADJUNTAR',
      'SERENAZGO_INTERVENCIONES_EXPORTAR'
  )
ON CONFLICT DO NOTHING;

-- Serenos de Cuadrilla / Campo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_INTERVENCIONES_VER',
      'SERENAZGO_INTERVENCIONES_CREAR',
      'SERENAZGO_INTERVENCIONES_EDITAR',
      'SERENAZGO_INTERVENCIONES_REGISTRAR_ACTUACION',
      'SERENAZGO_INTERVENCIONES_ADJUNTAR'
  )
ON CONFLICT DO NOTHING;
