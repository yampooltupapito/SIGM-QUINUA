-- ==============================================================================
-- MIGRACIÓN V41: SUBSISTEMA DE PROGRAMACIÓN DE TURNOS Y SERVICIOS DE SERENAZGO
-- Módulo 3: Seguridad Ciudadana y Serenazgo — SIGM-QUINUA (Prompt 40 de 150)
-- ==============================================================================

-- 1. Secuencias correlativas institucionales
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_programacion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_turno START WITH 1 INCREMENT BY 1;

-- 2. Actualización de tabla serenazgo_turnos
ALTER TABLE serenazgo_turnos
    ADD COLUMN IF NOT EXISTS tipo_turno VARCHAR(30) NOT NULL DEFAULT 'DIURNO',
    ADD COLUMN IF NOT EXISTS descripcion TEXT,
    ADD COLUMN IF NOT EXISTS duracion_referencial_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS observaciones TEXT,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE serenazgo_turnos DROP CONSTRAINT IF EXISTS chk_serenazgo_turno_estado;
ALTER TABLE serenazgo_turnos ADD CONSTRAINT chk_serenazgo_turno_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'ARCHIVADO'));

-- 3. Actualización de tabla serenazgo_programacion_servicios
ALTER TABLE serenazgo_programacion_servicios
    DROP CONSTRAINT IF EXISTS uq_serenazgo_personal_fecha_turno,
    DROP CONSTRAINT IF EXISTS chk_serenazgo_prog_estado;

ALTER TABLE serenazgo_programacion_servicios ALTER COLUMN personal_id DROP NOT NULL;

ALTER TABLE serenazgo_programacion_servicios
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(30),
    ADD COLUMN IF NOT EXISTS fecha_hora_inicio TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS fecha_hora_fin TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS tipo_servicio VARCHAR(50) NOT NULL DEFAULT 'PATRULLAJE_PREVENTIVO',
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS responsable_servicio_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS descripcion TEXT,
    ADD COLUMN IF NOT EXISTS motivo_reprogramacion TEXT,
    ADD COLUMN IF NOT EXISTS motivo_cancelacion TEXT,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Asignar códigos correlativos a registros preexistentes si los hubiera
UPDATE serenazgo_programacion_servicios
SET codigo = 'PRG-2026-' || LPAD(nextval('seq_serenazgo_programacion')::TEXT, 5, '0')
WHERE codigo IS NULL;

ALTER TABLE serenazgo_programacion_servicios ALTER COLUMN codigo SET NOT NULL;
ALTER TABLE serenazgo_programacion_servicios ADD CONSTRAINT uq_serenazgo_prog_codigo UNIQUE (codigo);

ALTER TABLE serenazgo_programacion_servicios
    ADD CONSTRAINT chk_serenazgo_prog_estado
    CHECK (estado IN ('BORRADOR', 'PROGRAMADO', 'PROGRAMADA', 'CONFIRMADA', 'EN_CURSO', 'PRESENTE', 'AUSENTE', 'JUSTIFICADO', 'FINALIZADA', 'CANCELADO', 'CANCELADA'));

