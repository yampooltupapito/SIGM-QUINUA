-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V40
-- Módulo 3: Seguridad Ciudadana y Serenazgo
-- Subsistema: Registro y Administración de Sectores y Zonas de Patrullaje
-- ==============================================================================

-- 1. Actualización de restricciones y campos de la tabla serenazgo_sectores
ALTER TABLE serenazgo_sectores DROP CONSTRAINT IF EXISTS chk_serenazgo_sector_estado;
ALTER TABLE serenazgo_sectores ADD CONSTRAINT chk_serenazgo_sector_estado 
    CHECK (estado IN ('BORRADOR', 'ACTIVO', 'INACTIVO', 'ARCHIVADO'));

ALTER TABLE serenazgo_sectores 
    ADD COLUMN IF NOT EXISTS fecha_inicio_vigencia DATE,
    ADD COLUMN IF NOT EXISTS fecha_fin_vigencia DATE,
    ADD COLUMN IF NOT EXISTS observaciones TEXT,
    ADD COLUMN IF NOT EXISTS geometria_geojson TEXT,
    ADD COLUMN IF NOT EXISTS tiene_delimitacion BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_serenazgo_sectores_delimitacion ON serenazgo_sectores(tiene_delimitacion);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sectores_oficina ON serenazgo_sectores(oficina_responsable_id);

-- 2. Actualización de restricciones y campos de la tabla serenazgo_zonas
ALTER TABLE serenazgo_zonas DROP CONSTRAINT IF EXISTS chk_serenazgo_zona_estado;
ALTER TABLE serenazgo_zonas ADD CONSTRAINT chk_serenazgo_zona_estado 
    CHECK (estado IN ('BORRADOR', 'ACTIVO', 'INACTIVO', 'ARCHIVADO'));

ALTER TABLE serenazgo_zonas 
    ADD COLUMN IF NOT EXISTS prioridad_operativa VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
    ADD COLUMN IF NOT EXISTS fecha_inicio_vigencia DATE,
    ADD COLUMN IF NOT EXISTS fecha_fin_vigencia DATE,
    ADD COLUMN IF NOT EXISTS observaciones TEXT,
    ADD COLUMN IF NOT EXISTS geometria_geojson TEXT,
    ADD COLUMN IF NOT EXISTS tiene_delimitacion BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE serenazgo_zonas DROP CONSTRAINT IF EXISTS chk_serenazgo_zona_prioridad;
ALTER TABLE serenazgo_zonas ADD CONSTRAINT chk_serenazgo_zona_prioridad 
    CHECK (prioridad_operativa IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA'));

CREATE INDEX IF NOT EXISTS idx_serenazgo_zonas_delimitacion ON serenazgo_zonas(tiene_delimitacion);
CREATE INDEX IF NOT EXISTS idx_serenazgo_zonas_prioridad ON serenazgo_zonas(prioridad_operativa);
CREATE INDEX IF NOT EXISTS idx_serenazgo_zonas_estado ON serenazgo_zonas(estado);

-- 3. Historial de Delimitaciones Geográficas (Versionado Territorial Inmutable)
CREATE TABLE IF NOT EXISTS serenazgo_territorio_historial_delimitaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_entidad VARCHAR(20) NOT NULL, -- 'SECTOR' o 'ZONA'
    entidad_id UUID NOT NULL,
    numero_version INT NOT NULL DEFAULT 1,
    geometria_geojson TEXT NOT NULL,
    motivo_cambio TEXT NOT NULL,
    documento_referencia VARCHAR(150),
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_hist_delim_tipo CHECK (tipo_entidad IN ('SECTOR', 'ZONA'))
);

CREATE INDEX IF NOT EXISTS idx_hist_delim_entidad ON serenazgo_territorio_historial_delimitaciones(tipo_entidad, entidad_id);
CREATE INDEX IF NOT EXISTS idx_hist_delim_fecha ON serenazgo_territorio_historial_delimitaciones(created_at);

-- 4. Historial de Reasignaciones de Sector para Zonas
CREATE TABLE IF NOT EXISTS serenazgo_zonas_historial_sector (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zona_id UUID NOT NULL REFERENCES serenazgo_zonas(id) ON DELETE CASCADE,
    sector_anterior_id UUID NOT NULL REFERENCES serenazgo_sectores(id) ON DELETE RESTRICT,
    sector_nuevo_id UUID NOT NULL REFERENCES serenazgo_sectores(id) ON DELETE RESTRICT,
    motivo TEXT NOT NULL,
    documento_referencia VARCHAR(150),
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hist_reasig_zona ON serenazgo_zonas_historial_sector(zona_id);
CREATE INDEX IF NOT EXISTS idx_hist_reasig_fecha ON serenazgo_zonas_historial_sector(created_at);

-- 5. Historial de Transiciones de Estado Territorial
CREATE TABLE IF NOT EXISTS serenazgo_territorio_historial_estados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_entidad VARCHAR(20) NOT NULL, -- 'SECTOR' o 'ZONA'
    entidad_id UUID NOT NULL,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    motivo TEXT NOT NULL,
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_hist_estado_tipo CHECK (tipo_entidad IN ('SECTOR', 'ZONA'))
);

CREATE INDEX IF NOT EXISTS idx_hist_estado_entidad ON serenazgo_territorio_historial_estados(tipo_entidad, entidad_id);

-- 6. Sembrado de Permisos RBAC para Administración Territorial
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SECTORES_CREAR', 'Registrar Sectores de Patrullaje', 'CREATE',
       'Permite crear nuevos sectores de patrullaje municipal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SECTORES_EDITAR', 'Editar Sectores de Patrullaje', 'UPDATE',
       'Permite modificar datos técnicos y administrativos de sectores', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SECTORES_CAMBIAR_ESTADO', 'Cambiar Estado de Sector', 'UPDATE',
       'Permite cambiar el estado operativo de sectores', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ZONAS_VER', 'Ver Zonas de Patrullaje', 'VIEW',
       'Permite consultar las zonas de patrullaje adscritas a sectores', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ZONAS_CREAR', 'Registrar Zonas de Patrullaje', 'CREATE',
       'Permite crear nuevas zonas de patrullaje dentro de un sector', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ZONAS_EDITAR', 'Editar Zonas de Patrullaje', 'UPDATE',
       'Permite modificar datos técnicos y operativos de zonas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ZONAS_REASIGNAR', 'Reasignar Zonas de Sector', 'UPDATE',
       'Permite reasignar una zona de patrullaje a otro sector', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_GEOGRAFIA_EDITAR', 'Editar Geometrías y Delimitaciones', 'UPDATE',
       'Permite delimitar y modificar geometrías de sectores y zonas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_GEOGRAFIA_IMPORTAR', 'Importar Capas GeoJSON', 'MANAGE',
       'Permite importar capas y geometrías en formato GeoJSON', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_GEOGRAFIA_EXPORTAR', 'Exportar Capas Territoriales', 'EXPORT',
       'Permite exportar capas territoriales en GeoJSON o Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de permisos territoriales al Administrador y Superadministrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND (p.codigo LIKE 'SERENAZGO_SECTORES_%' OR p.codigo LIKE 'SERENAZGO_ZONAS_%' OR p.codigo LIKE 'SERENAZGO_GEOGRAFIA_%')
ON CONFLICT DO NOTHING;

-- Asignación de permisos al Sereno
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_SECTORES_VER',
      'SERENAZGO_ZONAS_VER',
      'SERENAZGO_GEOGRAFIA_EXPORTAR'
  )
ON CONFLICT DO NOTHING;
