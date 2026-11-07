-- ==============================================================================
-- SIGM-QUINUA: Migración Flyway V54
-- Módulo 4: Recaudación Tributaria Municipal
-- Subsistema de Registro, Caracterización y Control de Predios Municipales (Prompt 53)
-- ==============================================================================

-- 1. Ampliación de la tabla de Predios Tributarios
ALTER TABLE tributaria_predios
    ADD COLUMN IF NOT EXISTS clasificacion_predio VARCHAR(50) NOT NULL DEFAULT 'CASA_HABITACION',
    ADD COLUMN IF NOT EXISTS uso_declarado VARCHAR(50) NOT NULL DEFAULT 'VIVIENDA',
    ADD COLUMN IF NOT EXISTS departamento VARCHAR(100) NOT NULL DEFAULT 'Ayacucho',
    ADD COLUMN IF NOT EXISTS provincia VARCHAR(100) NOT NULL DEFAULT 'Huamanga',
    ADD COLUMN IF NOT EXISTS distrito VARCHAR(100) NOT NULL DEFAULT 'Quinua',
    ADD COLUMN IF NOT EXISTS ubigeo VARCHAR(10) NOT NULL DEFAULT '050108',
    ADD COLUMN IF NOT EXISTS comunidad_centro_poblado VARCHAR(150),
    ADD COLUMN IF NOT EXISTS tipo_via VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nombre_via VARCHAR(150),
    ADD COLUMN IF NOT EXISTS numero_municipal VARCHAR(30),
    ADD COLUMN IF NOT EXISTS manzana VARCHAR(30),
    ADD COLUMN IF NOT EXISTS lote VARCHAR(30),
    ADD COLUMN IF NOT EXISTS interior VARCHAR(30),
    ADD COLUMN IF NOT EXISTS zona VARCHAR(100),
    ADD COLUMN IF NOT EXISTS area_terreno_m2 NUMERIC(12, 4) NOT NULL DEFAULT 0 CHECK (area_terreno_m2 >= 0),
    ADD COLUMN IF NOT EXISTS tiene_agua BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tiene_desague BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tiene_luz BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tiene_alumbrado_publico BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tiene_pistas_veredas BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tiene_limpieza_publica BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS latitud NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS longitud NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS precision_metros INTEGER,
    ADD COLUMN IF NOT EXISTS fuente_coordenadas VARCHAR(50) NOT NULL DEFAULT 'NO_DISPONIBLE',
    ADD COLUMN IF NOT EXISTS motivo_cambio_estado TEXT,
    ADD COLUMN IF NOT EXISTS fecha_cambio_estado TIMESTAMP WITH TIME ZONE;

-- Actualizar area_terreno_m2 con area_terreno para registros existentes
UPDATE tributaria_predios
SET area_terreno_m2 = CASE
    WHEN unidad_medida_area = 'HA' THEN area_terreno * 10000
    ELSE area_terreno
END
WHERE area_terreno_m2 = 0 AND area_terreno > 0;

-- 2. Tabla de Construcciones y Mejoras del Predio
CREATE TABLE IF NOT EXISTS tributaria_construcciones_predio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    predio_id UUID NOT NULL REFERENCES tributaria_predios(id) ON DELETE CASCADE,
    codigo_construccion VARCHAR(50) NOT NULL,
    seccion_bloque VARCHAR(100),
    nivel_piso INTEGER NOT NULL DEFAULT 1 CHECK (nivel_piso >= 1 AND nivel_piso <= 20),
    area_construida NUMERIC(12, 4) NOT NULL CHECK (area_construida >= 0),
    material_predominante VARCHAR(50) NOT NULL DEFAULT 'ADOBE_TAPIA',
    estado_conservacion VARCHAR(50) NOT NULL DEFAULT 'REGULAR',
    anio_construccion INTEGER CHECK (anio_construccion IS NULL OR (anio_construccion >= 1800 AND anio_construccion <= 2100)),
    clasificacion_edificacion VARCHAR(50) NOT NULL DEFAULT 'PREDOMINANTE',
    descripcion TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_const_predio ON tributaria_construcciones_predio(predio_id);
CREATE INDEX IF NOT EXISTS idx_trib_const_piso ON tributaria_construcciones_predio(nivel_piso);
CREATE INDEX IF NOT EXISTS idx_trib_const_mat ON tributaria_construcciones_predio(material_predominante);

-- 3. Tabla de Documentos y Archivos de Sustento del Predio
CREATE TABLE IF NOT EXISTS tributaria_documentos_predio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    predio_id UUID NOT NULL REFERENCES tributaria_predios(id) ON DELETE CASCADE,
    tipo_documento VARCHAR(50) NOT NULL DEFAULT 'DECLARACION_JURADA',
    titulo VARCHAR(255) NOT NULL,
    archivo_url VARCHAR(500),
    numero_folio VARCHAR(50),
    fecha_emision DATE,
    entidad_emisora VARCHAR(150),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_doc_predio ON tributaria_documentos_predio(predio_id);
CREATE INDEX IF NOT EXISTS idx_trib_doc_tipo ON tributaria_documentos_predio(tipo_documento);

-- 4. Tabla de Historial y Auditoría de Estados del Predio
CREATE TABLE IF NOT EXISTS tributaria_historial_estados_predio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    predio_id UUID NOT NULL REFERENCES tributaria_predios(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(40) NOT NULL,
    nuevo_estado VARCHAR(40) NOT NULL,
    motivo TEXT NOT NULL,
    resolucion_sustento VARCHAR(100),
    usuario_registro VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_predio ON tributaria_historial_estados_predio(predio_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_predio_fecha ON tributaria_historial_estados_predio(fecha_registro);

-- 5. Nuevos Permisos Especializados para Gestión y Control Predial
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_CAMBIAR_ESTADO', 'Cambiar Estado de Predio', 'UPDATE',
       'Permite modificar el estado administrativo de un predio con justificación sustentada', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_GESTIONAR_CONSTRUCCIONES', 'Gestionar Construcciones de Predio', 'UPDATE',
       'Permite registrar, actualizar y detallar bloques y niveles construidos en el predio', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_GESTIONAR_RELACIONES', 'Gestionar Titulares y Poseedores del Predio', 'UPDATE',
       'Permite asociar y modificar relaciones contribuyente-predio y porcentajes de participación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_GESTIONAR_DOCUMENTOS', 'Gestionar Documentos y Planos de Predio', 'UPDATE',
       'Permite adjuntar constancias, títulos, planos y documentos sustentatorios de predios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_VER_HISTORIAL', 'Ver Historial y Auditoría Predial', 'VIEW',
       'Permite auditar el historial de cambios, titularidades y transiciones del predio', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_VER_MAPA', 'Visualizar Mapa Territorial de Predios', 'VIEW',
       'Permite consultar la ubicación geográfica y cartográfica de predios en Quinua', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_EXPORTAR', 'Exportar Catastro de Predios', 'VIEW',
       'Permite descargar el registro de predios tributarios en formatos Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignación a Roles Administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_PREDIOS_CAMBIAR_ESTADO',
      'TRIBUTARIA_PREDIOS_GESTIONAR_CONSTRUCCIONES',
      'TRIBUTARIA_PREDIOS_GESTIONAR_RELACIONES',
      'TRIBUTARIA_PREDIOS_GESTIONAR_DOCUMENTOS',
      'TRIBUTARIA_PREDIOS_VER_HISTORIAL',
      'TRIBUTARIA_PREDIOS_VER_MAPA',
      'TRIBUTARIA_PREDIOS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
