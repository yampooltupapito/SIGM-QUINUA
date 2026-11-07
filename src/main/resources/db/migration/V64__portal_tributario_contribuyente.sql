-- =============================================================================
-- MIGRACIÓN FLYWAY V64: PORTAL TRIBUTARIO DIGITAL DEL CONTRIBUYENTE (PROMPT 63)
-- Sistema Integral de Gestión Municipal - SIGM-QUINUA
-- Municipalidad Distrital de Quinua (Huamanga, Ayacucho)
-- =============================================================================

-- 1. Vinculación entre Cuentas de Usuario y Contribuyentes Tributarios
CREATE TABLE IF NOT EXISTS tributaria_vinculaciones_usuario_contribuyente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
    tipo_vinculacion VARCHAR(40) NOT NULL CHECK (tipo_vinculacion IN ('TITULAR_VERIFICADO', 'REPRESENTANTE_ACREDITADO', 'APODERADO_ACREDITADO', 'OTRA_RELACION_AUTORIZADA')),
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'EN_VERIFICACION', 'OBSERVADA', 'APROBADA', 'RECHAZADA', 'REVOCADA')),
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_verificacion TIMESTAMP WITH TIME ZONE,
    verificado_por BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    documento_sustento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    fecha_vigencia_hasta DATE,
    motivo_observacion TEXT,
    motivo_rechazo_revocacion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_vinculacion_usuario_contribuyente UNIQUE (usuario_id, contribuyente_id)
);

