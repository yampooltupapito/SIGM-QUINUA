-- ==============================================================================
-- SIGM-QUINUA - PROMPT 28 DE 150: CONTROL DE PAGOS, CUOTAS Y OBLIGACIONES
-- Módulo: Mercados Municipales - Finanzas Comerciales y Cobranzas
-- ==============================================================================

-- 1. Secuencias para numeración correlativa segura y controlada
CREATE SEQUENCE IF NOT EXISTS mercados_obligaciones_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mercados_pagos_seq START WITH 1 INCREMENT BY 1;

-- 2. Conceptos de Cobro Comercial de Mercados
CREATE TABLE IF NOT EXISTS mercados_conceptos_cobro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_concepto VARCHAR(50) NOT NULL, -- USO_PUESTO_COMERCIAL, SERVICIO_MERCADO, DERECHO_ADMINISTRATIVO, OBLIGACION_CONTRACTUAL, OTRO
    periodicidad VARCHAR(30) NOT NULL DEFAULT 'MENSUAL', -- PAGO_UNICO, MENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL, PERSONALIZADA
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, INACTIVO
    requiere_asignacion BOOLEAN NOT NULL DEFAULT true,
    oficina_responsable_id BIGINT REFERENCES oficinas(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_conceptos_cobro_codigo ON mercados_conceptos_cobro(codigo);
CREATE INDEX IF NOT EXISTS idx_conceptos_cobro_estado ON mercados_conceptos_cobro(estado);

-- 3. Tarifas Autorizadas de Mercados (con versionamiento y vigencia)
CREATE TABLE IF NOT EXISTS mercados_tarifas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concepto_id UUID NOT NULL REFERENCES mercados_conceptos_cobro(id) ON DELETE RESTRICT,
    mercado_id UUID REFERENCES mercados_mercados(id) ON DELETE SET NULL,
    tipo_puesto_id BIGINT REFERENCES mercados_tipos_puesto(id) ON DELETE SET NULL,
    monto NUMERIC(12,2) NOT NULL CHECK (monto >= 0.00),
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, INACTIVA, HISTORICA
    documento_sustento_id UUID,
    fuente_normativa VARCHAR(255),
    usuario_aprobacion_id BIGINT REFERENCES usuarios(id),
    version_tarifa INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_tarifa_fechas CHECK (fecha_fin_vigencia IS NULL OR fecha_fin_vigencia >= fecha_inicio_vigencia)
);

