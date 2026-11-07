-- ============================================================================
-- MIGRACIÓN FLYWAY V34: PLANO INTERACTIVO DE MERCADOS Y PUESTOS COMERCIALES
-- SIGM-QUINUA: Sistema Integral de Gestión Municipal - Municipalidad Distrital de Quinua
-- Módulo 2: Mercados Municipales - Prompt 32 de 150
-- ============================================================================

-- 1. Tabla de planos maestros por mercado
CREATE TABLE IF NOT EXISTS mercados_planos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE RESTRICT,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    ancho_base NUMERIC(10, 2) NOT NULL DEFAULT 1400.00,
    alto_base NUMERIC(10, 2) NOT NULL DEFAULT 900.00,
    unidad_referencia VARCHAR(30) NOT NULL DEFAULT 'PIXEL',
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    version_actual_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_mercados_planos_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_mercados_planos_dimensiones CHECK (ancho_base > 0 AND alto_base > 0)
);

CREATE INDEX IF NOT EXISTS idx_mercados_planos_mercado ON mercados_planos(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_planos_codigo ON mercados_planos(codigo);

-- 2. Tabla de versiones del plano
CREATE TABLE IF NOT EXISTS mercados_planos_versiones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_id UUID NOT NULL REFERENCES mercados_planos(id) ON DELETE CASCADE,
    numero_version INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    descripcion_cambios VARCHAR(500),
    usuario_creador_id UUID,
    usuario_creador_nombre VARCHAR(100),
    usuario_publicador_id UUID,
    usuario_publicador_nombre VARCHAR(100),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_publicacion TIMESTAMP WITH TIME ZONE,
    hash_contenido VARCHAR(64),
    imagen_referencia_url VARCHAR(255),
    imagen_escala NUMERIC(6, 3) DEFAULT 1.000,
    imagen_pos_x NUMERIC(10, 2) DEFAULT 0.00,
    imagen_pos_y NUMERIC(10, 2) DEFAULT 0.00,
    imagen_opacidad NUMERIC(4, 2) DEFAULT 0.50,
    imagen_visible BOOLEAN DEFAULT FALSE,
    imagen_bloqueada BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    CONSTRAINT uq_mercados_planos_version UNIQUE (plano_id, numero_version),
    CONSTRAINT chk_mercados_planos_version_estado CHECK (estado IN ('BORRADOR', 'EN_REVISION', 'PUBLICADA', 'ARCHIVADA'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_planos_versiones_plano ON mercados_planos_versiones(plano_id);
CREATE INDEX IF NOT EXISTS idx_mercados_planos_versiones_estado ON mercados_planos_versiones(plano_id, estado);

-- 3. Llave foránea diferida desde mercados_planos hacia su versión actual
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_mercados_planos_version_actual'
    ) THEN
        ALTER TABLE mercados_planos
            ADD CONSTRAINT fk_mercados_planos_version_actual
            FOREIGN KEY (version_actual_id)
            REFERENCES mercados_planos_versiones(id)
            ON DELETE SET NULL;
    END IF;
END $$;

-- 4. Tabla de elementos gráficos del plano
CREATE TABLE IF NOT EXISTS mercados_planos_elementos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_plano_id UUID NOT NULL REFERENCES mercados_planos_versiones(id) ON DELETE CASCADE,
    tipo_elemento VARCHAR(40) NOT NULL,
    referencia_entidad_id UUID,
    codigo_visual VARCHAR(60) NOT NULL,
    etiqueta VARCHAR(150),
    coordenada_x NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    coordenada_y NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    ancho NUMERIC(10, 2) NOT NULL DEFAULT 60.00,
    alto NUMERIC(10, 2) NOT NULL DEFAULT 60.00,
    rotacion NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    orden_capa INT NOT NULL DEFAULT 0,
    estilo_configurado TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mercados_planos_elementos_tipo CHECK (
        tipo_elemento IN (
            'MERCADO', 'SECTOR', 'PABELLON', 'ZONA', 'PUESTO',
            'PASILLO', 'ACCESO', 'SALIDA', 'ESCALERA', 'AREA_COMUN',
            'ETIQUETA', 'OTRO'
        )
    ),
    CONSTRAINT chk_mercados_planos_elementos_dimensiones CHECK (ancho > 0 AND alto > 0)
);

CREATE INDEX IF NOT EXISTS idx_mercados_planos_elementos_version ON mercados_planos_elementos(version_plano_id);
CREATE INDEX IF NOT EXISTS idx_mercados_planos_elementos_ref ON mercados_planos_elementos(version_plano_id, referencia_entidad_id);
CREATE INDEX IF NOT EXISTS idx_mercados_planos_elementos_tipo ON mercados_planos_elementos(version_plano_id, tipo_elemento);

-- 5. Tabla de auditoría inmutable de planos
CREATE TABLE IF NOT EXISTS mercados_planos_auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plano_id UUID NOT NULL,
    version_plano_id UUID,
    tipo_evento VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    usuario_id UUID,
    username VARCHAR(100) NOT NULL,
    detalles_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mercados_planos_auditoria_plano ON mercados_planos_auditoria(plano_id);
CREATE INDEX IF NOT EXISTS idx_mercados_planos_auditoria_fecha ON mercados_planos_auditoria(created_at);

-- 6. Inserción de los 10 Permisos del Sistema para Planos de Mercados
INSERT INTO permisos (codigo, nombre, accion, descripcion, modulo_id)
SELECT p.codigo, p.nombre, p.accion, p.descripcion, m.id
FROM (
    VALUES
        ('MERCADOS_PLANO_VER', 'Ver Planos de Mercados', 'VIEW', 'Visualizar planos interactivos y distribución espacial de puestos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_CREAR', 'Crear Planos de Mercados', 'CREATE', 'Configurar nuevos lienzos y planos maestros de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_EDITAR', 'Editar Planos de Mercados', 'UPDATE', 'Acceder al editor gráfico de planos y manipular elementos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_GUARDAR', 'Guardar Distribución de Planos', 'MANAGE', 'Guardar borradores de planos y cambios de distribución', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_REVISAR', 'Revisar Versiones de Planos', 'APPROVE', 'Enviar a revisión y calificar versiones técnicas de planos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_PUBLICAR', 'Publicar Planos de Mercados', 'APPROVE', 'Publicar versiones oficiales de planos y archivar anteriores', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_VERSIONES_VER', 'Ver Historial de Versiones', 'VIEW', 'Consultar versiones históricas y trazabilidad de planos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_RESTAURAR', 'Restaurar Versiones de Planos', 'MANAGE', 'Crear un nuevo borrador a partir de una versión histórica archivada', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_REFERENCIA_GESTIONAR', 'Gestionar Imágenes de Referencia', 'MANAGE', 'Cargar y ajustar imágenes de referencia para calcar planos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PLANO_AUDITORIA_VER', 'Ver Auditoría de Planos', 'VIEW', 'Consultar la traza de auditoría de modificaciones de planos', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- 7. Asignar permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_PLANO_VER',
        'MERCADOS_PLANO_CREAR',
        'MERCADOS_PLANO_EDITAR',
        'MERCADOS_PLANO_GUARDAR',
        'MERCADOS_PLANO_REVISAR',
        'MERCADOS_PLANO_PUBLICAR',
        'MERCADOS_PLANO_VERSIONES_VER',
        'MERCADOS_PLANO_RESTAURAR',
        'MERCADOS_PLANO_REFERENCIA_GESTIONAR',
        'MERCADOS_PLANO_AUDITORIA_VER'
    )
) p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
