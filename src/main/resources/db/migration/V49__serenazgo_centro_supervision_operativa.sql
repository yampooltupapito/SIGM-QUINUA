-- V49__serenazgo_centro_supervision_operativa.sql
-- Módulo 3: Seguridad Ciudadana y Serenazgo - Prompt 48
-- Centro de Supervisión, Monitoreo y Control Operativo

-- 1. Secuencia de Seguimientos de Supervisión
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_supervision_seguimientos START WITH 1 INCREMENT BY 1;

-- 2. Tabla Principal: Seguimientos de Supervisión
CREATE TABLE IF NOT EXISTS serenazgo_supervision_seguimientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_registro_origen VARCHAR(40) NOT NULL,
    registro_origen_id UUID NOT NULL,
    registro_origen_codigo VARCHAR(50),
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    oficina_responsable_nombre VARCHAR(150),
    supervisor_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    supervisor_nombre VARCHAR(200) NOT NULL,
    responsable_asignado_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    responsable_asignado_nombre VARCHAR(200),
    tipo_seguimiento VARCHAR(40) NOT NULL,
    descripcion TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',
    prioridad_administrativa VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_compromiso TIMESTAMP WITH TIME ZONE,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    resultado TEXT,
    motivo_cancelacion TEXT,
    oficina_escalamiento VARCHAR(150),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_codigo ON serenazgo_supervision_seguimientos(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_estado ON serenazgo_supervision_seguimientos(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_tipo ON serenazgo_supervision_seguimientos(tipo_registro_origen);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_reg_id ON serenazgo_supervision_seguimientos(registro_origen_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_supervisor ON serenazgo_supervision_seguimientos(supervisor_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_responsable ON serenazgo_supervision_seguimientos(responsable_asignado_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_fecha ON serenazgo_supervision_seguimientos(fecha_creacion);

-- 3. Tabla de Observaciones y Subsanaciones de Supervisión
CREATE TABLE IF NOT EXISTS serenazgo_supervision_observaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seguimiento_id UUID NOT NULL REFERENCES serenazgo_supervision_seguimientos(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    usuario_nombre VARCHAR(200) NOT NULL,
    usuario_rol VARCHAR(100),
    motivo VARCHAR(150) NOT NULL,
    detalle TEXT NOT NULL,
    estado_resultante VARCHAR(30),
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_obs_seg ON serenazgo_supervision_observaciones(seguimiento_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_obs_fecha ON serenazgo_supervision_observaciones(fecha);

-- 4. Tabla de Historial y Trazabilidad de Supervisión
CREATE TABLE IF NOT EXISTS serenazgo_supervision_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seguimiento_id UUID NOT NULL REFERENCES serenazgo_supervision_seguimientos(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    usuario_nombre VARCHAR(200) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    detalle TEXT,
    ip_origen VARCHAR(50),
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_hist_seg ON serenazgo_supervision_historial(seguimiento_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_hist_fecha ON serenazgo_supervision_historial(fecha);

-- 5. Sembrado de Permisos del Centro de Supervisión
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_VER', 'Acceder al centro de supervisión y monitoreo', 'VIEW',
       'Permite consultar la consola operativa de supervisión de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_VER_INDICADORES', 'Consultar indicadores y métricas de supervisión', 'VIEW',
       'Permite consultar el cálculo en tiempo de consulta de los KPIs operativos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_VER_PENDIENTES', 'Consultar bandeja de pendientes de supervisión', 'VIEW',
       'Permite visualizar elementos que requieren atención operativa o administrativa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_CREAR_SEGUIMIENTO', 'Crear seguimientos de supervisión', 'CREATE',
       'Permite abrir expedientes de seguimiento administrativo sobre actuaciones de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_ASIGNAR', 'Asignar responsables a seguimientos', 'UPDATE',
       'Permite derivar o asignar un seguimiento a un usuario o funcionario competente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_OBSERVAR', 'Registrar observaciones en supervisión', 'UPDATE',
       'Permite emitir observaciones técnicas o de procedimiento', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_SUBSANAR', 'Registrar subsanaciones y respuestas', 'UPDATE',
       'Permite al responsable documentar la atención o subsanación de una observación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_REVISAR', 'Revisar respuestas y actuaciones', 'UPDATE',
       'Permite evaluar la conformidad técnica de subsanaciones y actuaciones', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_ESCALAR', 'Escalar actuaciones a otras oficinas', 'UPDATE',
       'Permite remitir el seguimiento a otra unidad orgánica competente', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_RESOLVER', 'Registrar resolución administrativa', 'UPDATE',
       'Permite asentar la resolución fundada de una supervisión operativa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_CERRAR', 'Cerrar y concluir seguimientos', 'UPDATE',
       'Permite el cierre definitivo del seguimiento con resultado documentado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SUPERVISION_EXPORTAR', 'Exportar reportes y datos de supervisión', 'VIEW',
       'Permite descargar reportes consolidados en Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_SUPERADMIN', 'ROLE_ADMIN', 'ROLE_SUPERVISOR_SERENAZGO', 'ROLE_JEFE_OFICINA', 'ROLE_OPERADOR_CENTRAL')
  AND p.codigo LIKE 'SERENAZGO_SUPERVISION_%'
ON CONFLICT DO NOTHING;
