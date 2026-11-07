-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V55: Subsistema de Configuración y Control Normativo de Conceptos Tributarios
-- Prompt 54 de 150: Versiones, Parámetros, Normativa, Calendario y Reglas
-- ==============================================================================

-- 1. Ampliar tributaria_conceptos con naturaleza jurídica, oficina y control de versiones
ALTER TABLE tributaria_conceptos
    ADD COLUMN IF NOT EXISTS naturaleza_tributaria VARCHAR(40) DEFAULT 'IMPUESTO',
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 2. Ampliar tributaria_periodos con control de versión concurrente
ALTER TABLE tributaria_periodos
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 3. Ampliar tributaria_normas_configuradas con enlace oficial, hash y alcance
ALTER TABLE tributaria_normas_configuradas
    ADD COLUMN IF NOT EXISTS enlace_oficial VARCHAR(500),
    ADD COLUMN IF NOT EXISTS alcance_territorial VARCHAR(100) DEFAULT 'DISTRITAL_QUINUA',
    ADD COLUMN IF NOT EXISTS archivo_hash VARCHAR(128),
    ADD COLUMN IF NOT EXISTS norma_modifica_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 4. Tabla: tributaria_configuraciones_concepto (Configuraciones versionadas por período y concepto)
CREATE TABLE IF NOT EXISTS tributaria_configuraciones_concepto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concepto_id UUID NOT NULL REFERENCES tributaria_conceptos(id) ON DELETE RESTRICT,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    numero_version INT NOT NULL DEFAULT 1,
    version_anterior_id UUID REFERENCES tributaria_configuraciones_concepto(id) ON DELETE SET NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    descripcion_motivo TEXT,
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    zona VARCHAR(50),
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    usuario_creador VARCHAR(100),
    usuario_aprobador VARCHAR(100),
    fecha_aprobacion TIMESTAMP WITH TIME ZONE,
    resolucion_aprobacion VARCHAR(150),
    usuario_activador VARCHAR(100),
    fecha_activacion TIMESTAMP WITH TIME ZONE,
    motivo_desactivacion TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_concepto_periodo_version UNIQUE(concepto_id, periodo_id, numero_version)
);

CREATE INDEX IF NOT EXISTS idx_trib_conf_concepto ON tributaria_configuraciones_concepto(concepto_id);
CREATE INDEX IF NOT EXISTS idx_trib_conf_periodo ON tributaria_configuraciones_concepto(periodo_id);
CREATE INDEX IF NOT EXISTS idx_trib_conf_estado ON tributaria_configuraciones_concepto(estado);
CREATE INDEX IF NOT EXISTS idx_trib_conf_norma ON tributaria_configuraciones_concepto(norma_id);
CREATE INDEX IF NOT EXISTS idx_trib_conf_vigencia ON tributaria_configuraciones_concepto(fecha_inicio_vigencia, fecha_fin_vigencia);

