-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V24: Módulo 2 - Mercados Municipales (Administración Avanzada)
-- Tablas: mercados_documentos, mercados_cambios_estado
-- Permisos: MERCADOS_MERCADO_CAMBIAR_ESTADO, MERCADOS_MERCADO_EXPORTAR,
--           MERCADOS_MERCADO_DOCUMENTOS, MERCADOS_MERCADO_HISTORIAL_VER
-- ==============================================================================

-- 1. Tabla de Documentos Asociados a Mercados Municipales
CREATE TABLE IF NOT EXISTS mercados_documentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE RESTRICT,
    tipo_documento VARCHAR(50) NOT NULL DEFAULT 'ADMINISTRATIVO',
    descripcion VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT chk_mercados_doc_tipo CHECK (tipo_documento IN ('ADMINISTRATIVO', 'PLANO', 'INFORME', 'ACTA', 'OTRO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_documentos_mercado ON mercados_documentos(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_documentos_doc ON mercados_documentos(documento_id);

-- 2. Tabla de Historial de Cambios de Estado de Mercados Municipales
CREATE TABLE IF NOT EXISTS mercados_cambios_estado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30) NOT NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    usuario_responsable VARCHAR(100) NOT NULL,
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mercados_cambio_estado_ant CHECK (estado_anterior IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO')),
    CONSTRAINT chk_mercados_cambio_estado_nue CHECK (estado_nuevo IN ('ACTIVO', 'INACTIVO', 'EN_MANTENIMIENTO', 'SUSPENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_cambios_estado_mercado ON mercados_cambios_estado(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_cambios_estado_fecha ON mercados_cambios_estado(fecha_cambio);

-- 3. Inserción de Nuevos Permisos Específicos para la Administración de Mercados
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_CAMBIAR_ESTADO', 'Cambiar Estado de Mercado Municipal', 'UPDATE',
       'Permite activar, desactivar, suspender o poner en mantenimiento un mercado municipal de Quinua', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_EXPORTAR', 'Exportar Reportes de Mercados Municipales', 'EXPORT',
       'Permite exportar padrones oficiales y reportes de mercados en formatos Excel y PDF', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_DOCUMENTOS', 'Gestionar Documentos de Mercados Municipales', 'MANAGE',
       'Permite asociar, consultar y administrar documentos, planos y actas de mercados municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'MERCADOS_MERCADO_HISTORIAL_VER', 'Ver Historial de Mercados Municipales', 'VIEW',
       'Permite auditar el historial de modificaciones y trazabilidad de estados de mercados municipales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'DESARROLLO_ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

-- 4. Asignación de Permisos a Roles Institucionales
-- Rol Administrador y Superadmin: Todos los permisos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'MERCADOS_MERCADO_CAMBIAR_ESTADO',
      'MERCADOS_MERCADO_EXPORTAR',
      'MERCADOS_MERCADO_DOCUMENTOS',
      'MERCADOS_MERCADO_HISTORIAL_VER'
  )
ON CONFLICT DO NOTHING;

-- Rol Jefe de Oficina
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'MERCADOS_MERCADO_CAMBIAR_ESTADO',
      'MERCADOS_MERCADO_EXPORTAR',
      'MERCADOS_MERCADO_DOCUMENTOS',
      'MERCADOS_MERCADO_HISTORIAL_VER'
  )
ON CONFLICT DO NOTHING;

-- Rol Operador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR'
  AND p.codigo IN (
      'MERCADOS_MERCADO_EXPORTAR',
      'MERCADOS_MERCADO_DOCUMENTOS',
      'MERCADOS_MERCADO_HISTORIAL_VER'
  )
ON CONFLICT DO NOTHING;
