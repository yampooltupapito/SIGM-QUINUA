-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V59: Subsistema de Pagos Tributarios, Caja Municipal y Conciliación
-- Prompt 58 de 150: Cajas, Sesiones, Pagos, Aplicaciones a Obligaciones, Arqueos y Conciliaciones
-- ==============================================================================

-- 1. Secuencias para códigos correlativos institucionales
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_caja START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_sesion_caja START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_tributaria_conciliacion_caja START WITH 1 INCREMENT BY 1;

-- 2. Tabla de Cajas Municipales (Puntos de atención y ventanillas de recaudación)
CREATE TABLE IF NOT EXISTS tributaria_cajas_municipales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ubicacion VARCHAR(255),
    cajero_asignado_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_caja_cod ON tributaria_cajas_municipales(codigo);
CREATE INDEX IF NOT EXISTS idx_tributaria_caja_estado ON tributaria_cajas_municipales(estado);
CREATE INDEX IF NOT EXISTS idx_tributaria_caja_oficina ON tributaria_cajas_municipales(oficina_id);

-- 3. Tabla de Sesiones de Caja Municipal (Turnos de apertura, recaudación y cierre)
CREATE TABLE IF NOT EXISTS tributaria_sesiones_caja (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_sesion VARCHAR(50) NOT NULL UNIQUE,
    caja_id UUID NOT NULL REFERENCES tributaria_cajas_municipales(id) ON DELETE RESTRICT,
    cajero_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_apertura TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    monto_apertura NUMERIC(14, 2) NOT NULL DEFAULT 0.00 CHECK (monto_apertura >= 0),
    monto_cierre_declarado NUMERIC(14, 2),
    monto_cierre_calculado NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    diferencia NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_recaudado_efectivo NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_recaudado_otros_medios NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_recaudado_general NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_operaciones INT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTA',
    observaciones TEXT,
    justificacion_diferencia TEXT,
    supervisor_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_sesion_caja ON tributaria_sesiones_caja(caja_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_sesion_cajero ON tributaria_sesiones_caja(cajero_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_sesion_estado ON tributaria_sesiones_caja(estado);
CREATE INDEX IF NOT EXISTS idx_tributaria_sesion_fec_aper ON tributaria_sesiones_caja(fecha_apertura);

-- Índice único parcial: sólo puede existir 1 sesión activa por caja
CREATE UNIQUE INDEX IF NOT EXISTS uq_sesion_caja_activa
    ON tributaria_sesiones_caja (caja_id)
    WHERE estado IN ('ABIERTA', 'EN_CIERRE');

-- Índice único parcial: sólo puede existir 1 sesión activa por cajero
CREATE UNIQUE INDEX IF NOT EXISTS uq_sesion_cajero_activo
    ON tributaria_sesiones_caja (cajero_id)
    WHERE estado IN ('ABIERTA', 'EN_CIERRE');

-- 4. Tabla de Conciliaciones de Caja y Arqueos
CREATE TABLE IF NOT EXISTS tributaria_conciliaciones_caja (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_conciliacion VARCHAR(50) NOT NULL UNIQUE,
    sesion_caja_id UUID NOT NULL REFERENCES tributaria_sesiones_caja(id) ON DELETE RESTRICT,
    fecha_conciliacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_esperado NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_verificado NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    diferencia NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT,
    desglose_denominaciones_json JSONB,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_tributaria_concil_sesion ON tributaria_conciliaciones_caja(sesion_caja_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_concil_estado ON tributaria_conciliaciones_caja(estado);
CREATE INDEX IF NOT EXISTS idx_tributaria_concil_fecha ON tributaria_conciliaciones_caja(fecha_conciliacion);

-- 5. Adecuación de la tabla tributaria_pagos existente
ALTER TABLE tributaria_pagos
    ADD COLUMN IF NOT EXISTS sesion_caja_id UUID REFERENCES tributaria_sesiones_caja(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS fecha_confirmacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS usuario_confirmacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS fecha_reversion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS usuario_reversion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS motivo_reversion TEXT,
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100),
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_tributaria_pagos_idempotency
    ON tributaria_pagos (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_sesion ON tributaria_pagos(sesion_caja_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_fec_conf ON tributaria_pagos(fecha_confirmacion);

-- 6. Adecuación de la tabla tributaria_aplicaciones_pago existente
ALTER TABLE tributaria_aplicaciones_pago
    ADD COLUMN IF NOT EXISTS cuota_id UUID REFERENCES tributaria_obligaciones_cuotas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS movimiento_cuenta_id UUID REFERENCES tributaria_movimientos_cuenta_corriente(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_tributaria_apl_cuota ON tributaria_aplicaciones_pago(cuota_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_apl_mov ON tributaria_aplicaciones_pago(movimiento_cuenta_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_apl_estado ON tributaria_aplicaciones_pago(estado);

-- 7. Registro de Permisos de Seguridad RBAC para Caja, Pagos y Conciliación
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_VER', 'Ver Cajas Municipales', 'VIEW',
       'Permite consultar las ventanillas y cajas recaudadoras municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_ADMINISTRAR', 'Administrar Cajas Municipales', 'MANAGE',
       'Permite crear, configurar y asignar cajeros a ventanillas municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_ABRIR', 'Abrir Sesión de Caja', 'CREATE',
       'Permite aperturar un turno de recaudación con fondo inicial de sencillo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_CERRAR', 'Cerrar Sesión de Caja', 'UPDATE',
       'Permite cerrar turnos de caja registrando el arqueo de efectivo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_ARQUEAR', 'Realizar Arqueo de Caja', 'MANAGE',
       'Permite efectuar el conteo físico de billetes y monedas en ventanilla', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_SUPERVISAR', 'Supervisar Sesiones de Caja', 'MANAGE',
       'Permite supervisar y aprobar diferencias de arqueo y cierres de sesión', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_REGISTRAR', 'Registrar Pagos Tributarios', 'CREATE',
       'Permite registrar ingresos presenciales y depósitos bancarios de contribuyentes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_CONFIRMAR', 'Confirmar Pagos Tributarios', 'UPDATE',
       'Permite confirmar pagos verificados y aplicar abonos a cuenta corriente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_APLICAR', 'Aplicar Pagos a Obligaciones', 'UPDATE',
       'Permite imputar importes recaudados a cuotas y obligaciones exigibles', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_ANULAR', 'Anular Pagos Tributarios', 'DELETE',
       'Permite anular registros de pago en borrador o no confirmados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_REVERTIR', 'Revertir Pagos Confirmados', 'UPDATE',
       'Permite revertir pagos confirmados generando contraasientos en cuenta corriente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_VER_HISTORIAL', 'Ver Historial de Pagos', 'VIEW',
       'Permite auditar la trazabilidad de operaciones de recaudación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCILIACION_VER', 'Ver Conciliaciones de Caja', 'VIEW',
       'Permite consultar actas de arqueo y conciliaciones financieras', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCILIACION_EJECUTAR', 'Ejecutar Conciliación Financiera', 'MANAGE',
       'Permite generar la comparación de ingresos registrados vs recaudación bancaria y caja', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CONCILIACION_APROBAR', 'Aprobar Conciliación Financiera', 'APPROVE',
       'Permite aprobar el cierre formal de conciliación por tesorería', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PAGOS_EXPORTAR', 'Exportar Pagos Tributarios', 'EXPORT',
       'Permite exportar reportes de pagos a Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_CAJA_REPORTES', 'Reportes de Caja y Conciliación', 'VIEW',
       'Permite generar reportes de cierre diario de caja y recaudación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 8. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'TRIBUTARIA_CAJA_VER',
      'TRIBUTARIA_CAJA_ADMINISTRAR',
      'TRIBUTARIA_CAJA_ABRIR',
      'TRIBUTARIA_CAJA_CERRAR',
      'TRIBUTARIA_CAJA_ARQUEAR',
      'TRIBUTARIA_CAJA_SUPERVISAR',
      'TRIBUTARIA_PAGOS_VER',
      'TRIBUTARIA_PAGOS_REGISTRAR',
      'TRIBUTARIA_PAGOS_CONFIRMAR',
      'TRIBUTARIA_PAGOS_APLICAR',
      'TRIBUTARIA_PAGOS_ANULAR',
      'TRIBUTARIA_PAGOS_REVERTIR',
      'TRIBUTARIA_PAGOS_VER_HISTORIAL',
      'TRIBUTARIA_CONCILIACION_VER',
      'TRIBUTARIA_CONCILIACION_EJECUTAR',
      'TRIBUTARIA_CONCILIACION_APROBAR',
      'TRIBUTARIA_PAGOS_EXPORTAR',
      'TRIBUTARIA_CAJA_REPORTES'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Asignación al rol Cajero
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_CAJERO')
  AND p.codigo IN (
      'TRIBUTARIA_CAJA_VER',
      'TRIBUTARIA_CAJA_ABRIR',
      'TRIBUTARIA_CAJA_CERRAR',
      'TRIBUTARIA_CAJA_ARQUEAR',
      'TRIBUTARIA_PAGOS_VER',
      'TRIBUTARIA_PAGOS_REGISTRAR',
      'TRIBUTARIA_PAGOS_APLICAR',
      'TRIBUTARIA_PAGOS_CONFIRMAR',
      'TRIBUTARIA_PAGOS_VER_HISTORIAL'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Poblado de Caja Municipal Principal por defecto si no existe
INSERT INTO tributaria_cajas_municipales (codigo, nombre, ubicacion, estado, observaciones)
VALUES ('CAJA-01', 'Ventanilla Principal de Recaudación', 'Palacio Municipal - Jr. 28 de Julio S/N, Plaza Principal Quinua', 'ACTIVA', 'Caja física central de recaudación tributaria y tasas municipales')
ON CONFLICT (codigo) DO NOTHING;
