-- ==============================================================================
-- SIGM-QUINUA | Módulo 3: Seguridad Ciudadana y Serenazgo
-- V51: Auditoría Técnica, Optimización de Índices Compuestos e Integridad de Trazabilidad
-- Prompt 50 de 150: Cierre Técnico e Integración Final del Módulo de Serenazgo
-- ==============================================================================

-- 1. Índices compuestos para trazabilidad cruzada y consultas de supervisión
CREATE INDEX IF NOT EXISTS idx_serenazgo_sup_seg_tipo_reg 
    ON serenazgo_supervision_seguimientos(tipo_registro_origen, registro_origen_id);

CREATE INDEX IF NOT EXISTS idx_serenazgo_doc_rel_entidad 
    ON serenazgo_documentos_relaciones(tipo_entidad, entidad_id);

-- 2. Índices compuestos para optimización de agregaciones territoriales y temporales
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_sec_fecha 
    ON serenazgo_incidencias(sector_id, fecha_reporte DESC);

CREATE INDEX IF NOT EXISTS idx_serenazgo_pat_sec_fecha 
    ON serenazgo_patrullajes(sector_id, fecha_inicio DESC);

CREATE INDEX IF NOT EXISTS idx_serenazgo_atn_inc_estado 
    ON serenazgo_atenciones_incidencia(incidencia_id, estado);

CREATE INDEX IF NOT EXISTS idx_serenazgo_interv_atn_inc 
    ON serenazgo_intervenciones(incidencia_id, atencion_id);

-- 3. Índice para acelerar búsquedas de duplicados georreferenciados en incidencias
CREATE INDEX IF NOT EXISTS idx_serenazgo_inc_geo 
    ON serenazgo_incidencias(latitud, longitud) 
    WHERE latitud IS NOT NULL AND longitud IS NOT NULL;

-- 4. Índice para búsquedas rápidas por fecha y canal en alertas ciudadanas
CREATE INDEX IF NOT EXISTS idx_serenazgo_alt_canal_fecha 
    ON serenazgo_alertas_ciudadanas(canal_origen, fecha_recepcion DESC);
