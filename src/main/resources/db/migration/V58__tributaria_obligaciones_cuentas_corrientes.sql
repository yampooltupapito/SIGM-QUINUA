-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V58: Subsistema Profesional de Obligaciones Tributarias y Cuentas Corrientes
-- Prompt 57 de 150: Generación, Cuotas, Cuentas Corrientes, Movimientos, Saldos y Auditoría
-- ==============================================================================

-- 1. Secuencias para códigos institucionales y correlativos
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_cuenta_corriente START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_lote_obligacion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_movimiento_cta START WITH 1 INCREMENT BY 1;

-- 2. Adecuación de la tabla tributaria_obligaciones existente
ALTER TABLE tributaria_obligaciones
    ADD COLUMN IF NOT EXISTS tipo_determinacion VARCHAR(50) NOT NULL DEFAULT 'IMPUESTO_PREDIAL',
    ADD COLUMN IF NOT EXISTS determinacion_origen_id UUID,
    ADD COLUMN IF NOT EXISTS numero_version_determinacion INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS total_cuotas INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS importe_original NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS importe_vigente NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS estado_financiero VARCHAR(40) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN IF NOT EXISTS estado_exigibilidad VARCHAR(40) NOT NULL DEFAULT 'NO_EXIGIBLE',
    ADD COLUMN IF NOT EXISTS snapshot_determinacion JSONB,
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS motivo_ajuste TEXT,
    ADD COLUMN IF NOT EXISTS motivo_anulacion TEXT,
    ADD COLUMN IF NOT EXISTS resolucion_sustento VARCHAR(150),
    ADD COLUMN IF NOT EXISTS fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_validacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS usuario_validador VARCHAR(100),
    ADD COLUMN IF NOT EXISTS fecha_anulacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS usuario_anulador VARCHAR(100);

-- Actualizar importes originales si son cero en registros existentes
UPDATE tributaria_obligaciones
SET importe_original = importe_determinado,
    importe_vigente = importe_determinado
WHERE importe_original = 0.00 AND importe_determinado > 0;

