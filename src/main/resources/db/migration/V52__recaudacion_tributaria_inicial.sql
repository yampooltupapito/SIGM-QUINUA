-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V52: Módulo 4 - Recaudación Tributaria Municipal (Arquitectura y BD Inicial)
-- ==============================================================================

-- 1. Secuencias para generación de identificadores y códigos tributarios
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_contribuyente START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_predio START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_obligacion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_pago START WITH 1 INCREMENT BY 1;

-- 2. Períodos Tributarios y Fiscales
CREATE TABLE IF NOT EXISTS tributaria_periodos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    anio INT NOT NULL UNIQUE CHECK (anio >= 2000 AND anio <= 2100),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    uit_vigente NUMERIC(10, 2) CHECK (uit_vigente IS NULL OR uit_vigente > 0),
    estado VARCHAR(30) NOT NULL DEFAULT 'CONFIGURADO',
    resolucion_aprobacion VARCHAR(150),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_periodos_anio ON tributaria_periodos(anio);
CREATE INDEX IF NOT EXISTS idx_tributaria_periodos_estado ON tributaria_periodos(estado);

-- 3. Normas Tributarias Configuradas (Fuentes Normativas Institucionales)
CREATE TABLE IF NOT EXISTS tributaria_normas_configuradas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_norma VARCHAR(40) NOT NULL,
    numero_norma VARCHAR(120) NOT NULL,
    entidad_emisora VARCHAR(150) NOT NULL DEFAULT 'Municipalidad Distrital de Quinua',
    descripcion TEXT NOT NULL,
    fecha_publicacion DATE,
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    fuente_documental_id UUID,
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA',
    usuario_validador_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_normas_tipo ON tributaria_normas_configuradas(tipo_norma);
CREATE INDEX IF NOT EXISTS idx_tributaria_normas_estado ON tributaria_normas_configuradas(estado_validacion);
CREATE INDEX IF NOT EXISTS idx_tributaria_normas_vigencia ON tributaria_normas_configuradas(fecha_inicio_vigencia, fecha_fin_vigencia);

-- 4. Conceptos Tributarios (Impuesto Predial, Arbitrios Municipales, Tasas)
CREATE TABLE IF NOT EXISTS tributaria_conceptos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_concepto VARCHAR(40) NOT NULL,
    periodicidad VARCHAR(30) NOT NULL DEFAULT 'ANUAL',
    aplica_a_predio BOOLEAN NOT NULL DEFAULT TRUE,
    cuenta_presupuestal VARCHAR(50),
    estado VARCHAR(30) NOT NULL DEFAULT 'VIGENTE',
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    norma_legal_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_conceptos_cod ON tributaria_conceptos(codigo);
CREATE INDEX IF NOT EXISTS idx_tributaria_conceptos_tipo ON tributaria_conceptos(tipo_concepto);
CREATE INDEX IF NOT EXISTS idx_tributaria_conceptos_estado ON tributaria_conceptos(estado);

-- 5. Contribuyentes Tributarios (Naturales, Jurídicos, Sucesiones)
CREATE TABLE IF NOT EXISTS tributaria_contribuyentes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_contribuyente VARCHAR(50) NOT NULL UNIQUE,
    persona_id BIGINT REFERENCES personas(id) ON DELETE RESTRICT,
    tipo_contribuyente VARCHAR(40) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    nombre_razon_social VARCHAR(255) NOT NULL,
    nombre_comercial VARCHAR(255),
    representante_legal_nombre VARCHAR(255),
    representante_legal_documento VARCHAR(30),
    domicilio_fiscal VARCHAR(255),
    referencia_domicilio TEXT,
    telefono VARCHAR(50),
    correo VARCHAR(150),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    oficina_registro_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_tributaria_contribuyente_doc UNIQUE(tipo_documento, numero_documento)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_contrib_cod ON tributaria_contribuyentes(codigo_contribuyente);
CREATE INDEX IF NOT EXISTS idx_tributaria_contrib_doc ON tributaria_contribuyentes(tipo_documento, numero_documento);
CREATE INDEX IF NOT EXISTS idx_tributaria_contrib_nombre ON tributaria_contribuyentes(nombre_razon_social);
CREATE INDEX IF NOT EXISTS idx_tributaria_contrib_estado ON tributaria_contribuyentes(estado);
CREATE INDEX IF NOT EXISTS idx_tributaria_contrib_persona ON tributaria_contribuyentes(persona_id);

-- 6. Predios Tributarios (Urbanos y Rurales del Distrito de Quinua)
CREATE TABLE IF NOT EXISTS tributaria_predios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_predio VARCHAR(50) NOT NULL UNIQUE,
    codigo_catastral VARCHAR(50),
    tipo_predio VARCHAR(30) NOT NULL DEFAULT 'URBANO',
    direccion VARCHAR(255) NOT NULL,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    sector_nombre VARCHAR(120),
    referencia_ubicacion TEXT,
    area_terreno NUMERIC(12, 4) NOT NULL CHECK (area_terreno >= 0),
    area_construida NUMERIC(12, 4) NOT NULL DEFAULT 0 CHECK (area_construida >= 0),
    unidad_medida_area VARCHAR(10) NOT NULL DEFAULT 'M2',
    valor_arancelario_referencial NUMERIC(14, 2) CHECK (valor_arancelario_referencial IS NULL OR valor_arancelario_referencial >= 0),
    valor_autovaluo_referencial NUMERIC(14, 2) CHECK (valor_autovaluo_referencial IS NULL OR valor_autovaluo_referencial >= 0),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_predios_cod ON tributaria_predios(codigo_predio);
