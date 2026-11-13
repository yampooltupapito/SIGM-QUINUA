-- =====================================================================================
-- V70: Recaudación Tributaria - normalizar valores enumerados huérfanos
--
-- Los datos sintéticos de prueba cargaron en varias columnas @Enumerated(STRING) valores
-- que NO existen en los enums de Java. Al leer esas filas Hibernate lanza
-- "No enum constant ..." y las pantallas del menú Recaudación Tributaria responden 500.
--
-- Esta migración reemplaza cada valor huérfano por la constante válida equivalente.
-- Es idempotente (solo toca filas con el valor huérfano) y afecta únicamente a tablas
-- tributaria_*. No cambia estructura, índices ni otras tablas.
-- =====================================================================================

-- Receptor de cargo de notificación
UPDATE tributaria_cargos_recepcion
   SET tipo_vinculo_receptor = 'TITULAR_CONTRIBUYENTE'
 WHERE tipo_vinculo_receptor = 'TITULAR';

-- Construcciones de predio
UPDATE tributaria_construcciones_predio
   SET material_predominante = 'ADOBE_TAPIA'
 WHERE material_predominante = 'PIEDRA_BARRO';

-- Contribuyentes
UPDATE tributaria_contribuyentes
   SET estado_verificacion = 'VERIFICADO_DOCUMENTALMENTE'
 WHERE estado_verificacion = 'VERIFICADO';

-- Cronogramas de fraccionamiento
UPDATE tributaria_cronogramas_fraccionamiento
   SET estado = 'VIGENTE'
 WHERE estado = 'APROBADO';

-- Gestiones de deuda
UPDATE tributaria_gestiones_deuda
   SET tipo_gestion = 'CONTACTO_ADMINISTRATIVO'
 WHERE tipo_gestion IN ('CORREO_RECORDATORIO', 'LLAMADA_PREVENTIVA', 'ESQUELA_ORIENTATIVA');

UPDATE tributaria_gestiones_deuda
   SET tipo_gestion = 'ATENCION_PRESENCIAL'
 WHERE tipo_gestion = 'VISITA_INFORMATIVA';

-- Historiales
UPDATE tributaria_historial_notificaciones
   SET tipo_evento = 'CREACION'
 WHERE tipo_evento = 'EMISION_NOTIFICACION';

UPDATE tributaria_historial_obligaciones
   SET accion = 'GENERACION'
 WHERE accion = 'EMISION_SINTETICA';

-- Notificaciones
UPDATE tributaria_notificaciones
   SET estado_administrativo = 'CERRADA'
 WHERE estado_administrativo = 'ENTREGADA';

UPDATE tributaria_notificaciones
   SET estado_administrativo = 'EN_GESTION'
 WHERE estado_administrativo = 'PROGRAMADA';

UPDATE tributaria_notificaciones
   SET estado_administrativo = 'AUTORIZADA'
 WHERE estado_administrativo = 'EMITIDA';

UPDATE tributaria_notificaciones
   SET estado_entrega = 'ENTREGADA_CON_EVIDENCIA'
 WHERE estado_entrega = 'ENTREGADA';

UPDATE tributaria_notificaciones
   SET estado_validez_legal = 'VALIDADA_POR_RESPONSABLE'
 WHERE estado_validez_legal = 'VALIDA';

-- Obligaciones tributarias.
-- Todas las filas con estos valores tienen estado_financiero = PENDIENTE y estado_exigibilidad
-- = EXIGIBLE (sin pagos aplicados), por lo que se mapean a VIGENTE para mantener la coherencia
-- con su situación financiera real.
UPDATE tributaria_obligaciones
   SET estado = 'VIGENTE'
 WHERE estado IN ('EMITIDA', 'CANCELADA');

-- Pagos
UPDATE tributaria_pagos SET medio_pago = 'EFECTIVO_CAJA'          WHERE medio_pago = 'EFECTIVO';
UPDATE tributaria_pagos SET medio_pago = 'TRANSFERENCIA_BANCARIA' WHERE medio_pago = 'TRANSFERENCIA';
UPDATE tributaria_pagos SET medio_pago = 'BILLETERA_DIGITAL'      WHERE medio_pago = 'PAGO_MOVIL';
UPDATE tributaria_pagos SET medio_pago = 'DEPOSITO_CUENTA'        WHERE medio_pago = 'DEPOSITO';

-- Predios
UPDATE tributaria_predios SET tipo_predio = 'RURAL'      WHERE tipo_predio = 'RUSTICO';
UPDATE tributaria_predios SET uso_declarado = 'ARTESANAL' WHERE uso_declarado = 'TALLER_ARTESANAL';

-- Seguimiento de deudas
UPDATE tributaria_seguimiento_deudas
   SET clasificacion = 'VENCIDA_EXIGIBLE'
 WHERE clasificacion IN ('ORDINARIA', 'PRE_COACTIVA');

UPDATE tributaria_seguimiento_deudas
   SET clasificacion = 'PENDIENTE_NO_VENCIDA'
 WHERE clasificacion = 'PREVENTIVA';
