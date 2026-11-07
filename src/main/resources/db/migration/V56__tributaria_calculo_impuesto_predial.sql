-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V56: Subsistema de Cálculo y Determinación del Impuesto Predial
-- Prompt 55 de 150: Motor de Cálculo, Desglose, Tramos, Simulación y Auditoría
-- ==============================================================================

-- 1. Tabla: tributaria_calculos_predial (Cabecera de liquidación y simulaciones de impuesto predial)
CREATE TABLE IF NOT EXISTS tributaria_calculos_predial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_calculo VARCHAR(50) NOT NULL UNIQUE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    configuracion_id UUID REFERENCES tributaria_configuraciones_concepto(id) ON DELETE SET NULL,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    tipo_calculo VARCHAR(30) NOT NULL DEFAULT 'SIMULACION',
    estado VARCHAR(30) NOT NULL DEFAULT 'SIMULACION',
    fecha_calculo TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_referencia DATE NOT NULL DEFAULT CURRENT_DATE,
    uit_aplicada NUMERIC(12, 2) NOT NULL CHECK (uit_aplicada > 0),
    cantidad_predios INT NOT NULL DEFAULT 1,
    total_autovaluo_bruto NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (total_autovaluo_bruto >= 0),
    total_deducciones_beneficios NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (total_deducciones_beneficios >= 0),
    base_imponible_neta NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (base_imponible_neta >= 0),
    impuesto_calculado_tramos NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (impuesto_calculado_tramos >= 0),
    impuesto_minimo_legal NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (impuesto_minimo_legal >= 0),
    aplica_impuesto_minimo BOOLEAN NOT NULL DEFAULT FALSE,
    impuesto_anual_determinado NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (impuesto_anual_determinado >= 0),
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    beneficio_aplicado VARCHAR(100) DEFAULT 'NINGUNO',
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
    calculo_predecesor_id UUID REFERENCES tributaria_calculos_predial(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_cod ON tributaria_calculos_predial(codigo_calculo);
CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_contrib ON tributaria_calculos_predial(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_periodo ON tributaria_calculos_predial(periodo_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_estado ON tributaria_calculos_predial(estado);
CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_tipo ON tributaria_calculos_predial(tipo_calculo);
CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_fecha ON tributaria_calculos_predial(fecha_calculo);

-- 2. Tabla: tributaria_calculos_predial_predios (Desglose de valorización de predios componentes)
CREATE TABLE IF NOT EXISTS tributaria_calculos_predial_predios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calculo_id UUID NOT NULL REFERENCES tributaria_calculos_predial(id) ON DELETE CASCADE,
    predio_id UUID NOT NULL REFERENCES tributaria_predios(id) ON DELETE RESTRICT,
    codigo_predio VARCHAR(50) NOT NULL,
    direccion_predio VARCHAR(255) NOT NULL,
    tipo_predio VARCHAR(30) NOT NULL DEFAULT 'URBANO',
    clasificacion_predio VARCHAR(50) DEFAULT 'CASA_HABITACION',
    porcentaje_participacion NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    area_terreno NUMERIC(12, 4) NOT NULL DEFAULT 0.0000,
    unidad_medida_terreno VARCHAR(10) NOT NULL DEFAULT 'M2',
    arancel_unitario_m2 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    valor_terreno NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    cantidad_construcciones INT NOT NULL DEFAULT 0,
    area_total_construida NUMERIC(12, 4) NOT NULL DEFAULT 0.0000,
    valor_construcciones NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    valor_instalaciones NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    valor_autovaluo_total NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    valor_autovaluo_imputable NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_items_calc ON tributaria_calculos_predial_predios(calculo_id);
CREATE INDEX IF NOT EXISTS idx_trib_calc_pred_items_pred ON tributaria_calculos_predial_predios(predio_id);

-- 3. Tabla: tributaria_calculos_predial_tramos (Desglose de cálculo por tramo de la escala progresiva)
CREATE TABLE IF NOT EXISTS tributaria_calculos_predial_tramos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calculo_id UUID NOT NULL REFERENCES tributaria_calculos_predial(id) ON DELETE CASCADE,
    numero_tramo INT NOT NULL,
    descripcion_tramo VARCHAR(100) NOT NULL,
    limite_inferior_uit NUMERIC(8, 2) NOT NULL,
    limite_superior_uit NUMERIC(8, 2),
    limite_inferior_monto NUMERIC(14, 2) NOT NULL,
    limite_superior_monto NUMERIC(14, 2),
    alicuota_porcentaje NUMERIC(5, 2) NOT NULL,
    base_imponible_tramo NUMERIC(14, 2) NOT NULL,
    impuesto_tramo NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trib_calc_tramos_calc ON tributaria_calculos_predial_tramos(calculo_id);

-- 4. Tabla: tributaria_historial_calculo_predial (Auditoría de estados y transiciones de cálculo)
CREATE TABLE IF NOT EXISTS tributaria_historial_calculo_predial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calculo_id UUID NOT NULL REFERENCES tributaria_calculos_predial(id) ON DELETE CASCADE,
    accion VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(30),
    nuevo_estado VARCHAR(30) NOT NULL,
    motivo TEXT NOT NULL,
    resolucion_sustento VARCHAR(150),
    usuario_registro VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detalles_cambios JSONB
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_calc_pred_id ON tributaria_historial_calculo_predial(calculo_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_calc_pred_fecha ON tributaria_historial_calculo_predial(fecha_registro);

-- 5. Nuevos Permisos para el Motor de Impuesto Predial
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_VER', 'Ver Cálculos de Impuesto Predial', 'VIEW',
       'Permite consultar determinaciones y autovalúos de impuesto predial', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_SIMULAR', 'Simular Impuesto Predial', 'VIEW',
       'Permite simular autovalúos y cálculo progresivo sin generar obligaciones', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_CALCULAR', 'Efectuar Determinación de Impuesto Predial', 'CREATE',
       'Permite registrar cálculos formales y preliminares de impuesto predial', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_REVISAR', 'Revisar y Observar Liquidación Predial', 'UPDATE',
       'Permite elevar a revisión técnica y asentar observaciones en determinaciones', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_APROBAR', 'Aprobar Determinación de Impuesto Predial', 'UPDATE',
       'Permite la aprobación formal del cálculo de impuesto predial según resolución', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_ANULAR', 'Anular Determinación de Impuesto Predial', 'UPDATE',
       'Permite anular motivadamente un cálculo de impuesto predial con registro de auditoría', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_VER_HISTORIAL', 'Ver Historial de Determinación Predial', 'VIEW',
       'Permite consultar la auditoría y traza de versiones de liquidación predial', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIAL_EXPORTAR', 'Exportar Cálculos y Fichas de Autovalúo', 'VIEW',
       'Permite descargar reportes y hojas de autovalúo en Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignación a Roles Administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_PREDIAL_VER',
      'TRIBUTARIA_PREDIAL_SIMULAR',
      'TRIBUTARIA_PREDIAL_CALCULAR',
      'TRIBUTARIA_PREDIAL_REVISAR',
      'TRIBUTARIA_PREDIAL_APROBAR',
      'TRIBUTARIA_PREDIAL_ANULAR',
      'TRIBUTARIA_PREDIAL_VER_HISTORIAL',
      'TRIBUTARIA_PREDIAL_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