-- Índices de alto rendimiento para tributaria_obligaciones
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_tipo_det ON tributaria_obligaciones(tipo_determinacion);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_det_orig ON tributaria_obligaciones(determinacion_origen_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_est_fin ON tributaria_obligaciones(estado_financiero);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_est_exig ON tributaria_obligaciones(estado_exigibilidad);
CREATE INDEX IF NOT EXISTS idx_tributaria_obl_fec_reg ON tributaria_obligaciones(fecha_registro);

-- Índice único para idempotencia estricta: evita generar doble obligación activa para una misma determinación y cuota
CREATE UNIQUE INDEX IF NOT EXISTS uq_tributaria_obl_det_cuota
    ON tributaria_obligaciones (determinacion_origen_id, cuota_numero)
    WHERE estado NOT IN ('ANULADA', 'RECTIFICADA');

-- 3. Tabla de Cuotas de Obligación Tributaria
CREATE TABLE IF NOT EXISTS tributaria_obligaciones_cuotas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    obligacion_id UUID NOT NULL REFERENCES tributaria_obligaciones(id) ON DELETE CASCADE,
    numero_cuota INT NOT NULL,
    importe NUMERIC(14, 2) NOT NULL CHECK (importe >= 0),
    importe_pagado NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (importe_pagado >= 0),
    saldo NUMERIC(14, 2) NOT NULL CHECK (saldo >= 0),
    fecha_vencimiento DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_cuota_obligacion_num UNIQUE (obligacion_id, numero_cuota)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_cuota_obl ON tributaria_obligaciones_cuotas(obligacion_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_cuota_venc ON tributaria_obligaciones_cuotas(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_tributaria_cuota_estado ON tributaria_obligaciones_cuotas(estado);

-- 4. Tabla de Cuentas Corrientes Tributarias (Una cuenta activa por contribuyente institucional)
CREATE TABLE IF NOT EXISTS tributaria_cuentas_corrientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contribuyente_id UUID NOT NULL UNIQUE REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    codigo_cuenta VARCHAR(50) NOT NULL UNIQUE,
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    saldo_actual NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_cargos NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_abonos NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura DATE NOT NULL DEFAULT CURRENT_DATE,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_cta_contrib ON tributaria_cuentas_corrientes(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_cta_codigo ON tributaria_cuentas_corrientes(codigo_cuenta);
CREATE INDEX IF NOT EXISTS idx_tributaria_cta_estado ON tributaria_cuentas_corrientes(estado);

-- 5. Tabla de Libro Diario de Movimientos de Cuenta Corriente
CREATE TABLE IF NOT EXISTS tributaria_movimientos_cuenta_corriente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_corriente_id UUID NOT NULL REFERENCES tributaria_cuentas_corrientes(id) ON DELETE RESTRICT,
    obligacion_id UUID REFERENCES tributaria_obligaciones(id) ON DELETE SET NULL,
    cuota_id UUID REFERENCES tributaria_obligaciones_cuotas(id) ON DELETE SET NULL,
    numero_secuencia BIGINT NOT NULL,
    tipo_movimiento VARCHAR(40) NOT NULL,
    naturaleza VARCHAR(20) NOT NULL,
    importe NUMERIC(14, 2) NOT NULL CHECK (importe > 0),
    saldo_anterior NUMERIC(14, 2) NOT NULL,
    saldo_posterior NUMERIC(14, 2) NOT NULL,
    fecha_operacion DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referencia_origen VARCHAR(100),
    documento_sustento VARCHAR(150),
    descripcion TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO',
    movimiento_reverso_id UUID REFERENCES tributaria_movimientos_cuenta_corriente(id) ON DELETE SET NULL,
    usuario_responsable VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_mov_cta_fec ON tributaria_movimientos_cuenta_corriente(cuenta_corriente_id, fecha_registro);
CREATE INDEX IF NOT EXISTS idx_tributaria_mov_obl ON tributaria_movimientos_cuenta_corriente(obligacion_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_mov_tipo ON tributaria_movimientos_cuenta_corriente(tipo_movimiento);
CREATE INDEX IF NOT EXISTS idx_tributaria_mov_fec_op ON tributaria_movimientos_cuenta_corriente(fecha_operacion);
CREATE INDEX IF NOT EXISTS idx_tributaria_mov_sec ON tributaria_movimientos_cuenta_corriente(cuenta_corriente_id, numero_secuencia);

-- 6. Tabla de Lotes de Generación Controlada de Obligaciones
CREATE TABLE IF NOT EXISTS tributaria_lotes_generacion_obligaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_lote VARCHAR(50) NOT NULL UNIQUE,
    tipo_determinacion VARCHAR(50) NOT NULL,
    periodo_id UUID NOT NULL REFERENCES tributaria_periodos(id) ON DELETE RESTRICT,
    total_evaluadas INT NOT NULL DEFAULT 0,
    total_elegibles INT NOT NULL DEFAULT 0,
    total_generadas INT NOT NULL DEFAULT 0,
    total_duplicadas INT NOT NULL DEFAULT 0,
    total_errores INT NOT NULL DEFAULT 0,
    importe_total_generado NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_PROCESO',
    usuario_registro VARCHAR(100),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_lote_cod ON tributaria_lotes_generacion_obligaciones(codigo_lote);
CREATE INDEX IF NOT EXISTS idx_tributaria_lote_tipo_per ON tributaria_lotes_generacion_obligaciones(tipo_determinacion, periodo_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_lote_estado ON tributaria_lotes_generacion_obligaciones(estado);

-- 7. Tabla de Detalles del Lote de Generación
CREATE TABLE IF NOT EXISTS tributaria_detalles_lote_generacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lote_id UUID NOT NULL REFERENCES tributaria_lotes_generacion_obligaciones(id) ON DELETE CASCADE,
    determinacion_id UUID NOT NULL,
    codigo_determinacion VARCHAR(50),
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    obligacion_id UUID REFERENCES tributaria_obligaciones(id) ON DELETE SET NULL,
    resultado VARCHAR(30) NOT NULL,
    importe_generado NUMERIC(14, 2) DEFAULT 0.00,
    mensaje_detalle TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tributaria_det_lote ON tributaria_detalles_lote_generacion(lote_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_det_det_id ON tributaria_detalles_lote_generacion(determinacion_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_det_contrib ON tributaria_detalles_lote_generacion(contribuyente_id);

-- 8. Tabla de Historial y Auditoría de Estados de Obligación
CREATE TABLE IF NOT EXISTS tributaria_historial_obligaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    obligacion_id UUID NOT NULL REFERENCES tributaria_obligaciones(id) ON DELETE CASCADE,
    accion VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(40),
    estado_nuevo VARCHAR(40) NOT NULL,
    importe_anterior NUMERIC(14, 2),
    importe_nuevo NUMERIC(14, 2),
    motivo TEXT,
    resolucion_sustento VARCHAR(150),
    usuario_registro VARCHAR(100),
    fecha_registro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tributaria_hist_obl ON tributaria_historial_obligaciones(obligacion_id, fecha_registro);

-- 9. Poblado de Cuentas Corrientes iniciales para contribuyentes que aún no la posean
INSERT INTO tributaria_cuentas_corrientes (contribuyente_id, codigo_cuenta, moneda, saldo_actual, total_cargos, total_abonos, estado, fecha_apertura, created_by)
SELECT c.id,
       'CTA-CT-' || c.codigo_contribuyente || '-PEN',
       'PEN',
       0.00,
       0.00,
       0.00,
       'ACTIVA',
       CURRENT_DATE,
       'MIGRACION_V58'
FROM tributaria_contribuyentes c
WHERE NOT EXISTS (
    SELECT 1 FROM tributaria_cuentas_corrientes cta WHERE cta.contribuyente_id = c.id
)
ON CONFLICT (contribuyente_id) DO NOTHING;

-- 10. Permisos de Seguridad Institucional para Obligaciones y Cuentas Corrientes
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_GENERAR', 'Generar Obligaciones Individuales', 'CREATE',
       'Permite generar obligaciones tributarias desde determinaciones aprobadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE', 'Generar Obligaciones por Lote', 'CREATE',
       'Permite procesar lotes masivos de determinaciones tributarias autorizadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_REVISAR', 'Revisar Obligaciones Tributarias', 'UPDATE',
       'Permite revisar administrativamente y observar obligaciones tributarias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_VALIDAR', 'Validar Obligaciones Tributarias', 'APPROVE',
       'Permite validar formalmente obligaciones y darles vigencia exigible', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_SUSPENDER', 'Suspender Obligaciones Tributarias', 'UPDATE',
       'Permite suspender la exigibilidad por reclamo o procedimiento legal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_RECTIFICAR', 'Rectificar Obligaciones Tributarias', 'UPDATE',
       'Permite aplicar rectificaciones autorizadas con trazabilidad de saldos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_ANULAR', 'Anular Obligaciones Tributarias', 'DELETE',
       'Permite anular obligaciones autorizadas revirtiendo el saldo en cuenta corriente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_VER_HISTORIAL', 'Ver Historial de Obligaciones', 'VIEW',
       'Permite consultar la bitácora y auditoría de cambios de cada obligación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CUENTA_CORRIENTE_VER', 'Ver Cuentas Corrientes Tributarias', 'VIEW',
       'Permite consultar estados de cuenta y saldos individuales de contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CUENTA_CORRIENTE_VER_MOVIMIENTOS', 'Ver Movimientos de Cuenta Corriente', 'VIEW',
       'Permite auditar el libro diario cronológico de cargos y abonos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CUENTA_CORRIENTE_EXPORTAR', 'Exportar Cuentas Corrientes', 'EXPORT',
       'Permite exportar estados de cuenta de contribuyentes a PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_OBLIGACIONES_EXPORTAR', 'Exportar Obligaciones Tributarias', 'EXPORT',
       'Permite exportar padrones y reportes de obligaciones a PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 11. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_OBLIGACIONES_VER',
      'TRIBUTARIA_OBLIGACIONES_GENERAR',
      'TRIBUTARIA_OBLIGACIONES_GENERAR_LOTE',
      'TRIBUTARIA_OBLIGACIONES_REVISAR',
      'TRIBUTARIA_OBLIGACIONES_VALIDAR',
      'TRIBUTARIA_OBLIGACIONES_SUSPENDER',
      'TRIBUTARIA_OBLIGACIONES_RECTIFICAR',
      'TRIBUTARIA_OBLIGACIONES_ANULAR',
      'TRIBUTARIA_OBLIGACIONES_VER_HISTORIAL',
      'TRIBUTARIA_CUENTA_CORRIENTE_VER',
      'TRIBUTARIA_CUENTA_CORRIENTE_VER_MOVIMIENTOS',
      'TRIBUTARIA_CUENTA_CORRIENTE_EXPORTAR',
      'TRIBUTARIA_OBLIGACIONES_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
