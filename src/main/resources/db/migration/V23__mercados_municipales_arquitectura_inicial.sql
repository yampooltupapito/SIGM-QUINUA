-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V23: Módulo 2 - Gestión de Mercados Municipales (Arquitectura Inicial)
-- Tablas: mercados_mercados, mercados_sectores, mercados_pabellones,
--         mercados_tipos_puesto, mercados_puestos, mercados_actividades,
--         mercados_comerciantes, mercados_comerciante_actividades,
--         mercados_asignaciones, mercados_historial_ocupacion,
--         mercados_autorizaciones
-- ==============================================================================

-- 1. Tabla de Mercados Municipales
CREATE TABLE IF NOT EXISTS mercados_mercados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    direccion VARCHAR(255) NOT NULL,
    referencia_ubicacion VARCHAR(255),
    distrito VARCHAR(100) NOT NULL DEFAULT 'Quinua',
    provincia VARCHAR(100) NOT NULL DEFAULT 'Huamanga',
    departamento VARCHAR(100) NOT NULL DEFAULT 'Ayacucho',
    latitud NUMERIC(10, 8),
    longitud NUMERIC(11, 8),
    capacidad_referencial INT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    oficina_administradora_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mercados_capacidad CHECK (capacidad_referencial >= 0),
    CONSTRAINT chk_mercados_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_codigo ON mercados_mercados(codigo);
CREATE INDEX IF NOT EXISTS idx_mercados_estado ON mercados_mercados(estado);
CREATE INDEX IF NOT EXISTS idx_mercados_oficina ON mercados_mercados(oficina_administradora_id);

-- 2. Tabla de Sectores por Mercado
CREATE TABLE IF NOT EXISTS mercados_sectores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    tipo_sector VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    orden_visual INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_mercados_sectores_mercado_codigo UNIQUE (mercado_id, codigo),
    CONSTRAINT chk_mercados_sectores_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_sectores_mercado ON mercados_sectores(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_sectores_estado ON mercados_sectores(estado);

-- 3. Tabla de Pabellones por Mercado
CREATE TABLE IF NOT EXISTS mercados_pabellones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE CASCADE,
    sector_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    nivel INT NOT NULL DEFAULT 1,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    orden_visual INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_mercados_pabellones_mercado_codigo UNIQUE (mercado_id, codigo),
    CONSTRAINT chk_mercados_pabellones_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_mercados_pabellones_nivel CHECK (nivel >= 1)
);

