-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V1: Estructura Institucional Base
-- Tablas: municipalidades, oficinas, cargos
-- ==============================================================================

-- 1. Tabla de Municipalidad
CREATE TABLE IF NOT EXISTS municipalidades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    nombre_corto VARCHAR(100) NOT NULL,
    ubigeo VARCHAR(6) NOT NULL,
    departamento VARCHAR(50) NOT NULL DEFAULT 'Ayacucho',
    provincia VARCHAR(50) NOT NULL DEFAULT 'Huamanga',
    distrito VARCHAR(50) NOT NULL DEFAULT 'Quinua',
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_institucional VARCHAR(100),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_municipalidad_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. Tabla de Oficinas y Unidades Orgánicas (Jerárquica)
CREATE TABLE IF NOT EXISTS oficinas (
    id BIGSERIAL PRIMARY KEY,
    municipalidad_id BIGINT NOT NULL,
    oficina_padre_id BIGINT,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    sigla VARCHAR(30),
    descripcion VARCHAR(500),
    nivel_jerarquico INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_oficinas_municipalidad FOREIGN KEY (municipalidad_id) REFERENCES municipalidades(id) ON DELETE RESTRICT,
    CONSTRAINT fk_oficinas_padre FOREIGN KEY (oficina_padre_id) REFERENCES oficinas(id) ON DELETE RESTRICT,
    CONSTRAINT uq_oficinas_codigo UNIQUE (codigo),
    CONSTRAINT chk_oficina_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 3. Tabla de Cargos Institucionales
CREATE TABLE IF NOT EXISTS cargos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    nivel_jerarquico INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_cargos_codigo UNIQUE (codigo),
    CONSTRAINT chk_cargo_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);
