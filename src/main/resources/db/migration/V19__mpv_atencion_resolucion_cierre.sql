-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V19 - ATENCIÓN, RESOLUCIÓN, NOTIFICACIÓN Y CIERRE DE EXPEDIENTES
-- ==============================================================================

-- 1. Ampliación de Columnas en Expedientes
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS fecha_inicio_atencion TIMESTAMP WITH TIME ZONE;
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS usuario_atencion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL;
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS resultado_final VARCHAR(40);
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS ubicacion_archivo VARCHAR(200);
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS es_reabierto BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mpv_expedientes ADD COLUMN IF NOT EXISTS motivo_reapertura TEXT;

CREATE INDEX IF NOT EXISTS idx_mpv_exp_usuario_atencion ON mpv_expedientes(usuario_atencion_id);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_resultado_final ON mpv_expedientes(resultado_final);

-- 2. Evaluaciones Técnicas y Administrativas Internas
CREATE TABLE IF NOT EXISTS mpv_evaluaciones (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    oficina_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    usuario_evaluador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    tipo_evaluacion VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    conclusiones TEXT NOT NULL,
    recomendacion TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    fecha_evaluacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_eval_tipo CHECK (tipo_evaluacion IN ('TECNICA', 'LEGAL', 'ADMINISTRATIVA', 'PRESUPUESTAL', 'AMBIENTAL', 'INSPECCION', 'OTRO')),
    CONSTRAINT chk_mpv_eval_estado CHECK (estado IN ('BORRADOR', 'FINALIZADA', 'ANULADA_CON_TRAZABILIDAD'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_eval_exp ON mpv_evaluaciones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_eval_usuario ON mpv_evaluaciones(usuario_evaluador_id);
CREATE INDEX IF NOT EXISTS idx_mpv_eval_oficina ON mpv_evaluaciones(oficina_id);

-- 3. Documentos Asociados a Evaluaciones
CREATE TABLE IF NOT EXISTS mpv_evaluacion_documentos (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id BIGINT NOT NULL REFERENCES mpv_evaluaciones(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE RESTRICT,
    tipo_documento VARCHAR(50) DEFAULT 'INFORME',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mpv_eval_doc_eval ON mpv_evaluacion_documentos(evaluacion_id);
CREATE INDEX IF NOT EXISTS idx_mpv_eval_doc_doc ON mpv_evaluacion_documentos(documento_id);

-- 4. Propuestas de Decisión
CREATE TABLE IF NOT EXISTS mpv_propuestas_decision (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    tipo_decision VARCHAR(50) NOT NULL,
    resultado_propuesto VARCHAR(40) NOT NULL,
    fundamento TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    usuario_elaborador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio_revision TIMESTAMP WITH TIME ZONE,
    fecha_resolucion_revision TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_prop_tipo CHECK (tipo_decision IN ('RESOLUCION_SUBGERENCIAL', 'RESOLUCION_GERENCIAL', 'RESOLUCION_ALCALDIA', 'OFICIO_RESPUESTA', 'PROVEIDO_FINAL', 'CONSTANCIA', 'DECRETO_ALCALDIA', 'OTRO')),
    CONSTRAINT chk_mpv_prop_resultado CHECK (resultado_propuesto IN ('FAVORABLE', 'DESFAVORABLE', 'PARCIALMENTE_FAVORABLE', 'IMPROCEDENTE', 'INADMISIBLE', 'DESISTIMIENTO', 'OTRO')),
    CONSTRAINT chk_mpv_prop_estado CHECK (estado IN ('BORRADOR', 'EN_REVISION', 'OBSERVADA', 'APROBADA_INTERNAMENTE', 'RECHAZADA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_prop_exp ON mpv_propuestas_decision(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_prop_usuario ON mpv_propuestas_decision(usuario_elaborador_id);
CREATE INDEX IF NOT EXISTS idx_mpv_prop_oficina ON mpv_propuestas_decision(oficina_id);
CREATE INDEX IF NOT EXISTS idx_mpv_prop_estado ON mpv_propuestas_decision(estado);

-- 5. Revisiones Internas de Propuestas de Decisión
CREATE TABLE IF NOT EXISTS mpv_revision_propuestas (
    id BIGSERIAL PRIMARY KEY,
    propuesta_id BIGINT NOT NULL REFERENCES mpv_propuestas_decision(id) ON DELETE CASCADE,
    usuario_revisor_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    accion VARCHAR(30) NOT NULL,
    observaciones TEXT NOT NULL,
    fecha_revision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mpv_rev_accion CHECK (accion IN ('APROBADA_INTERNAMENTE', 'DEVUELTA_CON_OBSERVACIONES', 'RECHAZADA'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_rev_prop ON mpv_revision_propuestas(propuesta_id);
CREATE INDEX IF NOT EXISTS idx_mpv_rev_revisor ON mpv_revision_propuestas(usuario_revisor_id);

-- 6. Correlativos de Numeración Documental Independientes
CREATE TABLE IF NOT EXISTS mpv_correlativos_documentales (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(50) NOT NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE RESTRICT,
    anio INTEGER NOT NULL,
    ultimo_numero BIGINT NOT NULL DEFAULT 0,
    formato VARCHAR(100) NOT NULL DEFAULT '{NUM}-{ANIO}-MDQ/{OFICINA}',
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_mpv_correlativo_doc UNIQUE (tipo_documento, oficina_id, anio)
);

CREATE INDEX IF NOT EXISTS idx_mpv_correlativo_busq ON mpv_correlativos_documentales(tipo_documento, oficina_id, anio);

-- 7. Decisiones Administrativas Formales Emitidas
CREATE TABLE IF NOT EXISTS mpv_decisiones (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    propuesta_id BIGINT REFERENCES mpv_propuestas_decision(id) ON DELETE SET NULL,
    tipo_decision VARCHAR(50) NOT NULL,
    resultado VARCHAR(40) NOT NULL,
    fundamento TEXT NOT NULL,
    autoridad_competente_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_emisora_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    cargo_autoridad VARCHAR(150),
    fecha_decision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_documento VARCHAR(100),
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    estado_emision VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    evidencia_formalizacion TEXT,
    fecha_emision TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_dec_tipo CHECK (tipo_decision IN ('RESOLUCION_SUBGERENCIAL', 'RESOLUCION_GERENCIAL', 'RESOLUCION_ALCALDIA', 'OFICIO_RESPUESTA', 'PROVEIDO_FINAL', 'CONSTANCIA', 'DECRETO_ALCALDIA', 'OTRO')),
    CONSTRAINT chk_mpv_dec_resultado CHECK (resultado IN ('FAVORABLE', 'DESFAVORABLE', 'PARCIALMENTE_FAVORABLE', 'IMPROCEDENTE', 'INADMISIBLE', 'DESISTIMIENTO', 'OTRO')),
    CONSTRAINT chk_mpv_dec_emision CHECK (estado_emision IN ('BORRADOR', 'PENDIENTE_VALIDACION', 'PENDIENTE_FIRMA', 'EMITIDO', 'ANULADO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_dec_exp ON mpv_decisiones(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_dec_autoridad ON mpv_decisiones(autoridad_competente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_dec_numero ON mpv_decisiones(numero_documento);
CREATE INDEX IF NOT EXISTS idx_mpv_dec_emision ON mpv_decisiones(estado_emision);

-- 8. Notificaciones Administrativas (Legalmente Practicadas)
CREATE TABLE IF NOT EXISTS mpv_notificaciones_administrativas (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    decision_id BIGINT REFERENCES mpv_decisiones(id) ON DELETE SET NULL,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    destinatario_nombre VARCHAR(255) NOT NULL,
    destinatario_documento VARCHAR(30),
    destinatario_direccion TEXT,
    destinatario_email VARCHAR(150),
    medio_notificacion VARCHAR(40) NOT NULL,
    estado_notificacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_intento TIMESTAMP WITH TIME ZONE,
    fecha_practica_valida TIMESTAMP WITH TIME ZONE,
    numero_acuse_recibo VARCHAR(100),
    evidencia_documental_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    observaciones TEXT,
    usuario_responsable_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    es_legalmente_valida BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_mpv_notif_medio CHECK (medio_notificacion IN ('CASILLA_ELECTRONICA', 'CORREO_ELECTRONICO_FORMAL', 'DOMICILIO_FISICO', 'CEDULA_NOTIFICACION', 'EDICTO')),
    CONSTRAINT chk_mpv_notif_estado CHECK (estado_notificacion IN ('PENDIENTE', 'INTENTO_ENVIADO', 'NOTIFICADA_VALIDA', 'OBSERVADA_RECHAZADA', 'FALLIDA'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_notif_exp ON mpv_notificaciones_administrativas(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_notif_decision ON mpv_notificaciones_administrativas(decision_id);
CREATE INDEX IF NOT EXISTS idx_mpv_notif_estado ON mpv_notificaciones_administrativas(estado_notificacion);
CREATE INDEX IF NOT EXISTS idx_mpv_notif_valida ON mpv_notificaciones_administrativas(es_legalmente_valida);

-- 9. Cierres Administrativos de Expedientes
CREATE TABLE IF NOT EXISTS mpv_cierres (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    decision_id BIGINT REFERENCES mpv_decisiones(id) ON DELETE SET NULL,
    usuario_cierre_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_cierre_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    motivo_cierre TEXT NOT NULL,
    tipo_cierre VARCHAR(40) NOT NULL,
    documento_cierre_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    fecha_cierre TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT chk_mpv_cierre_tipo CHECK (tipo_cierre IN ('ATENCION_CONCLUIDA', 'DESISTIMIENTO', 'IMPROCEDENCIA', 'SILENCIO_ADMINISTRATIVO', 'CADUCIDAD', 'OTRO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_cierre_exp ON mpv_cierres(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_cierre_usuario ON mpv_cierres(usuario_cierre_id);

-- 10. Archivo Administrativo
CREATE TABLE IF NOT EXISTS mpv_archivos (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    usuario_archivo_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    oficina_archivo_id BIGINT NOT NULL REFERENCES oficinas(id) ON DELETE RESTRICT,
    codigo_archivador VARCHAR(100),
    ubicacion_fisica VARCHAR(200),
    tomo_folio VARCHAR(100),
    motivo_archivo TEXT NOT NULL,
    fecha_archivo TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_mpv_arch_exp ON mpv_archivos(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_arch_usuario ON mpv_archivos(usuario_archivo_id);

-- 11. Reaperturas Administrativas
CREATE TABLE IF NOT EXISTS mpv_reaperturas (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    usuario_reapertura_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    motivo_legal TEXT NOT NULL,
    acto_reapertura_referencia VARCHAR(150),
    fecha_reapertura TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_mpv_reap_exp ON mpv_reaperturas(expediente_id);

-- 12. Permisos Granulares de Atención, Resolución, Notificación y Cierre
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ATENCION_VER', 'Ver bandeja de expedientes en atención', 'VIEW', 'Permite consultar la bandeja de trabajo de expedientes asignados'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ATENCION_INICIAR', 'Iniciar formalmente atención de expediente', 'UPDATE', 'Permite transicionar un expediente asignado al estado EN_ATENCION'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EVALUACION_CREAR', 'Elaborar evaluaciones técnicas y legales', 'CREATE', 'Permite crear informes y evaluaciones técnicas en expedientes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EVALUACION_FINALIZAR', 'Finalizar evaluación técnica', 'UPDATE', 'Permite sellar una evaluación técnica para elevar a propuesta'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_PROPUESTA_CREAR', 'Elaborar propuesta de decisión administrativa', 'CREATE', 'Permite formular proyectos de resolución u oficios de respuesta'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_PROPUESTA_REVISAR', 'Revisar propuestas de decisión', 'VIEW', 'Permite a jefes y revisores analizar proyectos de decisión'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_PROPUESTA_APROBAR', 'Aprobar internamente o devolver propuestas', 'UPDATE', 'Permite dar conformidad interna a proyectos de decisión o devolverlos con observaciones'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DECISION_EMITIR', 'Emitir decisiones y resoluciones administrativas', 'CREATE', 'Permite a funcionarios competentes emitir actos administrativos vinculantes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DOCUMENTO_GENERAR', 'Generar documentos oficiales en PDF', 'CREATE', 'Permite confeccionar resoluciones y documentos oficiales con formato institucional'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_DOCUMENTO_FORMALIZAR', 'Registrar formalización y emisión de documentos', 'UPDATE', 'Permite registrar la firma y expedición formal de documentos emitidos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_NOTIFICACION_GESTIONAR', 'Gestionar y acreditar notificaciones administrativas', 'MANAGE', 'Permite practicar y validar formalmente notificaciones conforme a la LPAG'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_CERRAR', 'Cerrar administrativamente expedientes', 'UPDATE', 'Permite dar por concluido el procedimiento administrativo'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_ARCHIVAR', 'Archivar expedientes cerrados', 'UPDATE', 'Permite enviar expedientes concluidos al archivo pasivo institucional'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_EXPEDIENTE_REABRIR', 'Reabrir excepcionalmente expedientes concluidos', 'UPDATE', 'Permite reaperturas extraordinarias por mandato administrativo o legal'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación de Permisos a Roles Institucionales
-- SUPER_ADMIN y ADMIN_GENERAL
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
    'MESAPARTES_ATENCION_VER', 'MESAPARTES_ATENCION_INICIAR', 'MESAPARTES_EVALUACION_CREAR',
    'MESAPARTES_EVALUACION_FINALIZAR', 'MESAPARTES_PROPUESTA_CREAR', 'MESAPARTES_PROPUESTA_REVISAR',
    'MESAPARTES_PROPUESTA_APROBAR', 'MESAPARTES_DECISION_EMITIR', 'MESAPARTES_DOCUMENTO_GENERAR',
    'MESAPARTES_DOCUMENTO_FORMALIZAR', 'MESAPARTES_NOTIFICACION_GESTIONAR', 'MESAPARTES_EXPEDIENTE_CERRAR',
    'MESAPARTES_EXPEDIENTE_ARCHIVAR', 'MESAPARTES_EXPEDIENTE_REABRIR'
  )
ON CONFLICT DO NOTHING;

-- JEFE_OFICINA
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
    'MESAPARTES_ATENCION_VER', 'MESAPARTES_ATENCION_INICIAR', 'MESAPARTES_EVALUACION_CREAR',
    'MESAPARTES_EVALUACION_FINALIZAR', 'MESAPARTES_PROPUESTA_CREAR', 'MESAPARTES_PROPUESTA_REVISAR',
    'MESAPARTES_PROPUESTA_APROBAR', 'MESAPARTES_DECISION_EMITIR', 'MESAPARTES_DOCUMENTO_GENERAR',
    'MESAPARTES_DOCUMENTO_FORMALIZAR', 'MESAPARTES_NOTIFICACION_GESTIONAR', 'MESAPARTES_EXPEDIENTE_CERRAR',
    'MESAPARTES_EXPEDIENTE_ARCHIVAR'
  )
ON CONFLICT DO NOTHING;

-- OPERADOR / TRABAJADOR
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR'
  AND p.codigo IN (
    'MESAPARTES_ATENCION_VER', 'MESAPARTES_ATENCION_INICIAR', 'MESAPARTES_EVALUACION_CREAR',
    'MESAPARTES_EVALUACION_FINALIZAR', 'MESAPARTES_PROPUESTA_CREAR', 'MESAPARTES_DOCUMENTO_GENERAR'
  )
ON CONFLICT DO NOTHING;

-- MESA_PARTES (Notificaciones, recepción de resultados y archivo de ser el caso)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_MESA_PARTES'
  AND p.codigo IN (
    'MESAPARTES_ATENCION_VER', 'MESAPARTES_NOTIFICACION_GESTIONAR', 'MESAPARTES_EXPEDIENTE_ARCHIVAR'
  )
ON CONFLICT DO NOTHING;
