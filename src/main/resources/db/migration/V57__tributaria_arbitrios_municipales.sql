-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V57: Subsistema de Cálculo, Distribución y Control de Arbitrios Municipales
-- Prompt 56 de 150: Servicios, Costos Sustentados, Criterios, Simulación y Liquidación
-- ==============================================================================

-- 1. Tabla: tributaria_servicios_arbitrio (Catálogo de Servicios Municipales Sujetos a Arbitrios)
CREATE TABLE IF NOT EXISTS tributaria_servicios_arbitrio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    concepto_tributario_id UUID REFERENCES tributaria_conceptos(id) ON DELETE RESTRICT,
    tipo_servicio VARCHAR(40) NOT NULL,
    periodicidad VARCHAR(30) NOT NULL DEFAULT 'MENSUAL',
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_serv_arb_cod ON tributaria_servicios_arbitrio(codigo);
CREATE INDEX IF NOT EXISTS idx_trib_serv_arb_tipo ON tributaria_servicios_arbitrio(tipo_servicio);
CREATE INDEX IF NOT EXISTS idx_trib_serv_arb_est ON tributaria_servicios_arbitrio(estado);

-- 2. Tabla: tributaria_configuraciones_arbitrio (Configuración Operativa y Normativa Anual/Periódica)
CREATE TABLE IF NOT EXISTS tributaria_configuraciones_arbitrio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_configuracion VARCHAR(60) NOT NULL UNIQUE,
    servicio_id UUID NOT NULL REFERENCES tributaria_servicios_arbitrio(id) ON DELETE RESTRICT,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    zona VARCHAR(60),
    version_configuracion INT NOT NULL DEFAULT 1,
    configuracion_predecesora_id UUID REFERENCES tributaria_configuraciones_arbitrio(id) ON DELETE SET NULL,
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    costo_total_anual NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (costo_total_anual >= 0),
    costo_total_mensual NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (costo_total_mensual >= 0),
    metodologia_distribucion VARCHAR(50) NOT NULL DEFAULT 'USO_PREDIO_ACTIVIDAD',
    cantidad_predios_afectos INT NOT NULL DEFAULT 0 CHECK (cantidad_predios_afectos >= 0),
    descripcion_justificacion TEXT,
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
    CONSTRAINT uq_arb_conf_servicio_periodo_ver UNIQUE (servicio_id, periodo_id, version_configuracion)
);

CREATE INDEX IF NOT EXISTS idx_trib_conf_arb_serv ON tributaria_configuraciones_arbitrio(servicio_id);
CREATE INDEX IF NOT EXISTS idx_trib_conf_arb_per ON tributaria_configuraciones_arbitrio(periodo_id);
CREATE INDEX IF NOT EXISTS idx_trib_conf_arb_est ON tributaria_configuraciones_arbitrio(estado);
CREATE INDEX IF NOT EXISTS idx_trib_conf_arb_vig ON tributaria_configuraciones_arbitrio(fecha_inicio_vigencia, fecha_fin_vigencia);

-- 3. Tabla: tributaria_costos_servicio_arbitrio (Componentes de Costos Sustentados y Auditados)
CREATE TABLE IF NOT EXISTS tributaria_costos_servicio_arbitrio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuracion_id UUID NOT NULL REFERENCES tributaria_configuraciones_arbitrio(id) ON DELETE CASCADE,
    categoria_costo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    importe NUMERIC(14, 2) NOT NULL CHECK (importe > 0),
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    periodo_costo VARCHAR(40) NOT NULL DEFAULT 'ANUAL',
    documento_sustento_ref VARCHAR(200),
    fuente_sustento VARCHAR(255),
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'DECLARADO',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_costos_arb_conf ON tributaria_costos_servicio_arbitrio(configuracion_id);
CREATE INDEX IF NOT EXISTS idx_trib_costos_arb_cat ON tributaria_costos_servicio_arbitrio(categoria_costo);
CREATE INDEX IF NOT EXISTS idx_trib_costos_arb_val ON tributaria_costos_servicio_arbitrio(estado_validacion);

