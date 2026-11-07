-- ==============================================================================
-- SIGM-QUINUA: MIGRACIÓN V46
-- Módulo 3: Seguridad Ciudadana y Serenazgo
-- Subsistema: Gestión Integral de Patrullajes, Rutas y Recorridos (Prompt 45 de 150)
-- ==============================================================================

-- 1. Secuencia correlativa para Rutas de Patrullaje
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_rutas_patrullaje START WITH 1 INCREMENT BY 1;

-- 2. Actualización de Estados permitidos en serenazgo_patrullajes
ALTER TABLE serenazgo_patrullajes DROP CONSTRAINT IF EXISTS chk_serenazgo_pat_estado;
ALTER TABLE serenazgo_patrullajes ADD CONSTRAINT chk_serenazgo_pat_estado
    CHECK (estado IN ('BORRADOR', 'PROGRAMADO', 'ASIGNADO', 'EN_CURSO', 'EN_PAUSA', 'FINALIZADO', 'CANCELADO', 'INTERRUMPIDO'));

-- 3. Tabla de Rutas de Patrullaje Autorizadas
CREATE TABLE IF NOT EXISTS serenazgo_rutas_patrullaje (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    sector_id UUID NOT NULL REFERENCES serenazgo_sectores(id) ON DELETE RESTRICT,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    distancia_estimada_km NUMERIC(6, 2),
    tiempo_estimado_min INTEGER,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    version_ruta INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_serenazgo_ruta_estado CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_rutas_sector ON serenazgo_rutas_patrullaje(sector_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_rutas_estado ON serenazgo_rutas_patrullaje(estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_rutas_codigo ON serenazgo_rutas_patrullaje(codigo);

-- 4. Puntos de Control de la Ruta Autorizada (Waypoints / Hitos)
CREATE TABLE IF NOT EXISTS serenazgo_rutas_puntos_control (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruta_id UUID NOT NULL REFERENCES serenazgo_rutas_patrullaje(id) ON DELETE CASCADE,
    orden INTEGER NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    referencia_ubicacion VARCHAR(255),
    latitud NUMERIC(10, 7),
    longitud NUMERIC(10, 7),
    tiempo_estimado_desde_inicio_min INTEGER,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_serenazgo_ruta_punto_orden UNIQUE (ruta_id, orden),
    CONSTRAINT chk_serenazgo_punto_lat CHECK (latitud IS NULL OR (latitud >= -90 AND latitud <= 90)),
    CONSTRAINT chk_serenazgo_punto_lon CHECK (longitud IS NULL OR (longitud >= -180 AND longitud <= 180))
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_rutas_puntos_ruta ON serenazgo_rutas_puntos_control(ruta_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_rutas_puntos_orden ON serenazgo_rutas_puntos_control(ruta_id, orden);

-- 5. Campos complementarios en serenazgo_patrullajes
ALTER TABLE serenazgo_patrullajes
    ADD COLUMN IF NOT EXISTS ruta_id UUID REFERENCES serenazgo_rutas_patrullaje(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS programacion_servicio_id UUID REFERENCES serenazgo_programacion_servicios(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS fecha_hora_inicio_programada TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS fecha_hora_fin_programada TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS fecha_hora_inicio_real TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS fecha_hora_fin_real TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS objetivo_operativo TEXT,
    ADD COLUMN IF NOT EXISTS descripcion TEXT,
    ADD COLUMN IF NOT EXISTS resultado_administrativo TEXT,
    ADD COLUMN IF NOT EXISTS km_inicio NUMERIC(8, 2),
    ADD COLUMN IF NOT EXISTS km_fin NUMERIC(8, 2),
    ADD COLUMN IF NOT EXISTS motivo_pausa TEXT,
    ADD COLUMN IF NOT EXISTS motivo_cancelacion TEXT,
    ADD COLUMN IF NOT EXISTS cambio_ruta_motivo TEXT,
    ADD COLUMN IF NOT EXISTS es_extraordinario BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS motivo_extraordinario TEXT,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Sincronizar fechas programadas con existentes si estuvieran nulas
UPDATE serenazgo_patrullajes
SET fecha_hora_inicio_programada = fecha_inicio,
    fecha_hora_fin_programada = fecha_fin
WHERE fecha_hora_inicio_programada IS NULL;

CREATE INDEX IF NOT EXISTS idx_serenazgo_pat_ruta ON serenazgo_patrullajes(ruta_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_pat_prog ON serenazgo_patrullajes(programacion_servicio_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_pat_fechas_prog ON serenazgo_patrullajes(fecha_hora_inicio_programada, fecha_hora_fin_programada);

-- 6. Registro de Pasos por Puntos de Control (Checkpoints Ejecutados u Omitidos)
CREATE TABLE IF NOT EXISTS serenazgo_patrullaje_visitas_puntos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patrullaje_id UUID NOT NULL REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    punto_control_id UUID NOT NULL REFERENCES serenazgo_rutas_puntos_control(id) ON DELETE RESTRICT,
    orden_visita INTEGER NOT NULL,
    fecha_hora_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    personal_registro_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    metodo_registro VARCHAR(40) NOT NULL DEFAULT 'MANUAL',
    estado_visita VARCHAR(30) NOT NULL DEFAULT 'VISITADO',
    motivo_omision TEXT,
    latitud_registro NUMERIC(10, 7),
    longitud_registro NUMERIC(10, 7),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pat_visita_metodo CHECK (metodo_registro IN ('MANUAL', 'GPS_VERIFICADO', 'SUPERVISADO', 'RADIO_OPERADOR')),
    CONSTRAINT chk_pat_visita_estado CHECK (estado_visita IN ('VISITADO', 'OMITIDO')),
    CONSTRAINT chk_pat_visita_lat CHECK (latitud_registro IS NULL OR (latitud_registro >= -90 AND latitud_registro <= 90)),
    CONSTRAINT chk_pat_visita_lon CHECK (longitud_registro IS NULL OR (longitud_registro >= -180 AND longitud_registro <= 180))
);

CREATE INDEX IF NOT EXISTS idx_pat_visitas_patrullaje ON serenazgo_patrullaje_visitas_puntos(patrullaje_id);
CREATE INDEX IF NOT EXISTS idx_pat_visitas_punto ON serenazgo_patrullaje_visitas_puntos(punto_control_id);

-- 7. Registro de Novedades Operativas durante el Patrullaje
CREATE TABLE IF NOT EXISTS serenazgo_patrullaje_novedades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patrullaje_id UUID NOT NULL REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    tipo_novedad VARCHAR(50) NOT NULL,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT NOT NULL,
    personal_reporta_id UUID NOT NULL REFERENCES serenazgo_personal(id) ON DELETE RESTRICT,
    incidencia_vinculada_id UUID REFERENCES serenazgo_incidencias(id) ON DELETE SET NULL,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pat_novedad_tipo CHECK (tipo_novedad IN ('OBSERVACION_PREVENTIVA', 'CAMBIO_RECORRIDO', 'PAUSA_OPERATIVA', 'SOLICITUD_APOYO', 'INCIDENCIA_DETECTADA', 'DESPERFECTO_UNIDAD', 'OTRO'))
);

CREATE INDEX IF NOT EXISTS idx_pat_novedades_patrullaje ON serenazgo_patrullaje_novedades(patrullaje_id);
CREATE INDEX IF NOT EXISTS idx_pat_novedades_tipo ON serenazgo_patrullaje_novedades(tipo_novedad);

-- 8. Asignación de Equipamiento Operativo a Patrullajes
CREATE TABLE IF NOT EXISTS serenazgo_patrullaje_equipamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patrullaje_id UUID NOT NULL REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    equipamiento_id UUID NOT NULL REFERENCES serenazgo_equipamiento(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL DEFAULT 1,
    estado_entrega VARCHAR(100),
    estado_devolucion VARCHAR(100),
    fecha_devolucion TIMESTAMP WITH TIME ZONE,
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pat_equip_patrullaje ON serenazgo_patrullaje_equipamiento(patrullaje_id);
CREATE INDEX IF NOT EXISTS idx_pat_equip_equipamiento ON serenazgo_patrullaje_equipamiento(equipamiento_id);

-- 9. Historial y Trazabilidad Operativa de Patrullajes
CREATE TABLE IF NOT EXISTS serenazgo_patrullaje_historial (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patrullaje_id UUID NOT NULL REFERENCES serenazgo_patrullajes(id) ON DELETE CASCADE,
    accion VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(40),
    estado_nuevo VARCHAR(40),
    descripcion TEXT NOT NULL,
    username VARCHAR(100) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pat_hist_patrullaje ON serenazgo_patrullaje_historial(patrullaje_id);
CREATE INDEX IF NOT EXISTS idx_pat_hist_fecha ON serenazgo_patrullaje_historial(fecha);

-- 10. Vinculaciones de Incidencias e Intervenciones con Patrullajes
ALTER TABLE serenazgo_incidencias
    ADD COLUMN IF NOT EXISTS patrullaje_id UUID REFERENCES serenazgo_patrullajes(id) ON DELETE SET NULL;

ALTER TABLE serenazgo_intervenciones
    ADD COLUMN IF NOT EXISTS patrullaje_id UUID REFERENCES serenazgo_patrullajes(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_serenazgo_incidencias_patrullaje ON serenazgo_incidencias(patrullaje_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_intervenciones_patrullaje ON serenazgo_intervenciones(patrullaje_id);

-- 11. Poblado de Rutas y Puntos de Control Oficiales de Quinua
DO $$
DECLARE
    v_sector_urbano UUID;
    v_sector_pampa UUID;
    v_sector_artesanal UUID;
    v_ruta1_id UUID;
    v_ruta2_id UUID;
    v_ruta3_id UUID;
BEGIN
    -- Identificar o crear sectores oficiales de Quinua
    SELECT id INTO v_sector_urbano FROM serenazgo_sectores WHERE codigo = 'SEC-01' LIMIT 1;
    IF v_sector_urbano IS NULL THEN
        INSERT INTO serenazgo_sectores (codigo, nombre, descripcion, color_hex, estado)
        VALUES ('SEC-01', 'Sector 01 - Casco Urbano y Plaza Mayor', 'Área urbana central, sede municipal, templo y mercado principal', '#3b82f6', 'ACTIVO')
        RETURNING id INTO v_sector_urbano;
    END IF;

    SELECT id INTO v_sector_pampa FROM serenazgo_sectores WHERE codigo = 'SEC-02' LIMIT 1;
    IF v_sector_pampa IS NULL THEN
        INSERT INTO serenazgo_sectores (codigo, nombre, descripcion, color_hex, estado)
        VALUES ('SEC-02', 'Sector 02 - Santuario Histórico de la Pampa de Quinua', 'Área monumental, campo de batalla, obelisco y miradores turísticos', '#10b981', 'ACTIVO')
        RETURNING id INTO v_sector_pampa;
    END IF;

    SELECT id INTO v_sector_artesanal FROM serenazgo_sectores WHERE codigo = 'SEC-03' LIMIT 1;
    IF v_sector_artesanal IS NULL THEN
        INSERT INTO serenazgo_sectores (codigo, nombre, descripcion, color_hex, estado)
        VALUES ('SEC-03', 'Sector 03 - Vía Artesanal y Centros de Producción', 'Corredor de talleres de alfarería tradicional y feria de artesanos', '#f59e0b', 'ACTIVO')
        RETURNING id INTO v_sector_artesanal;
    END IF;

    -- Ruta 1: Casco Urbano
    SELECT id INTO v_ruta1_id FROM serenazgo_rutas_patrullaje WHERE codigo = 'RUT-QUI-01' LIMIT 1;
    IF v_ruta1_id IS NULL THEN
        INSERT INTO serenazgo_rutas_patrullaje (codigo, nombre, descripcion, sector_id, distancia_estimada_km, tiempo_estimado_min, estado, version_ruta)
        VALUES ('RUT-QUI-01', 'Ruta 01 - Perímetro Cívico Plaza Mayor y Posta Médica', 'Ronda preventiva que cubre Plaza de Armas, Palacio Municipal, Posta Médica y Jr. Sucre', v_sector_urbano, 2.50, 45, 'ACTIVA', 1)
        RETURNING id INTO v_ruta1_id;

        INSERT INTO serenazgo_rutas_puntos_control (ruta_id, orden, codigo, nombre, descripcion, referencia_ubicacion, latitud, longitud, tiempo_estimado_desde_inicio_min, activo) VALUES
        (v_ruta1_id, 1, 'PC-01-01', 'Caseta de Seguridad Plaza Mayor', 'Punto de partida y verificación en Plaza de Quinua', 'Frontis Municipalidad Distrital', -13.0425000, -74.1388000, 0, true),
        (v_ruta1_id, 2, 'PC-01-02', 'Centro de Salud Quinua', 'Verificación de accesos y tranquilidad en área hospitalaria', 'Jr. Arequipa s/n', -13.0435000, -74.1378000, 15, true),
        (v_ruta1_id, 3, 'PC-01-03', 'Mercado Central de Abastos', 'Control de orden público e inspección preventiva en pasajes comerciales', 'Av. 9 de Diciembre c/ Jr. Sucre', -13.0442000, -74.1392000, 30, true),
        (v_ruta1_id, 4, 'PC-01-04', 'Complejo Recreacional y Educativo', 'Vigilancia en perímetro escolar I.E. Quinua', 'Jr. San Martín frente al estadio', -13.0418000, -74.1405000, 45, true);
    END IF;

    -- Ruta 2: Santuario Histórico
    SELECT id INTO v_ruta2_id FROM serenazgo_rutas_patrullaje WHERE codigo = 'RUT-QUI-02' LIMIT 1;
    IF v_ruta2_id IS NULL THEN
        INSERT INTO serenazgo_rutas_patrullaje (codigo, nombre, descripcion, sector_id, distancia_estimada_km, tiempo_estimado_min, estado, version_ruta)
        VALUES ('RUT-QUI-02', 'Ruta 02 - Corredor Histórico Pampa de Quinua', 'Ronda preventiva y auxilio al visitante en el Santuario Histórico de la Pampa de Ayacucho', v_sector_pampa, 4.20, 60, 'ACTIVA', 1)
        RETURNING id INTO v_ruta2_id;

        INSERT INTO serenazgo_rutas_puntos_control (ruta_id, orden, codigo, nombre, descripcion, referencia_ubicacion, latitud, longitud, tiempo_estimado_desde_inicio_min, activo) VALUES
        (v_ruta2_id, 1, 'PC-02-01', 'Arco y Caseta de Ingreso al Santuario', 'Control vehicular y bienvenida a visitantes', 'Entrada carretera a la Pampa', -13.0489000, -74.1352000, 0, true),
        (v_ruta2_id, 2, 'PC-02-02', 'Museo de Sitio de Quinua', 'Patrullaje preventivo en instalaciones culturales', 'Plazoleta del Museo de Sitio', -13.0512000, -74.1340000, 20, true),
        (v_ruta2_id, 3, 'PC-02-03', 'Explanada del Obelisco de la Pampa', 'Supervisión en la explanada cívica y mirador histórico', 'Monumento a los Vencedores de Ayacucho', -13.0558000, -74.1315000, 40, true),
        (v_ruta2_id, 4, 'PC-02-04', 'Zona de Estacionamiento y Vianderas', 'Prevención de conatos y auxilio al turista', 'Área de gastronomía y servicios', -13.0540000, -74.1328000, 60, true);
    END IF;

    -- Ruta 3: Vía Artesanal
    SELECT id INTO v_ruta3_id FROM serenazgo_rutas_patrullaje WHERE codigo = 'RUT-QUI-03' LIMIT 1;
    IF v_ruta3_id IS NULL THEN
        INSERT INTO serenazgo_rutas_patrullaje (codigo, nombre, descripcion, sector_id, distancia_estimada_km, tiempo_estimado_min, estado, version_ruta)
        VALUES ('RUT-QUI-03', 'Ruta 03 - Corredor Alfarero y Talleres Tradicionales', 'Ronda preventiva que protege los corredores de artesanos ceramistas de Quinua', v_sector_artesanal, 3.10, 50, 'ACTIVA', 1)
        RETURNING id INTO v_ruta3_id;

        INSERT INTO serenazgo_rutas_puntos_control (ruta_id, orden, codigo, nombre, descripcion, referencia_ubicacion, latitud, longitud, tiempo_estimado_desde_inicio_min, activo) VALUES
        (v_ruta3_id, 1, 'PC-03-01', 'Ingreso Corredor de Artesanos', 'Portal de acceso sur al circuito cerámico', 'Intersección Vía Evitamiento sur', -13.0398000, -74.1412000, 0, true),
        (v_ruta3_id, 2, 'PC-03-02', 'Feria Artesanal Mamacha Cocharcas', 'Vigilancia en puestos de alfareros tradicionales', 'Jr. Bellavista s/n', -13.0405000, -74.1425000, 25, true),
        (v_ruta3_id, 3, 'PC-03-03', 'Mirador Loma de Huayllapampa', 'Punto alto de observación territorial preventiva', 'Camino afirmado a Huayllapampa', -13.0375000, -74.1450000, 50, true);
    END IF;
END $$;

-- 12. Inserción de Permisos RBAC para Patrullajes
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_VER', 'Ver Patrullajes y Recorridos', 'VIEW',
       'Permite consultar la bandeja de patrullajes, rutas y recorridos de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_CREAR', 'Planificar Patrullajes de Serenazgo', 'CREATE',
       'Permite registrar y planificar nuevos patrullajes preventivos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_EDITAR', 'Editar Datos de Patrullaje', 'UPDATE',
       'Permite modificar datos de patrullajes en planificación o borradores', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_PROGRAMAR', 'Programar Fechas y Horarios de Patrullaje', 'UPDATE',
       'Permite fijar o reprogramar el horario operativo del patrullaje', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_ASIGNAR', 'Asignar Recursos a Patrullajes', 'ASSIGN',
       'Permite asignar personal, unidades móviles y equipos al patrullaje', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_INICIAR', 'Iniciar Ejecución de Patrullaje', 'MANAGE',
       'Permite dar inicio formal y operativo al patrullaje en campo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_REGISTRAR_RECORRIDO', 'Registrar Paso por Puntos de Control', 'UPDATE',
       'Permite marcar la visita u omisión justificada a puntos de control en ruta', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_REGISTRAR_NOVEDAD', 'Registrar Novedad de Patrullaje', 'CREATE',
       'Permite registrar incidencias, pausas y novedades ocurridas durante el recorrido', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_CAMBIAR_RUTA', 'Autorizar Cambio de Ruta de Patrullaje', 'UPDATE',
       'Permite modificar la ruta en curso con justificación operativa', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_FINALIZAR', 'Finalizar Patrullaje de Serenazgo', 'MANAGE',
       'Permite concluir formalmente el patrullaje y emitir resultado administrativo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_CANCELAR', 'Cancelar Patrullaje Planificado', 'MANAGE',
       'Permite cancelar un patrullaje con justificación documental obligatoria', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_PATRULLAJES_EXPORTAR', 'Exportar Fichas y Reportes de Patrullaje', 'EXPORT',
       'Permite exportar fichas de servicio y planillas en PDF y Excel', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 13. Asignación de Permisos a Roles Institucionales
-- Administradores: Acceso Total
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo LIKE 'SERENAZGO_PATRULLAJES_%'
ON CONFLICT DO NOTHING;

-- Supervisores de Serenazgo: Gestión y Supervisión Operativa Completa
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SUPERVISOR_SERENAZGO'
  AND p.codigo LIKE 'SERENAZGO_PATRULLAJES_%'
ON CONFLICT DO NOTHING;

-- Operadores de Central
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_OPERADOR_CENTRAL'
  AND p.codigo IN (
      'SERENAZGO_PATRULLAJES_VER',
      'SERENAZGO_PATRULLAJES_CREAR',
      'SERENAZGO_PATRULLAJES_EDITAR',
      'SERENAZGO_PATRULLAJES_PROGRAMAR',
      'SERENAZGO_PATRULLAJES_ASIGNAR',
      'SERENAZGO_PATRULLAJES_INICIAR',
      'SERENAZGO_PATRULLAJES_REGISTRAR_RECORRIDO',
      'SERENAZGO_PATRULLAJES_REGISTRAR_NOVEDAD',
      'SERENAZGO_PATRULLAJES_CAMBIAR_RUTA',
      'SERENAZGO_PATRULLAJES_FINALIZAR',
      'SERENAZGO_PATRULLAJES_EXPORTAR'
  )
ON CONFLICT DO NOTHING;

-- Serenos de Cuadrilla / Campo
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_SERENO'
  AND p.codigo IN (
      'SERENAZGO_PATRULLAJES_VER',
      'SERENAZGO_PATRULLAJES_INICIAR',
      'SERENAZGO_PATRULLAJES_REGISTRAR_RECORRIDO',
      'SERENAZGO_PATRULLAJES_REGISTRAR_NOVEDAD',
      'SERENAZGO_PATRULLAJES_CAMBIAR_RUTA',
      'SERENAZGO_PATRULLAJES_FINALIZAR'
  )
ON CONFLICT DO NOTHING;
