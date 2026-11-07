-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V18 - DERIVACIÓN DE EXPEDIENTES, ASIGNACIÓN Y SEGUIMIENTO
-- ==============================================================================

-- 1. Ampliación de Estados y Auditoría en Derivaciones
ALTER TABLE mpv_derivaciones DROP CONSTRAINT IF EXISTS chk_mpv_derivacion_estado;
ALTER TABLE mpv_derivaciones ADD CONSTRAINT chk_mpv_derivacion_estado
    CHECK (estado IN ('PENDIENTE', 'RECIBIDA', 'RECHAZADA', 'DEVUELTA', 'FINALIZADA', 'CANCELADA'));

ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS usuario_receptor_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL;
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS indicaciones TEXT;
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS motivo_devolucion TEXT;
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS correlativo_derivacion VARCHAR(50);
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE mpv_derivaciones ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_mpv_deriv_origen ON mpv_derivaciones(oficina_origen_id);
CREATE INDEX IF NOT EXISTS idx_mpv_deriv_usuario_deriv ON mpv_derivaciones(usuario_derivador_id);
CREATE INDEX IF NOT EXISTS idx_mpv_deriv_usuario_rec ON mpv_derivaciones(usuario_receptor_id);

-- 2. Asignación del Responsable Actual en Expedientes
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS responsable_actual_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_mpv_exp_responsable ON mpv_expedientes(responsable_actual_id);

-- 3. Tabla de Asignaciones Internas de Expedientes a Trabajadores
CREATE TABLE IF NOT EXISTS mpv_asignaciones (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    oficina_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    usuario_asignador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    usuario_asignado_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    usuario_anterior_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    motivo TEXT,
    indicaciones TEXT,
    fecha_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_asig_estado CHECK (estado IN ('ACTIVO', 'REASIGNADO', 'FINALIZADO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_asig_exp ON mpv_asignaciones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_asig_oficina ON mpv_asignaciones(oficina_id);
CREATE INDEX IF NOT EXISTS idx_mpv_asig_usuario ON mpv_asignaciones(usuario_asignado_id);
CREATE INDEX IF NOT EXISTS idx_mpv_asig_estado ON mpv_asignaciones(estado);

-- 4. Tabla de Actuaciones Internas (Informes, Evaluaciones, Solicitudes)
CREATE TABLE IF NOT EXISTS mpv_actuaciones (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    tipo VARCHAR(40) NOT NULL,
    descripcion TEXT NOT NULL,
    usuario_responsable_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    fecha_actuacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'INTERNA',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_actuacion_tipo CHECK (tipo IN ('REVISION', 'INFORME', 'EVALUACION', 'OBSERVACION_INTERNA', 'SOLICITUD_INFORMACION', 'REMISION', 'OTRA')),
    CONSTRAINT chk_mpv_actuacion_visibilidad CHECK (visibilidad IN ('INTERNA', 'CIUDADANO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_act_exp ON mpv_actuaciones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_act_usuario ON mpv_actuaciones(usuario_responsable_id);
CREATE INDEX IF NOT EXISTS idx_mpv_act_oficina ON mpv_actuaciones(oficina_id);
CREATE INDEX IF NOT EXISTS idx_mpv_act_fecha ON mpv_actuaciones(fecha_actuacion);

-- 5. Documentos Asociados a Actuaciones Internas
CREATE TABLE IF NOT EXISTS mpv_actuacion_documentos (
    id BIGSERIAL PRIMARY KEY,
    actuacion_id BIGINT NOT NULL REFERENCES mpv_actuaciones(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE RESTRICT,
    tipo_documento VARCHAR(50) DEFAULT 'INFORME',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mpv_act_doc_act ON mpv_actuacion_documentos(actuacion_id);
CREATE INDEX IF NOT EXISTS idx_mpv_act_doc_doc ON mpv_actuacion_documentos(documento_id);

-- 6. Trazabilidad de Eventos de Derivación
CREATE TABLE IF NOT EXISTS mpv_derivacion_eventos (
    id BIGSERIAL PRIMARY KEY,
    derivacion_id BIGINT NOT NULL REFERENCES mpv_derivaciones(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(30) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    motivo TEXT,
    observacion TEXT,
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mpv_der_ev_der ON mpv_derivacion_eventos(derivacion_id);
CREATE INDEX IF NOT EXISTS idx_mpv_der_ev_fecha ON mpv_derivacion_eventos(fecha_evento);

-- 7. Catálogo de Permisos Granulares para Derivación y Seguimiento
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DERIVACION_CREAR', 'Derivar expedientes entre dependencias', 'CREATE', 'Permite remitir expedientes a otras dependencias'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DERIVACION_VER', 'Ver bandeja de derivaciones', 'VIEW', 'Permite visualizar derivaciones emitidas y recibidas'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DERIVACION_RECIBIR', 'Recibir derivaciones de expedientes', 'UPDATE', 'Permite aceptar la recepción formal de una derivación'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DERIVACION_RECHAZAR', 'Rechazar derivaciones de expedientes', 'UPDATE', 'Permite rechazar derivaciones improcedentes con justificación'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DERIVACION_DEVOLVER', 'Devolver derivaciones a oficina remitente', 'UPDATE', 'Permite devolver expedientes con motivo obligatorio'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ASIGNACION_CREAR', 'Asignar expedientes a trabajadores', 'ASSIGN', 'Permite asignar expedientes a servidores de la oficina'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ASIGNACION_MODIFICAR', 'Reasignar expedientes entre trabajadores', 'ASSIGN', 'Permite transferir la responsabilidad interna de un expediente'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ACTUACION_CREAR', 'Registrar actuaciones internas en expedientes', 'CREATE', 'Permite añadir informes, evaluaciones y actuaciones administrativas'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_SEGUIMIENTO_VER', 'Consultar seguimiento integral de expedientes', 'VIEW', 'Permite visualizar la trazabilidad, mapa de recorrido e historial'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ESTADISTICAS_OFICINA', 'Consultar estadísticas y métricas de derivación', 'VIEW', 'Permite ver indicadores de carga y atención por oficina'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignar nuevos permisos a Roles Autorizados
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_MESA_PARTES', 'ROLE_OPERADOR', 'ROLE_JEFE_OFICINA')
  AND p.codigo IN (
    'MESAPARTES_DERIVACION_CREAR', 'MESAPARTES_DERIVACION_VER', 'MESAPARTES_DERIVACION_RECIBIR',
    'MESAPARTES_DERIVACION_RECHAZAR', 'MESAPARTES_DERIVACION_DEVOLVER', 'MESAPARTES_ASIGNACION_CREAR',
    'MESAPARTES_ASIGNACION_MODIFICAR', 'MESAPARTES_ACTUACION_CREAR', 'MESAPARTES_SEGUIMIENTO_VER',
    'MESAPARTES_ESTADISTICAS_OFICINA'
  )
ON CONFLICT DO NOTHING;
