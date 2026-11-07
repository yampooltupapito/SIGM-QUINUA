-- ============================================================================
-- Módulo 4: Recaudación Tributaria Municipal - Municipalidad Distrital de Quinua
-- Prompt 61: Fraccionamientos Tributarios, Convenios de Pago y Control de Cronogramas
-- ============================================================================

-- 1. Tabla de Programas de Fraccionamiento Tributario
CREATE TABLE IF NOT EXISTS tributaria_programas_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_prog_frac_codigo ON tributaria_programas_fraccionamiento(codigo);
CREATE INDEX IF NOT EXISTS idx_trib_prog_frac_estado ON tributaria_programas_fraccionamiento(estado);
CREATE INDEX IF NOT EXISTS idx_trib_prog_frac_vigencia ON tributaria_programas_fraccionamiento(fecha_inicio_vigencia, fecha_fin_vigencia);

-- 2. Tabla de Parámetros de Fraccionamiento Tributario
CREATE TABLE IF NOT EXISTS tributaria_parametros_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    programa_id UUID NOT NULL REFERENCES tributaria_programas_fraccionamiento(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    tipo_parametro VARCHAR(50) NOT NULL,
    valor_numerico NUMERIC(12, 4),
    valor_texto VARCHAR(255),
    unidad VARCHAR(30) NOT NULL,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT uq_trib_param_frac UNIQUE (programa_id, codigo, fecha_inicio_vigencia)
);

CREATE INDEX IF NOT EXISTS idx_trib_param_frac_prog ON tributaria_parametros_fraccionamiento(programa_id);
CREATE INDEX IF NOT EXISTS idx_trib_param_frac_cod ON tributaria_parametros_fraccionamiento(codigo);
CREATE INDEX IF NOT EXISTS idx_trib_param_frac_estado ON tributaria_parametros_fraccionamiento(estado);

-- 3. Tabla de Solicitudes de Fraccionamiento Tributario
CREATE TABLE IF NOT EXISTS tributaria_solicitudes_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_solicitud VARCHAR(50) NOT NULL UNIQUE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    programa_id UUID NOT NULL REFERENCES tributaria_programas_fraccionamiento(id) ON DELETE RESTRICT,
    fecha_solicitud DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PRESENTADA',
    motivo TEXT NOT NULL,
    numero_cuotas_propuestas INT NOT NULL CHECK (numero_cuotas_propuestas >= 1),
    monto_total_deuda NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    monto_cuota_inicial NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    evaluador_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_evaluacion TIMESTAMP WITH TIME ZONE,
    observacion_evaluacion TEXT,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_sol_frac_codigo ON tributaria_solicitudes_fraccionamiento(codigo_solicitud);
CREATE INDEX IF NOT EXISTS idx_trib_sol_frac_contrib ON tributaria_solicitudes_fraccionamiento(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_sol_frac_prog ON tributaria_solicitudes_fraccionamiento(programa_id);
CREATE INDEX IF NOT EXISTS idx_trib_sol_frac_estado ON tributaria_solicitudes_fraccionamiento(estado);
CREATE INDEX IF NOT EXISTS idx_trib_sol_frac_fecha ON tributaria_solicitudes_fraccionamiento(fecha_solicitud);

-- 4. Tabla de Detalle de Obligaciones Evaluadas en Solicitud
CREATE TABLE IF NOT EXISTS tributaria_detalles_solicitud_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_id UUID NOT NULL REFERENCES tributaria_solicitudes_fraccionamiento(id) ON DELETE CASCADE,
    obligacion_id UUID NOT NULL REFERENCES tributaria_obligaciones(id) ON DELETE RESTRICT,
    saldo_evaluado NUMERIC(14, 2) NOT NULL CHECK (saldo_evaluado > 0),
    fecha_corte DATE NOT NULL,
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'CONFORME',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT uq_trib_det_sol_obl UNIQUE (solicitud_id, obligacion_id)
);

