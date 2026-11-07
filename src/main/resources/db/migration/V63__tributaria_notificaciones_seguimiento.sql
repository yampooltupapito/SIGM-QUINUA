-- ============================================================================
-- Módulo 4: Recaudación Tributaria Municipal - Municipalidad Distrital de Quinua
-- Prompt 62: Sistema Integral de Notificaciones Tributarias, Comunicaciones,
--           Cargos de Recepción y Seguimiento de Obligaciones Municipales
-- ============================================================================

-- 1. Catálogo de Tipos de Notificación Tributaria
CREATE TABLE IF NOT EXISTS tributaria_tipos_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(40) NOT NULL,
    requiere_documento BOOLEAN NOT NULL DEFAULT FALSE,
    requiere_validacion_legal BOOLEAN NOT NULL DEFAULT FALSE,
    requiere_evidencia BOOLEAN NOT NULL DEFAULT FALSE,
    norma_id UUID REFERENCES tributaria_normas_configuradas(id) ON DELETE SET NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_tipo_notif_cod ON tributaria_tipos_notificacion(codigo);
CREATE INDEX IF NOT EXISTS idx_trib_tipo_notif_cat ON tributaria_tipos_notificacion(categoria);
CREATE INDEX IF NOT EXISTS idx_trib_tipo_notif_est ON tributaria_tipos_notificacion(estado);

-- 2. Catálogo de Medios de Notificación / Comunicación
CREATE TABLE IF NOT EXISTS tributaria_medios_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    es_medio_formal BOOLEAN NOT NULL DEFAULT FALSE,
    requiere_direccion_fisica BOOLEAN NOT NULL DEFAULT FALSE,
    requiere_acuse_digital BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_medio_notif_cod ON tributaria_medios_notificacion(codigo);
CREATE INDEX IF NOT EXISTS idx_trib_medio_notif_est ON tributaria_medios_notificacion(estado);

-- 3. Tabla de Lotes de Notificaciones Tributarias
CREATE TABLE IF NOT EXISTS tributaria_lotes_notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_notificacion_id UUID NOT NULL REFERENCES tributaria_tipos_notificacion(id) ON DELETE RESTRICT,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    total_registros INT NOT NULL DEFAULT 0,
    total_validos INT NOT NULL DEFAULT 0,
    total_observados INT NOT NULL DEFAULT 0,
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    descripcion TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_lote_notif_cod ON tributaria_lotes_notificaciones(codigo);
CREATE INDEX IF NOT EXISTS idx_trib_lote_notif_est ON tributaria_lotes_notificaciones(estado);
CREATE INDEX IF NOT EXISTS idx_trib_lote_notif_fecha ON tributaria_lotes_notificaciones(fecha_creacion);

-- 4. Tabla Principal de Notificaciones Tributarias
CREATE TABLE IF NOT EXISTS tributaria_notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_notificacion VARCHAR(50) NOT NULL UNIQUE,
    tipo_notificacion_id UUID NOT NULL REFERENCES tributaria_tipos_notificacion(id) ON DELETE RESTRICT,
    contribuyente_id UUID NOT NULL REFERENCES tributaria_contribuyentes(id) ON DELETE RESTRICT,
    destinatario_nombre VARCHAR(255) NOT NULL,
    destinatario_numero_doc VARCHAR(30) NOT NULL,
    representante_id UUID REFERENCES tributaria_representantes_contribuyente(id) ON DELETE SET NULL,
    domicilio_notificacion VARCHAR(500) NOT NULL,
    referencia_ubicacion VARCHAR(255),
    medio_notificacion_id UUID NOT NULL REFERENCES tributaria_medios_notificacion(id) ON DELETE RESTRICT,
    documento_origen_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    documento_notificacion_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    hash_documento VARCHAR(64),
    codigo_qr VARCHAR(255),
    obligacion_id UUID REFERENCES tributaria_obligaciones(id) ON DELETE SET NULL,
    convenio_id UUID REFERENCES tributaria_convenios_fraccionamiento(id) ON DELETE SET NULL,
    seguimiento_deuda_id UUID REFERENCES tributaria_seguimiento_deudas(id) ON DELETE SET NULL,
    lote_id UUID REFERENCES tributaria_lotes_notificaciones(id) ON DELETE SET NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_programada DATE,
    estado_administrativo VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    estado_entrega VARCHAR(35) NOT NULL DEFAULT 'NO_PROGRAMADA',
    estado_validez_legal VARCHAR(35) NOT NULL DEFAULT 'NO_APLICA',
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    usuario_responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    idempotency_key VARCHAR(120) UNIQUE,
    observaciones TEXT,
    motivo_anulacion TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_notif_codigo ON tributaria_notificaciones(codigo_notificacion);
