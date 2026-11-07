-- ==============================================================================
-- V41_2: Homogeneizar restricción CHECK de estado_reserva para equipamiento
-- ==============================================================================

ALTER TABLE serenazgo_programacion_equipamiento
    DROP CONSTRAINT IF EXISTS chk_prog_equipo_estado;

ALTER TABLE serenazgo_programacion_equipamiento
    ADD CONSTRAINT chk_prog_equipo_estado
    CHECK (estado_reserva IN ('RESERVADA', 'ASIGNADA', 'LIBERADA', 'CANCELADA', 'RESERVADO', 'ENTREGADO', 'DEVUELTO', 'CANCELADO'));