-- 5. Tabla: tributaria_parametros_configuracion (Parámetros, alícuotas y valores de la configuración)
CREATE TABLE IF NOT EXISTS tributaria_parametros_configuracion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuracion_id UUID NOT NULL REFERENCES tributaria_configuraciones_concepto(id) ON DELETE CASCADE,
    codigo VARCHAR(80) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_dato VARCHAR(30) NOT NULL DEFAULT 'DECIMAL',
    valor_decimal NUMERIC(16, 6),
    valor_texto TEXT,
    valor_booleano BOOLEAN,
    valor_fecha DATE,
    unidad VARCHAR(50) NOT NULL DEFAULT 'PEN',
    origen_normativo VARCHAR(255),
    es_obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    orden_visual INT NOT NULL DEFAULT 1,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_parametro_config_codigo UNIQUE(configuracion_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_trib_param_config ON tributaria_parametros_configuracion(configuracion_id);
CREATE INDEX IF NOT EXISTS idx_trib_param_codigo ON tributaria_parametros_configuracion(codigo);

-- 6. Tabla: tributaria_reglas_configuradas (Reglas de negocio, deducciones y beneficios autorizados)
CREATE TABLE IF NOT EXISTS tributaria_reglas_configuradas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuracion_id UUID NOT NULL REFERENCES tributaria_configuraciones_concepto(id) ON DELETE CASCADE,
    codigo VARCHAR(80) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    tipo_regla VARCHAR(50) NOT NULL,
    condicion_aplicacion TEXT NOT NULL,
    base_legal_articulo VARCHAR(150),
    porcentaje_beneficio NUMERIC(5, 2),
    monto_fijo_beneficio NUMERIC(12, 2),
    orden_aplicacion INT NOT NULL DEFAULT 1,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_regla_config_codigo UNIQUE(configuracion_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_trib_reglas_config ON tributaria_reglas_configuradas(configuracion_id);

-- 7. Tabla: tributaria_calendario_vencimientos (Cronograma anual de cuotas y fechas límites)
CREATE TABLE IF NOT EXISTS tributaria_calendario_vencimientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concepto_id UUID NOT NULL REFERENCES tributaria_conceptos(id) ON DELETE RESTRICT,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    numero_cuota INT NOT NULL DEFAULT 1,
    descripcion_cuota VARCHAR(100) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    permite_descuento BOOLEAN NOT NULL DEFAULT FALSE,
    porcentaje_descuento NUMERIC(5, 2),
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
    resolucion_prorroga VARCHAR(150),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_calendario_concepto_periodo_cuota UNIQUE(concepto_id, periodo_id, numero_cuota)
);

CREATE INDEX IF NOT EXISTS idx_trib_cal_concepto_periodo ON tributaria_calendario_vencimientos(concepto_id, periodo_id);
CREATE INDEX IF NOT EXISTS idx_trib_cal_vencimiento ON tributaria_calendario_vencimientos(fecha_vencimiento);

-- 8. Tabla: tributaria_historial_configuraciones (Auditoría completa de transiciones y versiones)
CREATE TABLE IF NOT EXISTS tributaria_historial_configuraciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuracion_id UUID NOT NULL REFERENCES tributaria_configuraciones_concepto(id) ON DELETE CASCADE,
    accion VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(30),
    nuevo_estado VARCHAR(30) NOT NULL,
    motivo TEXT NOT NULL,
    resolucion_sustento VARCHAR(150),
    usuario_registro VARCHAR(100) NOT NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detalles_cambios JSONB
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_conf_id ON tributaria_historial_configuraciones(configuracion_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_conf_fecha ON tributaria_historial_configuraciones(fecha_registro);

-- 9. Registro de nuevos permisos del subsistema de conceptos tributarios
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_CREAR', 'Crear Conceptos Tributarios', 'CREATE',
       'Permite registrar nuevos conceptos tributarios en el catálogo municipal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_EDITAR', 'Editar Conceptos Tributarios', 'UPDATE',
       'Permite actualizar la información básica de conceptos tributarios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_REVISAR', 'Revisar Configuraciones Tributarias', 'UPDATE',
       'Permite enviar y emitir observaciones técnicas sobre configuraciones en revisión', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_APROBAR', 'Aprobar Configuraciones Tributarias', 'UPDATE',
       'Permite la aprobación formal y jurídica de versiones de configuración tributaria', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_ACTIVAR', 'Activar Parámetros Tributarios', 'UPDATE',
       'Permite activar formalmente una versión configurada para aplicación municipal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_DESACTIVAR', 'Desactivar Parámetros Tributarios', 'UPDATE',
       'Permite cesar la vigencia de una versión de configuración tributaria', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_VER_HISTORIAL', 'Ver Historial y Comparar Versiones', 'VIEW',
       'Permite consultar la auditoría y comparar parámetros entre versiones de configuración', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_GESTIONAR_NORMAS', 'Gestionar Normas Tributarias', 'UPDATE',
       'Permite registrar y vincular ordenanzas, decretos y fuentes normativas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_EXPORTAR', 'Exportar Conceptos y Configuraciones', 'VIEW',
       'Permite descargar catálogos, parámetros y normas en formatos Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 10. Asignación de permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_CONCEPTOS_CREAR',
      'TRIBUTARIA_CONCEPTOS_EDITAR',
      'TRIBUTARIA_CONCEPTOS_REVISAR',
      'TRIBUTARIA_CONCEPTOS_APROBAR',
      'TRIBUTARIA_CONCEPTOS_ACTIVAR',
      'TRIBUTARIA_CONCEPTOS_DESACTIVAR',
      'TRIBUTARIA_CONCEPTOS_VER_HISTORIAL',
      'TRIBUTARIA_CONCEPTOS_GESTIONAR_NORMAS',
      'TRIBUTARIA_CONCEPTOS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
