-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V9: Tabla de Tokens de Recuperación Segura de Contraseña
-- ==============================================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMPTZ,
    ip_solicitud VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_password_reset_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT uq_password_reset_token_hash UNIQUE (token_hash)
);

-- Índices B-tree para consultas de alta velocidad y revocación
CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_password_reset_usuario ON password_reset_tokens (usuario_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_expires ON password_reset_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_used ON password_reset_tokens (used);

COMMENT ON TABLE password_reset_tokens IS 'Tokens temporales para recuperación de acceso con huella criptográfica SHA-256';
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'Huella criptográfica SHA-256 del token aleatorio entregado al usuario';
COMMENT ON COLUMN password_reset_tokens.expires_at IS 'Fecha y hora de caducidad del token (vigencia corta)';
COMMENT ON COLUMN password_reset_tokens.used IS 'Indicador de un solo uso para evitar ataques de repetición';
