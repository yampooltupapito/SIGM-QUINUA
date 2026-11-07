-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V15: Módulo 1 - Mesa de Partes Virtual (Arquitectura, Entidades y Flujos)
-- Tablas: mpv_tipos_tramite, mpv_correlativos, mpv_expedientes,
--         mpv_expediente_historial, mpv_expediente_documentos,
--         mpv_derivaciones, mpv_observaciones, mpv_subsanaciones
-- ==============================================================================

-- 1. Catálogo de Tipos de Trámites Institucionales
CREATE TABLE IF NOT EXISTS mpv_tipos_tramite (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    requiere_documentos BOOLEAN NOT NULL DEFAULT TRUE,
    permite_presentacion_virtual BOOLEAN NOT NULL DEFAULT TRUE,
    plazo_dias INT NOT NULL DEFAULT 30,
    unidad_plazo VARCHAR(20) NOT NULL DEFAULT 'DIAS_HABILES',
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_tipo_tramite_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_mpv_tipo_tramite_unidad CHECK (unidad_plazo IN ('DIAS_HABILES', 'DIAS_CALENDARIO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_tipos_tramite_estado ON mpv_tipos_tramite(estado);
CREATE INDEX IF NOT EXISTS idx_mpv_tipos_tramite_oficina ON mpv_tipos_tramite(oficina_responsable_id);

-- 2. Tabla de Control Concurrente de Correlativos (Numeración Atómica y Segura)
CREATE TABLE IF NOT EXISTS mpv_correlativos (
    id BIGSERIAL PRIMARY KEY,
    prefijo VARCHAR(20) NOT NULL DEFAULT 'MPV',
    anio INT NOT NULL,
    ultimo_numero INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mpv_correlativo UNIQUE (prefijo, anio)
);

-- 3. Entidad Central de Expedientes Administrativos
CREATE TABLE IF NOT EXISTS mpv_expedientes (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    numero_expediente VARCHAR(50) UNIQUE,
    anio INT NOT NULL,
    asunto VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo_tramite_id BIGINT NOT NULL REFERENCES mpv_tipos_tramite(id) ON DELETE RESTRICT,
    solicitante_id BIGINT NOT NULL REFERENCES personas(id) ON DELETE RESTRICT,
    oficina_origen_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE RESTRICT,
    estado_actual VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    fecha_presentacion TIMESTAMP WITH TIME ZONE,
    fecha_recepcion TIMESTAMP WITH TIME ZONE,
    fecha_limite TIMESTAMP WITH TIME ZONE,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    canal_ingreso VARCHAR(20) NOT NULL DEFAULT 'VIRTUAL',
    prioridad VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    confidencialidad VARCHAR(20) NOT NULL DEFAULT 'INTERNO',
    usuario_registro_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_exp_estado CHECK (estado_actual IN (
        'BORRADOR', 'PRESENTADO', 'RECIBIDO', 'EN_REVISION', 'OBSERVADO',
        'EN_SUBSANACION', 'DERIVADO', 'EN_ATENCION', 'RESUELTO', 'NOTIFICADO',
        'ARCHIVADO', 'ANULADO'
    )),
    CONSTRAINT chk_mpv_exp_canal CHECK (canal_ingreso IN ('VIRTUAL', 'PRESENCIAL')),
    CONSTRAINT chk_mpv_exp_prioridad CHECK (prioridad IN ('NORMAL', 'ALTA', 'URGENTE')),
    CONSTRAINT chk_mpv_exp_confidencialidad CHECK (confidencialidad IN ('PUBLICO', 'INTERNO', 'CONFIDENCIAL', 'RESTRINGIDO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_exp_numero ON mpv_expedientes(numero_expediente);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_anio ON mpv_expedientes(anio);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_estado ON mpv_expedientes(estado_actual);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_solicitante ON mpv_expedientes(solicitante_id);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_oficina_resp ON mpv_expedientes(oficina_responsable_id);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_fecha_pres ON mpv_expedientes(fecha_presentacion);

-- 4. Historial Inmutable de Transiciones y Movimientos del Expediente
CREATE TABLE IF NOT EXISTS mpv_expediente_historial (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    observacion VARCHAR(1000),
    usuario_responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    fecha_movimiento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    identificador_correlacion VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_mpv_hist_exp ON mpv_expediente_historial(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_hist_fecha ON mpv_expediente_historial(fecha_movimiento);

-- 5. Relación de Documentos y Anexos Vinculados al Expediente
CREATE TABLE IF NOT EXISTS mpv_expediente_documentos (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE RESTRICT,
    tipo_relacion VARCHAR(50) NOT NULL DEFAULT 'ANEXO',
    fecha_asociacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_asocia_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    orden INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_mpv_exp_doc UNIQUE (expediente_id, documento_id),
    CONSTRAINT chk_mpv_doc_relacion CHECK (tipo_relacion IN (
        'PRINCIPAL', 'ANEXO', 'SUBSANACION', 'INFORME', 'OFICIO', 'RESOLUCION', 'CONSTANCIA'
    )),
    CONSTRAINT chk_mpv_doc_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_exp_docs_exp ON mpv_expediente_documentos(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_docs_doc ON mpv_expediente_documentos(documento_id);

-- 6. Derivaciones entre Unidades Orgánicas / Oficinas
CREATE TABLE IF NOT EXISTS mpv_derivaciones (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    oficina_origen_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    oficina_destino_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    usuario_derivador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    motivo VARCHAR(1000) NOT NULL,
    fecha_derivacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_recepcion TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    observaciones VARCHAR(1000),
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mpv_derivacion_estado CHECK (estado IN ('PENDIENTE', 'RECIBIDA', 'RECHAZADA', 'FINALIZADA'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_deriv_exp ON mpv_derivaciones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_deriv_destino ON mpv_derivaciones(oficina_destino_id);
CREATE INDEX IF NOT EXISTS idx_mpv_deriv_estado ON mpv_derivaciones(estado);

-- 7. Observaciones Administrativas al Administrado
CREATE TABLE IF NOT EXISTS mpv_observaciones (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    descripcion TEXT NOT NULL,
    usuario_observador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_notificacion TIMESTAMP WITH TIME ZONE,
    fecha_limite TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_respuesta TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_mpv_obs_estado CHECK (estado IN ('PENDIENTE', 'SUBSANADA', 'VENCIDA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_obs_exp ON mpv_observaciones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_obs_estado ON mpv_observaciones(estado);

-- 8. Subsanaciones Presentadas por el Administrado
CREATE TABLE IF NOT EXISTS mpv_subsanaciones (
    id BIGSERIAL PRIMARY KEY,
    observacion_id BIGINT NOT NULL REFERENCES mpv_observaciones(id) ON DELETE CASCADE,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    respuesta_ciudadano TEXT NOT NULL,
    fecha_subsanacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_subsanador_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PRESENTADA',
    CONSTRAINT chk_mpv_subsanacion_estado CHECK (estado IN ('PRESENTADA', 'ACEPTADA', 'RECHAZADA'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_subs_obs ON mpv_subsanaciones(observacion_id);
CREATE INDEX IF NOT EXISTS idx_mpv_subs_exp ON mpv_subsanaciones(expediente_id);

-- 9. Registro de Permisos de Mesa de Partes Virtual
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_CREAR', 'Crear borrador y presentar expedientes', 'CREATE', 'Permite crear y presentar expedientes administrativos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_VER_PROPIO', 'Ver expedientes propios del administrado', 'VIEW', 'Permite a los administrados consultar sus propios expedientes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_VER_OFICINA', 'Ver expedientes de la unidad orgánica propia', 'VIEW', 'Permite a los servidores consultar expedientes asignados a su oficina'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_VER_TODOS', 'Ver todos los expedientes institucionales', 'VIEW', 'Permite a operadores de mesa de partes y administradores consultar cualquier expediente'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_RECIBIR', 'Recibir expedientes presentados y verificar requisitos', 'UPDATE', 'Permite a operadores recibir y admitir a trámite los expedientes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_DERIVAR', 'Derivar expedientes entre dependencias municipales', 'ASSIGN', 'Permite transferir y canalizar la atención de expedientes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_OBSERVAR', 'Observar expedientes y requerir subsanaciones', 'UPDATE', 'Permite formular observaciones formales al administrado'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_SUBSANAR', 'Subsanar observaciones de expedientes', 'UPDATE', 'Permite responder a observaciones y adjuntar descargos o documentos subsanatorios'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_RESOLVER', 'Emitir acto resolutivo o fin de trámite', 'APPROVE', 'Permite resolver y concluir formalmente expedientes asignados'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_ARCHIVAR', 'Archivar expedientes concluidos o depurados', 'MANAGE', 'Permite archivar expedientes concluidos conforme a conservación'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_CONFIGURAR_TRAMITES', 'Parametrizar catálogo de tipos de trámites', 'MANAGE', 'Permite configurar requisitos, plazos y oficinas de los trámites'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ADMIN', 'Administración integral de Mesa de Partes Virtual', 'MANAGE', 'Acceso pleno a la supervisión, auditoría y gobernanza de expedientes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de Permisos a Roles Institucionales
-- 1) Administradores
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'MESAPARTES_%'
ON CONFLICT DO NOTHING;

-- 2) Operadores de Mesa de Partes
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_MESA_PARTES', 'ROLE_OPERADOR')
  AND p.codigo IN (
      'MESAPARTES_EXPEDIENTE_CREAR',
      'MESAPARTES_EXPEDIENTE_VER_OFICINA',
      'MESAPARTES_EXPEDIENTE_VER_TODOS',
      'MESAPARTES_EXPEDIENTE_RECIBIR',
      'MESAPARTES_EXPEDIENTE_DERIVAR',
      'MESAPARTES_EXPEDIENTE_OBSERVAR'
  )
ON CONFLICT DO NOTHING;

-- 3) Jefes de Oficina
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'MESAPARTES_EXPEDIENTE_VER_OFICINA',
      'MESAPARTES_EXPEDIENTE_RECIBIR',
      'MESAPARTES_EXPEDIENTE_DERIVAR',
      'MESAPARTES_EXPEDIENTE_OBSERVAR',
      'MESAPARTES_EXPEDIENTE_RESOLVER',
      'MESAPARTES_EXPEDIENTE_ARCHIVAR'
  )
ON CONFLICT DO NOTHING;

-- 4) Ciudadanos / Administrados
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_CIUDADANO'
  AND p.codigo IN (
      'MESAPARTES_EXPEDIENTE_CREAR',
      'MESAPARTES_EXPEDIENTE_VER_PROPIO',
      'MESAPARTES_EXPEDIENTE_SUBSANAR'
  )
ON CONFLICT DO NOTHING;

-- 10. Poblado Inicial del Catálogo de Trámites Basado en Unidades Orgánicas de Quinua
INSERT INTO mpv_tipos_tramite (codigo, nombre, descripcion, oficina_responsable_id, requiere_documentos, permite_presentacion_virtual, plazo_dias, unidad_plazo, estado)
SELECT 'TRAM-GEN', 'Solicitud General / Trámite Común', 'Solicitudes ordinarias dirigidas a la alcaldía o secretaría general', o.id, true, true, 30, 'DIAS_HABILES', 'ACTIVO'
FROM oficinas o WHERE o.codigo = 'SEC-GEN'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_tipos_tramite (codigo, nombre, descripcion, oficina_responsable_id, requiere_documentos, permite_presentacion_virtual, plazo_dias, unidad_plazo, estado)
SELECT 'TRAM-POS', 'Constancia de Posesión de Predio', 'Emisión de constancia de posesión pacífica y continua para servicios básicos', o.id, true, true, 15, 'DIAS_HABILES', 'ACTIVO'
FROM oficinas o WHERE o.codigo = 'SG-CATASTRO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_tipos_tramite (codigo, nombre, descripcion, oficina_responsable_id, requiere_documentos, permite_presentacion_virtual, plazo_dias, unidad_plazo, estado)
SELECT 'TRAM-LIC', 'Licencia de Funcionamiento Comercial', 'Autorización municipal para actividades económicas, comerciales y artesanales en Quinua', o.id, true, true, 10, 'DIAS_HABILES', 'ACTIVO'
FROM oficinas o WHERE o.codigo = 'SG-DES-ECONOMICO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_tipos_tramite (codigo, nombre, descripcion, oficina_responsable_id, requiere_documentos, permite_presentacion_virtual, plazo_dias, unidad_plazo, estado)
SELECT 'TRAM-AIP', 'Acceso a la Información Pública (Ley N° 27806)', 'Petición formal de documentos y datos públicos de la entidad municipal', o.id, false, true, 10, 'DIAS_HABILES', 'ACTIVO'
FROM oficinas o WHERE o.codigo = 'SEC-GEN'
ON CONFLICT (codigo) DO NOTHING;
