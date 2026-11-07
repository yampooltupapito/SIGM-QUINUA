-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V38: Módulo 3 - Administración Integral del Personal de Serenazgo
-- ==============================================================================

-- 1. Historial de Cargos Operativos del Personal
CREATE TABLE IF NOT EXISTS serenazgo_personal_historial_cargos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE CASCADE,
    cargo_anterior_id UUID REFERENCES serenazgo_cargos_operativos(id) ON DELETE SET NULL,
    cargo_nuevo_id UUID NOT NULL REFERENCES serenazgo_cargos_operativos(id) ON DELETE RESTRICT,
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    motivo TEXT NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_hist_cargo_personal ON serenazgo_personal_historial_cargos(personal_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_hist_cargo_nuevo ON serenazgo_personal_historial_cargos(cargo_nuevo_id);

-- 2. Historial de Estados del Personal (Altas, Bajas, Suspensiones, Retiros)
CREATE TABLE IF NOT EXISTS serenazgo_personal_historial_estados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    fecha_efectiva DATE NOT NULL DEFAULT CURRENT_DATE,
    motivo TEXT NOT NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_hist_est_ant CHECK (estado_anterior IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'RETIRADO')),
    CONSTRAINT chk_serenazgo_hist_est_nue CHECK (estado_nuevo IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'RETIRADO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_hist_estado_personal ON serenazgo_personal_historial_estados(personal_id);

-- 3. Documentación Digital y Legajo del Personal
CREATE TABLE IF NOT EXISTS serenazgo_personal_documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(100),
    descripcion VARCHAR(255),
    fecha_emision DATE,
    fecha_vencimiento DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'VIGENTE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_doc_estado CHECK (estado IN ('VIGENTE', 'VENCIDO', 'ANULADO')),
    CONSTRAINT chk_serenazgo_doc_tipo CHECK (tipo_documento IN ('CONTRATO', 'CONSTANCIA_CAPACITACION', 'DESIGNACION', 'CERTIFICADO_MEDICO', 'RECORD_CONDUCTOR', 'ANTECEDENTES', 'OTRO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_personal_doc_personal ON serenazgo_personal_documentos(personal_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_personal_doc_doc ON serenazgo_personal_documentos(documento_id);

-- 4. Permisos Granulares para Gestión de Personal de Serenazgo
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_CREAR', 'Registrar Personal de Serenazgo', 'CREATE',
       'Permite registrar e incorporar nuevos efectivos al cuerpo de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_EDITAR', 'Editar Datos de Personal de Serenazgo', 'UPDATE',
       'Permite actualizar la ficha laboral y datos de serenos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_ESTADO_CAMBIAR', 'Cambiar Estado de Personal', 'UPDATE',
       'Permite suspender, retirar o reactivar efectivos de serenazgo con motivo fundado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_CARGO_CAMBIAR', 'Asignar / Cambiar Cargo Operativo', 'ASSIGN',
       'Permite reasignar o promover de cargo a un efectivo de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_DOCUMENTOS_VER', 'Ver Documentos de Legajo de Personal', 'VIEW',
       'Permite consultar contratos y certificados del personal de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_DOCUMENTOS_GESTIONAR', 'Gestionar Documentos de Legajo', 'MANAGE',
       'Permite adjuntar, desvincular o anular documentos de legajo de personal', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_HISTORIAL_VER', 'Ver Historial de Cargos y Estados', 'VIEW',
       'Permite auditar la línea de tiempo laboral del personal de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_FICHA_EXPORTAR', 'Exportar Ficha Informativa en PDF', 'EXPORT',
       'Permite generar y descargar la ficha técnica en PDF institucional', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_ESTADISTICAS_VER', 'Ver Estadísticas de Personal', 'VIEW',
       'Permite visualizar métricas de dotación y distribución de serenos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 5. Asignación a Roles Institucionales
-- Administrador y Superadministrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'SERENAZGO_PERSONAL_%'
ON CONFLICT DO NOTHING;

-- Jefe de Oficina
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'SERENAZGO_PERSONAL_VER',
      'SERENAZGO_PERSONAL_DOCUMENTOS_VER',
      'SERENAZGO_PERSONAL_HISTORIAL_VER',
      'SERENAZGO_PERSONAL_FICHA_EXPORTAR',
      'SERENAZGO_PERSONAL_ESTADISTICAS_VER'
  )
ON CONFLICT DO NOTHING;