CREATE INDEX IF NOT EXISTS idx_tributaria_predios_cat ON tributaria_predios(codigo_catastral);
CREATE INDEX IF NOT EXISTS idx_tributaria_predios_dir ON tributaria_predios(direccion);
CREATE INDEX IF NOT EXISTS idx_tributaria_predios_sector ON tributaria_predios(sector_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_predios_estado ON tributaria_predios(estado);

-- 7. Relación Contribuyente - Predio (Titularidad, Copropiedad, Posesión)
CREATE TABLE IF NOT EXISTS tributaria_relaciones_contribuyente_predio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    predio_id UUID NOT NULL REFERENCES tributaria_predios(id) ON DELETE RESTRICT,
    tipo_relacion VARCHAR(40) NOT NULL,
    porcentaje_participacion NUMERIC(5, 2) NOT NULL DEFAULT 100.00 CHECK (porcentaje_participacion > 0 AND porcentaje_participacion <= 100.00),
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    es_principal BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    documento_sustento_id UUID,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_rel_cp_contrib ON tributaria_relaciones_contribuyente_predio(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_rel_cp_predio ON tributaria_relaciones_contribuyente_predio(predio_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_rel_cp_estado ON tributaria_relaciones_contribuyente_predio(estado);

-- 8. Obligaciones Tributarias y Cuentas Corrientes
CREATE TABLE IF NOT EXISTS tributaria_obligaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_obligacion VARCHAR(50) NOT NULL UNIQUE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    concepto_id UUID NOT NULL REFERENCES tributaria_conceptos(id) ON DELETE RESTRICT,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    predio_id UUID REFERENCES tributaria_predios(id) ON DELETE RESTRICT,
    cuota_numero INT NOT NULL DEFAULT 1,
    importe_determinado NUMERIC(14, 2) NOT NULL CHECK (importe_determinado >= 0),
    importe_ajustes NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_pagado NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (importe_pagado >= 0),
    saldo NUMERIC(14, 2) NOT NULL CHECK (saldo >= 0),
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    fecha_determinacion DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    version_calculo VARCHAR(50),
    norma_aplicable_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_obl_cod ON tributaria_obligaciones(codigo_obligacion);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_contrib ON tributaria_obligaciones(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_conc_per ON tributaria_obligaciones(concepto_id, periodo_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_predio ON tributaria_obligaciones(predio_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_estado ON tributaria_obligaciones(estado);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_saldo ON tributaria_obligaciones(saldo);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_venc ON tributaria_obligaciones(fecha_vencimiento);

-- 9. Pagos Tributarios
CREATE TABLE IF NOT EXISTS tributaria_pagos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_pago VARCHAR(50) NOT NULL UNIQUE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    fecha_hora_pago TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe NUMERIC(14, 2) NOT NULL CHECK (importe > 0),
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    medio_pago VARCHAR(40) NOT NULL,
    referencia_externa VARCHAR(100),
    numero_operacion_bancaria VARCHAR(100),
    banco_origen VARCHAR(100),
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO',
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    recibo_caja_id UUID,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_cod ON tributaria_pagos(codigo_pago);
CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_contrib ON tributaria_pagos(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_fecha ON tributaria_pagos(fecha_hora_pago);
CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_estado ON tributaria_pagos(estado);

-- 10. Aplicación de Pagos a Obligaciones Tributarias
CREATE TABLE IF NOT EXISTS tributaria_aplicaciones_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pago_id UUID NOT NULL REFERENCES tributaria_pagos(id) ON DELETE RESTRICT,
    obligacion_id UUID NOT NULL REFERENCES tributaria_obligaciones(id) ON DELETE RESTRICT,
    importe_aplicado NUMERIC(14, 2) NOT NULL CHECK (importe_aplicado > 0),
    fecha_hora_aplicacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_aplicacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_apl_pago ON tributaria_aplicaciones_pago(pago_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_apl_obl ON tributaria_aplicaciones_pago(obligacion_id);

-- 11. Poblado Inicial de Períodos Fiscales Base (Quinua)
INSERT INTO tributaria_periodos (anio, fecha_inicio, fecha_fin, uit_vigente, estado, resolucion_aprobacion, observaciones) VALUES
    (2024, '2024-01-01', '2024-12-31', 5150.00, 'CERRADO', 'D.S. N° 309-2023-EF', 'Ejercicio fiscal 2024 cerrado administrativamente'),
    (2025, '2025-01-01', '2025-12-31', 5350.00, 'CERRADO', 'D.S. N° 256-2024-EF', 'Ejercicio fiscal 2025 cerrado administrativamente'),
    (2026, '2026-01-01', '2026-12-31', 5500.00, 'APERTURADO', 'D.S. N° 285-2025-EF', 'Ejercicio fiscal 2026 vigente en la Municipalidad Distrital de Quinua')
ON CONFLICT (anio) DO NOTHING;

-- 12. Poblado de Conceptos Tributarios Oficiales Base (D. Leg. 776 y Ley Orgánica de Municipalidades)
INSERT INTO tributaria_conceptos (codigo, nombre, descripcion, tipo_concepto, periodicidad, aplica_a_predio, estado, fecha_inicio_vigencia) VALUES
    ('IMP_PREDIAL', 'Impuesto Predial Anual', 'Tributo de periodicidad anual que grava el valor de los predios urbanos y rústicos (D. Leg. 776)', 'IMPUESTO_PREDIAL', 'ANUAL', TRUE, 'VIGENTE', '2020-01-01'),
    ('ARB_LIMPIEZA', 'Arbitrios - Limpieza Pública', 'Tasa por el servicio de recolección de residuos sólidos y barrido de vías públicas', 'ARBITRIOS_MUNICIPALES', 'TRIMESTRAL', TRUE, 'VIGENTE', '2020-01-01'),
    ('ARB_PARQUES', 'Arbitrios - Parques y Jardines', 'Tasa por los servicios de implementación, recuperación y mantenimiento de áreas verdes', 'ARBITRIOS_MUNICIPALES', 'TRIMESTRAL', TRUE, 'VIGENTE', '2020-01-01'),
    ('ARB_SERENAZGO', 'Arbitrios - Serenazgo Municipal', 'Tasa por el servicio de patrullaje, vigilancia y seguridad ciudadana en el distrito', 'ARBITRIOS_MUNICIPALES', 'TRIMESTRAL', TRUE, 'VIGENTE', '2020-01-01'),
    ('DERECHO_EMISION', 'Derecho de Emisión Mecanizada', 'Costo por el servicio de emisión y distribución mecanizada del autovalúo y cuponera', 'DERECHO_EMISION', 'ANUAL', TRUE, 'VIGENTE', '2020-01-01')
ON CONFLICT (codigo) DO NOTHING;

-- 13. Permisos Institucionales para el Módulo 4: Recaudación Tributaria
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_MODULO_VER', 'Acceso al Módulo de Recaudación Tributaria', 'VIEW',
       'Permite el acceso al panel y funcionalidades tributarias municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_DASHBOARD_VER', 'Ver Dashboard de Recaudación Tributaria', 'VIEW',
       'Permite consultar indicadores, padrones y estadísticas tributarias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_VER', 'Ver Padrón de Contribuyentes', 'VIEW',
       'Permite consultar contribuyentes registrados en la municipalidad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_CREAR', 'Registrar Nuevo Contribuyente', 'CREATE',
       'Permite empadronar personas naturales y jurídicas como contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONTRIBUYENTES_EDITAR', 'Modificar Datos de Contribuyente', 'UPDATE',
       'Permite actualizar domicilios y datos de contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_VER', 'Ver Padrón de Predios Tributarios', 'VIEW',
       'Permite consultar predios urbanos y rústicos registrados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_CREAR', 'Registrar Nuevo Predio Tributario', 'CREATE',
       'Permite incorporar nuevos predios a la base tributaria municipal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PREDIOS_EDITAR', 'Modificar Datos de Predio', 'UPDATE',
       'Permite actualizar áreas, direcciones y características de predios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_VER', 'Ver Catálogo de Conceptos Tributarios', 'VIEW',
       'Permite consultar tributos, arbitrios y tasas municipales configuradas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCEPTOS_CONFIGURAR', 'Configurar Conceptos y Normas', 'UPDATE',
       'Permite configurar alícuotas, normas y periodicidades tributarias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_VER', 'Consultar Obligaciones y Deudas', 'VIEW',
       'Permite consultar cuentas corrientes y estados de cuenta tributarios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_VER', 'Consultar Operaciones de Pago', 'VIEW',
       'Permite auditar pagos y liquidaciones tributarias realizadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_REPORTES_VER', 'Generar Reportes de Recaudación', 'VIEW',
       'Permite emitir reportes institucionales de recaudación tributaria', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 14. Asignación Automática de Permisos al Rol Administrador Institucional
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_MODULO_VER',
      'TRIBUTARIA_DASHBOARD_VER',
      'TRIBUTARIA_CONTRIBUYENTES_VER',
      'TRIBUTARIA_CONTRIBUYENTES_CREAR',
      'TRIBUTARIA_CONTRIBUYENTES_EDITAR',
      'TRIBUTARIA_PREDIOS_VER',
      'TRIBUTARIA_PREDIOS_CREAR',
      'TRIBUTARIA_PREDIOS_EDITAR',
      'TRIBUTARIA_CONCEPTOS_VER',
      'TRIBUTARIA_CONCEPTOS_CONFIGURAR',
      'TRIBUTARIA_OBLIGACIONES_VER',
      'TRIBUTARIA_PAGOS_VER',
      'TRIBUTARIA_REPORTES_VER'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
