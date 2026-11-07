-- =============================================================================
-- SIGM-QUINUA: Migración Flyway V26
-- Módulo 2: Mercados Municipales — Administración Integral de Puestos Comerciales
-- Ampliación de mercados_puestos, historial de estados, historial de ubicación,
-- documentos de puesto y permisos granulares.
-- =============================================================================

-- 1. Ampliación de columnas en mercados_puestos
ALTER TABLE mercados_puestos
    ADD COLUMN IF NOT EXISTS zona_id UUID REFERENCES mercados_zonas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS nombre_referencial VARCHAR(150),
    ADD COLUMN IF NOT EXISTS ancho_metros NUMERIC(8, 2),
    ADD COLUMN IF NOT EXISTS largo_metros NUMERIC(8, 2),
    ADD COLUMN IF NOT EXISTS nivel INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS estado_fisico VARCHAR(30) NOT NULL DEFAULT 'OPERATIVO';

-- Índices adicionales para mercados_puestos
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_zona ON mercados_puestos(zona_id);
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_estado_fisico ON mercados_puestos(estado_fisico);
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_tipo ON mercados_puestos(tipo_puesto_id);

-- Restricciones de validación
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_mercados_puestos_ancho') THEN
        ALTER TABLE mercados_puestos ADD CONSTRAINT chk_mercados_puestos_ancho CHECK (ancho_metros IS NULL OR ancho_metros >= 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_mercados_puestos_largo') THEN
        ALTER TABLE mercados_puestos ADD CONSTRAINT chk_mercados_puestos_largo CHECK (largo_metros IS NULL OR largo_metros >= 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_mercados_puestos_nivel') THEN
        ALTER TABLE mercados_puestos ADD CONSTRAINT chk_mercados_puestos_nivel CHECK (nivel >= 1);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_mercados_puestos_estado_fisico') THEN
        ALTER TABLE mercados_puestos ADD CONSTRAINT chk_mercados_puestos_estado_fisico CHECK (estado_fisico IN ('OPERATIVO', 'EN_MANTENIMIENTO', 'INHABILITADO'));
    END IF;
END $$;

-- 2. Historial Inmutable de Cambios de Estado de Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_puestos_cambios_estado (
    id BIGSERIAL PRIMARY KEY,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE CASCADE,
    tipo_cambio VARCHAR(30) NOT NULL DEFAULT 'ESTADO_FISICO',
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    usuario_responsable VARCHAR(100) NOT NULL,
    usuario_username VARCHAR(100),
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_puesto_cambio_tipo CHECK (tipo_cambio IN ('ESTADO_FISICO', 'ESTADO_OPERATIVO'))
);

CREATE INDEX IF NOT EXISTS idx_puestos_cambios_puesto ON mercados_puestos_cambios_estado(puesto_id);
CREATE INDEX IF NOT EXISTS idx_puestos_cambios_fecha ON mercados_puestos_cambios_estado(fecha_cambio);

-- 3. Historial Inmutable de Cambios de Ubicación de Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_puestos_cambios_ubicacion (
    id BIGSERIAL PRIMARY KEY,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE CASCADE,
    sector_anterior_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    sector_nuevo_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    pabellon_anterior_id UUID REFERENCES mercados_pabellones(id) ON DELETE SET NULL,
    pabellon_nuevo_id UUID REFERENCES mercados_pabellones(id) ON DELETE SET NULL,
    zona_anterior_id UUID REFERENCES mercados_zonas(id) ON DELETE SET NULL,
    zona_nueva_id UUID REFERENCES mercados_zonas(id) ON DELETE SET NULL,
    ubicacion_ref_anterior VARCHAR(255),
    ubicacion_ref_nueva VARCHAR(255),
    motivo VARCHAR(500) NOT NULL,
    usuario_responsable VARCHAR(100) NOT NULL,
    usuario_username VARCHAR(100),
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_puestos_ubicacion_puesto ON mercados_puestos_cambios_ubicacion(puesto_id);
CREATE INDEX IF NOT EXISTS idx_puestos_ubicacion_fecha ON mercados_puestos_cambios_ubicacion(fecha_cambio);

-- 4. Asociación Documental para Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_puestos_documentos (
    id BIGSERIAL PRIMARY KEY,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    tipo_documento VARCHAR(50) NOT NULL DEFAULT 'PLANO_PUESTO',
    descripcion VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uq_mercados_puestos_documentos UNIQUE (puesto_id, documento_id)
);

CREATE INDEX IF NOT EXISTS idx_puestos_doc_puesto ON mercados_puestos_documentos(puesto_id);

-- 5. Sembrado de Permisos Granulares de Puestos
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_CREAR', 'Crear Puestos Comerciales', 'CREATE',
       'Permite registrar nuevos puestos comerciales en los mercados municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_EDITAR', 'Editar Puestos Comerciales', 'UPDATE',
       'Permite modificar datos técnicos, dimensiones y ubicación de puestos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_CAMBIAR_ESTADO', 'Cambiar Estado de Puesto', 'UPDATE',
       'Permite modificar el estado físico y operativo de los puestos con motivo obligatorio', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_CAMBIAR_UBICACION', 'Cambiar Ubicación de Puesto', 'UPDATE',
       'Permite reubicar puestos entre sectores, pabellones y zonas con justificación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_EXPORTAR', 'Exportar Puestos Comerciales', 'EXPORT',
       'Permite exportar padrones y catálogos de puestos a Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_HISTORIAL_VER', 'Ver Historial de Puestos', 'VIEW',
       'Permite auditar el historial de estados y ubicaciones de puestos comerciales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de permisos a roles administrativos y operativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_SUPERADMIN', 'ROLE_ADMIN', 'ROLE_JEFE_OFICINA', 'ROLE_OPERADOR')
  AND p.codigo IN (
      'MERCADOS_PUESTO_VER',
      'MERCADOS_PUESTO_GESTIONAR',
      'MERCADOS_PUESTO_CREAR',
      'MERCADOS_PUESTO_EDITAR',
      'MERCADOS_PUESTO_CAMBIAR_ESTADO',
      'MERCADOS_PUESTO_CAMBIAR_UBICACION',
      'MERCADOS_PUESTO_EXPORTAR',
      'MERCADOS_PUESTO_HISTORIAL_VER'
  )
ON CONFLICT DO NOTHING;
