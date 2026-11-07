-- ==============================================================================
-- SIGM-QUINUA | Módulo 3: Seguridad Ciudadana y Serenazgo
-- V44: Asignación, Despacho y Atención de Incidencias de Seguridad Ciudadana
-- ==============================================================================

-- 1. Secuencia para correlativo institucional de atenciones (ATN-YYYY-00000)
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_atencion START WITH 1 INCREMENT BY 1;

-- 2. Tabla Principal: Atenciones Operativas de Incidencias de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_atenciones_incidencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    incidencia_id UUID NOT NULL REFERENCES serenazgo_incidencias(id) ON DELETE RESTRICT,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    supervisor_asignador_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    responsable_operativo_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    unidad_movil_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    fecha_hora_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_hora_despacho TIMESTAMP WITH TIME ZONE,
    fecha_hora_aceptacion TIMESTAMP WITH TIME ZONE,
    fecha_hora_rechazo TIMESTAMP WITH TIME ZONE,
    fecha_hora_inicio TIMESTAMP WITH TIME ZONE,
    fecha_hora_finalizacion TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_ASIGNACION',
    prioridad_operativa VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
    canal_comunicacion VARCHAR(50) NOT NULL DEFAULT 'RADIO_TETRA_VHF',
    instrucciones_despacho TEXT,
    motivo_rechazo TEXT,
    motivo_espera TEXT,
    motivo_cancelacion TEXT,
    resultado_administrativo VARCHAR(50),
    motivo_finalizacion TEXT,
    observaciones_operativas TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_serenazgo_atn_estado CHECK (estado IN (
        'PENDIENTE_ASIGNACION', 'ASIGNADA', 'DESPACHADA', 'ACEPTADA',
        'EN_ATENCION', 'EN_ESPERA', 'FINALIZADA', 'CANCELADA'
    )),
    CONSTRAINT chk_serenazgo_atn_prioridad CHECK (prioridad_operativa IN (
        'BAJA', 'MEDIA', 'ALTA', 'CRITICA'
    ))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_codigo ON serenazgo_atenciones_incidencia(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_incidencia ON serenazgo_atenciones_incidencia(incidencia_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_estado ON serenazgo_atenciones_incidencia(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_fecha ON serenazgo_atenciones_incidencia(fecha_hora_asignacion);
CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_sector ON serenazgo_atenciones_incidencia(sector_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_unidad ON serenazgo_atenciones_incidencia(unidad_movil_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_responsable ON serenazgo_atenciones_incidencia(responsable_operativo_id);

-- 3. Tabla: Personal Asignado a la Atención Operativa
CREATE TABLE IF NOT EXISTS serenazgo_atencion_personal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    atencion_id UUID NOT NULL REFERENCES serenazgo_atenciones_incidencia(id) ON DELETE CASCADE,
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    funcion_operativa VARCHAR(50) NOT NULL DEFAULT 'PATRULLERO',
    es_responsable BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_desasignacion TIMESTAMP WITH TIME ZONE,
    motivo_desasignacion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    observaciones VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_atn_per_atencion ON serenazgo_atencion_personal(atencion_id);
CREATE INDEX IF NOT EXISTS idx_atn_per_personal ON serenazgo_atencion_personal(personal_id);
CREATE INDEX IF NOT EXISTS idx_atn_per_activo ON serenazgo_atencion_personal(personal_id, activo);

-- 4. Tabla: Equipamiento Asignado a la Atención Operativa
CREATE TABLE IF NOT EXISTS serenazgo_atencion_equipamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    atencion_id UUID NOT NULL REFERENCES serenazgo_atenciones_incidencia(id) ON DELETE CASCADE,
    equipamiento_id UUID NOT NULL REFERENCES serenazgo_equipamiento(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL DEFAULT 1,
    fecha_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion TIMESTAMP WITH TIME ZONE,
    devuelto BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_atn_eq_atencion ON serenazgo_atencion_equipamiento(atencion_id);
CREATE INDEX IF NOT EXISTS idx_atn_eq_equipamiento ON serenazgo_atencion_equipamiento(equipamiento_id);

-- 5. Tabla: Bitácora Inmutable de Novedades y Eventos Operativos
CREATE TABLE IF NOT EXISTS serenazgo_atencion_eventos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    atencion_id UUID NOT NULL REFERENCES serenazgo_atenciones_incidencia(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    personal_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detalles_json TEXT,
    visible_ciudadano BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_atn_ev_atencion ON serenazgo_atencion_eventos(atencion_id, fecha_evento);

-- 6. Tabla: Escalamientos Institucionales a Entidades Externas
CREATE TABLE IF NOT EXISTS serenazgo_atencion_escalamientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    atencion_id UUID NOT NULL REFERENCES serenazgo_atenciones_incidencia(id) ON DELETE CASCADE,
    entidad_destinataria VARCHAR(50) NOT NULL,
    motivo TEXT NOT NULL,
    canal_comunicacion VARCHAR(50) NOT NULL DEFAULT 'TELEFONO_DIRECTO',
    referencia_comunicacion VARCHAR(100),
    contacto_nombre VARCHAR(150),
    fecha_escalamiento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmacion_recepcion BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_confirmacion TIMESTAMP WITH TIME ZONE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT
);

CREATE INDEX IF NOT EXISTS idx_atn_esc_atencion ON serenazgo_atencion_escalamientos(atencion_id);
CREATE INDEX IF NOT EXISTS idx_atn_esc_entidad ON serenazgo_atencion_escalamientos(entidad_destinataria);

-- 7. Catálogo de Permisos Institucionales para Gestión de Atenciones
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_VER', 'Ver Atenciones de Serenazgo', 'VIEW', 'Consulta y visualización del centro de atenciones operativas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_CREAR', 'Crear Atención Operativa', 'CREATE', 'Registro de nueva orden de atención operativa sobre incidencia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_ASIGNAR', 'Asignar Recursos a Atención', 'ASSIGN', 'Asignación de personal, unidad móvil y equipamiento a la atención', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_DESPACHAR', 'Despachar Atención de Serenazgo', 'MANAGE', 'Despacho y activación operativa de recursos hacia la incidencia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_ACEPTAR', 'Aceptar o Rechazar Despacho', 'UPDATE', 'Registro de aceptación o rechazo justificado por la unidad operativa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_INICIAR', 'Iniciar Atención en Sitio', 'UPDATE', 'Registro de llegada al lugar de los hechos e inicio de actuaciones', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'Registrar Novedad en Atención', 'CREATE', 'Bitácora y seguimiento operativo en tiempo real durante la atención', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_REASIGNAR', 'Reasignar Recursos Operativos', 'UPDATE', 'Sustitución justificada de personal, responsable o móvil asignado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_ESCALAR', 'Escalar Atención a Entidad Externa', 'CREATE', 'Registro de derivación o coordinación con PNP, SAMU, Bomberos, etc.', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_FINALIZAR', 'Finalizar Atención Operativa', 'MANAGE', 'Cierre y registro de resultados administrativos de la atención', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_CANCELAR', 'Cancelar Atención Operativa', 'MANAGE', 'Cancelación administrativa justificada de una orden de atención', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ATENCIONES_EXPORTAR', 'Exportar Atenciones Operativas', 'EXPORT', 'Generación de reportes institucionales en PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 8. Asignación de Permisos a Roles Institucionales
-- Rol Administrador y Superadmin
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ADMIN_SISTEMA')
  AND p.codigo IN (
      'SERENAZGO_ATENCIONES_VER', 'SERENAZGO_ATENCIONES_CREAR', 'SERENAZGO_ATENCIONES_ASIGNAR',
      'SERENAZGO_ATENCIONES_DESPACHAR', 'SERENAZGO_ATENCIONES_ACEPTAR', 'SERENAZGO_ATENCIONES_INICIAR',
      'SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'SERENAZGO_ATENCIONES_REASIGNAR',
      'SERENAZGO_ATENCIONES_ESCALAR', 'SERENAZGO_ATENCIONES_FINALIZAR',
      'SERENAZGO_ATENCIONES_CANCELAR', 'SERENAZGO_ATENCIONES_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Rol Supervisor Serenazgo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_SUPERVISOR_SERENAZGO', 'ROLE_SERENAZGO_SUPERVISOR')
  AND p.codigo IN (
      'SERENAZGO_ATENCIONES_VER', 'SERENAZGO_ATENCIONES_CREAR', 'SERENAZGO_ATENCIONES_ASIGNAR',
      'SERENAZGO_ATENCIONES_DESPACHAR', 'SERENAZGO_ATENCIONES_ACEPTAR', 'SERENAZGO_ATENCIONES_INICIAR',
      'SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD', 'SERENAZGO_ATENCIONES_REASIGNAR',
      'SERENAZGO_ATENCIONES_ESCALAR', 'SERENAZGO_ATENCIONES_FINALIZAR',
      'SERENAZGO_ATENCIONES_CANCELAR', 'SERENAZGO_ATENCIONES_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- Rol Sereno Operativo (Aceptar, Iniciar, Registrar Novedad, Ver)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_SERENO', 'ROLE_SERENAZGO')
  AND p.codigo IN (
      'SERENAZGO_ATENCIONES_VER', 'SERENAZGO_ATENCIONES_ACEPTAR',
      'SERENAZGO_ATENCIONES_INICIAR', 'SERENAZGO_ATENCIONES_REGISTRAR_NOVEDAD'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
