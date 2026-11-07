-- ==============================================================================
-- SIGM-QUINUA - Migración V12: Perfiles de Usuario, Preferencias y Fotografía
-- Municipalidad Distrital de Quinua, Huamanga, Ayacucho, Perú
-- ==============================================================================

-- 1. Agregar columna de fotografía a la tabla de personas
ALTER TABLE personas ADD COLUMN IF NOT EXISTS foto_url VARCHAR(255);

-- 2. Tabla para almacenar las preferencias individuales de usuario
CREATE TABLE IF NOT EXISTS usuario_preferencias (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    tema VARCHAR(20) NOT NULL DEFAULT 'OSCURO',
    idioma VARCHAR(10) NOT NULL DEFAULT 'es',
    notif_email BOOLEAN NOT NULL DEFAULT true,
    notif_sistema BOOLEAN NOT NULL DEFAULT true,
    page_size INT NOT NULL DEFAULT 10,
    sidebar_colapsado BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_usuario_preferencias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices de consulta rápida
CREATE INDEX IF NOT EXISTS idx_usuario_preferencias_usuario_id ON usuario_preferencias(usuario_id);

-- 3. Comentarios descriptivos para documentación en base de datos
COMMENT ON TABLE usuario_preferencias IS 'Preferencias de visualización, interfaz y notificaciones para usuarios de SIGM-QUINUA';
COMMENT ON COLUMN usuario_preferencias.tema IS 'Tema de interfaz: OSCURO, CLARO, SISTEMA';
COMMENT ON COLUMN usuario_preferencias.page_size IS 'Cantidad de elementos por defecto en tablas paginadas (10, 20, 50, 100)';
COMMENT ON COLUMN personas.foto_url IS 'Ruta relativa de almacenamiento seguro para la fotografía de perfil';
