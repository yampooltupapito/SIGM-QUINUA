-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V7: Auditoría Administrativa de Usuarios, Roles, Permisos y Estructura
-- Tabla: auditoria_administrativa
-- ==============================================================================

CREATE TABLE IF NOT EXISTS auditoria_administrativa (
    id BIGSERIAL PRIMARY KEY,
    usuario_responsable VARCHAR(100) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    entidad_afectada VARCHAR(100) NOT NULL,
    entidad_id BIGINT,
    resultado VARCHAR(50) NOT NULL DEFAULT 'EXITO',
    detalle TEXT,
    ip_origen VARCHAR(45),
    user_agent VARCHAR(255),
    trace_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_admin_resultado CHECK (resultado IN ('EXITO', 'FALLO', 'ALERTA'))
);

CREATE INDEX IF NOT EXISTS idx_audit_admin_usuario ON auditoria_administrativa(usuario_responsable);
CREATE INDEX IF NOT EXISTS idx_audit_admin_entidad ON auditoria_administrativa(entidad_afectada, entidad_id);
CREATE INDEX IF NOT EXISTS idx_audit_admin_fecha ON auditoria_administrativa(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_admin_accion ON auditoria_administrativa(accion);
