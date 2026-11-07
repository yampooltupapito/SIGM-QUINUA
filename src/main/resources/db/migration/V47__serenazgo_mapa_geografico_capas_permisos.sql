-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V47
-- Módulo 3: Seguridad Ciudadana y Serenazgo
-- Subsistema: Mapas Geográficos Interactivos, Incidencias y Zonas de Patrullaje
-- ==============================================================================

-- 1. Intentar habilitar extensión PostGIS de manera segura (fail-safe condicional)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'postgis') THEN
        BEGIN
            CREATE EXTENSION IF NOT EXISTS postgis;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'PostGIS no pudo ser inicializado por permisos o configuración, continuando en modo estándar WGS84/GeoJSON.';
        END;
    END IF;
END $$;

-- 2. Tabla de Metadatos de Capas Institucionales Cartográficas
CREATE TABLE IF NOT EXISTS serenazgo_mapa_capas_institucionales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_geometria VARCHAR(50) NOT NULL, -- 'POINT', 'LINESTRING', 'POLYGON', 'MULTIPOLYGON', 'MIXED'
    fuente_origen VARCHAR(150) NOT NULL, -- 'MUNICIPALIDAD_DISTRITAL_QUINUA', 'SINASEC', 'IGN', 'MININTER'
    sistema_referencia VARCHAR(30) NOT NULL DEFAULT 'EPSG:4326',
    version_capa INT NOT NULL DEFAULT 1,
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'APROBADA', -- 'PENDIENTE', 'APROBADA', 'OBSERVADA', 'HISTORICA'
    responsable_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_incorporacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    geometria_geojson TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_capas_codigo ON serenazgo_mapa_capas_institucionales(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_capas_activo ON serenazgo_mapa_capas_institucionales(activo);

-- 3. Índices en coordenadas para consultas espaciales por Bounding Box y Proximidad
CREATE INDEX IF NOT EXISTS idx_serenazgo_incidencias_coords 
    ON serenazgo_incidencias(latitud, longitud) 
    WHERE latitud IS NOT NULL AND longitud IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_coords 
    ON serenazgo_alertas_ciudadanas(latitud, longitud) 
    WHERE latitud IS NOT NULL AND longitud IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_serenazgo_intervenciones_coords 
    ON serenazgo_intervenciones(latitud, longitud) 
    WHERE latitud IS NOT NULL AND longitud IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_serenazgo_puntos_control_coords 
    ON serenazgo_rutas_puntos_control(latitud, longitud) 
    WHERE latitud IS NOT NULL AND longitud IS NOT NULL;

-- 4. Sembrado de Delimitaciones Poligonales Oficiales en Sectores de Quinua (GeoJSON WGS84: [longitud, latitud])
UPDATE serenazgo_sectores
SET tiene_delimitacion = TRUE,
    geometria_geojson = '{"type":"Polygon","coordinates":[[[-74.1435,-13.0395],[-74.1345,-13.0395],[-74.1345,-13.0465],[-74.1435,-13.0465],[-74.1435,-13.0395]]]}'
WHERE codigo = 'SEC-01' AND (geometria_geojson IS NULL OR tiene_delimitacion = FALSE);

UPDATE serenazgo_sectores
SET tiene_delimitacion = TRUE,
    geometria_geojson = '{"type":"Polygon","coordinates":[[[-74.1365,-13.0465],[-74.1275,-13.0465],[-74.1275,-13.0590],[-74.1365,-13.0590],[-74.1365,-13.0465]]]}'
WHERE codigo = 'SEC-02' AND (geometria_geojson IS NULL OR tiene_delimitacion = FALSE);

UPDATE serenazgo_sectores
SET tiene_delimitacion = TRUE,
    geometria_geojson = '{"type":"Polygon","coordinates":[[[-74.1495,-13.0350],[-74.1415,-13.0350],[-74.1415,-13.0440],[-74.1495,-13.0440],[-74.1495,-13.0350]]]}'
WHERE codigo = 'SEC-03' AND (geometria_geojson IS NULL OR tiene_delimitacion = FALSE);

-- Delimitaciones de Zonas Urbanas dentro del Sector 01
UPDATE serenazgo_zonas
SET tiene_delimitacion = TRUE,
    geometria_geojson = '{"type":"Polygon","coordinates":[[[-74.1405,-13.0410],[-74.1365,-13.0410],[-74.1365,-13.0445],[-74.1405,-13.0445],[-74.1405,-13.0410]]]}'
WHERE codigo = 'ZON-01-01' AND (geometria_geojson IS NULL OR tiene_delimitacion = FALSE);

-- 5. Sembrado de Capa Institucional de Límites Distritales de Quinua
INSERT INTO serenazgo_mapa_capas_institucionales (
    codigo, nombre, descripcion, tipo_geometria, fuente_origen, sistema_referencia, version_capa, estado_validacion, activo, geometria_geojson
) VALUES (
    'CAPA-QUI-01',
    'Delimitación Institucional de Sectores Operativos de Serenazgo Quinua',
    'Polígonos autorizados de cobertura de sectores y cuadrantes de patrullaje preventivo distrital.',
    'POLYGON',
    'MUNICIPALIDAD_DISTRITAL_QUINUA',
    'EPSG:4326',
    1,
    'APROBADA',
    TRUE,
    '{"type":"FeatureCollection","features":[]}'
) ON CONFLICT (codigo) DO NOTHING;

-- 6. Sembrado de Permisos RBAC para Cartografía y Mapas Geográficos
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VER', 'Ver Mapa Geográfico de Serenazgo', 'VIEW',
       'Permite acceder a la visualización cartográfica general de seguridad ciudadana', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VER_INCIDENCIAS', 'Ver Capa de Incidencias en Mapa', 'VIEW',
       'Permite visualizar marcadores y capas de incidencias registradas en el mapa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VER_ALERTAS', 'Ver Capa de Alertas en Mapa', 'VIEW',
       'Permite visualizar marcadores de alertas ciudadanas autorizadas en el mapa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VER_INTERVENCIONES', 'Ver Capa de Intervenciones en Mapa', 'VIEW',
       'Permite visualizar actuaciones e intervenciones documentadas en el mapa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VER_PATRULLAJES', 'Ver Capa de Patrullajes y Rutas', 'VIEW',
       'Permite visualizar rutas, puntos de control y patrullajes activos en el mapa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VER_RECORRIDOS', 'Ver Recorridos Documentados de Patrullaje', 'VIEW',
       'Permite visualizar los puntos de recorrido efectivamente documentados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_GESTIONAR_CAPAS', 'Gestionar Capas Cartográficas', 'MANAGE',
       'Permite activar, desactivar y configurar capas institucionales cartográficas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_IMPORTAR_GEOMETRIAS', 'Importar Geometrías GeoJSON', 'MANAGE',
       'Permite cargar y validar archivos GeoJSON oficiales para sectores y rutas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_VALIDAR_GEOMETRIAS', 'Validar Geometrías Cartográficas', 'UPDATE',
       'Permite validar técnicamente las delimitaciones antes de su publicación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MAPA_EXPORTAR', 'Exportar Cartografía y Reportes de Mapa', 'EXPORT',
       'Permite exportar capas en GeoJSON, reportes cartográficos en PDF y estadísticas en Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 7. Asignación de Permisos a Roles del Sistema
-- Administrador y Superadministrador: Todos los permisos del mapa
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'SERENAZGO_MAPA_%'
ON CONFLICT DO NOTHING;

-- Supervisor de Serenazgo: Visualización completa, exportación y validación
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SUPERVISOR_SERENAZGO'
  AND p.codigo IN (
      'SERENAZGO_MAPA_VER',
      'SERENAZGO_MAPA_VER_INCIDENCIAS',
      'SERENAZGO_MAPA_VER_ALERTAS',
      'SERENAZGO_MAPA_VER_INTERVENCIONES',
      'SERENAZGO_MAPA_VER_PATRULLAJES',
      'SERENAZGO_MAPA_VER_RECORRIDOS',
      'SERENAZGO_MAPA_VALIDAR_GEOMETRIAS',
      'SERENAZGO_MAPA_EXPORTAR'
  )
ON CONFLICT DO NOTHING;

-- Operador de Central de Comunicaciones: Visualización operativa y exportación
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR_CC'
  AND p.codigo IN (
      'SERENAZGO_MAPA_VER',
      'SERENAZGO_MAPA_VER_INCIDENCIAS',
      'SERENAZGO_MAPA_VER_ALERTAS',
      'SERENAZGO_MAPA_VER_INTERVENCIONES',
      'SERENAZGO_MAPA_VER_PATRULLAJES',
      'SERENAZGO_MAPA_VER_RECORRIDOS'
  )
ON CONFLICT DO NOTHING;

-- Sereno de Campo: Visualización de sectores, rutas y patrullajes
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_MAPA_VER',
      'SERENAZGO_MAPA_VER_INCIDENCIAS',
      'SERENAZGO_MAPA_VER_PATRULLAJES'
  )
ON CONFLICT DO NOTHING;
