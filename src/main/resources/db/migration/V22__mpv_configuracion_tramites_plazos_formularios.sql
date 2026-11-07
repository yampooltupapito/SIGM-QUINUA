-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V22 (PROMPT 19)
-- MESA DE PARTES VIRTUAL: CONFIGURACIÓN ADMINISTRATIVA, CATÁLOGO DE TRÁMITES,
-- REQUISITOS, PLAZOS, CALENDARIO, FORMULARIOS DINÁMICOS Y PLANTILLAS
-- ==============================================================================

-- 1. Ampliación de la Tabla de Tipos de Trámite para Soporte TUPA y Ciclo de Aprobación
ALTER TABLE mpv_tipos_tramite
    ADD COLUMN IF NOT EXISTS codigo_tupa VARCHAR(50),
    ADD COLUMN IF NOT EXISTS denominacion_oficial VARCHAR(250),
    ADD COLUMN IF NOT EXISTS base_normativa TEXT,
    ADD COLUMN IF NOT EXISTS categoria VARCHAR(100) DEFAULT 'GENERAL',
    ADD COLUMN IF NOT EXISTS version_actual INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS estado_catalogo VARCHAR(20) NOT NULL DEFAULT 'PUBLICADO',
    ADD COLUMN IF NOT EXISTS requiere_tasa BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS monto_tasa NUMERIC(10, 2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS cuenta_tasa VARCHAR(100),
    ADD COLUMN IF NOT EXISTS permite_presencial BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_vigencia_desde TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS fecha_vigencia_hasta TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS aprobado_por VARCHAR(100),
    ADD COLUMN IF NOT EXISTS fecha_aprobacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS publicado_por VARCHAR(100),
    ADD COLUMN IF NOT EXISTS fecha_publicacion TIMESTAMP WITH TIME ZONE;

-- Actualizar denominación oficial predeterminada en trámites existentes
UPDATE mpv_tipos_tramite 
SET denominacion_oficial = nombre 
WHERE denominacion_oficial IS NULL;

-- 2. Tabla de Versionado Inmutable de Procedimientos / Trámites TUPA
CREATE TABLE IF NOT EXISTS mpv_tramite_versiones (
    id BIGSERIAL PRIMARY KEY,
    tipo_tramite_id BIGINT NOT NULL REFERENCES mpv_tipos_tramite(id) ON DELETE RESTRICT,
    numero_version INT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    codigo_tupa VARCHAR(50),
    denominacion_oficial VARCHAR(250),
    base_normativa TEXT,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    plazo_dias INT NOT NULL DEFAULT 30,
    unidad_plazo VARCHAR(20) NOT NULL DEFAULT 'DIAS_HABILES',
    requiere_tasa BOOLEAN NOT NULL DEFAULT FALSE,
    monto_tasa NUMERIC(10, 2) DEFAULT 0.00,
    cuenta_tasa VARCHAR(100),
    permite_virtual BOOLEAN NOT NULL DEFAULT TRUE,
    permite_presencial BOOLEAN NOT NULL DEFAULT TRUE,
    formulario_id BIGINT REFERENCES mpv_formularios(id) ON DELETE SET NULL,
    formulario_version_id BIGINT REFERENCES mpv_formulario_versiones(id) ON DELETE SET NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PUBLICADO',
    fecha_vigencia_desde TIMESTAMP WITH TIME ZONE,
    fecha_vigencia_hasta TIMESTAMP WITH TIME ZONE,
    motivo_cambio VARCHAR(500),
    aprobado_por VARCHAR(100),
    fecha_aprobacion TIMESTAMP WITH TIME ZONE,
    publicado_por VARCHAR(100),
    fecha_publicacion TIMESTAMP WITH TIME ZONE,
    snapshot_configuracion_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uq_mpv_tramite_version UNIQUE(tipo_tramite_id, numero_version),
    CONSTRAINT chk_mpv_ver_estado CHECK (estado IN ('BORRADOR', 'EN_REVISION', 'APROBADO', 'PUBLICADO', 'SUSPENDIDO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_ver_tramite ON mpv_tramite_versiones(tipo_tramite_id);
CREATE INDEX IF NOT EXISTS idx_mpv_ver_estado ON mpv_tramite_versiones(estado);

-- 3. Tabla de Calendario Administrativo y Días No Laborables (Feriados Nacionales, Regionales y Cívicos de Quinua)
CREATE TABLE IF NOT EXISTS mpv_calendarios_administrativos (
    id BIGSERIAL PRIMARY KEY,
    anio INT NOT NULL,
    fecha DATE NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'FERIADO_NACIONAL',
    es_laborable BOOLEAN NOT NULL DEFAULT FALSE,
    aplica_todos_tramites BOOLEAN NOT NULL DEFAULT TRUE,
    base_normativa VARCHAR(250),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_cal_tipo CHECK (tipo IN ('FERIADO_NACIONAL', 'FERIADO_REGIONAL', 'DIA_NO_LABORABLE_CIVICO', 'DUELO_INSTITUCIONAL', 'EXCEPCION'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_cal_fecha ON mpv_calendarios_administrativos(fecha);
CREATE INDEX IF NOT EXISTS idx_mpv_cal_anio ON mpv_calendarios_administrativos(anio);

-- 4. Sembrado de Días No Laborables Oficiales y Regionales (Especial Quinua / Ayacucho)
INSERT INTO mpv_calendarios_administrativos (anio, fecha, nombre, tipo, es_laborable, aplica_todos_tramites, base_normativa)
VALUES
    (2026, '2026-01-01', 'Año Nuevo', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-04-02', 'Jueves Santo', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-04-03', 'Viernes Santo', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-05-01', 'Día del Trabajo', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-06-07', 'Batalla de Arica y Día de la Bandera', 'FERIADO_NACIONAL', FALSE, TRUE, 'Ley 31788'),
    (2026, '2026-06-29', 'San Pedro y San Pablo', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-07-23', 'Día de la Fuerza Aérea del Perú', 'FERIADO_NACIONAL', FALSE, TRUE, 'Ley 31822'),
    (2026, '2026-07-28', 'Fiestas Patrias - Día de la Independencia', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-07-29', 'Fiestas Patrias - Gran Parada Militar', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-08-06', 'Batalla de Junín', 'FERIADO_NACIONAL', FALSE, TRUE, 'Ley 31530'),
    (2026, '2026-08-30', 'Santa Rosa de Lima', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-10-08', 'Combate de Angamos', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-11-01', 'Día de Todos los Santos', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-12-08', 'Inmaculada Concepción', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713'),
    (2026, '2026-12-09', 'Batalla de Ayacucho - Pampa de Quinua', 'FERIADO_NACIONAL', FALSE, TRUE, 'Ley 31381'),
    (2026, '2026-12-25', 'Navidad del Señor', 'FERIADO_NACIONAL', FALSE, TRUE, 'D.L. 713')
ON CONFLICT (fecha) DO NOTHING;

-- 5. Tabla de Plantillas Documentales Administrativas
CREATE TABLE IF NOT EXISTS mpv_plantillas_documentales (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_documental VARCHAR(50) NOT NULL,
    cuerpo_plantilla TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'PUBLICADO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_plantilla_tipo CHECK (tipo_documental IN (
        'CARGO_RECEPCION', 'OFICIO_DERIVACION', 'NOTIFICACION_OBSERVACION', 'RESOLUCION_FINAL', 'CONSTANCIA_CIERRE'
    )),
    CONSTRAINT chk_mpv_plantilla_estado CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_plantillas_codigo ON mpv_plantillas_documentales(codigo);
CREATE INDEX IF NOT EXISTS idx_mpv_plantillas_tipo ON mpv_plantillas_documentales(tipo_documental);

-- 6. Sembrado de Plantillas Documentales Oficiales Base
INSERT INTO mpv_plantillas_documentales (codigo, nombre, descripcion, tipo_documental, cuerpo_plantilla, version, estado)
VALUES
    ('PLANT-CARGO-001', 'Cargo Oficial de Recepción Virtual', 'Constancia formal de ingreso por mesa de partes virtual', 'CARGO_RECEPCION',
     'MUNICIPALIDAD DISTRITAL DE QUINUA\nCARGO DE RECEPCIÓN N° {{numero_expediente}}\nFecha y Hora: {{fecha}}\nSolicitante: {{solicitante_nombre}}\nDocumento: {{solicitante_doc}}\nAsunto: {{asunto}}\nOficina Asignada: {{oficina}}\nPlazo Legal: {{plazo_dias}} días hábiles.\nCódigo de Seguimiento: {{codigo_seguimiento}}', 1, 'PUBLICADO'),

    ('PLANT-DERIV-001', 'Oficio de Derivación Inter-Oficinas', 'Comunicación interna de traslado de expediente', 'OFICIO_DERIVACION',
     'OFICIO DE TRASLADO N° {{numero_expediente}}-MDQ\nPara: {{oficina_destino}}\nDe: {{oficina_origen}}\nAsunto: Derivación de Expediente {{numero_expediente}}\nMotivo: {{motivo}}\nInstrucción: Proceder con la evaluación técnica según competencia en el plazo de ley.', 1, 'PUBLICADO'),

    ('PLANT-OBS-001', 'Notificación Formal de Observación', 'Requerimiento de subsanación de requisitos conforme al TUO Ley 27444', 'NOTIFICACION_OBSERVACION',
     'NOTIFICACIÓN DE OBSERVACIÓN ADMINISTRATIVA\nExpediente: {{numero_expediente}}\nAdministrado: {{solicitante_nombre}}\nSe comunica que habiéndose revisado los recaudos de su solicitud, se han formulado las siguientes observaciones: {{detalle_observacion}}\nPlazo para subsanar: 2 días hábiles contados a partir del día siguiente de la presente notificación.\nVencimiento: {{fecha_limite}}.', 1, 'PUBLICADO'),

    ('PLANT-RES-001', 'Resolución de Cierre y Conclusión', 'Acto administrativo formal que resuelve la solicitud', 'RESOLUCION_FINAL',
     'RESOLUCIÓN DE GERENCIA / SUBGERENCIA N° {{numero_resolucion}}\nExpediente: {{numero_expediente}}\nVISTO: La solicitud presentada por {{solicitante_nombre}} sobre {{asunto}}.\nCONSIDERANDO: Que conforme a las atribuciones de la Ley Orgánica de Municipalidades N° 27972 y el TUPA institucional...\nSE RESUELVE: Declarar {{resultado_final}} la solicitud.\nRegístrese, comuníquese y archívese.', 1, 'PUBLICADO')
ON CONFLICT (codigo) DO NOTHING;

-- 7. Tabla de Pista Inmutable de Auditoría para Cambios de Configuración
CREATE TABLE IF NOT EXISTS mpv_configuracion_historial (
    id BIGSERIAL PRIMARY KEY,
    entidad_tipo VARCHAR(50) NOT NULL,
    entidad_id BIGINT NOT NULL,
    codigo_referencia VARCHAR(100),
    tipo_cambio VARCHAR(30) NOT NULL,
    version_anterior INT,
    version_nueva INT,
    motivo VARCHAR(500),
    cambios_json TEXT,
    usuario_id BIGINT,
    username VARCHAR(100) NOT NULL,
    ip_origen VARCHAR(45),
    fecha_cambio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mpv_hist_tipo CHECK (entidad_tipo IN (
        'TRAMITE', 'REQUISITO', 'FORMULARIO', 'PLAZO', 'CALENDARIO', 'PLANTILLA', 'NUMERACION'
    )),
    CONSTRAINT chk_mpv_hist_cambio CHECK (tipo_cambio IN (
        'CREACION', 'MODIFICACION', 'REVISION', 'ENVIADO_REVISION', 'APROBACION', 'PUBLICACION', 'SUSPENSION', 'DESACTIVACION', 'VERSIONADO'
    ))
);

CREATE INDEX IF NOT EXISTS idx_mpv_hist_entidad ON mpv_configuracion_historial(entidad_tipo, entidad_id);
CREATE INDEX IF NOT EXISTS idx_mpv_hist_fecha ON mpv_configuracion_historial(fecha_cambio);

-- 8. Creación de Permisos Granulares de Configuración Administrativa
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_CONFIG_VER', 'Ver panel de configuración de Mesa de Partes', 'VIEW', 'Permite el acceso a los catálogos y parámetros de Mesa de Partes Virtual'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_TRAMITE_CREAR', 'Crear trámites en borrador', 'CREATE', 'Permite registrar nuevos tipos de trámite en estado borrador'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_TRAMITE_EDITAR', 'Editar trámites en borrador', 'UPDATE', 'Permite modificar configuraciones de trámites no publicados'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_TRAMITE_REVISAR', 'Enviar o revisar trámites', 'MANAGE', 'Permite enviar trámites a revisión y verificar requisitos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_TRAMITE_APROBAR', 'Aprobar trámites TUPA', 'APPROVE', 'Permite aprobar técnicamente trámites revisados (Separación de Funciones)'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_TRAMITE_PUBLICAR', 'Publicar versiones de trámites', 'APPROVE', 'Permite activar oficialmente una versión de trámite para la ciudadanía'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_TRAMITE_SUSPENDER', 'Suspender o inactivar trámites', 'UPDATE', 'Permite pausar temporalmente o dar de baja procedimientos del catálogo'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_REQUISITO_GESTIONAR', 'Administrar requisitos documentales', 'MANAGE', 'Permite configurar requisitos, formatos y tamaños por trámite'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_FORMULARIO_GESTIONAR', 'Diseñar formularios dinámicos', 'MANAGE', 'Permite agregar y configurar campos en el constructor visual de formularios'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_FORMULARIO_PUBLICAR', 'Publicar versiones de formularios', 'APPROVE', 'Permite publicar versiones inmutables de formularios dinámicos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_PLAZO_GESTIONAR', 'Configurar plazos administrativos', 'MANAGE', 'Permite ajustar plazos legales y unidades de cómputo por trámite'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_CALENDARIO_GESTIONAR', 'Administrar feriados y calendario', 'MANAGE', 'Permite registrar días no laborables y excepciones del calendario oficial'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_NUMERACION_CONFIGURAR', 'Consultar y configurar numeración', 'MANAGE', 'Permite supervisar correlativos y prefijos sin manipulación arbitraria'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_PLANTILLA_GESTIONAR', 'Administrar plantillas documentales', 'MANAGE', 'Permite crear y versionar plantillas de cargos, oficios y resoluciones'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_CONFIG_HISTORIAL_VER', 'Ver historial de cambios de configuración', 'VIEW', 'Permite auditar modificaciones y diferencias entre versiones de configuración'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- 9. Asignación de Permisos a Roles
-- Superadmin y Administrador: Acceso a configuración integral y publicación
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'MESAPARTES_CONFIG_VER',
      'MESAPARTES_TRAMITE_CREAR',
      'MESAPARTES_TRAMITE_EDITAR',
      'MESAPARTES_TRAMITE_REVISAR',
      'MESAPARTES_TRAMITE_APROBAR',
      'MESAPARTES_TRAMITE_PUBLICAR',
      'MESAPARTES_TRAMITE_SUSPENDER',
      'MESAPARTES_REQUISITO_GESTIONAR',
      'MESAPARTES_FORMULARIO_GESTIONAR',
      'MESAPARTES_FORMULARIO_PUBLICAR',
      'MESAPARTES_PLAZO_GESTIONAR',
      'MESAPARTES_CALENDARIO_GESTIONAR',
      'MESAPARTES_NUMERACION_CONFIGURAR',
      'MESAPARTES_PLANTILLA_GESTIONAR',
      'MESAPARTES_CONFIG_HISTORIAL_VER'
  )
ON CONFLICT DO NOTHING;

-- Jefe de Oficina: Crear, editar, revisar, requisitos, formularios
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'MESAPARTES_CONFIG_VER',
      'MESAPARTES_TRAMITE_CREAR',
      'MESAPARTES_TRAMITE_EDITAR',
      'MESAPARTES_TRAMITE_REVISAR',
      'MESAPARTES_REQUISITO_GESTIONAR',
      'MESAPARTES_FORMULARIO_GESTIONAR',
      'MESAPARTES_CONFIG_HISTORIAL_VER'
  )
ON CONFLICT DO NOTHING;