CREATE INDEX IF NOT EXISTS idx_mercados_tarifas_concepto ON mercados_tarifas(concepto_id);
CREATE INDEX IF NOT EXISTS idx_mercados_tarifas_mercado ON mercados_tarifas(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_tarifas_vigencia ON mercados_tarifas(fecha_inicio_vigencia, fecha_fin_vigencia);

-- 4. Cajas Físicas y Sesiones de Caja
CREATE TABLE IF NOT EXISTS mercados_cajas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    mercado_id UUID REFERENCES mercados_mercados(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, INACTIVA
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mercados_cajas_sesiones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    caja_id UUID NOT NULL REFERENCES mercados_cajas(id),
    usuario_cajero_id BIGINT NOT NULL REFERENCES usuarios(id),
    cajero_username VARCHAR(100) NOT NULL,
    fecha_apertura TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    monto_inicial NUMERIC(12,2) NOT NULL DEFAULT 0.00 CHECK (monto_inicial >= 0.00),
    total_ingresos NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_reversos NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    monto_final_esperado NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    monto_final_declarado NUMERIC(12,2),
    diferencia NUMERIC(12,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA', -- ABIERTA, CERRADA, CONCILIADA, ANULADA
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cajas_sesiones_caja ON mercados_cajas_sesiones(caja_id);
CREATE INDEX IF NOT EXISTS idx_cajas_sesiones_usuario ON mercados_cajas_sesiones(usuario_cajero_id);
CREATE INDEX IF NOT EXISTS idx_cajas_sesiones_estado ON mercados_cajas_sesiones(estado);

-- 5. Obligaciones Económicas Comerciales
CREATE TABLE IF NOT EXISTS mercados_obligaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_obligacion VARCHAR(50) NOT NULL UNIQUE,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id),
    asignacion_id UUID REFERENCES mercados_asignaciones(id),
    puesto_id UUID REFERENCES mercados_puestos(id),
    concepto_id UUID NOT NULL REFERENCES mercados_conceptos_cobro(id),
    tarifa_id UUID REFERENCES mercados_tarifas(id),
    periodo_inicio DATE,
    periodo_fin DATE,
    fecha_emision DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento DATE,
    monto_original NUMERIC(12,2) NOT NULL CHECK (monto_original > 0.00),
    monto_pagado NUMERIC(12,2) NOT NULL DEFAULT 0.00 CHECK (monto_pagado >= 0.00),
    saldo_pendiente NUMERIC(12,2) NOT NULL CHECK (saldo_pendiente >= 0.00),
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, PARCIALMENTE_PAGADA, PAGADA, VENCIDA, SUSPENDIDA, ANULADA
    expediente_id UUID,
    documento_sustento_id UUID,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_obligacion_periodo CHECK (periodo_fin IS NULL OR periodo_inicio IS NULL OR periodo_fin >= periodo_inicio),
    CONSTRAINT chk_obligacion_saldo CHECK (saldo_pendiente <= monto_original)
);

CREATE INDEX IF NOT EXISTS idx_obligaciones_comerciante ON mercados_obligaciones(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_obligaciones_puesto ON mercados_obligaciones(puesto_id);
CREATE INDEX IF NOT EXISTS idx_obligaciones_asignacion ON mercados_obligaciones(asignacion_id);
CREATE INDEX IF NOT EXISTS idx_obligaciones_concepto ON mercados_obligaciones(concepto_id);
CREATE INDEX IF NOT EXISTS idx_obligaciones_estado ON mercados_obligaciones(estado);
CREATE INDEX IF NOT EXISTS idx_obligaciones_vencimiento ON mercados_obligaciones(fecha_vencimiento);

-- 6. Cronograma de Cuotas Programadas
CREATE TABLE IF NOT EXISTS mercados_cuotas_obligacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    obligacion_id UUID NOT NULL REFERENCES mercados_obligaciones(id) ON DELETE CASCADE,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    monto_programado NUMERIC(12,2) NOT NULL CHECK (monto_programado > 0.00),
    monto_pagado NUMERIC(12,2) NOT NULL DEFAULT 0.00 CHECK (monto_pagado >= 0.00),
    saldo NUMERIC(12,2) NOT NULL CHECK (saldo >= 0.00),
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, PARCIALMENTE_PAGADA, PAGADA, VENCIDA
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_obligacion_cuota UNIQUE (obligacion_id, numero_cuota)
);

CREATE INDEX IF NOT EXISTS idx_cuotas_obligacion ON mercados_cuotas_obligacion(obligacion_id);
CREATE INDEX IF NOT EXISTS idx_cuotas_vencimiento ON mercados_cuotas_obligacion(fecha_vencimiento);

-- 7. Pagos y Recaudaciones de Mercados
CREATE TABLE IF NOT EXISTS mercados_pagos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_pago VARCHAR(50) NOT NULL UNIQUE,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id),
    fecha_pago TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_recibido NUMERIC(12,2) NOT NULL CHECK (monto_recibido > 0.00),
    monto_aplicado NUMERIC(12,2) NOT NULL DEFAULT 0.00 CHECK (monto_aplicado >= 0.00),
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    medio_pago VARCHAR(40) NOT NULL, -- EFECTIVO, TRANSFERENCIA_BANCARIA, DEPOSITO_BANCARIO, TARJETA_DEBITO_CREDITO, BILLETERA_DIGITAL, OTRO
    referencia_operacion VARCHAR(100),
    estado VARCHAR(30) NOT NULL DEFAULT 'CONFIRMADO', -- CONFIRMADO, CONCILIADO, REVERSADO, ANULADO
    caja_sesion_id UUID REFERENCES mercados_cajas_sesiones(id),
    usuario_registro_id BIGINT REFERENCES usuarios(id),
    usuario_registro VARCHAR(100) NOT NULL,
    codigo_seguro VARCHAR(64) UNIQUE,
    hash_comprobante VARCHAR(128),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_pago_aplicado CHECK (monto_aplicado <= monto_recibido)
);

