-- ==============================================================================
-- MIGRACIÓN V41.1: COLUMNA COLOR_HEX EN TURNOS DE SERENAZGO
-- ==============================================================================
ALTER TABLE serenazgo_turnos
    ADD COLUMN IF NOT EXISTS color_hex VARCHAR(20) DEFAULT '#2563eb';
