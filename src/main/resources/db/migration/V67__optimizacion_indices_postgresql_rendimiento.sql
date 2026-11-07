-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V67: Optimización Integral de Índices de Alto Rendimiento en PostgreSQL
--                Cobertura: Núcleo, Mesa de Partes, Mercados, Serenazgo y Recaudación
-- ==============================================================================

-- 1. NÚCLEO DEL SISTEMA, AUTENTICACIÓN Y AUDITORÍA
-- Búsqueda de usuarios por persona asociada (usado en cada login y verificación JWT)
CREATE INDEX IF NOT EXISTS idx_usuarios_persona_fk 
    ON usuarios(persona_id) 
    WHERE persona_id IS NOT NULL;

-- Relación de roles y usuarios
CREATE INDEX IF NOT EXISTS idx_usuario_roles_comp 
    ON usuario_roles(rol_id, usuario_id);

-- Consultas de bitácora y auditoría administrativa
CREATE INDEX IF NOT EXISTS idx_auditoria_entidad_comp 
    ON auditoria_administrativa(entidad_afectada, entidad_id);

CREATE INDEX IF NOT EXISTS idx_auditoria_trace_id 
    ON auditoria_administrativa(trace_id) 
    WHERE trace_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_auditoria_usr_fec 
    ON auditoria_administrativa(usuario_responsable, created_at DESC);

-- Notificaciones institucionales no leídas
CREATE INDEX IF NOT EXISTS idx_notificaciones_usr_no_leida 
    ON notificaciones(usuario_id, fecha_creacion DESC) 
    WHERE leida = false;

-- 2. SERVICIO 1: MESA DE PARTES VIRTUAL Y TRÁMITE DOCUMENTARIO
-- Consultas de expedientes por solicitante / persona
CREATE INDEX IF NOT EXISTS idx_mpv_expedientes_solicitante 
    ON mpv_expedientes(solicitante_id, created_at DESC) 
    WHERE solicitante_id IS NOT NULL;

-- Consultas por tipo de trámite y estado operativo
CREATE INDEX IF NOT EXISTS idx_mpv_expedientes_tipo_est 
    ON mpv_expedientes(tipo_tramite_id, estado_actual);

-- Requisitos documentales verificados por expediente
CREATE INDEX IF NOT EXISTS idx_mpv_rev_req_exp_doc 
    ON mpv_revision_requisitos(expediente_id, documento_id);

-- 3. SERVICIO 2: MERCADOS MUNICIPALES
-- Puestos por mercado y estado operativo
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_mkt_est 
    ON mercados_puestos(mercado_id, estado);

-- Obligaciones tributarias de mercados por comerciante y estado de pago
CREATE INDEX IF NOT EXISTS idx_mercados_oblig_comerc_est 
    ON mercados_obligaciones(comerciante_id, estado, fecha_vencimiento);

-- Contratos vigentes de puestos
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_puesto 
    ON mercados_contratos(puesto_id, estado);

-- Pagos de mercados por comerciante y fecha
CREATE INDEX IF NOT EXISTS idx_mercados_pagos_comerc_fec 
    ON mercados_pagos(comerciante_id, fecha_pago DESC);

-- 4. SERVICIO 3: SEGURIDAD CIUDADANA Y SERENAZGO
-- Incidencias por sector territorial y estado
CREATE INDEX IF NOT EXISTS idx_serenazgo_incid_sec_est 
    ON serenazgo_incidencias(sector_id, estado, fecha_reporte DESC);

-- Alertas ciudadanas activas para despacho
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_activas 
    ON serenazgo_alertas_ciudadanas(estado, fecha_recepcion DESC) 
    WHERE estado IN ('RECIBIDA', 'EN_ATENCION');

-- Patrullajes por unidad móvil y turno
CREATE INDEX IF NOT EXISTS idx_serenazgo_patrullaje_unidad 
    ON serenazgo_patrullajes(unidad_movil_id, fecha_inicio DESC) 
    WHERE unidad_movil_id IS NOT NULL;

-- Intervenciones vinculadas a incidencias
CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_incid_fec 
    ON serenazgo_intervenciones(incidencia_id, created_at DESC);

-- 5. SERVICIO 4: RECAUDACIÓN TRIBUTARIA MUNICIPAL
-- Obligaciones por periodo y estado de cobro
CREATE INDEX IF NOT EXISTS idx_trib_oblig_periodo_est 
    ON tributaria_obligaciones(periodo_id, estado, fecha_vencimiento);

-- Obligaciones por predio
CREATE INDEX IF NOT EXISTS idx_trib_oblig_predio_est 
    ON tributaria_obligaciones(predio_id, estado);

-- Pagos tributarios por contribuyente
CREATE INDEX IF NOT EXISTS idx_trib_pagos_contrib_fec 
    ON tributaria_pagos(contribuyente_id, fecha_hora_pago DESC);

-- Recibos y constancias emitidas
CREATE INDEX IF NOT EXISTS idx_trib_docs_pago_tipo_fec 
    ON tributaria_documentos_pago(pago_tributario_id, tipo_documento_id, fecha_emision DESC);

-- Convenios de fraccionamiento activos por contribuyente
CREATE INDEX IF NOT EXISTS idx_trib_conv_fracc_contrib_est 
    ON tributaria_convenios_fraccionamiento(contribuyente_id, estado, fecha_aprobacion DESC);

-- Cuotas de fraccionamiento pendientes y vencidas
CREATE INDEX IF NOT EXISTS idx_trib_cuotas_fracc_est_venc 
    ON tributaria_cuotas_fraccionamiento(cronograma_id, estado, fecha_vencimiento);

-- Seguimiento de deudas por clasificación
CREATE INDEX IF NOT EXISTS idx_trib_seg_deuda_clasif_est 
    ON tributaria_seguimiento_deudas(clasificacion, estado_seguimiento);