CREATE INDEX IF NOT EXISTS idx_serenazgo_prog_codigo ON serenazgo_programacion_servicios(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_prog_fechas ON serenazgo_programacion_servicios(fecha_hora_inicio, fecha_hora_fin);
CREATE INDEX IF NOT EXISTS idx_serenazgo_prog_sector ON serenazgo_programacion_servicios(sector_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_prog_estado ON serenazgo_programacion_servicios(estado);

-- 4. Asignación múltiple de personal a programación
CREATE TABLE IF NOT EXISTS serenazgo_programacion_personal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    programacion_id UUID NOT NULL REFERENCES serenazgo_programacion_servicios(id) ON DELETE CASCADE,
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    funcion_operativa VARCHAR(50) NOT NULL DEFAULT 'PATRULLERO',
    es_responsable BOOLEAN NOT NULL DEFAULT FALSE,
    estado_asignacion VARCHAR(30) NOT NULL DEFAULT 'ASIGNADO',
    fecha_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_prog_personal UNIQUE (programacion_id, personal_id),
    CONSTRAINT chk_prog_personal_estado CHECK (estado_asignacion IN ('ASIGNADO', 'CONFIRMADO', 'SUSTITUIDO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_prog_personal_pid ON serenazgo_programacion_personal(personal_id, estado_asignacion);
CREATE INDEX IF NOT EXISTS idx_prog_personal_prg ON serenazgo_programacion_personal(programacion_id);

-- Migrar relaciones históricas de personal_id preexistentes a la tabla asociativa
INSERT INTO serenazgo_programacion_personal (programacion_id, personal_id, funcion_operativa, es_responsable, estado_asignacion)
SELECT p.id, p.personal_id, 'PATRULLERO', TRUE, 'ASIGNADO'
FROM serenazgo_programacion_servicios p
WHERE p.personal_id IS NOT NULL
ON CONFLICT (programacion_id, personal_id) DO NOTHING;

-- 5. Reserva de Unidades Móviles en Servicios Programados
CREATE TABLE IF NOT EXISTS serenazgo_programacion_unidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    programacion_id UUID NOT NULL REFERENCES serenazgo_programacion_servicios(id) ON DELETE CASCADE,
    unidad_id UUID NOT NULL REFERENCES serenazgo_unidades(id) ON DELETE RESTRICT,
    estado_reserva VARCHAR(30) NOT NULL DEFAULT 'RESERVADA',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_prog_unidad UNIQUE (programacion_id, unidad_id),
    CONSTRAINT chk_prog_unidad_estado CHECK (estado_reserva IN ('RESERVADA', 'ASIGNADA', 'LIBERADA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_prog_unidades_uid ON serenazgo_programacion_unidades(unidad_id, estado_reserva);

-- 6. Reserva de Equipamiento en Servicios Programados
CREATE TABLE IF NOT EXISTS serenazgo_programacion_equipamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    programacion_id UUID NOT NULL REFERENCES serenazgo_programacion_servicios(id) ON DELETE CASCADE,
    equipamiento_id UUID NOT NULL REFERENCES serenazgo_equipamiento(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL DEFAULT 1,
    estado_reserva VARCHAR(30) NOT NULL DEFAULT 'RESERVADO',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_prog_equipo UNIQUE (programacion_id, equipamiento_id),
    CONSTRAINT chk_prog_equipo_estado CHECK (estado_reserva IN ('RESERVADO', 'ENTREGADO', 'DEVUELTO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_prog_equipamiento_eid ON serenazgo_programacion_equipamiento(equipamiento_id, estado_reserva);

-- 7. Historial Inmutable y Auditoría de Programaciones de Servicio
CREATE TABLE IF NOT EXISTS serenazgo_programacion_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    programacion_id UUID NOT NULL REFERENCES serenazgo_programacion_servicios(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    motivo TEXT NOT NULL,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    detalles_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_prog_hist_prg ON serenazgo_programacion_historial(programacion_id, created_at);

-- 8. Sembrado de Permisos RBAC Granulares para Turnos y Programación
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_TURNOS_CREAR', 'Crear Turnos de Serenazgo', 'CREATE',
       'Permite registrar nuevas franjas horarias y turnos operativos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_TURNOS_EDITAR', 'Editar Turnos de Serenazgo', 'UPDATE',
       'Permite modificar horarios y parámetros de turnos operativos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_CREAR', 'Crear Programación de Servicios', 'CREATE',
       'Permite registrar nuevas programaciones de servicio de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_EDITAR', 'Editar Programación de Servicios', 'UPDATE',
       'Permite actualizar detalles y parámetros de servicios programados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_ASIGNAR', 'Asignar Personal y Recursos', 'ASSIGN',
       'Permite asignar efectivos, unidades y equipamiento a servicios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_CONFIRMAR', 'Confirmar Servicios Programados', 'APPROVE',
       'Permite confirmar y publicar servicios de guardia y patrullaje', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_REPROGRAMAR', 'Reprogramar Servicios', 'UPDATE',
       'Permite cambiar fechas, horarios o recursos con justificación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_CANCELAR', 'Cancelar Servicios Programados', 'UPDATE',
       'Permite anular o cancelar servicios programados con motivo obligatorio', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PROGRAMACION_EXPORTAR', 'Exportar Cuadrantes y Programación', 'EXPORT',
       'Permite exportar rol de servicios en PDF y Excel institucional', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de permisos al Administrador y Superadministrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND (p.codigo LIKE 'SERENAZGO_TURNOS_%' OR p.codigo LIKE 'SERENAZGO_PROGRAMACION_%')
ON CONFLICT DO NOTHING;

-- Asignación de permisos al Sereno
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_TURNOS_VER',
      'SERENAZGO_PROGRAMACION_VER',
      'SERENAZGO_PROGRAMACION_EXPORTAR'
  )
ON CONFLICT DO NOTHING;