CREATE INDEX IF NOT EXISTS idx_pagos_comerciante ON mercados_pagos(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_pagos_fecha ON mercados_pagos(fecha_pago);
CREATE INDEX IF NOT EXISTS idx_pagos_estado ON mercados_pagos(estado);
CREATE INDEX IF NOT EXISTS idx_pagos_caja ON mercados_pagos(caja_sesion_id);
CREATE INDEX IF NOT EXISTS idx_pagos_codigo_seguro ON mercados_pagos(codigo_seguro);

-- 8. Aplicaciones de Pago a Obligaciones y Cuotas
CREATE TABLE IF NOT EXISTS mercados_pagos_aplicaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pago_id UUID NOT NULL REFERENCES mercados_pagos(id) ON DELETE CASCADE,
    obligacion_id UUID NOT NULL REFERENCES mercados_obligaciones(id) ON DELETE RESTRICT,
    cuota_id UUID REFERENCES mercados_cuotas_obligacion(id) ON DELETE RESTRICT,
    monto_aplicado NUMERIC(12,2) NOT NULL CHECK (monto_aplicado > 0.00),
    fecha_aplicacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'APLICADO', -- APLICADO, REVERSADO
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pagos_aplicaciones_pago ON mercados_pagos_aplicaciones(pago_id);
CREATE INDEX IF NOT EXISTS idx_pagos_aplicaciones_obligacion ON mercados_pagos_aplicaciones(obligacion_id);

-- 9. Reversos de Pagos Controlados
CREATE TABLE IF NOT EXISTS mercados_pagos_reversos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pago_id UUID NOT NULL REFERENCES mercados_pagos(id),
    motivo TEXT NOT NULL,
    documento_sustento VARCHAR(100),
    usuario_reverso_id BIGINT REFERENCES usuarios(id),
    usuario_reverso VARCHAR(100) NOT NULL,
    fecha_reverso TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_reversado NUMERIC(12,2) NOT NULL CHECK (monto_reversado > 0.00),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pagos_reversos_pago ON mercados_pagos_reversos(pago_id);

-- 10. Movimientos de Cuenta Corriente del Comerciante
CREATE TABLE IF NOT EXISTS mercados_cuenta_corriente_movimientos (
    id BIGSERIAL PRIMARY KEY,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id),
    puesto_id UUID REFERENCES mercados_puestos(id),
    tipo_movimiento VARCHAR(20) NOT NULL, -- CARGO, ABONO, REVERSO
    concepto VARCHAR(150) NOT NULL,
    monto NUMERIC(12,2) NOT NULL CHECK (monto > 0.00),
    saldo_resultante NUMERIC(12,2) NOT NULL,
    referencia_id UUID,
    tipo_referencia VARCHAR(50), -- OBLIGACION, PAGO, REVERSO
    fecha_movimiento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario VARCHAR(100) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cuenta_corriente_comerciante ON mercados_cuenta_corriente_movimientos(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_cuenta_corriente_fecha ON mercados_cuenta_corriente_movimientos(fecha_movimiento);

-- 11. Inserción de Datos Iniciales y Configuración Base
INSERT INTO mercados_cajas (id, codigo, nombre, estado)
VALUES
    ('c1111111-1111-1111-1111-111111111111', 'CAJA-CENTRAL-01', 'Caja Recaudadora Principal Mercados', 'ACTIVA')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mercados_conceptos_cobro (id, codigo, nombre, descripcion, tipo_concepto, periodicidad, estado, requiere_asignacion)
VALUES 
    ('d1111111-1111-1111-1111-111111111111', 'USO_PUESTO_COMERCIAL', 'Merced Conductiva / Uso de Puesto Comercial', 'Derecho mensual de ocupación y uso comercial de puesto en mercado municipal', 'USO_PUESTO_COMERCIAL', 'MENSUAL', 'ACTIVO', true),
    ('d2222222-2222-2222-2222-222222222222', 'MANTENIMIENTO_LIMPIEZA', 'Servicio de Mantenimiento y Aseo', 'Cuota de mantenimiento, agua y aseo de áreas comunes del mercado', 'SERVICIO_MERCADO', 'MENSUAL', 'ACTIVO', true),
    ('d3333333-3333-3333-3333-333333333333', 'DERECHO_AUTORIZACION', 'Derecho de Trámite de Autorización Comercial', 'Derecho administrativo por emisión o renovación de autorización comercial', 'DERECHO_ADMINISTRATIVO', 'PAGO_UNICO', 'ACTIVO', false)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mercados_tarifas (id, concepto_id, monto, moneda, fecha_inicio_vigencia, estado, fuente_normativa)
VALUES
    ('e1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 50.00, 'PEN', '2026-01-01', 'ACTIVA', 'Ordenanza Municipal N° 012-2025-MDQ'),
    ('e2222222-2222-2222-2222-222222222222', 'd2222222-2222-2222-2222-222222222222', 15.00, 'PEN', '2026-01-01', 'ACTIVA', 'Ordenanza Municipal N° 012-2025-MDQ'),
    ('e3333333-3333-3333-3333-333333333333', 'd3333333-3333-3333-3333-333333333333', 30.00, 'PEN', '2026-01-01', 'ACTIVA', 'TUPA Institucional MDQ - Procedimiento N° 04')
ON CONFLICT (id) DO NOTHING;

-- 12. Registro de Permisos Granulares de Finanzas de Mercados (Prompt 28)
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT 
    m.id, p.codigo, p.nombre, p.accion, p.descripcion, 'ACTIVO'
FROM (
    VALUES
        ('MERCADOS_FINANZAS_VER', 'Ver Finanzas de Mercados', 'VIEW', 'Ver panel general de finanzas, deudas y recaudación de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONCEPTO_GESTIONAR', 'Gestionar Conceptos de Cobro', 'MANAGE', 'Configurar conceptos de cobro comercial de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_TARIFA_GESTIONAR', 'Gestionar Tarifas de Mercados', 'MANAGE', 'Configurar y aprobar tarifas de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_OBLIGACION_VER', 'Ver Obligaciones Económicas', 'VIEW', 'Consultar padrón de obligaciones y cuentas corrientes', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_OBLIGACION_CREAR', 'Generar Obligaciones', 'CREATE', 'Emitir obligaciones económicas y programar cuotas', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_OBLIGACION_AJUSTAR', 'Ajustar Obligaciones', 'UPDATE', 'Ajustar, suspender o anular obligaciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PAGO_VER', 'Ver Pagos de Mercados', 'VIEW', 'Consultar pagos, recibos y constancias', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PAGO_REGISTRAR', 'Registrar Pagos de Mercados', 'CREATE', 'Cobrar y registrar pagos y aplicaciones de deuda', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_PAGO_REVERSAR', 'Reversar Pagos de Mercados', 'MANAGE', 'Reversar pagos confirmados mediante procedimiento reglado', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CAJA_ABRIR', 'Abrir Caja de Mercados', 'MANAGE', 'Aperturar sesiones diarias de caja recaudadora', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CAJA_CERRAR', 'Cerrar Caja de Mercados', 'MANAGE', 'Cerrar y conciliar sesiones de caja con arqueo', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONCILIACION_GESTIONAR', 'Gestionar Conciliación', 'MANAGE', 'Conciliar transferencias y depósitos bancarios', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_FINANZAS_EXPORTAR', 'Exportar Finanzas de Mercados', 'EXPORT', 'Exportar padrón de deudas, pagos y reportes a Excel y PDF', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_FINANZAS_AUDITORIA_VER', 'Ver Auditoría Financiera', 'VIEW', 'Consultar bitácora de auditoría financiera de mercados', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_FINANZAS_VER',
        'MERCADOS_CONCEPTO_GESTIONAR',
        'MERCADOS_TARIFA_GESTIONAR',
        'MERCADOS_OBLIGACION_VER',
        'MERCADOS_OBLIGACION_CREAR',
        'MERCADOS_OBLIGACION_AJUSTAR',
        'MERCADOS_PAGO_VER',
        'MERCADOS_PAGO_REGISTRAR',
        'MERCADOS_PAGO_REVERSAR',
        'MERCADOS_CAJA_ABRIR',
        'MERCADOS_CAJA_CERRAR',
        'MERCADOS_CONCILIACION_GESTIONAR',
        'MERCADOS_FINANZAS_EXPORTAR',
        'MERCADOS_FINANZAS_AUDITORIA_VER'
    )
) p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
