-- ==============================================================================
-- SIGM-QUINUA: Migración Flyway V53
-- Módulo 4: Recaudación Tributaria Municipal
-- Subsistema de Registro y Administración de Contribuyentes Municipales (Prompt 52)
-- ==============================================================================

-- 1. Ampliación de la tabla de Contribuyentes Tributarios
ALTER TABLE tributaria_contribuyentes
    ADD COLUMN IF NOT EXISTS nombres VARCHAR(150),
    ADD COLUMN IF NOT EXISTS primer_apellido VARCHAR(100),
    ADD COLUMN IF NOT EXISTS segundo_apellido VARCHAR(100),
    ADD COLUMN IF NOT EXISTS telefono_alternativo VARCHAR(50),
    ADD COLUMN IF NOT EXISTS medio_contacto_preferido VARCHAR(40) NOT NULL DEFAULT 'CORREO_ELECTRONICO',
    ADD COLUMN IF NOT EXISTS estado_verificacion VARCHAR(40) NOT NULL DEFAULT 'NO_VERIFICADO',
    ADD COLUMN IF NOT EXISTS motivo_cambio_estado TEXT,
    ADD COLUMN IF NOT EXISTS fecha_cambio_estado TIMESTAMP WITH TIME ZONE;

-- 2. Tabla de Representantes Legales y Apoderados de Contribuyentes
CREATE TABLE IF NOT EXISTS tributaria_representantes_contribuyente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
    persona_representante_id BIGINT REFERENCES personas(id) ON DELETE SET NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    nombres_apellidos VARCHAR(255) NOT NULL,
    tipo_representacion VARCHAR(50) NOT NULL DEFAULT 'GERENTE_GENERAL',
    partida_registral VARCHAR(50),
    asiento_registral VARCHAR(30),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    documento_sustento_id UUID,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_rep_contrib ON tributaria_representantes_contribuyente(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_rep_doc ON tributaria_representantes_contribuyente(tipo_documento, numero_documento);
CREATE INDEX IF NOT EXISTS idx_trib_rep_estado ON tributaria_representantes_contribuyente(estado);

-- 3. Tabla de Domicilios del Contribuyente (Urbano y Rural / Centros Poblados de Quinua)
CREATE TABLE IF NOT EXISTS tributaria_domicilios_contribuyente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
    tipo_domicilio VARCHAR(40) NOT NULL DEFAULT 'DECLARADO',
    departamento VARCHAR(100) NOT NULL DEFAULT 'Ayacucho',
    provincia VARCHAR(100) NOT NULL DEFAULT 'Huamanga',
    distrito VARCHAR(100) NOT NULL DEFAULT 'Quinua',
    ubigeo VARCHAR(10) NOT NULL DEFAULT '050108',
    tipo_via VARCHAR(50),
    nombre_via VARCHAR(150),
    numero_municipal VARCHAR(30),
    interior VARCHAR(30),
    manzana_lote VARCHAR(50),
    comunidad_centro_poblado VARCHAR(150),
    referencia TEXT,
    es_fiscal BOOLEAN NOT NULL DEFAULT FALSE,
    es_notificacion BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin_vigencia DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_dom_contrib ON tributaria_domicilios_contribuyente(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_dom_fiscal ON tributaria_domicilios_contribuyente(contribuyente_id, es_fiscal);
CREATE INDEX IF NOT EXISTS idx_trib_dom_estado ON tributaria_domicilios_contribuyente(estado);

-- 4. Tabla de Contactos Autorizados del Contribuyente
CREATE TABLE IF NOT EXISTS tributaria_contactos_contribuyente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
    tipo_contacto VARCHAR(40) NOT NULL,
    valor VARCHAR(150) NOT NULL,
    descripcion VARCHAR(150),
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    autorizado_notificacion BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_cont_contrib ON tributaria_contactos_contribuyente(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_cont_tipo ON tributaria_contactos_contribuyente(tipo_contacto);

-- 5. Tabla de Historial y Auditoría de Estados del Contribuyente
CREATE TABLE IF NOT EXISTS tributaria_historial_estados_contribuyente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS idx_trib_hist_contrib ON tributaria_historial_estados_contribuyente(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_fecha ON tributaria_historial_estados_contribuyente(fecha_registro);

-- 6. Nuevos Permisos Especializados para Gestión y Control de Contribuyentes
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_CAMBIAR_ESTADO', 'Cambiar Estado de Contribuyente', 'UPDATE',
       'Permite modificar el estado administrativo de un contribuyente con motivo y resolución', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_VER_CONTACTOS', 'Ver Contactos de Contribuyentes', 'VIEW',
       'Permite visualizar teléfonos y correos electrónicos de contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_CONTACTOS', 'Gestionar Contactos de Contribuyentes', 'UPDATE',
       'Permite registrar y actualizar medios de contacto y teléfonos de contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_DOMICILIOS', 'Gestionar Domicilios de Contribuyentes', 'UPDATE',
       'Permite agregar domicilios fiscales, declarados y rurales a contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_REPRESENTANTES', 'Gestionar Representantes Legales', 'UPDATE',
       'Permite registrar y revocar apoderados y representantes de personas jurídicas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_VER_HISTORIAL', 'Ver Historial de Cambios de Contribuyente', 'VIEW',
       'Permite auditar el historial de estados y modificaciones del contribuyente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_EXPORTAR', 'Exportar Padrón de Contribuyentes', 'VIEW',
       'Permite descargar el padrón tributario de contribuyentes en Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 7. Asignación a Roles Administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_CONTRIBUYENTES_CAMBIAR_ESTADO',
      'TRIBUTARIA_CONTRIBUYENTES_VER_CONTACTOS',
      'TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_CONTACTOS',
      'TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_DOMICILIOS',
      'TRIBUTARIA_CONTRIBUYENTES_GESTIONAR_REPRESENTANTES',
      'TRIBUTARIA_CONTRIBUYENTES_VER_HISTORIAL',
      'TRIBUTARIA_CONTRIBUYENTES_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