CREATE INDEX IF NOT EXISTS idx_trib_det_sol_sol ON tributaria_detalles_solicitud_fraccionamiento(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_trib_det_sol_obl ON tributaria_detalles_solicitud_fraccionamiento(obligacion_id);

-- 5. Tabla de Cronogramas de Fraccionamiento
CREATE TABLE IF NOT EXISTS tributaria_cronogramas_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_id UUID NOT NULL REFERENCES tributaria_solicitudes_fraccionamiento(id) ON DELETE CASCADE,
    version_cronograma INT NOT NULL DEFAULT 1,
    importe_base NUMERIC(14, 2) NOT NULL,
    importe_cuota_inicial NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_financiado NUMERIC(14, 2) NOT NULL,
    importe_accesorios NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_total NUMERIC(14, 2) NOT NULL,
    numero_cuotas INT NOT NULL CHECK (numero_cuotas >= 1),
    tasa_interes_aplicada NUMERIC(8, 4) NOT NULL DEFAULT 0.0000,
    fecha_generacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'SIMULADO',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_cron_sol ON tributaria_cronogramas_fraccionamiento(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_trib_cron_estado ON tributaria_cronogramas_fraccionamiento(estado);

-- 6. Tabla de Cuotas del Cronograma de Fraccionamiento
CREATE TABLE IF NOT EXISTS tributaria_cuotas_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cronograma_id UUID NOT NULL REFERENCES tributaria_cronogramas_fraccionamiento(id) ON DELETE CASCADE,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    importe_capital NUMERIC(14, 2) NOT NULL,
    importe_interes NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    importe_total NUMERIC(14, 2) NOT NULL,
    importe_pagado NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    saldo NUMERIC(14, 2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT uq_trib_cuota_num UNIQUE (cronograma_id, numero_cuota)
);

CREATE INDEX IF NOT EXISTS idx_trib_cuota_cron ON tributaria_cuotas_fraccionamiento(cronograma_id);
CREATE INDEX IF NOT EXISTS idx_trib_cuota_venc ON tributaria_cuotas_fraccionamiento(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_trib_cuota_estado ON tributaria_cuotas_fraccionamiento(estado);

-- 7. Tabla de Convenios Formales de Fraccionamiento Tributario
CREATE TABLE IF NOT EXISTS tributaria_convenios_fraccionamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_convenio VARCHAR(50) NOT NULL UNIQUE,
    solicitud_id UUID NOT NULL UNIQUE REFERENCES tributaria_solicitudes_fraccionamiento(id) ON DELETE RESTRICT,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    programa_id UUID NOT NULL REFERENCES tributaria_programas_fraccionamiento(id) ON DELETE RESTRICT,
    cronograma_id UUID NOT NULL REFERENCES tributaria_cronogramas_fraccionamiento(id) ON DELETE RESTRICT,
    fecha_aprobacion DATE NOT NULL,
    fecha_inicio_vigencia DATE NOT NULL,
    resolucion_aprobacion VARCHAR(150) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'VIGENTE',
    responsable_aprobacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    documento_convenio_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    codigo_verificacion VARCHAR(64) NOT NULL UNIQUE,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_conv_codigo ON tributaria_convenios_fraccionamiento(codigo_convenio);
CREATE INDEX IF NOT EXISTS idx_trib_conv_contrib ON tributaria_convenios_fraccionamiento(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_conv_prog ON tributaria_convenios_fraccionamiento(programa_id);
CREATE INDEX IF NOT EXISTS idx_trib_conv_estado ON tributaria_convenios_fraccionamiento(estado);
CREATE INDEX IF NOT EXISTS idx_trib_conv_cod_verif ON tributaria_convenios_fraccionamiento(codigo_verificacion);

-- 8. Tabla de Incidencias y Bitácora de Convenios
CREATE TABLE IF NOT EXISTS tributaria_incidencias_convenio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    convenio_id UUID NOT NULL REFERENCES tributaria_convenios_fraccionamiento(id) ON DELETE CASCADE,
    tipo_incidencia VARCHAR(40) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_inc_conv ON tributaria_incidencias_convenio(convenio_id);
CREATE INDEX IF NOT EXISTS idx_trib_inc_tipo ON tributaria_incidencias_convenio(tipo_incidencia);

-- 9. Poblado Inicial del Programa General de Fraccionamiento 2026
INSERT INTO tributaria_programas_fraccionamiento (
    codigo, nombre, descripcion, fecha_inicio_vigencia, estado, created_by
) VALUES (
    'PROG-FRAC-2026-01',
    'Programa General de Facilidades de Pago Tributario 2026',
    'Régimen permanente de fraccionamiento de deudas tributarias municipales conforme al Art. 36 del TUO del Código Tributario para el Distrito de Quinua.',
    '2026-01-01',
    'ACTIVO',
    'SISTEMA'
) ON CONFLICT (codigo) DO NOTHING;

-- 10. Poblado de Parámetros Normativos del Programa General
INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'MONTO_MINIMO_ELEGIBLE', 'MONTO_MINIMO', 150.00, 'PEN', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'CUOTAS_MAXIMAS', 'CUOTAS_MAX', 24.00, 'CUOTAS', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'CUOTAS_MINIMAS', 'CUOTAS_MIN', 2.00, 'CUOTAS', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'PORCENTAJE_CUOTA_INICIAL_MIN', 'CUOTA_INICIAL_MIN_PCT', 10.00, '%', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'TASA_INTERES_MENSUAL', 'TASA_INTERES_MENSUAL', 0.7200, '%', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'PERIODICIDAD_DIAS', 'PERIODICIDAD_DIAS', 30.00, 'DIAS', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'DIAS_TOLERANCIA_VENCIMIENTO', 'TOLERANCIA_DIAS', 5.00, 'DIAS', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

INSERT INTO tributaria_parametros_fraccionamiento (
    programa_id, codigo, tipo_parametro, valor_numerico, unidad, fecha_inicio_vigencia, estado, created_by
)
SELECT p.id, 'CUOTAS_IMPAGAS_CAUSAL_PERDIDA', 'CUOTAS_INCUMPLIMIENTO_PERDIDA', 2.00, 'CUOTAS', '2026-01-01', TRUE, 'SISTEMA'
FROM tributaria_programas_fraccionamiento p WHERE p.codigo = 'PROG-FRAC-2026-01'
ON CONFLICT (programa_id, codigo, fecha_inicio_vigencia) DO NOTHING;

-- 11. Registro de Permisos de Seguridad RBAC
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_VER', 'Ver Fraccionamientos y Convenios', 'VIEW',
       'Permite consultar el listado de solicitudes, programas y convenios de pago', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_CONFIGURAR', 'Configurar Programas de Fraccionamiento', 'MANAGE',
       'Permite crear y actualizar programas y parámetros normativos de fraccionamiento', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR', 'Registrar Solicitud de Fraccionamiento', 'CREATE',
       'Permite ingresar nuevas solicitudes de fraccionamiento tributario', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_EVALUAR', 'Evaluar Solicitud de Fraccionamiento', 'UPDATE',
       'Permite realizar la evaluación técnica y legal de solicitudes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_OBSERVAR', 'Observar Solicitud de Fraccionamiento', 'UPDATE',
       'Permite formular observaciones administrativas a solicitudes', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_SUBSANAR', 'Subsanar Solicitud de Fraccionamiento', 'UPDATE',
       'Permite registrar subsanaciones de observaciones presentadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR', 'Simular Cronogramas de Pago', 'VIEW',
       'Permite simular cuotas e intereses de fraccionamiento sin efecto vinculante', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_APROBAR', 'Aprobar Convenio de Fraccionamiento', 'UPDATE',
       'Permite aprobar solicitudes y emitir convenios formales de pago', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_RECHAZAR', 'Rechazar Solicitud de Fraccionamiento', 'UPDATE',
       'Permite desestimar solicitudes con sustento administrativo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_GESTIONAR_CONVENIO', 'Gestionar Convenios Vigentes', 'MANAGE',
       'Permite administrar convenios, controlar cuotas e incidencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_REPROGRAMAR', 'Reprogramar Convenio de Pago', 'UPDATE',
       'Permite autorizar reprogramaciones y modificaciones justificadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_REVISAR_INCUMPLIMIENTO', 'Revisar Incumplimiento de Convenio', 'VIEW',
       'Permite revisar causales de pérdida y emitir informes técnicos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_VER_HISTORIAL', 'Ver Historial de Fraccionamiento', 'VIEW',
       'Permite consultar la auditoría cronológica de solicitudes y convenios', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_EXPORTAR', 'Exportar Cartera Fraccionada', 'EXPORT',
       'Permite exportar cronogramas, convenios y estados a formatos seguros', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_FRACCIONAMIENTOS_REPORTES', 'Reportes de Fraccionamientos', 'VIEW',
       'Permite generar estadísticas y reportes consolidados de facilidades de pago', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 12. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'TRIBUTARIA_FRACCIONAMIENTOS_%'
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_TRIBUTACION'
  AND p.codigo IN (
      'TRIBUTARIA_FRACCIONAMIENTOS_VER',
      'TRIBUTARIA_FRACCIONAMIENTOS_CONFIGURAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_EVALUAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_OBSERVAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_SUBSANAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_APROBAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_RECHAZAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_GESTIONAR_CONVENIO',
      'TRIBUTARIA_FRACCIONAMIENTOS_REPROGRAMAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_REVISAR_INCUMPLIMIENTO',
      'TRIBUTARIA_FRACCIONAMIENTOS_VER_HISTORIAL',
      'TRIBUTARIA_FRACCIONAMIENTOS_EXPORTAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_REPORTES'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_CAJERO'
  AND p.codigo IN (
      'TRIBUTARIA_FRACCIONAMIENTOS_VER',
      'TRIBUTARIA_FRACCIONAMIENTOS_SOLICITAR',
      'TRIBUTARIA_FRACCIONAMIENTOS_SIMULAR'
  )
ON CONFLICT DO NOTHING;
