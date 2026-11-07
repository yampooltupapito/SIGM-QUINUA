-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V39: Módulo 3 - Gestión de Unidades Móviles y Equipamiento de Serenazgo
-- ==============================================================================

-- 1. Secuencias Correlativas
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_equipamiento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_mantenimiento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_incidencia_tecnica START WITH 1 INCREMENT BY 1;

-- 2. Actualización de restricciones y columnas en serenazgo_unidades
DO $$
BEGIN
    -- Eliminar constraints antiguos de estado y tipo si existen
    ALTER TABLE serenazgo_unidades DROP CONSTRAINT IF EXISTS chk_serenazgo_unidad_estado;
    ALTER TABLE serenazgo_unidades DROP CONSTRAINT IF EXISTS chk_serenazgo_unidad_tipo;

    -- Agregar nuevo constraint ampliado de estado operativo
    ALTER TABLE serenazgo_unidades ADD CONSTRAINT chk_serenazgo_unidad_estado 
        CHECK (estado_operativo IN ('DISPONIBLE', 'OPERATIVA', 'ASIGNADA', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO', 'INACTIVA', 'RETIRADA'));

    -- Agregar nuevo constraint ampliado de tipo de unidad
    ALTER TABLE serenazgo_unidades ADD CONSTRAINT chk_serenazgo_unidad_tipo 
        CHECK (tipo_unidad IN ('CAMIONETA', 'MOTOCICLETA', 'BICICLETA', 'VEHICULO_APOYO', 'OTRO'));
END $$;

-- Nuevos campos en serenazgo_unidades
ALTER TABLE serenazgo_unidades ADD COLUMN IF NOT EXISTS color VARCHAR(50);
ALTER TABLE serenazgo_unidades ADD COLUMN IF NOT EXISTS numero_motor VARCHAR(100);
ALTER TABLE serenazgo_unidades ADD COLUMN IF NOT EXISTS numero_chasis VARCHAR(100);
ALTER TABLE serenazgo_unidades ADD COLUMN IF NOT EXISTS codigo_patrimonial VARCHAR(100);
ALTER TABLE serenazgo_unidades ADD COLUMN IF NOT EXISTS fecha_incorporacion DATE DEFAULT CURRENT_DATE;

CREATE INDEX IF NOT EXISTS idx_serenazgo_unidades_cod_patrimonial ON serenazgo_unidades(codigo_patrimonial);

-- 3. Actualización de restricciones y columnas en serenazgo_equipamiento
DO $$
BEGIN
    ALTER TABLE serenazgo_equipamiento DROP CONSTRAINT IF EXISTS chk_serenazgo_equipo_estado;
    ALTER TABLE serenazgo_equipamiento ADD CONSTRAINT chk_serenazgo_equipo_estado
        CHECK (estado IN ('DISPONIBLE', 'ASIGNADO', 'EN_MANTENIMIENTO', 'EN_REPARACION', 'DE_BAJA', 'PERDIDO'));
END $$;

ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS marca VARCHAR(100);
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS modelo VARCHAR(100);
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS codigo_patrimonial VARCHAR(100);
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL;
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS es_controlado_por_cantidad BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS stock_total INT NOT NULL DEFAULT 1;
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS stock_disponible INT NOT NULL DEFAULT 1;
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS fecha_incorporacion DATE DEFAULT CURRENT_DATE;
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE serenazgo_equipamiento ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_serenazgo_equipamiento_cod_patrimonial ON serenazgo_equipamiento(codigo_patrimonial);
CREATE INDEX IF NOT EXISTS idx_serenazgo_equipamiento_oficina ON serenazgo_equipamiento(oficina_responsable_id);

-- 4. Historial de Estados de Unidades Móviles
CREATE TABLE IF NOT EXISTS serenazgo_unidades_historial_estados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_id UUID NOT NULL REFERENCES serenazgo_unidades(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    fecha_efectiva TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_unidades_hist_est_unidad ON serenazgo_unidades_historial_estados(unidad_id);

-- 5. Asignaciones de Unidades Móviles a Personal
CREATE TABLE IF NOT EXISTS serenazgo_unidades_asignaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unidad_id UUID NOT NULL REFERENCES serenazgo_unidades(id) ON DELETE CASCADE,
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin_programada TIMESTAMP WITH TIME ZONE,
    fecha_devolucion_real TIMESTAMP WITH TIME ZONE,
    kilometraje_salida NUMERIC(10, 2),
    kilometraje_retorno NUMERIC(10, 2),
    motivo_servicio VARCHAR(255) NOT NULL,
    estado_asignacion VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    estado_inicial_unidad VARCHAR(30),
    estado_retorno_unidad VARCHAR(30),
    observaciones_entrega TEXT,
    observaciones_devolucion TEXT,
    usuario_entrega_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_recepcion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    documento_acta_entrega_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    documento_acta_devolucion_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_asig_uni_estado CHECK (estado_asignacion IN ('ACTIVA', 'DEVUELTA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_uni_unidad ON serenazgo_unidades_asignaciones(unidad_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_uni_personal ON serenazgo_unidades_asignaciones(personal_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_uni_estado ON serenazgo_unidades_asignaciones(estado_asignacion);

-- 6. Asignaciones de Equipamiento
CREATE TABLE IF NOT EXISTS serenazgo_equipamiento_asignaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipamiento_id UUID NOT NULL REFERENCES serenazgo_equipamiento(id) ON DELETE CASCADE,
    personal_id UUID REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    unidad_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    fecha_entrega TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion TIMESTAMP WITH TIME ZONE,
    cantidad INT NOT NULL DEFAULT 1,
    cantidad_devuelta INT NOT NULL DEFAULT 0,
    estado_asignacion VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    estado_bien_entrega VARCHAR(50),
    estado_bien_devolucion VARCHAR(50),
    motivo VARCHAR(255) NOT NULL,
    observaciones TEXT,
    documento_acta_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_entrega_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_recepcion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_asig_eqp_estado CHECK (estado_asignacion IN ('ACTIVA', 'DEVUELTA_TOTAL', 'DEVUELTA_PARCIAL', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_eqp_equipo ON serenazgo_equipamiento_asignaciones(equipamiento_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_eqp_personal ON serenazgo_equipamiento_asignaciones(personal_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_eqp_unidad ON serenazgo_equipamiento_asignaciones(unidad_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_asig_eqp_estado ON serenazgo_equipamiento_asignaciones(estado_asignacion);

-- 7. Mantenimientos Técnicos de Recursos (Unidades y Equipamiento)
CREATE TABLE IF NOT EXISTS serenazgo_mantenimientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_recurso VARCHAR(30) NOT NULL,
    unidad_id UUID REFERENCES serenazgo_unidades(id) ON DELETE CASCADE,
    equipamiento_id UUID REFERENCES serenazgo_equipamiento(id) ON DELETE CASCADE,
    tipo_mantenimiento VARCHAR(40) NOT NULL,
    fecha_programada DATE NOT NULL,
    fecha_realizada DATE,
    kilometraje_mantenimiento NUMERIC(10, 2),
    descripcion TEXT NOT NULL,
    taller_proveedor VARCHAR(200),
    costo NUMERIC(10, 2),
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
    motivo_cancelacion TEXT,
    resultado_tecnico TEXT,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_mnt_recurso CHECK (tipo_recurso IN ('UNIDAD_MOVIL', 'EQUIPAMIENTO')),
    CONSTRAINT chk_serenazgo_mnt_tipo CHECK (tipo_mantenimiento IN ('PREVENTIVO', 'CORRECTIVO', 'INSPECCION_TECNICA', 'OTRO')),
    CONSTRAINT chk_serenazgo_mnt_estado CHECK (estado IN ('PROGRAMADO', 'EN_PROCESO', 'FINALIZADO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_mnt_codigo ON serenazgo_mantenimientos(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_mnt_unidad ON serenazgo_mantenimientos(unidad_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_mnt_equipo ON serenazgo_mantenimientos(equipamiento_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_mnt_estado ON serenazgo_mantenimientos(estado);

-- 8. Incidencias Técnicas / Reportes de Daños o Fallas
CREATE TABLE IF NOT EXISTS serenazgo_incidencias_tecnicas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_recurso VARCHAR(30) NOT NULL,
    unidad_id UUID REFERENCES serenazgo_unidades(id) ON DELETE CASCADE,
    equipamiento_id UUID REFERENCES serenazgo_equipamiento(id) ON DELETE CASCADE,
    tipo_incidencia VARCHAR(50) NOT NULL,
    fecha_reporte TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT NOT NULL,
    personal_reporta_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    nivel_gravedad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA',
    acciones_tomadas TEXT,
    documento_evidencia_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_inc_tec_recurso CHECK (tipo_recurso IN ('UNIDAD_MOVIL', 'EQUIPAMIENTO')),
    CONSTRAINT chk_serenazgo_inc_tec_tipo CHECK (tipo_incidencia IN ('AVERIA', 'DANO', 'PERDIDA_REPORTADA', 'FALLA_FUNCIONAMIENTO', 'OBSERVACION_SEGURIDAD', 'OTRO')),
    CONSTRAINT chk_serenazgo_inc_tec_gravedad CHECK (nivel_gravedad IN ('LEVE', 'MEDIA', 'GRAVE', 'CRITICA')),
    CONSTRAINT chk_serenazgo_inc_tec_estado CHECK (estado IN ('REGISTRADA', 'EN_EVALUACION', 'EN_REPARACION', 'RESUELTA', 'DESESTIMADA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_tec_codigo ON serenazgo_incidencias_tecnicas(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_tec_unidad ON serenazgo_incidencias_tecnicas(unidad_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_tec_equipo ON serenazgo_incidencias_tecnicas(equipamiento_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_tec_estado ON serenazgo_incidencias_tecnicas(estado);

-- 9. Permisos Granulares del Subsistema
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_CREAR', 'Registrar Unidades Móviles', 'CREATE',
       'Permite dar de alta vehículos y unidades móviles de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_EDITAR', 'Editar Unidades Móviles', 'UPDATE',
       'Permite modificar datos técnicos y operativos de unidades móviles', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_CAMBIAR_ESTADO', 'Cambiar Estado de Unidad Móvil', 'UPDATE',
       'Permite registrar transiciones de estado de vehículos de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_ASIGNAR', 'Asignar / Devolver Unidades Móviles', 'ASSIGN',
       'Permite entregar y recibir unidades vehiculares al personal sereno', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_MANTENIMIENTO', 'Gestionar Mantenimiento de Unidades', 'MANAGE',
       'Permite programar, registrar y finalizar mantenimientos técnicos vehiculares', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_INCIDENCIAS', 'Gestionar Incidencias Técnicas', 'MANAGE',
       'Permite registrar y resolver averías y contingencias de recursos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_EQUIPAMIENTO_CREAR', 'Registrar Equipamiento Operativo', 'CREATE',
       'Permite dar de alta radios, linternas, chalecos y equipamiento de seguridad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_EQUIPAMIENTO_EDITAR', 'Editar Equipamiento Operativo', 'UPDATE',
       'Permite modificar datos de bienes y materiales de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_EQUIPAMIENTO_ASIGNAR', 'Asignar / Devolver Equipamiento', 'ASSIGN',
       'Permite entregar y recibir equipamiento a personal o patrullas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_EQUIPAMIENTO_EXPORTAR', 'Exportar Inventario de Equipos y Unidades', 'EXPORT',
       'Permite descargar reportes y listados en Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 10. Asignación a Roles Institucionales
-- Administrador y Superadministrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND (p.codigo LIKE 'SERENAZGO_UNIDADES_%' OR p.codigo LIKE 'SERENAZGO_EQUIPAMIENTO_%')
ON CONFLICT DO NOTHING;

-- Jefe de Oficina y Supervisor
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'SERENAZGO_UNIDADES_VER',
      'SERENAZGO_UNIDADES_ASIGNAR',
      'SERENAZGO_UNIDADES_MANTENIMIENTO',
      'SERENAZGO_UNIDADES_INCIDENCIAS',
      'SERENAZGO_EQUIPAMIENTO_VER',
      'SERENAZGO_EQUIPAMIENTO_ASIGNAR',
      'SERENAZGO_EQUIPAMIENTO_EXPORTAR'
  )
ON CONFLICT DO NOTHING;

-- Sereno Operativo (consulta y reporte de incidencia)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_UNIDADES_VER',
      'SERENAZGO_EQUIPAMIENTO_VER',
      'SERENAZGO_UNIDADES_INCIDENCIAS'
  )
ON CONFLICT DO NOTHING;
