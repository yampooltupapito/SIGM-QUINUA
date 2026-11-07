-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V35
-- Módulo 2: Mercados Municipales
-- Portal Web de Consulta y Autogestión para Comerciantes
-- Municipalidad Distrital de Quinua - Huamanga - Ayacucho
-- ==============================================================================

-- 1. Tabla de vinculación segura entre Usuario y Comerciante (titularidad y representación)
CREATE TABLE IF NOT EXISTS mercados_vinculos_usuario_comerciante (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    tipo_vinculo VARCHAR(50) NOT NULL, -- 'TITULAR', 'REPRESENTANTE_LEGAL', 'APODERADO'
    estado VARCHAR(50) NOT NULL, -- 'PENDIENTE', 'VERIFICADO', 'RECHAZADO', 'REVOCADO', 'VENCIDO'
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    documento_sustento_id UUID,
    verificado_por_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_verificacion TIMESTAMP WITH TIME ZONE,
    motivo_rechazo_revocacion TEXT,
    facultades_representacion TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vinculo_usuario_comerciante UNIQUE(usuario_id, comerciante_id),
    CONSTRAINT chk_vinculo_tipo CHECK (tipo_vinculo IN ('TITULAR', 'REPRESENTANTE_LEGAL', 'APODERADO')),
    CONSTRAINT chk_vinculo_estado CHECK (estado IN ('PENDIENTE', 'VERIFICADO', 'RECHAZADO', 'REVOCADO', 'VENCIDO'))
);

CREATE INDEX IF NOT EXISTS idx_vinculo_usuario ON mercados_vinculos_usuario_comerciante(usuario_id);
CREATE INDEX IF NOT EXISTS idx_vinculo_comerciante ON mercados_vinculos_usuario_comerciante(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_vinculo_estado ON mercados_vinculos_usuario_comerciante(estado);

-- 2. Tabla de preferencias de notificación para comerciantes
CREATE TABLE IF NOT EXISTS mercados_comerciante_preferencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE CASCADE,
    notif_email_solicitudes BOOLEAN NOT NULL DEFAULT TRUE,
    notif_email_inspecciones BOOLEAN NOT NULL DEFAULT TRUE,
    notif_email_pagos BOOLEAN NOT NULL DEFAULT TRUE,
    notif_email_vigencias BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_comerciante_preferencias UNIQUE(comerciante_id)
);

CREATE INDEX IF NOT EXISTS idx_comerciante_pref ON mercados_comerciante_preferencias(comerciante_id);

-- 3. Tabla de auditoría específica del portal del comerciante
CREATE TABLE IF NOT EXISTS mercados_portal_auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comerciante_id UUID NOT NULL,
    usuario_id BIGINT,
    username VARCHAR(100) NOT NULL,
    tipo_accion VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    detalles_json TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_portal_audit_comerciante ON mercados_portal_auditoria(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_portal_audit_fecha ON mercados_portal_auditoria(created_at);

-- 4. Rol institucional para Comerciantes de Mercado
INSERT INTO roles (codigo, nombre, descripcion, estado)
SELECT 'ROLE_COMERCIANTE', 'Comerciante de Mercado', 'Acceso al portal de autogestión y consulta para comerciantes de mercados municipales', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE codigo = 'ROLE_COMERCIANTE');

-- 5. Catálogo de Permisos RBAC para el Portal del Comerciante
-- Asegurando que 'accion' pertenezca a ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'ASSIGN', 'EXPORT', 'MANAGE')
INSERT INTO permisos (codigo, nombre, accion, descripcion, modulo_id)
SELECT p.codigo, p.nombre, p.accion, p.descripcion, m.id
FROM (
    VALUES
        ('PORTAL_COMERCIANTE_ACCEDER', 'Acceso al Portal del Comerciante', 'VIEW', 'Ingreso al portal de autogestión de mercados', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_PERFIL_VER', 'Ver Perfil Comercial', 'VIEW', 'Consultar datos de empadronamiento y contacto comercial', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_CONTACTO_EDITAR', 'Editar Datos de Contacto', 'UPDATE', 'Actualizar teléfono y correo electrónico de contacto comercial', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_PUESTOS_VER', 'Ver Puestos Asignados', 'VIEW', 'Consultar puestos y características físicas de ocupación propia', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_CONTRATOS_VER', 'Ver Contratos Propios', 'VIEW', 'Consultar contratos formalizados y fechas de vigencia propia', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_AUTORIZACIONES_VER', 'Ver Autorizaciones Comerciales', 'VIEW', 'Consultar licencias y permisos comerciales vigentes propios', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_FINANZAS_VER', 'Ver Obligaciones y Pagos Propios', 'VIEW', 'Consultar liquidaciones de merced conductiva y estados de cuenta propios', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_SOLICITUDES_VER', 'Ver Solicitudes Propias', 'VIEW', 'Consultar expedientes de renovación, cambio de titular y transferencias propias', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_SOLICITUDES_CREAR', 'Registrar Solicitud en Portal', 'CREATE', 'Presentar solicitudes de renovación de ocupación habilitadas', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR', 'Descargar Documentos y Constancias', 'EXPORT', 'Descargar recibos de pago, resoluciones y constancias propias', 'DESARROLLO_ECONOMICO'),
        ('PORTAL_COMERCIANTE_INCIDENCIAS_CREAR', 'Reportar Incidencias de Puesto', 'CREATE', 'Registrar reportes de incidencias relacionadas con sus puestos comerciales', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- 6. Asignación de Permisos a Roles Institucionales
-- Asignar a ROLE_ADMIN y ROLE_SUPERADMIN
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'PORTAL_COMERCIANTE_%'
  AND NOT EXISTS (
      SELECT 1 FROM rol_permisos rp
      WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

-- Asignar a ROLE_COMERCIANTE y ROLE_CIUDADANO
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_COMERCIANTE', 'ROLE_CIUDADANO')
  AND p.codigo LIKE 'PORTAL_COMERCIANTE_%'
  AND NOT EXISTS (
      SELECT 1 FROM rol_permisos rp
      WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
