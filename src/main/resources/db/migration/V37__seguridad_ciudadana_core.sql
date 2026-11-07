-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V37: Módulo 3 - Seguridad Ciudadana y Serenazgo (Arquitectura Inicial)
-- ==============================================================================

-- 1. Secuencias para generación de códigos correlativos seguros
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_personal START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_unidad START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_sector START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_zona START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_incidencia START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_alerta START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_intervencion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_patrullaje START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_supervision START WITH 1 INCREMENT BY 1;

-- 2. Cargos Operativos de Serenazgo (Catálogo Configurable)
CREATE TABLE IF NOT EXISTS serenazgo_cargos_operativos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    nivel_jerarquico INT NOT NULL DEFAULT 1,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_cargos_codigo ON serenazgo_cargos_operativos(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_cargos_activo ON serenazgo_cargos_operativos(activo);

-- 3. Personal de Serenazgo (Vinculado a Persona y Usuario del Núcleo Institucional)
CREATE TABLE IF NOT EXISTS serenazgo_personal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_interno VARCHAR(50) NOT NULL UNIQUE,
    persona_id BIGINT NOT NULL REFERENCES personas(id) ON DELETE RESTRICT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    cargo_operativo_id UUID REFERENCES serenazgo_cargos_operativos(id) ON DELETE SET NULL,
    tipo_documento_laboral VARCHAR(50),
    fecha_ingreso DATE,
    fecha_salida DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_serenazgo_personal_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'RETIRADO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_personal_codigo ON serenazgo_personal(codigo_interno);
CREATE INDEX IF NOT EXISTS idx_serenazgo_personal_persona ON serenazgo_personal(persona_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_personal_usuario ON serenazgo_personal(usuario_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_personal_estado ON serenazgo_personal(estado);

-- 4. Unidades Móviles de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_unidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_interno VARCHAR(50) NOT NULL UNIQUE,
    tipo_unidad VARCHAR(40) NOT NULL,
    placa VARCHAR(20) UNIQUE,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    anio INT,
    estado_operativo VARCHAR(30) NOT NULL DEFAULT 'OPERATIVA',
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    kilometraje_actual NUMERIC(10, 2),
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_serenazgo_unidad_tipo CHECK (tipo_unidad IN ('CAMIONETA', 'MOTOCICLETA', 'BICICLETA', 'OTRO')),
    CONSTRAINT chk_serenazgo_unidad_estado CHECK (estado_operativo IN ('OPERATIVA', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO', 'INACTIVA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_unidades_codigo ON serenazgo_unidades(codigo_interno);
CREATE INDEX IF NOT EXISTS idx_serenazgo_unidades_placa ON serenazgo_unidades(placa);
CREATE INDEX IF NOT EXISTS idx_serenazgo_unidades_estado ON serenazgo_unidades(estado_operativo);

-- 5. Equipamiento de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_equipamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_interno VARCHAR(50) NOT NULL UNIQUE,
    tipo_equipo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    numero_serie VARCHAR(100),
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    responsable_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    unidad_movil_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    fecha_asignacion DATE,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_equipo_estado CHECK (estado IN ('DISPONIBLE', 'ASIGNADO', 'EN_REPARACION', 'DE_BAJA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_equipamiento_codigo ON serenazgo_equipamiento(codigo_interno);
CREATE INDEX IF NOT EXISTS idx_serenazgo_equipamiento_estado ON serenazgo_equipamiento(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_equipamiento_responsable ON serenazgo_equipamiento(responsable_id);

-- 6. Historial de Asignaciones de Equipamiento
CREATE TABLE IF NOT EXISTS serenazgo_equipamiento_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipamiento_id UUID NOT NULL REFERENCES serenazgo_equipamiento(id) ON DELETE CASCADE,
    personal_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    unidad_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    fecha_entrega TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_eq_hist_equipo ON serenazgo_equipamiento_historial(equipamiento_id);

-- 7. Sectores de Patrullaje
CREATE TABLE IF NOT EXISTS serenazgo_sectores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    color_hex VARCHAR(10),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_sector_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_sectores_codigo ON serenazgo_sectores(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_sectores_estado ON serenazgo_sectores(estado);

-- 8. Zonas de Patrullaje por Sector
CREATE TABLE IF NOT EXISTS serenazgo_zonas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sector_id UUID NOT NULL REFERENCES serenazgo_sectores(id) ON DELETE CASCADE,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_serenazgo_sector_zona_codigo UNIQUE (sector_id, codigo),
    CONSTRAINT chk_serenazgo_zona_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_zonas_sector ON serenazgo_zonas(sector_id);

-- 9. Puntos de Referencia de Seguridad
CREATE TABLE IF NOT EXISTS serenazgo_puntos_referencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sector_id UUID NOT NULL REFERENCES serenazgo_sectores(id) ON DELETE CASCADE,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(50) NOT NULL DEFAULT 'PUNTO_INTERES',
    latitud NUMERIC(10, 7),
    longitud NUMERIC(10, 7),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_pt_lat CHECK (latitud IS NULL OR (latitud >= -90 AND latitud <= 90)),
    CONSTRAINT chk_serenazgo_pt_lon CHECK (longitud IS NULL OR (longitud >= -180 AND longitud <= 180)),
    CONSTRAINT chk_serenazgo_pt_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_pt_ref_sector ON serenazgo_puntos_referencia(sector_id);

-- 10. Turnos de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_turnos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    cruza_medianoche BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_turno_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_turnos_codigo ON serenazgo_turnos(codigo);

-- 11. Programación de Servicios de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_programacion_servicios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    turno_id UUID NOT NULL REFERENCES serenazgo_turnos(id) ON DELETE RESTRICT,
    fecha_servicio DATE NOT NULL,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    unidad_movil_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    rol_servicio VARCHAR(50) NOT NULL DEFAULT 'PATRULLAJE',
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
    responsable_programacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_serenazgo_personal_fecha_turno UNIQUE (personal_id, fecha_servicio, turno_id),
    CONSTRAINT chk_serenazgo_prog_estado CHECK (estado IN ('PROGRAMADO', 'PRESENTE', 'AUSENTE', 'JUSTIFICADO', 'CANCELADO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_prog_fecha ON serenazgo_programacion_servicios(fecha_servicio);
CREATE INDEX IF NOT EXISTS idx_serenazgo_prog_personal ON serenazgo_programacion_servicios(personal_id);

-- 12. Tipos de Incidencia de Seguridad Ciudadana (Catálogo Configurable)
CREATE TABLE IF NOT EXISTS serenazgo_tipos_incidencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL DEFAULT 'ORDEN_PUBLICO',
    prioridad_sugerida VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
    requiere_unidad BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_tipo_prioridad CHECK (prioridad_sugerida IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_tipo_inc_codigo ON serenazgo_tipos_incidencia(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_tipo_inc_activo ON serenazgo_tipos_incidencia(activo);

-- 13. Incidencias de Seguridad Ciudadana
CREATE TABLE IF NOT EXISTS serenazgo_incidencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_incidencia_id UUID NOT NULL REFERENCES serenazgo_tipos_incidencia(id) ON DELETE RESTRICT,
    prioridad VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
    descripcion TEXT NOT NULL,
    fecha_reporte TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ocurrencia TIMESTAMP WITH TIME ZONE,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    ubicacion_referencia VARCHAR(255) NOT NULL,
    latitud NUMERIC(10, 7),
    longitud NUMERIC(10, 7),
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA',
    canal_reporte VARCHAR(50) NOT NULL DEFAULT 'LLAMADA_OPERADOR',
    usuario_registro_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    responsable_id UUID REFERENCES serenazgo_personal(id) ON DELETE SET NULL,
    resultado_atencion TEXT,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_inc_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT chk_serenazgo_inc_estado CHECK (estado IN ('REGISTRADA', 'EN_EVALUACION', 'ASIGNADA', 'EN_ATENCION', 'RESUELTA', 'CERRADA', 'DESCARTADA')),
    CONSTRAINT chk_serenazgo_inc_lat CHECK (latitud IS NULL OR (latitud >= -90 AND latitud <= 90)),
    CONSTRAINT chk_serenazgo_inc_lon CHECK (longitud IS NULL OR (longitud >= -180 AND longitud <= 180))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_codigo ON serenazgo_incidencias(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_fecha ON serenazgo_incidencias(fecha_reporte);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_estado ON serenazgo_incidencias(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_prioridad ON serenazgo_incidencias(prioridad);
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_sector ON serenazgo_incidencias(sector_id);

-- 14. Alertas Ciudadanas de Seguridad
CREATE TABLE IF NOT EXISTS serenazgo_alertas_ciudadanas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    canal_origen VARCHAR(50) NOT NULL DEFAULT 'PORTAL_CIUDADANO',
    descripcion TEXT NOT NULL,
    fecha_recepcion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ubicacion_referencia VARCHAR(255) NOT NULL,
    latitud NUMERIC(10, 7),
    longitud NUMERIC(10, 7),
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    reportante_usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    reportante_anonimo BOOLEAN NOT NULL DEFAULT FALSE,
    reportante_telefono_contacto VARCHAR(30),
    incidencia_id UUID REFERENCES serenazgo_incidencias(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_alerta_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'CONVERTIDA_EN_INCIDENCIA', 'DESCARTADA')),
    CONSTRAINT chk_serenazgo_alerta_lat CHECK (latitud IS NULL OR (latitud >= -90 AND latitud <= 90)),
    CONSTRAINT chk_serenazgo_alerta_lon CHECK (longitud IS NULL OR (longitud >= -180 AND longitud <= 180))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_codigo ON serenazgo_alertas_ciudadanas(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_estado ON serenazgo_alertas_ciudadanas(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_fecha ON serenazgo_alertas_ciudadanas(fecha_recepcion);

-- 15. Intervenciones de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_intervenciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    incidencia_id UUID NOT NULL REFERENCES serenazgo_incidencias(id) ON DELETE RESTRICT,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP WITH TIME ZONE,
    responsable_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    unidad_movil_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    descripcion_actuacion TEXT NOT NULL,
    resultado VARCHAR(50) NOT NULL DEFAULT 'EN_CURSO',
    estado VARCHAR(30) NOT NULL DEFAULT 'INICIADA',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_interv_estado CHECK (estado IN ('INICIADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_codigo ON serenazgo_intervenciones(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_incidencia ON serenazgo_intervenciones(incidencia_id);

-- 16. Participantes en Intervenciones
CREATE TABLE IF NOT EXISTS serenazgo_intervenciones_participantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    intervencion_id UUID NOT NULL REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    funcion_desempenada VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    CONSTRAINT uq_serenazgo_interv_personal UNIQUE (intervencion_id, personal_id)
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_part ON serenazgo_intervenciones_participantes(intervencion_id);

-- 17. Patrullajes de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_patrullajes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    sector_id UUID NOT NULL REFERENCES serenazgo_sectores(id) ON DELETE RESTRICT,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    unidad_movil_id UUID REFERENCES serenazgo_unidades(id) ON DELETE SET NULL,
    tipo_patrullaje VARCHAR(40) NOT NULL DEFAULT 'PATRULLAJE_MUNICIPAL',
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP WITH TIME ZONE,
    responsable_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADO',
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_pat_estado CHECK (estado IN ('PROGRAMADO', 'EN_CURSO', 'FINALIZADO', 'INTERRUMPIDO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_patrullaje_codigo ON serenazgo_patrullajes(codigo);
CREATE INDEX IF NOT EXISTS idx_serenazgo_patrullaje_sector ON serenazgo_patrullajes(sector_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_patrullaje_estado ON serenazgo_patrullajes(estado);

-- 18. Participantes en Patrullaje
CREATE TABLE IF NOT EXISTS serenazgo_patrullajes_participantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patrullaje_id UUID NOT NULL REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    personal_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    funcion_desempenada VARCHAR(100) NOT NULL,
    fecha_incorporacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_retiro TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_serenazgo_patrullaje_personal UNIQUE (patrullaje_id, personal_id)
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_pat_part ON serenazgo_patrullajes_participantes(patrullaje_id);

-- 19. Puntos de Recorrido de Patrullaje
CREATE TABLE IF NOT EXISTS serenazgo_patrullajes_puntos_recorrido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patrullaje_id UUID NOT NULL REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitud NUMERIC(10, 7) NOT NULL,
    longitud NUMERIC(10, 7) NOT NULL,
    origen_dato VARCHAR(50) NOT NULL DEFAULT 'MARCACION_OPERADOR',
    observaciones TEXT,
    CONSTRAINT chk_serenazgo_recorrido_lat CHECK (latitud >= -90 AND latitud <= 90),
    CONSTRAINT chk_serenazgo_recorrido_lon CHECK (longitud >= -180 AND longitud <= 180)
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_recorrido_patrullaje ON serenazgo_patrullajes_puntos_recorrido(patrullaje_id);

-- 20. Evidencias de Seguridad Ciudadana
CREATE TABLE IF NOT EXISTS serenazgo_evidencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incidencia_id UUID REFERENCES serenazgo_incidencias(id) ON DELETE CASCADE,
    intervencion_id UUID REFERENCES serenazgo_intervenciones(id) ON DELETE CASCADE,
    patrullaje_id UUID REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    tipo_evidencia VARCHAR(40) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_registro_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    nivel_acceso VARCHAR(30) NOT NULL DEFAULT 'CONFIDENCIAL',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_evidencia_tipo CHECK (tipo_evidencia IN ('FOTOGRAFIA', 'VIDEO', 'AUDIO', 'ACTA_ESCRITA', 'PARTE_INFORMATIVO', 'OTRO')),
    CONSTRAINT chk_serenazgo_evidencia_acceso CHECK (nivel_acceso IN ('PUBLICO', 'INTERNO', 'CONFIDENCIAL', 'RESERVADO'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_evidencia_incidencia ON serenazgo_evidencias(incidencia_id);

-- 21. Supervisiones de Serenazgo
CREATE TABLE IF NOT EXISTS serenazgo_supervisiones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    supervisor_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    patrullaje_id UUID REFERENCES serenazgo_patrullajes(id) ON DELETE SET NULL,
    tipo_supervision VARCHAR(40) NOT NULL DEFAULT 'RUTINA',
    resultado VARCHAR(50) NOT NULL DEFAULT 'CONFORME',
    observaciones TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_superv_tipo CHECK (tipo_supervision IN ('RUTINA', 'INOPINADA', 'CONTROL_CALIDAD', 'SEGUIMIENTO')),
    CONSTRAINT chk_serenazgo_superv_estado CHECK (estado IN ('REGISTRADA', 'ATENDIDA', 'ARCHIVADA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_superv_codigo ON serenazgo_supervisiones(codigo);

-- ==============================================================================
-- 22. Poblado de Catálogos Técnicos Iniciales
-- ==============================================================================

-- Cargos Operativos
INSERT INTO serenazgo_cargos_operativos (codigo, nombre, descripcion, nivel_jerarquico) VALUES
    ('SERENO_PATRULLERO', 'Sereno Patrullero', 'Efectivo asignado a patrullaje a pie o vehicular', 1),
    ('CHOFER_PATRULLERO', 'Chofer de Unidad Móvil', 'Conductor de camioneta o motocicleta operativa', 1),
    ('OPERADOR_CENTRAL', 'Operador de Central de Comunicaciones', 'Recepción de llamadas, despacho de alertas y monitoreo', 2),
    ('SUPERVISOR_TURNO', 'Supervisor de Turno', 'Coordinación táctica de patrullajes y relevo de guardia', 3),
    ('COORDINADOR_GENERAL', 'Coordinador General de Seguridad', 'Gestión operativa del cuerpo de serenazgo distrital', 4)
ON CONFLICT (codigo) DO NOTHING;

-- Tipos de Incidencia Base (Estándar de Seguridad Municipal)
INSERT INTO serenazgo_tipos_incidencia (codigo, nombre, descripcion, categoria, prioridad_sugerida, requiere_unidad) VALUES
    ('ALTERACION_ORDEN', 'Alteración del Orden Público', 'Gritos, conatos de riña o perturbación de la tranquilidad vecinal', 'ORDEN_PUBLICO', 'MEDIA', false),
    ('RIESGO_VIA_PUBLICA', 'Riesgo o Peligro en Vía Pública', 'Obstáculos, zanjas sin señalización, cables caídos', 'RIESGO_URBANO', 'MEDIA', false),
    ('ACCIDENTE_TRANSITO', 'Accidente de Tránsito Reportado', 'Colisión o despiste en vías locales con requerimiento de apoyo', 'SEGURIDAD_VIAL', 'ALTA', true),
    ('SOLICITUD_APOYO', 'Solicitud de Apoyo Comunitario', 'Auxilio a ciudadanos vulnerables, adultos mayores o emergencias menores', 'APOYO_COMUNITARIO', 'BAJA', false),
    ('SITUACION_SOSPECHOSA', 'Situación Sospechosa Reportada', 'Presencia no justificada de personas o vehículos merodeando inmuebles', 'ORDEN_PUBLICO', 'MEDIA', true),
    ('EMERGENCIA_MEDICA_APOYO', 'Apoyo en Emergencia Médica', 'Comunicación de emergencia de salud para enlace con posta/centro médico', 'EMERGENCIAS', 'CRITICA', true),
    ('RUIDOS_MOLESTOS', 'Ruidos Molestos o Fiestas Clandestinas', 'Emisión sonora excesiva fuera del horario permitido', 'ORDEN_PUBLICO', 'BAJA', false),
    ('OTRO_APOYO_VECINAL', 'Otro Reporte de Apoyo Vecinal', 'Ocurrencias diversas no tipificadas sujetas a auxilio de serenazgo', 'APOYO_COMUNITARIO', 'BAJA', false)
ON CONFLICT (codigo) DO NOTHING;

-- Turnos Técnicos de Servicio
INSERT INTO serenazgo_turnos (codigo, nombre, hora_inicio, hora_fin, cruza_medianoche) VALUES
    ('TURNO_MANANA', 'Turno Mañana (07:00 - 15:00)', '07:00:00', '15:00:00', false),
    ('TURNO_TARDE', 'Turno Tarde (15:00 - 23:00)', '15:00:00', '23:00:00', false),
    ('TURNO_NOCHE', 'Turno Noche (23:00 - 07:00)', '23:00:00', '07:00:00', true)
ON CONFLICT (codigo) DO NOTHING;

-- ==============================================================================
-- 23. Permisos Institucionales para Seguridad Ciudadana y Serenazgo
-- ==============================================================================

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_MODULO_ACCEDER', 'Acceso al Módulo de Seguridad Ciudadana', 'VIEW',
       'Permite el acceso al panel principal del módulo de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_CATALOGOS_VER', 'Ver Catálogos de Seguridad', 'VIEW',
       'Permite consultar tipos de incidencia, cargos, turnos y sectores', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PERSONAL_VER', 'Ver Padrón de Personal de Serenazgo', 'VIEW',
       'Permite visualizar la nómina y cargos operativos de serenos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_UNIDADES_VER', 'Ver Unidades y Equipamiento', 'VIEW',
       'Permite visualizar unidades móviles y equipamiento de seguridad', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_SECTORES_VER', 'Ver Sectores y Cuadrantes', 'VIEW',
       'Permite visualizar sectores, zonas y puntos de referencia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_TURNOS_VER', 'Ver Turnos y Programación de Guardia', 'VIEW',
       'Permite consultar turnos y servicios programados de serenos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INCIDENCIAS_VER', 'Ver Registro de Incidencias', 'VIEW',
       'Permite consultar el historial y estado de incidencias registradas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ALERTAS_VER', 'Ver Alertas Ciudadanas', 'VIEW',
       'Permite consultar alertas recibidas desde el portal ciudadano', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_INTERVENCIONES_VER', 'Ver Intervenciones Operativas', 'VIEW',
       'Permite consultar intervenciones y actuaciones de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_VER', 'Ver Patrullajes y Rutas', 'VIEW',
       'Permite consultar patrullajes realizados y recorridos de vigilancia', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_EVIDENCIAS_VER', 'Ver Evidencias y Partes Informativos', 'VIEW',
       'Permite consultar documentos probatorios y archivos de incidencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_REPORTES_VER', 'Ver Estadísticas e Indicadores de Serenazgo', 'VIEW',
       'Permite consultar tableros de control y estadísticas operativas', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación a Roles Institucionales
-- 1) Administrador y Superadministrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'SERENAZGO_%'
ON CONFLICT DO NOTHING;

-- 2) Efectivo de Serenazgo (ROLE_SERENO)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_MODULO_ACCEDER',
      'SERENAZGO_CATALOGOS_VER',
      'SERENAZGO_SECTORES_VER',
      'SERENAZGO_TURNOS_VER',
      'SERENAZGO_INCIDENCIAS_VER',
      'SERENAZGO_ALERTAS_VER',
      'SERENAZGO_INTERVENCIONES_VER',
      'SERENAZGO_PATRULLAJES_VER'
  )
ON CONFLICT DO NOTHING;