-- 4. Tabla: tributaria_criterios_distribucion_arbitrio (Criterios y Factores de Distribución)
CREATE TABLE IF NOT EXISTS tributaria_criterios_distribucion_arbitrio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuracion_id UUID NOT NULL REFERENCES tributaria_configuraciones_arbitrio(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    tipo_dato VARCHAR(30) NOT NULL DEFAULT 'NUMERICO',
    unidad VARCHAR(40) NOT NULL DEFAULT 'COEFICIENTE',
    metodo_distribucion VARCHAR(50) NOT NULL,
    ponderacion_porcentaje NUMERIC(5, 2) NOT NULL DEFAULT 100.00 CHECK (ponderacion_porcentaje >= 0 AND ponderacion_porcentaje <= 100),
    factor_minimo NUMERIC(10, 4) DEFAULT 0.0000,
    factor_maximo NUMERIC(10, 4),
    fuente_normativa VARCHAR(255),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_crit_arb_conf ON tributaria_criterios_distribucion_arbitrio(configuracion_id);

-- 5. Tabla: tributaria_calculos_arbitrio (Cabecera de Liquidaciones y Simulaciones de Arbitrios)
CREATE TABLE IF NOT EXISTS tributaria_calculos_arbitrio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_calculo VARCHAR(50) NOT NULL UNIQUE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    predio_id UUID NOT NULL REFERENCES tributaria_predios(id) ON DELETE RESTRICT,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    configuracion_id UUID REFERENCES tributaria_configuraciones_arbitrio(id) ON DELETE SET NULL,
    tipo_calculo VARCHAR(35) NOT NULL DEFAULT 'SIMULACION',
    estado VARCHAR(30) NOT NULL DEFAULT 'SIMULACION',
    fecha_calculo TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_referencia DATE NOT NULL DEFAULT CURRENT_DATE,
    subperiodo VARCHAR(40) NOT NULL DEFAULT 'ANUAL',
    total_servicios INT NOT NULL DEFAULT 1 CHECK (total_servicios >= 0),
    importe_anual_total NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (importe_anual_total >= 0),
    importe_periodo_total NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (importe_periodo_total >= 0),
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    beneficio_aplicado VARCHAR(100) DEFAULT 'NINGUNO',
    porcentaje_descuento_beneficio NUMERIC(5, 2) NOT NULL DEFAULT 0.00 CHECK (porcentaje_descuento_beneficio >= 0 AND porcentaje_descuento_beneficio <= 100),
    monto_descuento_beneficio NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (monto_descuento_beneficio >= 0),
    resolucion_beneficio VARCHAR(150),
    snapshot_calculo JSONB,
    observaciones TEXT,
    resolucion_aprobacion VARCHAR(150),
    motivo_anulacion TEXT,
    usuario_calculo VARCHAR(100) NOT NULL,
    usuario_aprobador VARCHAR(100),
    fecha_aprobacion TIMESTAMP WITH TIME ZONE,
    usuario_anulador VARCHAR(100),
    fecha_anulacion TIMESTAMP WITH TIME ZONE,
    version_calculo INT NOT NULL DEFAULT 1,
    calculo_predecesor_id UUID REFERENCES tributaria_calculos_arbitrio(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_cod ON tributaria_calculos_arbitrio(codigo_calculo);
CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_contrib ON tributaria_calculos_arbitrio(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_pred ON tributaria_calculos_arbitrio(predio_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_per ON tributaria_calculos_arbitrio(periodo_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_est ON tributaria_calculos_arbitrio(estado);
CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_fec ON tributaria_calculos_arbitrio(fecha_calculo);

-- 6. Tabla: tributaria_calculos_arbitrio_detalle (Desglose Pormenorizado por Servicio Arbitrio)
CREATE TABLE IF NOT EXISTS tributaria_calculos_arbitrio_detalle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calculo_id UUID NOT NULL REFERENCES tributaria_calculos_arbitrio(id) ON DELETE CASCADE,
    servicio_id UUID NOT NULL REFERENCES tributaria_servicios_arbitrio(id) ON DELETE RESTRICT,
    configuracion_id UUID REFERENCES tributaria_configuraciones_arbitrio(id) ON DELETE SET NULL,
    codigo_servicio VARCHAR(50) NOT NULL,
    nombre_servicio VARCHAR(150) NOT NULL,
    tipo_servicio VARCHAR(40) NOT NULL,
    criterio_aplicado VARCHAR(150) NOT NULL,
    parametro_base_valor NUMERIC(12, 4) NOT NULL DEFAULT 0.0000,
    unidad_base VARCHAR(40) NOT NULL DEFAULT 'COEFICIENTE',
    tasa_unitario_mensual NUMERIC(14, 4) NOT NULL DEFAULT 0.0000,
    costo_mensual_servicio NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    costo_anual_servicio NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    descuento_aplicado NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_final_mensual NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_final_anual NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    explicacion_detalle TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_det_calc ON tributaria_calculos_arbitrio_detalle(calculo_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_arb_det_serv ON tributaria_calculos_arbitrio_detalle(servicio_id);

-- 7. Tabla: tributaria_historial_arbitrio (Auditoría Integral de Configuraciones y Determinaciones)
CREATE TABLE IF NOT EXISTS tributaria_historial_arbitrio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(40) NOT NULL, -- 'CONFIGURACION' o 'CALCULO'
    entidad_id UUID NOT NULL,
    codigo_referencia VARCHAR(60),
    accion VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(30),
    nuevo_estado VARCHAR(30) NOT NULL,
    motivo TEXT,
    usuario_registro VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_arb_ent ON tributaria_historial_arbitrio(entidad_tipo, entidad_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_arb_fec ON tributaria_historial_arbitrio(fecha_registro);

-- 8. Permisos de Seguridad Institucional para Arbitrios Municipales
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_VER', 'Ver Arbitrios Municipales', 'VIEW',
       'Permite visualizar servicios, configuraciones, costos y liquidaciones de arbitrios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_CONFIGURAR', 'Configurar Arbitrios Municipales', 'CREATE',
       'Permite formular configuraciones operativas de arbitrios municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_GESTIONAR_SERVICIOS', 'Gestionar Servicios de Arbitrios', 'MANAGE',
       'Permite administrar el catálogo de servicios públicos sujetos a arbitrios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_GESTIONAR_COSTOS', 'Gestionar Costos de Arbitrios', 'MANAGE',
       'Permite registrar y revisar costos de prestación de servicios municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_GESTIONAR_CRITERIOS', 'Gestionar Criterios de Distribución', 'MANAGE',
       'Permite estructurar y parametrizar criterios de distribución', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_REVISAR_CONFIGURACION', 'Revisar Configuración de Arbitrios', 'UPDATE',
       'Permite visar y remitir a control legal configuraciones de arbitrios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_APROBAR_CONFIGURACION', 'Aprobar Configuración de Arbitrios', 'APPROVE',
       'Permite autorizar formalmente configuraciones de arbitrios con ordenanza', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_ACTIVAR_CONFIGURACION', 'Activar Configuración de Arbitrios', 'UPDATE',
       'Permite activar operativamente una configuración de arbitrios para su aplicación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_SIMULAR', 'Simular Arbitrios Municipales', 'VIEW',
       'Permite ejecutar simulaciones hipotéticas de distribución y cálculo de arbitrios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_CALCULAR', 'Calcular Arbitrios Municipales', 'CREATE',
       'Permite generar determinaciones y cálculos preliminares de arbitrios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO', 'Revisar Cálculo de Arbitrios', 'UPDATE',
       'Permite enviar a fiscalización y registrar observaciones de cálculos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_APROBAR_CALCULO', 'Aprobar Cálculo de Arbitrios', 'APPROVE',
       'Permite aprobar técnicamente determinaciones de arbitrios municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_ANULAR_CALCULO', 'Anular Cálculo de Arbitrios', 'UPDATE',
       'Permite anular determinaciones de arbitrios con causa justificada', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_VER_HISTORIAL', 'Ver Historial de Arbitrios', 'VIEW',
       'Permite consultar la trazabilidad y auditoría histórica de arbitrios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_ARBITRIOS_EXPORTAR', 'Exportar Arbitrios Municipales', 'EXPORT',
       'Permite exportar padrones y liquidaciones de arbitrios a PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 9. Asignación de Permisos a Roles Institucionales
-- ROLE_ADMIN y ROLE_SUPERADMIN: Acceso total
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'TRIBUTARIA_ARBITRIOS_%'
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- ROLE_JEFE_OFICINA: Gestión, revisión y aprobación
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'TRIBUTARIA_ARBITRIOS_VER',
      'TRIBUTARIA_ARBITRIOS_CONFIGURAR',
      'TRIBUTARIA_ARBITRIOS_GESTIONAR_SERVICIOS',
      'TRIBUTARIA_ARBITRIOS_GESTIONAR_COSTOS',
      'TRIBUTARIA_ARBITRIOS_GESTIONAR_CRITERIOS',
      'TRIBUTARIA_ARBITRIOS_REVISAR_CONFIGURACION',
      'TRIBUTARIA_ARBITRIOS_APROBAR_CONFIGURACION',
      'TRIBUTARIA_ARBITRIOS_ACTIVAR_CONFIGURACION',
      'TRIBUTARIA_ARBITRIOS_SIMULAR',
      'TRIBUTARIA_ARBITRIOS_CALCULAR',
      'TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO',
      'TRIBUTARIA_ARBITRIOS_APROBAR_CALCULO',
      'TRIBUTARIA_ARBITRIOS_ANULAR_CALCULO',
      'TRIBUTARIA_ARBITRIOS_VER_HISTORIAL',
      'TRIBUTARIA_ARBITRIOS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- ROLE_ESPECIALISTA_TRIBUTARIO: Operación, costeo, cálculo y simulación
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_ESPECIALISTA_TRIBUTARIO'
  AND p.codigo IN (
      'TRIBUTARIA_ARBITRIOS_VER',
      'TRIBUTARIA_ARBITRIOS_CONFIGURAR',
      'TRIBUTARIA_ARBITRIOS_GESTIONAR_COSTOS',
      'TRIBUTARIA_ARBITRIOS_SIMULAR',
      'TRIBUTARIA_ARBITRIOS_CALCULAR',
      'TRIBUTARIA_ARBITRIOS_REVISAR_CALCULO',
      'TRIBUTARIA_ARBITRIOS_VER_HISTORIAL',
      'TRIBUTARIA_ARBITRIOS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;


-- 10. Catálogo Inicial de Servicios Municipales Sujetos a Arbitrios en Quinua
INSERT INTO tributaria_servicios_arbitrio (codigo, nombre, descripcion, tipo_servicio, periodicidad, estado)
VALUES
    ('ARB-REC-RESIDUOS', 'Limpieza Pública: Recolección y Disposición de Residuos Sólidos', 'Servicio municipal de recolección de residuos domiciliarios y comerciales en el distrito de Quinua', 'RECOLECCION_RESIDUOS', 'MENSUAL', 'ACTIVO'),
    ('ARB-BAR-CALLES', 'Limpieza Pública: Barrido de Calles y Vías Públicas', 'Barrido manual y mantenimiento de limpieza en calles, plazas y avenidas del casco urbano y centros poblados', 'BARRIDO_CALLES', 'MENSUAL', 'ACTIVO'),
    ('ARB-PAR-JARDINES', 'Mantenimiento de Parques, Jardines y Áreas Verdes Públicas', 'Cuidado, poda, riego y conservación de parques, bermas centrales y áreas recreacionales distritales', 'PARQUES_JARDINES', 'MENSUAL', 'ACTIVO'),
    ('ARB-SER-SERENAZGO', 'Seguridad Ciudadana y Servicio de Serenazgo Municipal', 'Patrullaje preventivo, vigilancia disuasiva y atención de alertas ciudadanas en sectores distritales', 'SERENAZGO', 'MENSUAL', 'ACTIVO')
ON CONFLICT (codigo) DO NOTHING;
