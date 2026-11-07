-- ==============================================================================
-- SIGM-QUINUA | Módulo 3: Seguridad Ciudadana y Serenazgo
-- V42: Registro y Administración de Incidencias de Seguridad Ciudadana
-- ==============================================================================

-- 1. Secuencia para correlativo institucional de incidencias (INC-YYYY-00000)
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_incidencia START WITH 1 INCREMENT BY 1;

-- 2. Enriquecimiento de la tabla central de incidencias
ALTER TABLE serenazgo_incidencias
    ADD COLUMN IF NOT EXISTS titulo VARCHAR(200) NOT NULL DEFAULT 'Incidencia de Seguridad Ciudadana',
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS programacion_servicio_id UUID REFERENCES serenazgo_programacion_servicios(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS personal_reporta_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS origen_hecho VARCHAR(40) NOT NULL DEFAULT 'HECHO_REPORTADO',
    ADD COLUMN IF NOT EXISTS nivel_confidencialidad VARCHAR(30) NOT NULL DEFAULT 'ORDINARIO',
    ADD COLUMN IF NOT EXISTS reportante_anonimo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS reportante_nombre VARCHAR(150),
    ADD COLUMN IF NOT EXISTS reportante_telefono VARCHAR(30),
    ADD COLUMN IF NOT EXISTS reportante_direccion VARCHAR(200),
    ADD COLUMN IF NOT EXISTS motivo_cierre TEXT,
    ADD COLUMN IF NOT EXISTS motivo_descarte TEXT,
    ADD COLUMN IF NOT EXISTS observaciones_internas TEXT,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Ajustar restricciones CHECK en caso de requerir nuevos estados u orígenes
ALTER TABLE serenazgo_incidencias
    DROP CONSTRAINT IF EXISTS chk_serenazgo_inc_estado;

ALTER TABLE serenazgo_incidencias
    ADD CONSTRAINT chk_serenazgo_inc_estado
    CHECK (estado IN ('REGISTRADA', 'EN_EVALUACION', 'ASIGNADA', 'EN_ATENCION', 'RESUELTA', 'CERRADA', 'DESCARTADA'));

-- Índices de consulta frecuente para incidencias
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_fecha_ocurrencia ON serenazgo_incidencias(fecha_ocurrencia);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_oficina ON serenazgo_incidencias(oficina_responsable_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_confidencialidad ON serenazgo_incidencias(nivel_confidencialidad);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_programacion ON serenazgo_incidencias(programacion_servicio_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_personal_rep ON serenazgo_incidencias(personal_reporta_id);

-- 3. Bitácora inmutable de eventos e historial de incidencias
CREATE TABLE IF NOT EXISTS serenazgo_incidencia_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incidencia_id UUID NOT NULL REFERENCES serenazgo_incidencias(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    prioridad_anterior VARCHAR(30),
    prioridad_nueva VARCHAR(30),
    motivo TEXT NOT NULL,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    detalles_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inc_hist_incidencia ON serenazgo_incidencia_historial(incidencia_id, created_at);

-- 4. Documentos adjuntos y registros fotográficos vinculados
CREATE TABLE IF NOT EXISTS serenazgo_incidencia_documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incidencia_id UUID NOT NULL REFERENCES serenazgo_incidencias(id) ON DELETE CASCADE,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    hash_sha256 VARCHAR(64),
    descripcion VARCHAR(255),
    es_fotografia BOOLEAN NOT NULL DEFAULT FALSE,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inc_doc_incidencia ON serenazgo_incidencia_documentos(incidencia_id);

-- 5. Seguimiento y notas internas administrativas
CREATE TABLE IF NOT EXISTS serenazgo_incidencia_comentarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incidencia_id UUID NOT NULL REFERENCES serenazgo_incidencias(id) ON DELETE CASCADE,
    comentario TEXT NOT NULL,
    es_interno BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inc_com_incidencia ON serenazgo_incidencia_comentarios(incidencia_id, created_at);

-- 6. Sembrado de permisos institucionales RBAC para el ciclo de incidencias
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_CREAR', 'Registrar Incidencia de Seguridad', 'CREATE',
       'Permite registrar nuevos hechos u ocurrencias reportadas en seguridad ciudadana', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_EDITAR', 'Editar Información de Incidencia', 'UPDATE',
       'Permite modificar datos informativos, ubicación y reportante de una incidencia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_CLASIFICAR', 'Clasificar Tipo de Incidencia', 'UPDATE',
       'Permite asignar o actualizar la tipología de seguridad de la incidencia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_CAMBIAR_ESTADO', 'Cambiar Estado de Incidencia', 'UPDATE',
       'Permite transicionar el ciclo de vida de la incidencia (evaluación, resolución, etc.)', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_PRIORIZAR', 'Modificar Prioridad de Incidencia', 'UPDATE',
       'Permite reclasificar la prioridad operativa (Baja, Media, Alta, Crítica)', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_CERRAR', 'Cierre Administrativo de Incidencias', 'MANAGE',
       'Permite concluir el seguimiento administrativo y archivar formalmente la incidencia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_ADJUNTAR', 'Adjuntar Documentos a Incidencia', 'CREATE',
       'Permite subir informes, actas o fotografías vinculadas al evento', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_EXPORTAR', 'Exportar Fichas y Reportes de Incidencias', 'EXPORT',
       'Permite generar reportes en PDF y planillas Excel de incidencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- Asignar nuevos permisos al rol SUPERADMIN, ADMIN_SISTEMA y SERENAZGO_SUPERVISOR
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre IN ('ROLE_SUPERADMIN', 'ROLE_ADMIN', 'ROLE_SERENAZGO_SUPERVISOR', 'ROLE_SUPERVISOR_SERENAZGO')
  AND p.codigo IN (
      'SERENAZGO_INCIDENCIAS_CREAR',
      'SERENAZGO_INCIDENCIAS_EDITAR',
      'SERENAZGO_INCIDENCIAS_CLASIFICAR',
      'SERENAZGO_INCIDENCIAS_CAMBIAR_ESTADO',
      'SERENAZGO_INCIDENCIAS_PRIORIZAR',
      'SERENAZGO_INCIDENCIAS_CERRAR',
      'SERENAZGO_INCIDENCIAS_ADJUNTAR',
      'SERENAZGO_INCIDENCIAS_EXPORTAR'
  )
ON CONFLICT (rol_id, permiso_id) DO NOTHING;