CREATE INDEX IF NOT EXISTS idx_trib_vinc_usuario ON tributaria_vinculaciones_usuario_contribuyente(usuario_id);
CREATE INDEX IF NOT EXISTS idx_trib_vinc_contrib ON tributaria_vinculaciones_usuario_contribuyente(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_vinc_estado ON tributaria_vinculaciones_usuario_contribuyente(estado);
CREATE INDEX IF NOT EXISTS idx_trib_vinc_activo ON tributaria_vinculaciones_usuario_contribuyente(activo);

-- 2. Solicitudes de Vinculación Tributaria Ciudadana
CREATE TABLE IF NOT EXISTS tributaria_solicitudes_vinculacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vinculacion_id UUID NOT NULL REFERENCES tributaria_vinculaciones_usuario_contribuyente(id) ON DELETE CASCADE,
    codigo_solicitud VARCHAR(50) NOT NULL UNIQUE,
    tipo_solicitud VARCHAR(40) NOT NULL CHECK (tipo_solicitud IN ('TITULAR', 'REPRESENTANTE_LEGAL', 'APODERADO', 'SUCESION')),
    sustento TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA' CHECK (estado IN ('REGISTRADA', 'EN_EVALUACION', 'OBSERVADA', 'APROBADA', 'DENEGADA')),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_trib_sol_vinc_id ON tributaria_solicitudes_vinculacion(vinculacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_sol_codigo ON tributaria_solicitudes_vinculacion(codigo_solicitud);
CREATE INDEX IF NOT EXISTS idx_trib_sol_estado ON tributaria_solicitudes_vinculacion(estado);

-- 3. Historial Personal de Accesos y Consultas en el Portal Tributario
CREATE TABLE IF NOT EXISTS tributaria_historial_acceso_portal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
    seccion_accedida VARCHAR(60) NOT NULL,
    ip_origen VARCHAR(60),
    user_agent VARCHAR(255),
    fecha_acceso TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_acc_usuario ON tributaria_historial_acceso_portal(usuario_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_acc_contrib ON tributaria_historial_acceso_portal(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_acc_fecha ON tributaria_historial_acceso_portal(fecha_acceso);

-- 4. Preferencias Ciudadanas de Notificación y Alertas Tributarias
CREATE TABLE IF NOT EXISTS tributaria_preferencias_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE CASCADE,
    notificar_vencimientos BOOLEAN NOT NULL DEFAULT TRUE,
    notificar_pagos_confirmados BOOLEAN NOT NULL DEFAULT TRUE,
    notificar_recibos BOOLEAN NOT NULL DEFAULT TRUE,
    notificar_notificaciones_formales BOOLEAN NOT NULL DEFAULT TRUE,
    correo_notificacion VARCHAR(150),
    telefono_notificacion VARCHAR(30),
    canal_preferido VARCHAR(30) NOT NULL DEFAULT 'PORTAL' CHECK (canal_preferido IN ('PORTAL', 'CORREO', 'AMBOS')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_preferencias_usuario_contribuyente UNIQUE (usuario_id, contribuyente_id)
);

CREATE INDEX IF NOT EXISTS idx_trib_pref_usr_cnt ON tributaria_preferencias_notificacion(usuario_id, contribuyente_id);

-- 5. Inserción de Permisos Institucionales para el Portal Tributario Ciudadano
-- Cumpliendo con el constraint chk_permiso_accion: ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'ASSIGN', 'EXPORT', 'MANAGE')
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_ACCEDER', 'Acceso al Portal Tributario Digital', 'VIEW',
       'Permite al ciudadano autenticado acceder a su panel tributario institucional', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_PERFIL', 'Ver Perfil Tributario Ciudadano', 'VIEW',
       'Permite consultar la información fiscal del contribuyente activo vinculado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_PREDIOS', 'Consultar Mis Predios', 'VIEW',
       'Permite consultar los predios urbanos y rústicos asociados al contribuyente activo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_OBLIGACIONES', 'Consultar Mis Obligaciones Tributarias', 'VIEW',
       'Permite consultar las obligaciones de Impuesto Predial y Arbitrios vigentes e históricas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_CUENTA_CORRIENTE', 'Consultar Mi Cuenta Corriente Tributaria', 'VIEW',
       'Permite consultar el estado de cuenta y movimientos de cargos y abonos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_PAGOS', 'Consultar Mis Pagos Tributarios', 'VIEW',
       'Permite consultar el historial de pagos y aplicaciones tributarias confirmadas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_DESCARGAR_RECIBOS', 'Descargar Mis Recibos y Comprobantes', 'EXPORT',
       'Permite descargar los comprobantes oficiales de pago en formato PDF institucional con QR', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_DEUDAS', 'Consultar Mis Deudas Tributarias', 'VIEW',
       'Permite visualizar deudas pendientes, vencidas y estado administrativo de exigibilidad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_FRACCIONAMIENTOS', 'Consultar Mis Fraccionamientos y Convenios', 'VIEW',
       'Permite revisar convenios de fraccionamiento, cronogramas de cuotas y estado de pagos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_VER_NOTIFICACIONES', 'Consultar Mis Notificaciones Tributarias', 'VIEW',
       'Permite consultar comunicaciones informativas y cédulas de notificación formal recibidas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_DESCARGAR_DOCUMENTOS', 'Descargar Documentos del Portal', 'EXPORT',
       'Permite descargar estados de cuenta informativos y cédulas digitales oficiales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'PORTAL_TRIBUTARIO_SOLICITAR_VINCULACION', 'Solicitar Vinculación Tributaria', 'CREATE',
       'Permite al ciudadano solicitar asociar un contribuyente titular o representado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PORTAL_VALIDAR_VINCULACION', 'Validar y Aprobar Vinculaciones Tributarias', 'APPROVE',
       'Permite al personal de rentas revisar el sustento y aprobar vinculaciones ciudadanas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_PORTAL_REVOCAR_VINCULACION', 'Revocar Vinculaciones Tributarias', 'DELETE',
       'Permite al personal de rentas o administradores dar de baja una representación autorizada', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignación de Permisos a Roles Institucionales
-- Asignar permisos de consulta y solicitud ciudadana a ROLE_CIUDADANO
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_CIUDADANO'
  AND p.codigo LIKE 'PORTAL_TRIBUTARIO_%'
ON CONFLICT DO NOTHING;

-- Asignar todos los permisos a Administradores y Superadministradores
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND (p.codigo LIKE 'PORTAL_TRIBUTARIO_%' OR p.codigo LIKE 'TRIBUTARIA_PORTAL_%')
ON CONFLICT DO NOTHING;

-- Asignar permisos de administración y validación a personal de Tributación
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_TRIBUTACION'
  AND (p.codigo LIKE 'PORTAL_TRIBUTARIO_%' OR p.codigo LIKE 'TRIBUTARIA_PORTAL_%')
ON CONFLICT DO NOTHING;

