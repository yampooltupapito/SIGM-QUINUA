-- ============================================================================
-- SIGM-QUINUA: MIGRACIÓN V33 - RENOVACIONES, CAMBIOS DE TITULAR Y TRANSFERENCIAS AUTORIZADAS
-- Módulo 2: Mercados Municipales - Prompt 31
-- ============================================================================

-- 1. Secuencia segura para códigos únicos de solicitudes de gestión de ocupación
CREATE SEQUENCE IF NOT EXISTS seq_mercados_solicitud_gestion START WITH 1 INCREMENT BY 1;

-- 2. Catálogo Configurable de Procedimientos de Gestión de Ocupación
CREATE TABLE IF NOT EXISTS mercados_procedimientos_gestion (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_solicitud VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    base_normativa TEXT,
    oficina_competente_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    requiere_titular_propuesto BOOLEAN NOT NULL DEFAULT FALSE,
    permite_transferencia BOOLEAN NOT NULL DEFAULT FALSE,
    plazo_dias_habiles INT DEFAULT 15,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_proc_gestion_tipo ON mercados_procedimientos_gestion(tipo_solicitud);
CREATE INDEX IF NOT EXISTS idx_proc_gestion_activo ON mercados_procedimientos_gestion(activo);

-- Seed de procedimientos institucionales
INSERT INTO mercados_procedimientos_gestion (codigo, tipo_solicitud, nombre, descripcion, base_normativa, requiere_titular_propuesto, permite_transferencia, plazo_dias_habiles, activo) VALUES
    ('PROC-REN-ASIG', 'RENOVACION_ASIGNACION', 'Renovación de Asignación de Puesto Comercial', 'Prórroga reglamentaria de la asignación del puesto para comerciante empadronado al día en sus obligaciones', 'Ley N° 27972 y Ordenanza Municipal N° 008-2023-MDQ', FALSE, FALSE, 15, TRUE),
    ('PROC-REN-CONT', 'RENOVACION_CONTRATO', 'Renovación de Contrato de Ocupación o Arrendamiento', 'Continuidad contractual mediante suscripción de nueva adenda o instrumento con la comuna de Quinua', 'Art. 83 Ley N° 27972 y Código Civil', FALSE, FALSE, 20, TRUE),
    ('PROC-REN-AUTO', 'RENOVACION_AUTORIZACION', 'Renovación de Autorización Municipal de Conducción', 'Extensión de vigencia del padrón o licencia municipal de funcionamiento en mercado', 'Reglamento Interno de Mercados de Quinua', FALSE, FALSE, 15, TRUE),
    ('PROC-CAMB-TIT', 'CAMBIO_TITULAR', 'Cambio de Titular de Puesto por Sucesión o Causal Excepcional', 'Modificación formal de titularidad debidamente sustentada por fallecimiento, incapacidad o cesión legítima', 'Art. 83 Ley N° 27972 y Ordenanza Municipal N° 008-2023-MDQ', TRUE, FALSE, 30, TRUE),
    ('PROC-TRAN-AUT', 'TRANSFERENCIA_AUTORIZADA', 'Transferencia Excepcional Autorizada de Conducción', 'Procedimiento extraordinario sujeto a autorización expresa de Concejo o Gerencia Municipal facultada', 'Acuerdo de Concejo Municipal N° 012-2023-MDQ', TRUE, FALSE, 30, FALSE)
ON CONFLICT (codigo) DO NOTHING;

-- 3. Tabla Principal: Solicitudes de Gestión de Ocupación
CREATE TABLE IF NOT EXISTS mercados_solicitudes_gestion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_solicitud VARCHAR(50) NOT NULL UNIQUE,
    tipo_solicitud VARCHAR(50) NOT NULL, -- RENOVACION_ASIGNACION, RENOVACION_CONTRATO, RENOVACION_AUTORIZACION, CAMBIO_TITULAR, TRANSFERENCIA_AUTORIZADA
    procedimiento_id BIGINT REFERENCES mercados_procedimientos_gestion(id) ON DELETE RESTRICT,
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE RESTRICT,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE RESTRICT,
    asignacion_origen_id UUID NOT NULL REFERENCES mercados_asignaciones(id) ON DELETE RESTRICT,
    contrato_origen_id UUID REFERENCES mercados_contratos(id) ON DELETE SET NULL,
    autorizacion_origen_id UUID REFERENCES mercados_autorizaciones(id) ON DELETE SET NULL,
    comerciante_actual_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    comerciante_propuesto_id UUID REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    fecha_presentacion DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_inicio_solicitada DATE,
    fecha_fin_solicitada DATE,
    motivo TEXT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'BORRADOR', -- BORRADOR, PRESENTADA, EN_REVISION, OBSERVADA, PENDIENTE_SUBSANACION, EN_EVALUACION, APROBADA, RECHAZADA, PENDIENTE_FORMALIZACION, FORMALIZADA, FINALIZADA, DESISTIDA, ANULADA
    expediente_id BIGINT REFERENCES mpv_expedientes(id) ON DELETE SET NULL,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    usuario_registro VARCHAR(100),
    usuario_evaluacion VARCHAR(100),
    fecha_evaluacion TIMESTAMP WITH TIME ZONE,
    informe_evaluacion TEXT,
    usuario_decision VARCHAR(100),
    fecha_decision TIMESTAMP WITH TIME ZONE,
    motivo_decision TEXT,
    documento_decision_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_formalizacion VARCHAR(100),
    fecha_formalizacion TIMESTAMP WITH TIME ZONE,
    asignacion_nueva_id UUID REFERENCES mercados_asignaciones(id) ON DELETE SET NULL,
    contrato_nuevo_id UUID REFERENCES mercados_contratos(id) ON DELETE SET NULL,
    autorizacion_nueva_id UUID REFERENCES mercados_autorizaciones(id) ON DELETE SET NULL,
    documento_formalizacion_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    token_verificacion_qr VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sol_gestion_tipo ON mercados_solicitudes_gestion(tipo_solicitud);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_estado ON mercados_solicitudes_gestion(estado);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_mercado ON mercados_solicitudes_gestion(mercado_id);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_puesto ON mercados_solicitudes_gestion(puesto_id);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_com_act ON mercados_solicitudes_gestion(comerciante_actual_id);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_com_prop ON mercados_solicitudes_gestion(comerciante_propuesto_id);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_asig_orig ON mercados_solicitudes_gestion(asignacion_origen_id);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_expediente ON mercados_solicitudes_gestion(expediente_id);
CREATE INDEX IF NOT EXISTS idx_sol_gestion_fpresentacion ON mercados_solicitudes_gestion(fecha_presentacion);

-- 4. Tabla de Evaluaciones Administrativas
CREATE TABLE IF NOT EXISTS mercados_solicitudes_evaluaciones (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id UUID NOT NULL REFERENCES mercados_solicitudes_gestion(id) ON DELETE CASCADE,
    fecha_evaluacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_evaluador VARCHAR(100) NOT NULL,
    sentido_evaluacion VARCHAR(50) NOT NULL, -- FAVORABLE, DESFAVORABLE, OBSERVADA, REQUIERE_INFORMACION
    informe_tecnico TEXT NOT NULL,
    observaciones TEXT,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sol_eval_sol ON mercados_solicitudes_evaluaciones(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_sol_eval_fecha ON mercados_solicitudes_evaluaciones(fecha_evaluacion);

-- 5. Tabla de Observaciones a la Solicitud
CREATE TABLE IF NOT EXISTS mercados_solicitudes_observaciones (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id UUID NOT NULL REFERENCES mercados_solicitudes_gestion(id) ON DELETE CASCADE,
    descripcion TEXT NOT NULL,
    requisito_relacionado VARCHAR(255),
    fecha_observacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_observador VARCHAR(100) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, SUBSANADA, REITERADA, DESESTIMADA
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sol_obs_sol ON mercados_solicitudes_observaciones(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_sol_obs_estado ON mercados_solicitudes_observaciones(estado);

-- 6. Tabla de Subsanaciones de Observaciones
CREATE TABLE IF NOT EXISTS mercados_solicitudes_subsanaciones (
    id BIGSERIAL PRIMARY KEY,
    observacion_id BIGINT NOT NULL REFERENCES mercados_solicitudes_observaciones(id) ON DELETE CASCADE,
    solicitud_id UUID NOT NULL REFERENCES mercados_solicitudes_gestion(id) ON DELETE CASCADE,
    descripcion TEXT NOT NULL,
    fecha_subsanacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_subsanador VARCHAR(100) NOT NULL,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    resultado_revision VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, CONFORME, RECHAZADA, INSUFICIENTE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sol_sub_obs ON mercados_solicitudes_subsanaciones(observacion_id);
CREATE INDEX IF NOT EXISTS idx_sol_sub_sol ON mercados_solicitudes_subsanaciones(solicitud_id);

-- 7. Historial Inmutable de Titularidad de Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_historial_titularidad (
    id BIGSERIAL PRIMARY KEY,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE RESTRICT,
    solicitud_id UUID REFERENCES mercados_solicitudes_gestion(id) ON DELETE SET NULL,
    asignacion_anterior_id UUID REFERENCES mercados_asignaciones(id) ON DELETE RESTRICT,
    asignacion_nueva_id UUID REFERENCES mercados_asignaciones(id) ON DELETE RESTRICT,
    titular_anterior_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    titular_nuevo_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    tipo_operacion VARCHAR(50) NOT NULL, -- CAMBIO_TITULAR, TRANSFERENCIA_AUTORIZADA, RENOVACION_ASIGNACION
    fecha_efectiva DATE NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    motivo TEXT NOT NULL,
    usuario_registro VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hist_tit_puesto ON mercados_historial_titularidad(puesto_id);
CREATE INDEX IF NOT EXISTS idx_hist_tit_tit_ant ON mercados_historial_titularidad(titular_anterior_id);
CREATE INDEX IF NOT EXISTS idx_hist_tit_tit_nue ON mercados_historial_titularidad(titular_nuevo_id);
CREATE INDEX IF NOT EXISTS idx_hist_tit_fecha ON mercados_historial_titularidad(fecha_efectiva);

-- 8. Permisos Granulares RBAC para el submódulo
INSERT INTO permisos (codigo, nombre, accion, descripcion, modulo_id)
SELECT p.codigo, p.nombre, p.accion, p.descripcion, m.id
FROM (
    VALUES
        ('MERCADOS_RENOVACION_VER', 'Ver Renovaciones de Mercados', 'VIEW', 'Visualizar solicitudes y trámites de renovación de asignaciones, contratos y autorizaciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_RENOVACION_CREAR', 'Registrar Solicitud de Renovación', 'CREATE', 'Registrar y presentar solicitudes de renovación en mercados municipales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_RENOVACION_EVALUAR', 'Evaluar Renovaciones de Mercados', 'APPROVE', 'Revisar antecedentes, requisitos y emitir informe técnico de renovación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_RENOVACION_APROBAR', 'Aprobar/Rechazar Renovaciones', 'APPROVE', 'Emitir decisión administrativa sobre trámites de renovación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CAMBIO_TITULAR_VER', 'Ver Cambios de Titular', 'VIEW', 'Visualizar solicitudes de cambio de titular de puestos de mercado', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CAMBIO_TITULAR_CREAR', 'Solicitar Cambio de Titular', 'CREATE', 'Registrar y presentar solicitudes formales de cambio de titularidad', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CAMBIO_TITULAR_EVALUAR', 'Evaluar Cambios de Titular', 'APPROVE', 'Evaluar legalidad y legitimidad de solicitudes de cambio de titular', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CAMBIO_TITULAR_APROBAR', 'Aprobar/Rechazar Cambios de Titular', 'APPROVE', 'Emitir decisión administrativa sobre solicitudes de cambio de titular', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_TRANSFERENCIA_VER', 'Ver Transferencias de Mercados', 'VIEW', 'Visualizar solicitudes de transferencias autorizadas de puestos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_TRANSFERENCIA_CREAR', 'Solicitar Transferencia de Mercados', 'CREATE', 'Registrar trámite de transferencia conforme a procedimiento habilitado', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_TRANSFERENCIA_EVALUAR', 'Evaluar Transferencias de Mercados', 'APPROVE', 'Calificar procedencia jurídica y cumplimiento de requisitos de transferencia', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_TRANSFERENCIA_APROBAR', 'Aprobar/Rechazar Transferencias', 'APPROVE', 'Decidir procedencia de transferencias autorizadas por Concejo o Gerencia', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_GESTION_OCUPACION_FORMALIZAR', 'Formalizar Decisiones de Ocupación', 'MANAGE', 'Ejecutar formalización de renovaciones, transferencias y cambios de titular', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_GESTION_OCUPACION_HISTORIAL_VER', 'Ver Historial de Titularidad', 'VIEW', 'Consultar línea de tiempo y registro inmutable de titulares por puesto', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_GESTION_OCUPACION_EXPORTAR', 'Exportar Reportes de Gestión de Ocupación', 'EXPORT', 'Generar reportes y exportaciones en formatos Excel y PDF', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar los 15 permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_RENOVACION_VER',
        'MERCADOS_RENOVACION_CREAR',
        'MERCADOS_RENOVACION_EVALUAR',
        'MERCADOS_RENOVACION_APROBAR',
        'MERCADOS_CAMBIO_TITULAR_VER',
        'MERCADOS_CAMBIO_TITULAR_CREAR',
        'MERCADOS_CAMBIO_TITULAR_EVALUAR',
        'MERCADOS_CAMBIO_TITULAR_APROBAR',
        'MERCADOS_TRANSFERENCIA_VER',
        'MERCADOS_TRANSFERENCIA_CREAR',
        'MERCADOS_TRANSFERENCIA_EVALUAR',
        'MERCADOS_TRANSFERENCIA_APROBAR',
        'MERCADOS_GESTION_OCUPACION_FORMALIZAR',
        'MERCADOS_GESTION_OCUPACION_HISTORIAL_VER',
        'MERCADOS_GESTION_OCUPACION_EXPORTAR'
    )
) p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
