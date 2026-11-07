-- ============================================================================
-- V17__mpv_recepcion_revision_observaciones.sql
-- Módulo 1: Mesa de Partes Virtual - Recepción, Verificación de Requisitos, Observaciones y Prioridades
-- Municipalidad Distrital de Quinua (SIGM-QUINUA)
-- ============================================================================

-- 1. Ampliación de Observaciones Administrativas: Tipo de observación, visibilidad y requisito asociado
ALTER TABLE mpv_observaciones
    ADD COLUMN IF NOT EXISTS tipo_observacion VARCHAR(50) NOT NULL DEFAULT 'FORMA',
    ADD COLUMN IF NOT EXISTS visibilidad VARCHAR(20) NOT NULL DEFAULT 'CIUDADANO',
    ADD COLUMN IF NOT EXISTS requisito_id BIGINT REFERENCES mpv_requisitos_documentales(id) ON DELETE SET NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_mpv_obs_visibilidad'
    ) THEN
        ALTER TABLE mpv_observaciones
            ADD CONSTRAINT chk_mpv_obs_visibilidad CHECK (visibilidad IN ('CIUDADANO', 'INTERNA'));
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_mpv_obs_tipo'
    ) THEN
        ALTER TABLE mpv_observaciones
            ADD CONSTRAINT chk_mpv_obs_tipo CHECK (tipo_observacion IN ('FORMA', 'FONDO', 'DOCUMENTAL', 'TECNICA'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_mpv_obs_exp_vis ON mpv_observaciones(expediente_id, visibilidad);
CREATE INDEX IF NOT EXISTS idx_mpv_obs_req ON mpv_observaciones(requisito_id);

-- 2. Tabla de Verificación de Requisitos Documentales por Expediente
CREATE TABLE IF NOT EXISTS mpv_revision_requisitos (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    requisito_id BIGINT NOT NULL REFERENCES mpv_requisitos_documentales(id) ON DELETE CASCADE,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    estado_revision VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_REVISION',
    observaciones TEXT,
    usuario_revisor_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_revision TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mpv_rev_req_estado CHECK (estado_revision IN ('PENDIENTE_REVISION', 'CONFORME', 'OBSERVADO', 'NO_APLICA')),
    CONSTRAINT uq_mpv_rev_req UNIQUE (expediente_id, requisito_id)
);

CREATE INDEX IF NOT EXISTS idx_mpv_rev_req_exp ON mpv_revision_requisitos(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_rev_req_req ON mpv_revision_requisitos(requisito_id);
CREATE INDEX IF NOT EXISTS idx_mpv_rev_req_estado ON mpv_revision_requisitos(estado_revision);

-- 3. Tabla de Registro de Cambios de Prioridad
CREATE TABLE IF NOT EXISTS mpv_cambios_prioridad (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    prioridad_anterior VARCHAR(20) NOT NULL,
    prioridad_nueva VARCHAR(20) NOT NULL,
    motivo VARCHAR(1000) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mpv_prioridad_exp ON mpv_cambios_prioridad(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_prioridad_usr ON mpv_cambios_prioridad(usuario_id);
