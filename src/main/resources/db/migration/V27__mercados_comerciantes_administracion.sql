-- ==============================================================================
-- SIGM-QUINUA — V27: REGISTRO Y ADMINISTRACIÓN INTEGRAL DE COMERCIANTES
-- Módulo 2: Mercados Municipales (Prompt 25 de 150)
-- Descripción:
--   1. Catálogo Institucional de Personas Jurídicas y Representantes Legales.
--   2. Secuencia segura de códigos de comerciantes municipales.
--   3. Adaptación del padrón mercados_comerciantes para soportar Persona Natural y Jurídica.
--   4. Tablas inmutables de auditoría de estados y gestión documental.
--   5. Permisos granulares y asignación a roles del sistema.
-- ==============================================================================

-- 1. Secuencia segura para generación de código interno de comerciante (evita COUNT(*) + 1)
CREATE SEQUENCE IF NOT EXISTS seq_mercados_codigo_comerciante START WITH 1 INCREMENT BY 1;

-- 2. Entidad Institucional Reutilizable: Persona Jurídica (Núcleo común)
CREATE TABLE IF NOT EXISTS personas_juridicas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruc VARCHAR(11) NOT NULL,
    razon_social VARCHAR(255) NOT NULL,
    nombre_comercial VARCHAR(255),
    domicilio_fiscal VARCHAR(255),
    telefono VARCHAR(20),
    correo VARCHAR(150),
    estado_registro_interno VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_VERIFICACION',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_personas_juridicas_ruc UNIQUE (ruc),
    CONSTRAINT chk_personas_juridicas_ruc CHECK (ruc ~ '^[0-9]{11}$'),
    CONSTRAINT chk_personas_juridicas_estado CHECK (estado_registro_interno IN ('PENDIENTE_VERIFICACION', 'VERIFICADO', 'OBSERVADO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_personas_juridicas_ruc ON personas_juridicas(ruc);
CREATE INDEX IF NOT EXISTS idx_personas_juridicas_razon_social ON personas_juridicas(razon_social);

-- 3. Representantes de Personas Jurídicas
CREATE TABLE IF NOT EXISTS personas_juridicas_representantes (
    id BIGSERIAL PRIMARY KEY,
    persona_juridica_id UUID NOT NULL REFERENCES personas_juridicas(id) ON DELETE CASCADE,
    persona_id BIGINT NOT NULL REFERENCES personas(id) ON DELETE RESTRICT,
    tipo_representacion VARCHAR(50) NOT NULL DEFAULT 'REPRESENTANTE_LEGAL',
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_pj_representante_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'REVOCADO')),
    CONSTRAINT chk_pj_representante_tipo CHECK (tipo_representacion IN ('GERENTE_GENERAL', 'APODERADO', 'ADMINISTRADOR', 'REPRESENTANTE_LEGAL', 'OTRO'))
);

CREATE INDEX IF NOT EXISTS idx_pj_rep_juridica ON personas_juridicas_representantes(persona_juridica_id);
CREATE INDEX IF NOT EXISTS idx_pj_rep_persona ON personas_juridicas_representantes(persona_id);

-- 4. Modificaciones a la tabla mercados_comerciantes para dualidad Natural/Jurídica y nuevo estado
-- Permitir nulos en persona_id para personas jurídicas
ALTER TABLE mercados_comerciantes ALTER COLUMN persona_id DROP NOT NULL;

-- Agregar columna persona_juridica_id
ALTER TABLE mercados_comerciantes ADD COLUMN IF NOT EXISTS persona_juridica_id UUID REFERENCES personas_juridicas(id) ON DELETE RESTRICT;

-- Agregar columna tipo_comerciante
ALTER TABLE mercados_comerciantes ADD COLUMN IF NOT EXISTS tipo_comerciante VARCHAR(20) NOT NULL DEFAULT 'PERSONA_NATURAL';

-- Eliminar constraint previa uq_mercados_comerciantes_persona si existe
ALTER TABLE mercados_comerciantes DROP CONSTRAINT IF EXISTS uq_mercados_comerciantes_persona;

-- Eliminar constraint de estado previo para incluir PENDIENTE_VERIFICACION
ALTER TABLE mercados_comerciantes DROP CONSTRAINT IF EXISTS chk_mercados_comerciantes_estado;
ALTER TABLE mercados_comerciantes ADD CONSTRAINT chk_mercados_comerciantes_estado 
    CHECK (estado IN ('PENDIENTE_VERIFICACION', 'ACTIVO', 'SUSPENDIDO', 'INACTIVO'));

