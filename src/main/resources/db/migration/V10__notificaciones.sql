-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V10: Tabla de Notificaciones Institucionales del Sistema
-- Tabla: notificaciones
-- ==============================================================================

CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'INFO',
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura TIMESTAMP WITH TIME ZONE,
    url_destino VARCHAR(255),
    origen VARCHAR(100) DEFAULT 'SISTEMA',
    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_notificacion_tipo CHECK (tipo IN ('INFO', 'EXITO', 'ALERTA', 'ERROR', 'SISTEMA'))
);

CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_notificaciones_leida ON notificaciones(usuario_id, leida);
CREATE INDEX IF NOT EXISTS idx_notificaciones_fecha ON notificaciones(fecha_creacion DESC);

-- Notificación inicial del sistema para usuarios existentes
INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo, leida, url_destino, origen)
SELECT 
    u.id,
    'Bienvenido a SIGM-QUINUA',
    'La plataforma municipal de la Municipalidad Distrital de Quinua se encuentra activa y lista para su operación institucional.',
    'SISTEMA',
    FALSE,
    '/admin/dashboard',
    'SISTEMA'
FROM usuarios u
WHERE NOT EXISTS (
    SELECT 1 FROM notificaciones n WHERE n.usuario_id = u.id AND n.titulo = 'Bienvenido a SIGM-QUINUA'
);
