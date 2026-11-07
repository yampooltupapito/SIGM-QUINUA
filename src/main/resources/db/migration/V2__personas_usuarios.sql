-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V2: Personas y Usuarios del Sistema
-- Tablas: personas, usuarios
-- ==============================================================================

-- 1. Tabla de Personas (Ciudadanos, Administrados y Servidores Municipales)
CREATE TABLE IF NOT EXISTS personas (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    correo VARCHAR(150),
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_personas_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT chk_personas_tipo_doc CHECK (tipo_documento IN ('DNI', 'RUC', 'CARNET_EXTRANJERIA', 'PASAPORTE', 'OTRO')),
    CONSTRAINT chk_personas_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. Tabla de Cuentas de Usuario para Acceso al Sistema
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_usuarios_persona FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE RESTRICT,
    CONSTRAINT uq_usuarios_persona UNIQUE (persona_id),
    CONSTRAINT uq_usuarios_username UNIQUE (username)
);