CREATE INDEX IF NOT EXISTS idx_mercados_pabellones_mercado ON mercados_pabellones(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_pabellones_sector ON mercados_pabellones(sector_id);

-- 4. Catálogo de Tipos de Puesto Comercial
CREATE TABLE IF NOT EXISTS mercados_tipos_puesto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    permite_instalacion_fija BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mercados_tipos_puesto_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 5. Tabla de Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_puestos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE CASCADE,
    sector_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    pabellon_id UUID REFERENCES mercados_pabellones(id) ON DELETE SET NULL,
    codigo VARCHAR(50) NOT NULL,
    numero VARCHAR(30) NOT NULL,
    descripcion VARCHAR(500),
    area_metros_cuadrados NUMERIC(8, 2),
    ubicacion_referencia VARCHAR(255),
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    tipo_puesto_id BIGINT REFERENCES mercados_tipos_puesto(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_mercados_puestos_mercado_codigo UNIQUE (mercado_id, codigo),
    CONSTRAINT chk_mercados_puestos_area CHECK (area_metros_cuadrados IS NULL OR area_metros_cuadrados >= 0),
    CONSTRAINT chk_mercados_puestos_estado CHECK (estado IN ('DISPONIBLE', 'OCUPADO', 'RESERVADO', 'EN_MANTENIMIENTO', 'INHABILITADO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_puestos_mercado ON mercados_puestos(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_sector ON mercados_puestos(sector_id);
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_pabellon ON mercados_puestos(pabellon_id);
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_estado ON mercados_puestos(estado);

-- 6. Catálogo de Actividades Comerciales
CREATE TABLE IF NOT EXISTS mercados_actividades (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(100),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mercados_actividades_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 7. Padrón de Comerciantes Municipales (Reutiliza Persona institucional)
CREATE TABLE IF NOT EXISTS mercados_comerciantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    persona_id BIGINT NOT NULL REFERENCES personas(id) ON DELETE RESTRICT,
    codigo_comerciante VARCHAR(50) NOT NULL UNIQUE,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    observaciones TEXT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_mercados_comerciantes_persona UNIQUE (persona_id),
    CONSTRAINT chk_mercados_comerciantes_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_comerciantes_persona ON mercados_comerciantes(persona_id);
CREATE INDEX IF NOT EXISTS idx_mercados_comerciantes_codigo ON mercados_comerciantes(codigo_comerciante);
CREATE INDEX IF NOT EXISTS idx_mercados_comerciantes_estado ON mercados_comerciantes(estado);

-- 8. Relación Comerciante - Actividades Comerciales (Muchos a Muchos)
CREATE TABLE IF NOT EXISTS mercados_comerciante_actividades (
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE CASCADE,
    actividad_id BIGINT NOT NULL REFERENCES mercados_actividades(id) ON DELETE RESTRICT,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comerciante_id, actividad_id)
);

CREATE INDEX IF NOT EXISTS idx_comerciante_actividades_comerciante ON mercados_comerciante_actividades(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_comerciante_actividades_actividad ON mercados_comerciante_actividades(actividad_id);

-- 9. Asignación de Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_asignaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE RESTRICT,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    tipo_asignacion VARCHAR(50) NOT NULL DEFAULT 'REGULAR',
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_asignacion_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_asignacion_estado CHECK (estado IN ('BORRADOR', 'VIGENTE', 'SUSPENDIDA', 'FINALIZADA', 'ANULADA'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_asignaciones_puesto ON mercados_asignaciones(puesto_id);
CREATE INDEX IF NOT EXISTS idx_mercados_asignaciones_comerciante ON mercados_asignaciones(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_mercados_asignaciones_estado ON mercados_asignaciones(estado);

-- Restricción exclusiva PostgreSQL: impide dos o más asignaciones VIGENTES simultáneas en el mismo puesto
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_puesto_asignacion_vigente ON mercados_asignaciones(puesto_id) WHERE estado = 'VIGENTE';

-- 10. Historial Inmutable de Ocupación y Movimientos de Puesto
CREATE TABLE IF NOT EXISTS mercados_historial_ocupacion (
    id BIGSERIAL PRIMARY KEY,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE RESTRICT,
    comerciante_id UUID REFERENCES mercados_comerciantes(id) ON DELETE SET NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_responsable VARCHAR(100) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mercados_historial_puesto ON mercados_historial_ocupacion(puesto_id);
CREATE INDEX IF NOT EXISTS idx_mercados_historial_comerciante ON mercados_historial_ocupacion(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_mercados_historial_fecha ON mercados_historial_ocupacion(fecha);

-- 11. Autorizaciones Comerciales de Mercados
CREATE TABLE IF NOT EXISTS mercados_autorizaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    puesto_id UUID REFERENCES mercados_puestos(id) ON DELETE SET NULL,
    tipo_autorizacion VARCHAR(50) NOT NULL DEFAULT 'PADRON_MUNICIPAL',
    numero_autorizacion VARCHAR(50) UNIQUE,
    referencia_documento VARCHAR(100),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'VIGENTE',
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    autoridad_competente_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_autorizacion_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_autorizacion_estado CHECK (estado IN ('EMITIDA', 'VIGENTE', 'SUSPENDIDA', 'VENCIDA', 'REVOCADA', 'ANULADA'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_autorizaciones_comerciante ON mercados_autorizaciones(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_mercados_autorizaciones_puesto ON mercados_autorizaciones(puesto_id);
CREATE INDEX IF NOT EXISTS idx_mercados_autorizaciones_estado ON mercados_autorizaciones(estado);

-- ==============================================================================
-- 12. Poblado de Datos Base del Catálogo de Mercados
-- ==============================================================================

-- Tipos de Puesto Base
INSERT INTO mercados_tipos_puesto (codigo, nombre, descripcion, permite_instalacion_fija, estado) VALUES
    ('PUESTO_FIJO', 'Puesto Fijo', 'Espacio estructurado cerrado o semi-cerrado de material permanente', true, 'ACTIVO'),
    ('ESPACIO_TEMPORAL', 'Espacio Temporal / Ferial', 'Área demarcada transitoria para días festivos o ferias comunales', false, 'ACTIVO'),
    ('LOCAL_COMERCIAL', 'Local Comercial Cerrado', 'Ambiente cerrado independiente con cerramiento propio y puerta enrollable', true, 'ACTIVO'),
    ('MODULO', 'Módulo o Stand', 'Estructura prefabricada o móvil autorizada', false, 'ACTIVO'),
    ('OTRO', 'Espacio Comunal Especial', 'Área comunal de carga, pesaje o acopio', false, 'ACTIVO')
ON CONFLICT (codigo) DO NOTHING;

-- Actividades Comerciales Base
INSERT INTO mercados_actividades (codigo, nombre, descripcion, categoria, estado) VALUES
    ('ACT-ALIM', 'Venta de Alimentos Frescos, Frutas y Verduras', 'Comercialización de frutas, verduras, tubérculos y hortalizas de la zona', 'PERECIBLES', 'ACTIVO'),
    ('ACT-ABAR', 'Abarrotes y Artículos de Primera Necesidad', 'Venta de productos no perecibles, cereales, harinas y conservas', 'ABARROTES', 'ACTIVO'),
    ('ACT-CARN', 'Carnicería y Derivados Cárnicos', 'Comercialización de carnes rojas, aves, cerdos y menudencias autorizadas', 'CARNICERIA', 'ACTIVO'),
    ('ACT-PESC', 'Pescados y Productos Hidrobiológicos', 'Expendio de productos marinos e hidrobiológicos con cadena de frío', 'PESCADERIA', 'ACTIVO'),
    ('ACT-VEST', 'Prendas de Vestir, Calzado y Textiles', 'Venta de confecciones, ropa tradicional, ponchos y calzado', 'TEXTILES', 'ACTIVO'),
    ('ACT-ARTE', 'Artesanía Tradicional, Cerámica y Recuerdos de Quinua', 'Comercio de cerámica de Quinua, retablos, piedra de Huamanga y recuerdos', 'ARTESANIAS', 'ACTIVO'),
    ('ACT-COMD', 'Comidas Preparadas y Gastronomía Típica', 'Puestos de venta de comida tradicional, caldos, picantes y bebidas calientes', 'GASTRONOMIA', 'ACTIVO'),
    ('ACT-SERV', 'Servicios Varios y Reparaciones', 'Costura, zapatería, cerrajería y servicios artesanales menores', 'SERVICIOS', 'ACTIVO')
ON CONFLICT (codigo) DO NOTHING;

-- ==============================================================================
-- 13. Poblado de Permisos Institucionales de Mercados Municipales (Módulo 2)
-- ==============================================================================

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MODULO_ACCEDER', 'Acceso al Módulo de Mercados Municipales', 'VIEW',
       'Permite acceder al panel y vistas del módulo de gestión de mercados de Quinua', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_VER', 'Ver Catálogo de Mercados Municipales', 'VIEW',
       'Permite visualizar los mercados municipales registrados y sus características', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_CREAR', 'Registrar Mercado Municipal', 'CREATE',
       'Permite registrar un nuevo mercado municipal en la jurisdicción de Quinua', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_EDITAR', 'Editar Mercado Municipal', 'UPDATE',
       'Permite modificar datos generales, límites y capacidades de mercados municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_SECTOR_GESTIONAR', 'Gestionar Sectores de Mercado', 'MANAGE',
       'Permite crear, clasificar y actualizar sectores comerciales dentro de un mercado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PABELLON_GESTIONAR', 'Gestionar Pabellones de Mercado', 'MANAGE',
       'Permite organizar pabellones, pasajes y niveles dentro del mercado municipal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_VER', 'Ver Puestos Comerciales', 'VIEW',
       'Permite consultar el estado, dimensiones y ubicación de puestos comerciales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_PUESTO_GESTIONAR', 'Gestionar Puestos Comerciales', 'MANAGE',
       'Permite crear, actualizar dimensiones, cambiar estado y administrar puestos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_COMERCIANTE_VER', 'Ver Padrón de Comerciantes', 'VIEW',
       'Permite consultar la lista y legajo de comerciantes municipales empadronados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_COMERCIANTE_GESTIONAR', 'Gestionar Padrón de Comerciantes', 'MANAGE',
       'Permite empadronar comerciantes, registrar actividades y gestionar su estado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_ASIGNACION_VER', 'Ver Asignaciones de Puestos', 'VIEW',
       'Permite consultar las asignaciones vigentes e históricas de puestos comerciales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_ASIGNACION_GESTIONAR', 'Gestionar Asignaciones de Puestos', 'MANAGE',
       'Permite registrar asignaciones, cesiones, desocupaciones y actualizar vigencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de Permisos a Roles Institucionales
-- 1) Administrador y Superadmin: Todos los permisos de mercados
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'MERCADOS_%'
ON CONFLICT DO NOTHING;

-- 2) Jefe de Oficina (Desarrollo Económico / Servicios / Rentas)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'MERCADOS_MODULO_ACCEDER',
      'MERCADOS_MERCADO_VER',
      'MERCADOS_PUESTO_VER',
      'MERCADOS_COMERCIANTE_VER',
      'MERCADOS_ASIGNACION_VER',
      'MERCADOS_ASIGNACION_GESTIONAR'
  )
ON CONFLICT DO NOTHING;

-- 3) Operador Administrativo de Mercados
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR'
  AND p.codigo IN (
      'MERCADOS_MODULO_ACCEDER',
      'MERCADOS_MERCADO_VER',
      'MERCADOS_PUESTO_VER',
      'MERCADOS_COMERCIANTE_VER',
      'MERCADOS_ASIGNACION_VER'
  )
ON CONFLICT DO NOTHING;
