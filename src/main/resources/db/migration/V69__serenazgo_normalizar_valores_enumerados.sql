-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V69: Normalización de valores enumerados del módulo Seguridad
-- Ciudadana / Serenazgo.
--
-- Contexto: cargas masivas de datos de prueba dejaron en columnas mapeadas con
-- @Enumerated(EnumType.STRING) valores que no existen en los enums de la
-- aplicación. Al leerlos JPA lanzaba "No enum constant ..." y las pantallas
-- Intervenciones, Equipamiento, Supervisión y Estadísticas respondían 500.
--
-- La migración es idempotente y no destructiva: solo reescribe los valores
-- huérfanos al valor válido equivalente y no altera estructura ni otros módulos.
-- ==============================================================================

-- 1. Resultado de intervenciones (ResultadoIntervencionSerenazgo)
UPDATE serenazgo_intervenciones
   SET resultado = 'ACTUACION_REGISTRADA'
 WHERE resultado = 'CONFORME';

-- 2. Tipo de equipo (TipoEquipamientoSerenazgo)
UPDATE serenazgo_equipamiento SET tipo_equipo = 'BOTIQUIN'           WHERE tipo_equipo = 'BOTIQUIN_PRIMEROS_AUXILIOS';
UPDATE serenazgo_equipamiento SET tipo_equipo = 'RADIO_COMUNICACION' WHERE tipo_equipo = 'RADIO_PORTATIL';
UPDATE serenazgo_equipamiento SET tipo_equipo = 'CHALECO_DISTINTIVO' WHERE tipo_equipo = 'CHALECO_REFLECTIVO';
UPDATE serenazgo_equipamiento SET tipo_equipo = 'LINTERNA'           WHERE tipo_equipo = 'LINTERNA_TACTICA';
UPDATE serenazgo_equipamiento SET tipo_equipo = 'CAMARA_TESTIGO'     WHERE tipo_equipo = 'CAMARA_CORPORAL';

-- 3. Eventos de atención (TipoEventoAtencion)
UPDATE serenazgo_atencion_eventos
   SET tipo_evento = 'DESPACHO_OPERATIVO'
 WHERE tipo_evento = 'DESPACHO_UNIDAD';

-- 4. Historial de incidencias (TipoEventoHistorialIncidencia)
UPDATE serenazgo_incidencia_historial
   SET tipo_evento = 'CAMBIO_ESTADO'
 WHERE tipo_evento = 'CAMBIO_ESTADO_OPERATIVO';

-- 5. Actuaciones de intervención (TipoActuacionIntervencion)
UPDATE serenazgo_intervencion_actuaciones
   SET tipo_actuacion = 'VERIFICACION_LUGAR'
 WHERE tipo_actuacion = 'CONSTATACION_PREVENTIVA';
