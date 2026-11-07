-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V13: Sistema Centralizado de Notificaciones, Correo y Alertas
-- ==============================================================================

-- 1. Ampliación de tipos y campos en la tabla notificaciones existente
ALTER TABLE notificaciones DROP CONSTRAINT IF EXISTS chk_notificacion_tipo;
ALTER TABLE notificaciones ADD CONSTRAINT chk_notificacion_tipo 
    CHECK (tipo IN ('INFORMACION', 'INFO', 'EXITO', 'ADVERTENCIA', 'ALERTA', 'ERROR', 'SEGURIDAD', 'TRAMITE', 'DOCUMENTO', 'PAGO', 'APROBACION', 'OBSERVACION', 'SISTEMA'));

ALTER TABLE notificaciones ADD COLUMN IF NOT EXISTS referencia_id VARCHAR(100);
ALTER TABLE notificaciones ADD COLUMN IF NOT EXISTS fecha_expiracion TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_notificaciones_tipo ON notificaciones(tipo);
CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario_tipo ON notificaciones(usuario_id, tipo);

-- 2. Tabla de Preferencias de Notificación por Categoría
CREATE TABLE IF NOT EXISTS notificacion_preferencias (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    canal_interno BOOLEAN NOT NULL DEFAULT TRUE,
    canal_email BOOLEAN NOT NULL DEFAULT TRUE,
    alertas_seguridad BOOLEAN NOT NULL DEFAULT TRUE,
    avisos_tramites BOOLEAN NOT NULL DEFAULT TRUE,
    avisos_documentos BOOLEAN NOT NULL DEFAULT TRUE,
    avisos_pagos BOOLEAN NOT NULL DEFAULT TRUE,
    comunicaciones_generales BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_notif_pref_usuario ON notificacion_preferencias(usuario_id);

-- 3. Tabla de Plantillas de Notificación y Correo
CREATE TABLE IF NOT EXISTS notificacion_plantillas (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    asunto_predeterminado VARCHAR(255) NOT NULL,
    cuerpo_html TEXT NOT NULL,
    cuerpo_texto TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_notif_plantillas_codigo ON notificacion_plantillas(codigo);

-- 4. Tabla de Cola Outbox Persistente de Envíos de Correo
CREATE TABLE IF NOT EXISTS notificacion_envios (
    id BIGSERIAL PRIMARY KEY,
    destinatario_correo VARCHAR(150) NOT NULL,
    destinatario_nombre VARCHAR(150),
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    plantilla_codigo VARCHAR(50),
    asunto VARCHAR(255) NOT NULL,
    cuerpo_html TEXT NOT NULL,
    cuerpo_texto TEXT,
    tipo VARCHAR(50) NOT NULL DEFAULT 'INFORMACION',
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    intentos INT NOT NULL DEFAULT 0,
    max_intentos INT NOT NULL DEFAULT 3,
    proximo_intento TIMESTAMP WITH TIME ZONE,
    fecha_envio TIMESTAMP WITH TIME ZONE,
    ultimo_error TEXT,
    idempotency_key VARCHAR(100) UNIQUE,
    origen VARCHAR(100) DEFAULT 'SISTEMA',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_envio_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'ENVIADO', 'FALLIDO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_notif_envios_estado ON notificacion_envios(estado);
CREATE INDEX IF NOT EXISTS idx_notif_envios_proximo ON notificacion_envios(proximo_intento);
CREATE INDEX IF NOT EXISTS idx_notif_envios_idempotency ON notificacion_envios(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_notif_envios_usuario ON notificacion_envios(usuario_id);

-- 5. Tabla de Historial de Intentos de Envío
CREATE TABLE IF NOT EXISTS notificacion_intentos (
    id BIGSERIAL PRIMARY KEY,
    envio_id BIGINT NOT NULL REFERENCES notificacion_envios(id) ON DELETE CASCADE,
    numero_intento INT NOT NULL,
    proveedor VARCHAR(50) NOT NULL,
    exitoso BOOLEAN NOT NULL,
    mensaje_respuesta TEXT,
    fecha_intento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notif_intentos_envio ON notificacion_intentos(envio_id);

-- 6. Tabla de Programaciones de Notificaciones Futuras
CREATE TABLE IF NOT EXISTS notificacion_programaciones (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'INFORMACION',
    canal VARCHAR(30) NOT NULL DEFAULT 'INTERNO',
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE CASCADE,
    fecha_programada TIMESTAMP WITH TIME ZONE NOT NULL,
    ejecutada BOOLEAN NOT NULL DEFAULT FALSE,
    cancelada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_ejecucion TIMESTAMP WITH TIME ZONE,
    creado_por VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_prog_canal CHECK (canal IN ('INTERNO', 'CORREO', 'AMBOS'))
);

CREATE INDEX IF NOT EXISTS idx_notif_prog_pendientes ON notificacion_programaciones(ejecutada, cancelada, fecha_programada);

-- 7. Inserción de las 10 Plantillas Oficiales de Correo Institucional
INSERT INTO notificacion_plantillas (codigo, nombre, asunto_predeterminado, cuerpo_html, cuerpo_texto)
VALUES
(
    'BIENVENIDA',
    'Bienvenida a SIGM-QUINUA',
    'Bienvenido(a) a la plataforma SIGM-QUINUA — Municipalidad Distrital de Quinua',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#3b82f6;margin-bottom:12px;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">¡Bienvenido(a), {{nombreDestinatario}}!</h3>'
    || '<p>Su cuenta en el <strong>Sistema Integral de Gestión Municipal (SIGM-QUINUA)</strong> ha sido creada exitosamente.</p>'
    || '<p>Ahora puede realizar trámites, consultar expedientes, recibir alertas institucionales y acceder a servicios digitales con total transparencia.</p>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#1d4ed8;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Acceder a mi Cuenta</a></div>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Huamanga, Ayacucho.<br/>Este es un mensaje institucional automático. Por favor no responder a este correo.</p>'
    || '</div>',
    'Bienvenido(a) a SIGM-QUINUA, {{nombreDestinatario}}. Su cuenta ha sido creada exitosamente. Ingrese en: {{enlaceAccion}}'
),
(
    'VERIFICACION_CORREO',
    'Verificación de Correo Electrónico',
    'Verificación de correo electrónico — SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#3b82f6;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">Confirmación de Correo Institucional</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, para completar la validación de su correo electrónico en el portal municipal, haga clic en el siguiente botón:</p>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#1d4ed8;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Verificar mi Correo</a></div>'
    || '<p style="font-size:13px;color:#94a3b8;">Si no solicitó este registro, ignore este mensaje.</p>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Gestión Digital Transparente.</p>'
    || '</div>',
    'Estimado(a) {{nombreDestinatario}}, verifique su correo en SIGM-QUINUA mediante el enlace: {{enlaceAccion}}'
),
(
    'RECUPERACION_PASSWORD',
    'Recuperación de Contraseña',
    'Instrucciones para restablecer su contraseña — SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#3b82f6;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">Recuperación de Acceso</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, hemos recibido una solicitud para restablecer la contraseña de su cuenta municipal.</p>'
    || '<p>Haga clic en el botón inferior para establecer una nueva clave. Este enlace expira en 15 minutos:</p>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#2563eb;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Restablecer Contraseña</a></div>'
    || '<p style="font-size:13px;color:#ef4444;">Si usted no solicitó el restablecimiento, le recomendamos ingresar a su cuenta y verificar su actividad de seguridad de inmediato.</p>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Seguridad Digital.</p>'
    || '</div>',
    'Recuperación de contraseña para {{nombreDestinatario}}. Restablezca su clave en: {{enlaceAccion}} (Válido por 15 minutos).'
),
(
    'CAMBIO_PASSWORD',
    'Confirmación de Cambio de Contraseña',
    'Notificación de cambio de contraseña exitoso — SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#10b981;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">Cambio de Contraseña Exitoso</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, le informamos que la contraseña de su cuenta en SIGM-QUINUA ha sido modificada correctamente el {{fecha}}.</p>'
    || '<p>Por motivos de seguridad, todas las demás sesiones activas en otros dispositivos han sido revocadas automáticamente.</p>'
    || '<p style="font-size:13px;color:#f59e0b;">Si no realizó este cambio, contacte de inmediato con la Oficina General de Tecnologías de la Información o restablezca su clave en el portal.</p>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Huamanga, Ayacucho.</p>'
    || '</div>',
    'Estimado(a) {{nombreDestinatario}}, su contraseña fue actualizada el {{fecha}}. Todas las demás sesiones han sido revocadas.'
),
(
    'ALERTA_SEGURIDAD',
    'Alerta de Seguridad Institucional',
    'Alerta de seguridad importante en su cuenta — SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;border-left:4px solid #ef4444;">'
    || '<h2 style="color:#ef4444;">Alerta de Seguridad SIGM-QUINUA</h2>'
    || '<h3 style="color:#ffffff;">Evento de Seguridad Detectado</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, se ha registrado el siguiente evento en su cuenta:</p>'
    || '<div style="background:#1e293b;padding:16px;border-radius:6px;margin:16px 0;color:#f8fafc;">'
    || '<strong>Detalle:</strong> {{mensaje}}<br/><strong>Fecha y Hora:</strong> {{fecha}}'
    || '</div>'
    || '<p>Si reconoce esta actividad, no requiere realizar ninguna acción. En caso contrario, acceda de inmediato a su perfil y revoque todas las sesiones activas.</p>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#dc2626;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Revisar Actividad de Seguridad</a></div>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Área de Ciberseguridad.</p>'
    || '</div>',
    'ALERTA DE SEGURIDAD: {{mensaje}} en su cuenta el {{fecha}}. Revise su actividad en: {{enlaceAccion}}'
),
(
    'NUEVA_NOTIFICACION',
    'Nueva Notificación en Sistema',
    'Tiene una nueva notificación en SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#3b82f6;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">{{titulo}}</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, tiene un nuevo aviso en el sistema municipal:</p>'
    || '<div style="background:#1e293b;padding:16px;border-radius:6px;margin:16px 0;color:#f8fafc;">{{mensaje}}</div>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#1d4ed8;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Ver en Plataforma</a></div>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Centro de Notificaciones.</p>'
    || '</div>',
    'Nueva notificación para {{nombreDestinatario}}: {{titulo}} - {{mensaje}}. Ver: {{enlaceAccion}}'
),
(
    'AVISO_INSTITUCIONAL',
    'Aviso Institucional Municipal',
    'Comunicado Institucional — Municipalidad Distrital de Quinua',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#f59e0b;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">Comunicado Oficial</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}:</p>'
    || '<div style="background:#1e293b;padding:16px;border-radius:6px;margin:16px 0;color:#f8fafc;line-height:1.6;">{{mensaje}}</div>'
    || '<p style="font-size:13px;color:#94a3b8;">Publicado el: {{fecha}}</p>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Huamanga, Ayacucho.</p>'
    || '</div>',
    'COMUNICADO OFICIAL QUINUA: {{mensaje}} (Publicado: {{fecha}})'
),
(
    'TRAMITE_ACTUALIZACION',
    'Actualización de Estado de Trámite',
    'Actualización del Expediente {{codigoSeguimiento}} — SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;">'
    || '<h2 style="color:#3b82f6;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">Actualización de Expediente</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, le informamos que su trámite con código <strong>{{codigoSeguimiento}}</strong> ha cambiado de estado:</p>'
    || '<div style="background:#1e293b;padding:16px;border-radius:6px;margin:16px 0;color:#f8fafc;">{{mensaje}}</div>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#1d4ed8;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Consultar Estado del Trámite</a></div>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Mesa de Partes y Trámite Documentario.</p>'
    || '</div>',
    'Actualización de Expediente {{codigoSeguimiento}}: {{mensaje}}. Consultar en: {{enlaceAccion}}'
),
(
    'DOCUMENTO_OBSERVADO',
    'Documento Observado en Mesa de Partes',
    'Observación en su trámite {{codigoSeguimiento}} — Subsanación requerida',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;border-left:4px solid #f59e0b;">'
    || '<h2 style="color:#f59e0b;">Mesa de Partes Municipal</h2>'
    || '<h3 style="color:#ffffff;">Subsanación de Requisitos Requerida</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, se ha registrado una observación en el expediente <strong>{{codigoSeguimiento}}</strong>:</p>'
    || '<div style="background:#1e293b;padding:16px;border-radius:6px;margin:16px 0;color:#f8fafc;">{{mensaje}}</div>'
    || '<p>Conforme al TUO de la Ley N° 27444, dispone de un plazo de dos (2) días hábiles para subsanar los requisitos observados.</p>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#d97706;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Subsanar Documentos</a></div>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Atención al Ciudadano.</p>'
    || '</div>',
    'OBSERVACIÓN en expediente {{codigoSeguimiento}}: {{mensaje}}. Ingrese para subsanar: {{enlaceAccion}}'
),
(
    'TRAMITE_APROBADO',
    'Trámite Finalizado y Aprobado',
    '¡Trámite Aprobado! Expediente {{codigoSeguimiento}} — SIGM-QUINUA',
    '<div style="font-family:sans-serif;max-width:600px;margin:0 auto;background:#101827;color:#e2e8f0;padding:24px;border-radius:8px;border-left:4px solid #10b981;">'
    || '<h2 style="color:#10b981;">Municipalidad Distrital de Quinua</h2>'
    || '<h3 style="color:#ffffff;">Trámite Aprobado y Concluido</h3>'
    || '<p>Estimado(a) {{nombreDestinatario}}, nos complace informarle que su trámite <strong>{{codigoSeguimiento}}</strong> ha sido aprobado y resuelto favorablemente.</p>'
    || '<div style="background:#1e293b;padding:16px;border-radius:6px;margin:16px 0;color:#f8fafc;">{{mensaje}}</div>'
    || '<div style="margin:24px 0;"><a href="{{enlaceAccion}}" style="background:#059669;color:#ffffff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;">Descargar Resolución / Constancia</a></div>'
    || '<hr style="border:none;border-top:1px solid #334155;margin:24px 0;"/>'
    || '<p style="font-size:12px;color:#94a3b8;">Municipalidad Distrital de Quinua — Gestión Eficiente al Servicio del Pueblo.</p>'
    || '</div>',
    'Su trámite {{codigoSeguimiento}} ha sido APROBADO: {{mensaje}}. Descargue su resolución en: {{enlaceAccion}}'
)
ON CONFLICT (codigo) DO NOTHING;
