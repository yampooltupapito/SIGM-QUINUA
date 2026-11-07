-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V6: Almacenamiento Seguro de Refresh Tokens y Auditoría de Seguridad
-- Tablas: refresh_tokens, auditoria_seguridad
-- ==============================================================================

-- 1. Tabla de Refresh Tokens Rotativos
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token_id VARCHAR(100) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    family_id VARCHAR(100) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_ip VARCHAR(50),
    user_agent VARCHAR(255),
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_tokens_token_id UNIQUE (token_id),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario ON refresh_tokens (usuario_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens (expires_at);

-- 2. Tabla de Auditoría de Eventos de Seguridad
CREATE TABLE IF NOT EXISTS auditoria_seguridad (
    id BIGSERIAL PRIMARY KEY,
    usuario VARCHAR(50),
    tipo_evento VARCHAR(50) NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    descripcion VARCHAR(500),
    ip_origen VARCHAR(50),
    user_agent VARCHAR(255),
    trace_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria_seguridad (usuario);
CREATE INDEX IF NOT EXISTS idx_auditoria_evento ON auditoria_seguridad (tipo_evento);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria_seguridad (created_at);
CREATE INDEX IF NOT EXISTS idx_auditoria_trace ON auditoria_seguridad (trace_id);
