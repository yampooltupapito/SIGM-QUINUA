-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V3: Catálogo Dinámico de Actores y Asignaciones Funcionales
-- Tablas: tipos_actores, persona_oficinas
-- ==============================================================================

-- 1. Catálogo Dinámico de Tipos de Actores
CREATE TABLE IF NOT EXISTS tipos_actores (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_tipos_actores_codigo UNIQUE (codigo),
    CONSTRAINT chk_tipo_actor_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. Tabla de Asignaciones Institucionales de Personas a Oficinas y Cargos
CREATE TABLE IF NOT EXISTS persona_oficinas (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    oficina_id BIGINT NOT NULL,
    cargo_id BIGINT,
    tipo_actor_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    principal BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_persona_oficinas_persona FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_persona_oficinas_oficina FOREIGN KEY (oficina_id) REFERENCES oficinas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_persona_oficinas_cargo FOREIGN KEY (cargo_id) REFERENCES cargos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_persona_oficinas_tipo_actor FOREIGN KEY (tipo_actor_id) REFERENCES tipos_actores(id) ON DELETE RESTRICT,
    CONSTRAINT chk_persona_oficina_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- Poblado de Catálogo Inicial de Tipos de Actores (Sin inventar datos personales)
INSERT INTO tipos_actores (codigo, nombre, descripcion) VALUES
    ('CIUDADANO', 'Ciudadano / Administrado', 'Vecino o administrado general que interactúa con la municipalidad'),
    ('TRABAJADOR_MUNICIPAL', 'Trabajador Municipal', 'Servidor público dependiente de la municipalidad'),
    ('JEFE_OFICINA', 'Jefe de Oficina / Unidad Orgánica', 'Titular responsable de unidad orgánica municipal'),
    ('FISCALIZADOR', 'Fiscalizador Municipal', 'Agente encargado de inspección tributaria y control urbano'),
    ('SERENO', 'Efectivo de Seguridad Ciudadana', 'Miembro del cuerpo de serenazgo distrital de Quinua'),
    ('COMERCIANTE', 'Comerciante / Titular de Licencia', 'Conductor de establecimiento comercial o puesto de abastos'),
    ('CONTRIBUYENTE', 'Contribuyente Tributario', 'Sujeto pasivo de tributos municipales e impuesto predial'),
    ('PROVEEDOR', 'Proveedor Municipal', 'Persona natural o jurídica proveedora de bienes y servicios'),
    ('BENEFICIARIO', 'Beneficiario Social', 'Poblador inscrito en programas sociales (Vaso de Leche, CIAM, etc.)'),
    ('SUPERVISOR', 'Supervisor Operativo', 'Auditor o supervisor de operaciones municipales'),
    ('OPERADOR', 'Operador del Sistema', 'Personal operativo que gestiona registros en la plataforma');
