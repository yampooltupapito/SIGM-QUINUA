-- ============================================================================
-- SIGM-QUINUA: MIGRACIÓN V28 - ASIGNACIÓN DE PUESTOS A COMERCIANTES, CONTROL DE OCUPACIÓN Y TRAZABILIDAD
-- Módulo 2: Mercados Municipales - Prompt 26
-- ============================================================================

-- 1. Secuencia segura para generación de códigos de asignación de puestos
CREATE SEQUENCE IF NOT EXISTS seq_mercados_codigo_asignacion START WITH 1 INCREMENT BY 1;

-- 2. Actualización de restricciones y nuevos estados en mercados_asignaciones
ALTER TABLE mercados_asignaciones DROP CONSTRAINT IF EXISTS chk_asignacion_estado;
ALTER TABLE mercados_asignaciones ADD CONSTRAINT chk_asignacion_estado 
    CHECK (estado IN ('BORRADOR', 'SOLICITADA', 'EN_EVALUACION', 'OBSERVADA', 'APROBADA', 'RECHAZADA', 'VIGENTE', 'SUSPENDIDA', 'FINALIZADA', 'ANULADA'));

-- 3. Ampliación de columnas en mercados_asignaciones para el flujo administrativo completo
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS codigo_asignacion VARCHAR(30);
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS fecha_solicitud DATE DEFAULT CURRENT_DATE;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS expediente_id BIGINT REFERENCES mpv_expedientes(id) ON DELETE SET NULL;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS motivo TEXT;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS usuario_registro VARCHAR(100);
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS usuario_evaluacion VARCHAR(100);
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS fecha_evaluacion TIMESTAMP WITH TIME ZONE;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS usuario_aprobacion VARCHAR(100);
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS fecha_aprobacion TIMESTAMP WITH TIME ZONE;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS motivo_rechazo TEXT;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS motivo_suspension TEXT;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS motivo_finalizacion TEXT;
ALTER TABLE mercados_asignaciones ADD COLUMN IF NOT EXISTS observaciones_evaluacion TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_codigo_asignacion ON mercados_asignaciones(codigo_asignacion) WHERE codigo_asignacion IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_mercados_asignaciones_expediente ON mercados_asignaciones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mercados_asignaciones_fsolicitud ON mercados_asignaciones(fecha_solicitud);

