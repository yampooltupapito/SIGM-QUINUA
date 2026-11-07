-- ==============================================================================
-- SIGM-QUINUA - V25: Organización Física de Mercados Municipales
-- Tablas: mercados_zonas, mercados_categorias_comerciales,
--         mercados_sectores_cambios_estado, mercados_pabellones_cambios_estado
-- Modificaciones: mercados_sectores, mercados_pabellones
-- Permisos: MERCADOS_SECTOR_VER, MERCADOS_PABELLON_VER,
--           MERCADOS_ESTRUCTURA_VER, MERCADOS_ZONA_GESTIONAR
-- ==============================================================================

-- 1. Ampliación de Estados y Control de Concurrencia en Sectores
ALTER TABLE mercados_sectores DROP CONSTRAINT IF EXISTS chk_mercados_sectores_estado;
ALTER TABLE mercados_sectores ADD CONSTRAINT chk_mercados_sectores_estado 
    CHECK (estado IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'));

ALTER TABLE mercados_sectores ADD COLUMN IF NOT EXISTS categoria_comercial VARCHAR(100);
ALTER TABLE mercados_sectores ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 2. Ampliación de Estados y Control de Concurrencia en Pabellones
ALTER TABLE mercados_pabellones DROP CONSTRAINT IF EXISTS chk_mercados_pabellones_estado;
ALTER TABLE mercados_pabellones ADD CONSTRAINT chk_mercados_pabellones_estado 
    CHECK (estado IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'));

ALTER TABLE mercados_pabellones ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 3. Historial Inmutable de Cambios de Estado para Sectores
CREATE TABLE IF NOT EXISTS mercados_sectores_cambios_estado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sector_id UUID NOT NULL REFERENCES mercados_sectores(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    usuario_responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_username VARCHAR(100) NOT NULL,
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mercados_sector_cambio_ant CHECK (estado_anterior IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO')),
    CONSTRAINT chk_mercados_sector_cambio_nue CHECK (estado_nuevo IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_sectores_cambios_sector ON mercados_sectores_cambios_estado(sector_id);
CREATE INDEX IF NOT EXISTS idx_mercados_sectores_cambios_fecha ON mercados_sectores_cambios_estado(fecha_cambio);

-- 4. Historial Inmutable de Cambios de Estado para Pabellones
CREATE TABLE IF NOT EXISTS mercados_pabellones_cambios_estado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pabellon_id UUID NOT NULL REFERENCES mercados_pabellones(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    usuario_responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_username VARCHAR(100) NOT NULL,
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mercados_pabellon_cambio_ant CHECK (estado_anterior IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO')),
    CONSTRAINT chk_mercados_pabellon_cambio_nue CHECK (estado_nuevo IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_pabellones_cambios_pabellon ON mercados_pabellones_cambios_estado(pabellon_id);
CREATE INDEX IF NOT EXISTS idx_mercados_pabellones_cambios_fecha ON mercados_pabellones_cambios_estado(fecha_cambio);

-- 5. Catálogo Administrable de Categorías Comerciales
CREATE TABLE IF NOT EXISTS mercados_categorias_comerciales (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    orden_visual INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mercados_categorias_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

INSERT INTO mercados_categorias_comerciales (codigo, nombre, descripcion, estado, orden_visual)
VALUES
    ('ABARROTES', 'Abarrotes y Víveres', 'Venta minorista de víveres secos, enlatados y de primera necesidad', 'ACTIVO', 1),
    ('FRUTAS_VERDURAS', 'Frutas y Verduras', 'Comercialización de hortalizas, tubérculos y frutas frescas', 'ACTIVO', 2),
    ('CARNES_PESCADOS', 'Carnes y Productos Hidrobiológicos', 'Expendio de carnes rojas, aves, pescados y mariscos bajo cadena de frío', 'ACTIVO', 3),
    ('COMIDAS_JUGOS', 'Comidas y Juguería', 'Puestos de comida preparada, gastronomía local y bebidas naturales', 'ACTIVO', 4),
    ('VESTIMENTA_CALZADO', 'Vestimenta y Calzado', 'Prendas de vestir, calzado, sombreros y textiles de uso diario', 'ACTIVO', 5),
    ('ARTESANIAS', 'Artesanías Tradicionales', 'Cerámica de Quinua, retablos, textiles típicos y recuerdos turísticos', 'ACTIVO', 6),
    ('SERVICIOS', 'Servicios Menores', 'Cerrajería, reparación de calzado, sastrería y servicios varios', 'ACTIVO', 7),
    ('OTROS', 'Otros Giros', 'Productos misceláneos y giros complementarios autorizados', 'ACTIVO', 8)
ON CONFLICT (codigo) DO NOTHING;

-- 6. Tabla de Zonas Comerciales y Áreas Comunes por Mercado
CREATE TABLE IF NOT EXISTS mercados_zonas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    tipo_zona VARCHAR(50) NOT NULL DEFAULT 'COMERCIAL',
    es_asignable BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    orden_visual INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_mercados_zonas_mercado_codigo UNIQUE (mercado_id, codigo),
    CONSTRAINT chk_mercados_zonas_tipo CHECK (tipo_zona IN ('COMERCIAL', 'CIRCULACION', 'SERVICIOS_COMUNES', 'ALMACENAMIENTO', 'MANTENIMIENTO', 'OTRAS')),
    CONSTRAINT chk_mercados_zonas_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_zonas_mercado ON mercados_zonas(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_zonas_estado ON mercados_zonas(estado);

-- 7. Registro de Nuevos Permisos de Seguridad
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_SECTOR_VER', 'Ver Sectores de Mercado', 'VIEW',
       'Permite consultar sectores, categorías comerciales y puestos asociados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PABELLON_VER', 'Ver Pabellones de Mercado', 'VIEW',
       'Permite consultar pabellones, niveles y puestos asociados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_ESTRUCTURA_VER', 'Ver Estructura de Mercados', 'VIEW',
       'Permite visualizar la jerarquía física y funcional de sectores, pabellones y puestos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_ZONA_GESTIONAR', 'Gestionar Zonas de Mercado', 'MANAGE',
       'Permite registrar y administrar zonas comerciales y áreas comunes no asignables', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

-- 8. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'MERCADOS_SECTOR_VER',
      'MERCADOS_PABELLON_VER',
      'MERCADOS_ESTRUCTURA_VER',
      'MERCADOS_ZONA_GESTIONAR'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'MERCADOS_SECTOR_VER',
      'MERCADOS_PABELLON_VER',
      'MERCADOS_ESTRUCTURA_VER',
      'MERCADOS_ZONA_GESTIONAR'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR'
  AND p.codigo IN (
      'MERCADOS_SECTOR_VER',
      'MERCADOS_PABELLON_VER',
      'MERCADOS_ESTRUCTURA_VER'
  )
ON CONFLICT DO NOTHING;