-- Restricción de integridad: Cada comerciante debe tener exactamente una identidad titular válida
ALTER TABLE mercados_comerciantes DROP CONSTRAINT IF EXISTS chk_comerciante_identidad;
ALTER TABLE mercados_comerciantes ADD CONSTRAINT chk_comerciante_identidad CHECK (
    (tipo_comerciante = 'PERSONA_NATURAL' AND persona_id IS NOT NULL AND persona_juridica_id IS NULL)
    OR
    (tipo_comerciante = 'PERSONA_JURIDICA' AND persona_juridica_id IS NOT NULL AND persona_id IS NULL)
);

-- Índices únicos condicionales para evitar duplicación de comerciantes para la misma persona o PJ
CREATE UNIQUE INDEX IF NOT EXISTS uq_comerciante_persona_idx ON mercados_comerciantes(persona_id) WHERE persona_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_comerciante_pj_idx ON mercados_comerciantes(persona_juridica_id) WHERE persona_juridica_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_mercados_comerciantes_tipo ON mercados_comerciantes(tipo_comerciante);

-- 5. Historial Inmutable de Cambios de Estado del Comerciante
CREATE TABLE IF NOT EXISTS mercados_comerciantes_cambios_estado (
    id BIGSERIAL PRIMARY KEY,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    motivo TEXT NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    observaciones TEXT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_username VARCHAR(100),
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comerciantes_cambios_estado_comerciante ON mercados_comerciantes_cambios_estado(comerciante_id);

-- 6. Documentos Sustentatorios Asociados al Comerciante (reutiliza tabla documentos)
CREATE TABLE IF NOT EXISTS mercados_comerciantes_documentos (
    id BIGSERIAL PRIMARY KEY,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    tipo_documento_comerciante VARCHAR(50) NOT NULL,
    observaciones VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uq_mercados_comerciante_documento UNIQUE (comerciante_id, documento_id)
);

CREATE INDEX IF NOT EXISTS idx_comerciantes_docs_comerciante ON mercados_comerciantes_documentos(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_comerciantes_docs_documento ON mercados_comerciantes_documentos(documento_id);

-- 7. Estado en la relación de actividades comerciales
ALTER TABLE mercados_comerciante_actividades ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

-- 8. Permisos Granulares del Padrón de Comerciantes Municipales
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT 
    m.id, p.codigo, p.nombre, p.accion, p.descripcion, 'ACTIVO'
FROM (
    VALUES
        ('MERCADOS_COMERCIANTE_VER', 'Ver Padrón de Comerciantes', 'VIEW', 'Consulta de comerciantes, fichas y actividades', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_GESTIONAR', 'Gestionar Padrón de Comerciantes', 'MANAGE', 'Administración integral de comerciantes en mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_CREAR', 'Crear Comerciante Municipal', 'CREATE', 'Registro de comerciantes personas naturales y jurídicas', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_EDITAR', 'Editar Comerciante Municipal', 'UPDATE', 'Modificación de datos de contacto y comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_CAMBIAR_ESTADO', 'Cambiar Estado de Comerciante', 'UPDATE', 'Transición de estados y registro de motivos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_DOCUMENTOS', 'Gestionar Documentos de Comerciante', 'MANAGE', 'Asociación y descarga de documentos sustentatorios', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_DATOS_SENSIBLES', 'Ver Datos Sensibles de Comerciante', 'VIEW', 'Visualización de DNI, RUC y contactos sin enmascarar', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_EXPORTAR', 'Exportar Padrón de Comerciantes', 'EXPORT', 'Generación de reportes en Excel y PDF', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_COMERCIANTE_HISTORIAL_VER', 'Ver Historial de Comerciante', 'VIEW', 'Auditoría cronológica de cambios y estados', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar permisos a roles del sistema
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_COMERCIANTE_VER',
        'MERCADOS_COMERCIANTE_GESTIONAR',
        'MERCADOS_COMERCIANTE_CREAR',
        'MERCADOS_COMERCIANTE_EDITAR',
        'MERCADOS_COMERCIANTE_CAMBIAR_ESTADO',
        'MERCADOS_COMERCIANTE_DOCUMENTOS',
        'MERCADOS_COMERCIANTE_DATOS_SENSIBLES',
        'MERCADOS_COMERCIANTE_EXPORTAR',
        'MERCADOS_COMERCIANTE_HISTORIAL_VER'
    )
) p
WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_COMERCIANTE_VER',
        'MERCADOS_COMERCIANTE_CREAR',
        'MERCADOS_COMERCIANTE_EDITAR',
        'MERCADOS_COMERCIANTE_DOCUMENTOS',
        'MERCADOS_COMERCIANTE_EXPORTAR',
        'MERCADOS_COMERCIANTE_HISTORIAL_VER'
    )
) p
WHERE r.nombre IN ('ROLE_JEFE_OFICINA', 'ROLE_OPERADOR')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