CREATE INDEX IF NOT EXISTS idx_trib_notif_contrib ON tributaria_notificaciones(contribuyente_id);
CREATE INDEX IF NOT EXISTS idx_trib_notif_tipo ON tributaria_notificaciones(tipo_notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_notif_medio ON tributaria_notificaciones(medio_notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_notif_est_adm ON tributaria_notificaciones(estado_administrativo);
CREATE INDEX IF NOT EXISTS idx_trib_notif_est_ent ON tributaria_notificaciones(estado_entrega);
CREATE INDEX IF NOT EXISTS idx_trib_notif_est_val ON tributaria_notificaciones(estado_validez_legal);
CREATE INDEX IF NOT EXISTS idx_trib_notif_lote ON tributaria_notificaciones(lote_id);
CREATE INDEX IF NOT EXISTS idx_trib_notif_fecha_crea ON tributaria_notificaciones(fecha_creacion);

-- 5. Detalle de Obligaciones Vinculadas a la Notificación
CREATE TABLE IF NOT EXISTS tributaria_detalles_notificacion_obligacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    obligacion_id UUID NOT NULL REFERENCES tributaria_obligaciones(id) ON DELETE RESTRICT,
    periodo VARCHAR(30),
    monto_insoluto NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    monto_interes NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    monto_total NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uq_trib_det_notif_obl UNIQUE (notificacion_id, obligacion_id)
);

CREATE INDEX IF NOT EXISTS idx_trib_det_notif_notif ON tributaria_detalles_notificacion_obligacion(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_det_notif_obl ON tributaria_detalles_notificacion_obligacion(obligacion_id);

-- 6. Asignaciones de Notificadores y Rutas de Notificación
CREATE TABLE IF NOT EXISTS tributaria_asignaciones_notificador (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    notificador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_programada DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ASIGNADA',
    sector_ruta VARCHAR(150),
    motivo_reasignacion TEXT,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_asig_notif ON tributaria_asignaciones_notificador(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_asig_user ON tributaria_asignaciones_notificador(notificador_id);
CREATE INDEX IF NOT EXISTS idx_trib_asig_estado ON tributaria_asignaciones_notificador(estado);
CREATE INDEX IF NOT EXISTS idx_trib_asig_fecha_prog ON tributaria_asignaciones_notificador(fecha_programada);

-- 7. Registro de Intentos de Notificación
CREATE TABLE IF NOT EXISTS tributaria_intentos_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    notificador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_hora_intento TIMESTAMP WITH TIME ZONE NOT NULL,
    medio_utilizado VARCHAR(60) NOT NULL,
    resultado VARCHAR(40) NOT NULL,
    ubicacion_referencia VARCHAR(255),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_int_notif ON tributaria_intentos_notificacion(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_int_user ON tributaria_intentos_notificacion(notificador_id);
CREATE INDEX IF NOT EXISTS idx_trib_int_resultado ON tributaria_intentos_notificacion(resultado);
CREATE INDEX IF NOT EXISTS idx_trib_int_fecha ON tributaria_intentos_notificacion(fecha_hora_intento);

-- 8. Evidencias de Notificación Tributaria
CREATE TABLE IF NOT EXISTS tributaria_evidencias_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    intento_id UUID REFERENCES tributaria_intentos_notificacion(id) ON DELETE SET NULL,
    tipo_evidencia VARCHAR(40) NOT NULL,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    hash_archivo VARCHAR(64) NOT NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_evid_notif ON tributaria_evidencias_notificacion(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_evid_tipo ON tributaria_evidencias_notificacion(tipo_evidencia);

-- 9. Cargo de Recepción Tributaria
CREATE TABLE IF NOT EXISTS tributaria_cargos_recepcion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL UNIQUE REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    intento_id UUID REFERENCES tributaria_intentos_notificacion(id) ON DELETE SET NULL,
    fecha_recepcion_declarada TIMESTAMP WITH TIME ZONE NOT NULL,
    receptor_identificado VARCHAR(255) NOT NULL,
    receptor_numero_doc VARCHAR(30),
    tipo_vinculo_receptor VARCHAR(40) NOT NULL,
    documento_evidencia_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    tiene_firma BOOLEAN NOT NULL DEFAULT FALSE,
    tiene_sello BOOLEAN NOT NULL DEFAULT FALSE,
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_cargo_notif ON tributaria_cargos_recepcion(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_cargo_fecha ON tributaria_cargos_recepcion(fecha_recepcion_declarada);

-- 10. Validación Procedimental y Checklist Legal
CREATE TABLE IF NOT EXISTS tributaria_validaciones_procedimiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    revisor_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_validacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado VARCHAR(30) NOT NULL,
    sustento TEXT NOT NULL,
    cumple_destinatario_identificado BOOLEAN NOT NULL DEFAULT FALSE,
    cumple_domicilio_valido BOOLEAN NOT NULL DEFAULT FALSE,
    cumple_documento_adjunto BOOLEAN NOT NULL DEFAULT FALSE,
    cumple_plazo_procedimental BOOLEAN NOT NULL DEFAULT FALSE,
    cumple_evidencia_minima BOOLEAN NOT NULL DEFAULT FALSE,
    cumple_datos_receptor_y_cargo BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_val_notif ON tributaria_validaciones_procedimiento(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_val_revisor ON tributaria_validaciones_procedimiento(revisor_id);
CREATE INDEX IF NOT EXISTS idx_trib_val_resultado ON tributaria_validaciones_procedimiento(resultado);

-- 11. Control y Cómputo de Plazos Jurídicos de Notificación
CREATE TABLE IF NOT EXISTS tributaria_controles_plazo_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL UNIQUE REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    tipo_plazo VARCHAR(60) NOT NULL,
    dias_habiles BOOLEAN NOT NULL DEFAULT TRUE,
    numero_dias INT NOT NULL CHECK (numero_dias > 0),
    fecha_hito_inicial DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    dias_restantes INT NOT NULL DEFAULT 0,
    estado_plazo VARCHAR(30) NOT NULL DEFAULT 'VIGENTE',
    fecha_cumplimiento DATE,
    motivo_suspension TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trib_plazo_notif ON tributaria_controles_plazo_notificacion(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_plazo_venc ON tributaria_controles_plazo_notificacion(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_trib_plazo_estado ON tributaria_controles_plazo_notificacion(estado_plazo);

-- 12. Historial de Eventos de Notificación Tributaria
CREATE TABLE IF NOT EXISTS tributaria_historial_notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID NOT NULL REFERENCES tributaria_notificaciones(id) ON DELETE CASCADE,
    tipo_evento VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    datos_anteriores TEXT,
    datos_nuevos TEXT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(60)
);

CREATE INDEX IF NOT EXISTS idx_trib_hist_notif ON tributaria_historial_notificaciones(notificacion_id);
CREATE INDEX IF NOT EXISTS idx_trib_hist_evento ON tributaria_historial_notificaciones(tipo_evento);
CREATE INDEX IF NOT EXISTS idx_trib_hist_fecha ON tributaria_historial_notificaciones(fecha_evento);

-- 13. Inserción de Datos Iniciales en Tipos de Notificación
INSERT INTO tributaria_tipos_notificacion (codigo, nombre, descripcion, categoria, requiere_documento, requiere_validacion_legal, requiere_evidencia, estado, created_by)
VALUES
('COM_INF_VENCIMIENTO_PREDIAL', 'Comunicación Informativa de Vencimiento de Impuesto Predial', 'Aviso general y preventivo sobre fechas límites de pago predial del trimestre', 'INFORMATIVA', FALSE, FALSE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('COM_INF_VENCIMIENTO_ARBITRIOS', 'Comunicación Informativa de Vencimiento de Arbitrios Municipales', 'Aviso preventivo mensual de servicios públicos de limpieza y serenazgo', 'INFORMATIVA', FALSE, FALSE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('REC_ADM_OBLIGACION_PENDIENTE', 'Recordatorio Administrativo de Obligaciones Pendientes', 'Recordatorio de deudas tributarias en etapa ordinaria sin efectos sancionatorios', 'RECORDATORIO', FALSE, FALSE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('AVIS_ADM_PREVIO_COBRANZA', 'Aviso Administrativo Previo a Acciones de Cobranza', 'Comunicación administrativa de requerimiento previo a emisión de actos formales', 'AVISO_ADMINISTRATIVO', TRUE, FALSE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('NOTIF_ORDEN_PAGO', 'Notificación Formal de Orden de Pago (OP)', 'Acto administrativo formal conforme al Art. 78 del Código Tributario peruano', 'NOTIFICACION_FORMAL', TRUE, TRUE, TRUE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('NOTIF_RESOLUCION_DETERMINACION', 'Notificación Formal de Resolución de Determinación (RD)', 'Acto administrativo formal que establece la existencia y cuantía de deuda tributaria', 'NOTIFICACION_FORMAL', TRUE, TRUE, TRUE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('NOTIF_RESOLUCION_MULTA', 'Notificación Formal de Resolución de Multa Tributaria (RM)', 'Sanción formal por infracciones formales o sustanciales tributarias municipales', 'NOTIFICACION_FORMAL', TRUE, TRUE, TRUE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('NOTIF_PERDIDA_FRACCIONAMIENTO', 'Notificación Formal de Pérdida de Fraccionamiento Tributario', 'Resolución formal declarando la pérdida de beneficios por cuotas impagas', 'NOTIFICACION_FORMAL', TRUE, TRUE, TRUE, 'ACTIVO', 'SISTEMA_MIGRACION')
ON CONFLICT (codigo) DO NOTHING;

-- 14. Inserción de Datos Iniciales en Medios de Notificación
INSERT INTO tributaria_medios_notificacion (codigo, nombre, descripcion, es_medio_formal, requiere_direccion_fisica, requiere_acuse_digital, estado, created_by)
VALUES
('ENTREGA_FISICA_DIRECTA', 'Entrega Física Presencial con Cédula y Cargo', 'Notificación personal en domicilio fiscal o procesal mediante notificador acreditado', TRUE, TRUE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('CORREO_POSTAL_AUTORIZADO', 'Correo Postal Certificado con Acuse Oficial', 'Remisión por servicio postal legalmente contratado con acuse de recibo verificable', TRUE, TRUE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('CASILLA_ELECTRONICA', 'Casilla Electrónica Municipal Oficial Habilitada', 'Buzón electrónico seguro con certificación de depósito y acuse informático conforme a ley', TRUE, FALSE, TRUE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('CORREO_ELECTRONICO_INFORMATIVO', 'Correo Electrónico Informativo No Vinculante', 'Envío simple de cortesía por email que no acredita notificación legal vinculante', FALSE, FALSE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION'),
('PORTAL_CIUDADANO_INTERNO', 'Mensaje Interno de Portal Ciudadano', 'Comunicación visual al acceder al portal ciudadano municipal sin efectos procesales', FALSE, FALSE, FALSE, 'ACTIVO', 'SISTEMA_MIGRACION')
ON CONFLICT (codigo) DO NOTHING;

-- 15. Inserción de Permisos Institucionales (Prompt 62)
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_VER', 'Ver Notificaciones Tributarias', 'VIEW',
       'Permite consultar el listado, fichas y expedientes de notificaciones tributarias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_CREAR', 'Crear Notificación Tributaria', 'CREATE',
       'Permite registrar comunicaciones y expedientes individuales de notificación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_EDITAR', 'Editar Notificación Tributaria', 'UPDATE',
       'Permite modificar datos preliminares en estado borrador u observada', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_VALIDAR', 'Validar Datos de Notificación', 'APPROVE',
       'Permite verificar datos de destinatario, domicilio y obligaciones preliminares', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_AUTORIZAR', 'Autorizar Emisión de Notificación', 'APPROVE',
       'Permite autorizar la emisión formal y generación de documentos oficiales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_GENERAR_LOTES', 'Generación de Notificaciones por Lotes', 'MANAGE',
       'Permite crear, validar y procesar lotes masivos de comunicaciones tributarias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_ASIGNAR', 'Asignar Notificador Tributario', 'ASSIGN',
       'Permite asignar actuaciones presenciales a personal notificador autorizado', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_REGISTRAR_INTENTO', 'Registrar Intento de Notificación', 'UPDATE',
       'Permite asentar las actuaciones, visitas y resultados obtenidos en campo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_ADJUNTAR_EVIDENCIA', 'Adjuntar Evidencias de Notificación', 'UPDATE',
       'Permite subir fotografías, actas y documentos digitalizados al gestor documental', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_REGISTRAR_CARGO', 'Registrar Cargo de Recepción', 'CREATE',
       'Permite asentar los datos verificables del receptor y recepción del documento', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_REVISAR_ENTREGA', 'Revisar Resultado de Entrega', 'APPROVE',
       'Permite al supervisor revisar los intentos y evidencias registradas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_VALIDAR_PROCEDIMIENTO', 'Validar Validez Legal Procedimental', 'APPROVE',
       'Permite calificar formalmente la eficacia jurídica de la notificación según ley', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_ANULAR', 'Anular Notificación Tributaria', 'DELETE',
       'Permite cancelar una notificación con sustento motivado y registro inmutable', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_VER_HISTORIAL', 'Ver Historial de Actuaciones', 'VIEW',
       'Permite auditar la cronología íntegra de eventos de una notificación', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_EXPORTAR', 'Exportar Notificaciones', 'EXPORT',
       'Permite descargar padrones y listas en formatos PDF y Excel institucional', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'TRIBUTARIA_NOTIFICACIONES_REPORTES', 'Reportes de Notificaciones Tributarias', 'VIEW',
       'Permite visualizar analítica, efectividad de entrega y control de plazos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'RENTAS_TRIBUTACION'
ON CONFLICT (codigo) DO NOTHING;

-- 16. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'TRIBUTARIA_NOTIFICACIONES_%'
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_TRIBUTACION'
  AND p.codigo IN (
      'TRIBUTARIA_NOTIFICACIONES_VER',
      'TRIBUTARIA_NOTIFICACIONES_CREAR',
      'TRIBUTARIA_NOTIFICACIONES_EDITAR',
      'TRIBUTARIA_NOTIFICACIONES_VALIDAR',
      'TRIBUTARIA_NOTIFICACIONES_AUTORIZAR',
      'TRIBUTARIA_NOTIFICACIONES_GENERAR_LOTES',
      'TRIBUTARIA_NOTIFICACIONES_ASIGNAR',
      'TRIBUTARIA_NOTIFICACIONES_REGISTRAR_INTENTO',
      'TRIBUTARIA_NOTIFICACIONES_ADJUNTAR_EVIDENCIA',
      'TRIBUTARIA_NOTIFICACIONES_REGISTRAR_CARGO',
      'TRIBUTARIA_NOTIFICACIONES_REVISAR_ENTREGA',
      'TRIBUTARIA_NOTIFICACIONES_VALIDAR_PROCEDIMIENTO',
      'TRIBUTARIA_NOTIFICACIONES_ANULAR',
      'TRIBUTARIA_NOTIFICACIONES_VER_HISTORIAL',
      'TRIBUTARIA_NOTIFICACIONES_EXPORTAR',
      'TRIBUTARIA_NOTIFICACIONES_REPORTES'
  )
ON CONFLICT DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo = 'ROLE_CAJERO'
  AND p.codigo IN (
      'TRIBUTARIA_NOTIFICACIONES_VER',
      'TRIBUTARIA_NOTIFICACIONES_VER_HISTORIAL'
  )
ON CONFLICT DO NOTHING;