-- 4. Ampliar mercados_historial_ocupacion para enlazar directamente con la asignación
ALTER TABLE mercados_historial_ocupacion ADD COLUMN IF NOT EXISTS asignacion_id UUID REFERENCES mercados_asignaciones(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_mercados_historial_asignacion ON mercados_historial_ocupacion(asignacion_id);

-- 5. Tabla de Evaluaciones y Observaciones Administrativas de Asignaciones
CREATE TABLE IF NOT EXISTS mercados_asignaciones_evaluaciones (
    id BIGSERIAL PRIMARY KEY,
    asignacion_id UUID NOT NULL REFERENCES mercados_asignaciones(id) ON DELETE CASCADE,
    tipo_evaluacion VARCHAR(50) NOT NULL, -- SOLICITUD, EVALUACION, OBSERVACION, SUBSANACION, APROBACION, RECHAZO
    resultado VARCHAR(50) NOT NULL,
    motivo TEXT NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_evaluador VARCHAR(100) NOT NULL,
    fecha_evaluacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_asignaciones_eval_asig ON mercados_asignaciones_evaluaciones(asignacion_id);
CREATE INDEX IF NOT EXISTS idx_asignaciones_eval_fecha ON mercados_asignaciones_evaluaciones(fecha_evaluacion);

-- 6. Tabla de Devoluciones Físicas y Entrega de Puestos
CREATE TABLE IF NOT EXISTS mercados_asignaciones_devoluciones (
    id BIGSERIAL PRIMARY KEY,
    asignacion_id UUID NOT NULL REFERENCES mercados_asignaciones(id) ON DELETE RESTRICT,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE RESTRICT,
    fecha_devolucion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsable_recepcion VARCHAR(100) NOT NULL,
    estado_fisico_puesto VARCHAR(50) NOT NULL DEFAULT 'BUENO', -- BUENO, REGULAR, MALO, REQUIERE_MANTENIMIENTO
    observaciones TEXT,
    documento_acta_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_devoluciones_asignacion ON mercados_asignaciones_devoluciones(asignacion_id);
CREATE INDEX IF NOT EXISTS idx_devoluciones_puesto ON mercados_asignaciones_devoluciones(puesto_id);

-- 7. Permisos Granulares del Sistema para Asignación y Control de Ocupación
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT 
    m.id, p.codigo, p.nombre, p.accion, p.descripcion, 'ACTIVO'
FROM (
    VALUES
        ('MERCADOS_ASIGNACION_VER', 'Ver Asignaciones de Puestos', 'VIEW', 'Consulta de solicitudes, ocupaciones y padrón de asignaciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_GESTIONAR', 'Gestionar Asignaciones de Puestos', 'MANAGE', 'Control integral del ciclo de vida de asignaciones de puestos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_CREAR', 'Crear Solicitud de Asignación', 'CREATE', 'Registro de solicitudes y borradores de asignación de puestos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_EVALUAR', 'Evaluar Solicitud de Asignación', 'MANAGE', 'Evaluación técnica y documental de solicitudes de asignación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_OBSERVAR', 'Observar Solicitud de Asignación', 'MANAGE', 'Emisión de observaciones administrativas y requerimientos de subsanación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_APROBAR', 'Aprobar Asignación de Puesto', 'MANAGE', 'Aprobación formal de asignación por funcionario competente', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_RECHAZAR', 'Rechazar Asignación de Puesto', 'MANAGE', 'Denegación motivada de solicitud de puesto comercial', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_ACTIVAR', 'Activar Ocupación de Puesto', 'MANAGE', 'Pase a estado vigente y toma de posesión efectiva del puesto', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_SUSPENDER', 'Suspender Asignación de Puesto', 'MANAGE', 'Suspensión temporal motivada de asignación vigente', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_FINALIZAR', 'Finalizar Asignación de Puesto', 'MANAGE', 'Cierre y culminación de asignación comercial', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_DEVOLUCION', 'Registrar Devolución de Puesto', 'MANAGE', 'Recepción física y acta de entrega de puesto comercial', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_HISTORIAL_VER', 'Ver Historial de Ocupación', 'VIEW', 'Consulta cronológica de la línea de tiempo y trazabilidad de ocupación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_ASIGNACION_EXPORTAR', 'Exportar Reportes de Asignación', 'EXPORT', 'Generación de reportes y constancias oficiales en PDF y Excel', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_ASIGNACION_VER',
        'MERCADOS_ASIGNACION_GESTIONAR',
        'MERCADOS_ASIGNACION_CREAR',
        'MERCADOS_ASIGNACION_EVALUAR',
        'MERCADOS_ASIGNACION_OBSERVAR',
        'MERCADOS_ASIGNACION_APROBAR',
        'MERCADOS_ASIGNACION_RECHAZAR',
        'MERCADOS_ASIGNACION_ACTIVAR',
        'MERCADOS_ASIGNACION_SUSPENDER',
        'MERCADOS_ASIGNACION_FINALIZAR',
        'MERCADOS_ASIGNACION_DEVOLUCION',
        'MERCADOS_ASIGNACION_HISTORIAL_VER',
        'MERCADOS_ASIGNACION_EXPORTAR'
    )
) p
WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_ASIGNACION_VER',
        'MERCADOS_ASIGNACION_CREAR',
        'MERCADOS_ASIGNACION_EVALUAR',
        'MERCADOS_ASIGNACION_HISTORIAL_VER',
        'MERCADOS_ASIGNACION_EXPORTAR'
    )
) p
WHERE r.nombre IN ('ROLE_JEFE_OFICINA', 'ROLE_OPERADOR')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
