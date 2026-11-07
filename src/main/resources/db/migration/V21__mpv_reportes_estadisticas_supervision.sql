-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V21: Mesa de Partes Virtual - Reportes, Estadísticas y Supervisión
-- Tablas: mpv_reporte_exportaciones
-- Índices: Optimización para agregaciones estadísticas en mpv_expedientes
-- Permisos: MESAPARTES_REPORTES_*, MESAPARTES_ESTADISTICAS_*, MESAPARTES_SUPERVISION_*
-- ==============================================================================

-- 1. Tabla de Historial de Exportaciones y Auditoría de Reportes
CREATE TABLE IF NOT EXISTS mpv_reporte_exportaciones (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    usuario_id BIGINT NOT NULL,
    tipo_reporte VARCHAR(80) NOT NULL,
    formato VARCHAR(20) NOT NULL,
    filtros_json TEXT,
    cantidad_registros INT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'COMPLETADO',
    tiempo_generacion_ms BIGINT,
    ip_origen VARCHAR(50),
    user_agent VARCHAR(255),
    fecha_exportacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mpv_rep_exp_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT uq_mpv_rep_exp_uuid UNIQUE (uuid),
    CONSTRAINT chk_mpv_rep_formato CHECK (formato IN ('EXCEL', 'PDF'))
);

-- Índices de auditoría de reportes
CREATE INDEX IF NOT EXISTS idx_mpv_rep_exp_usuario ON mpv_reporte_exportaciones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_mpv_rep_exp_fecha ON mpv_reporte_exportaciones(fecha_exportacion DESC);
CREATE INDEX IF NOT EXISTS idx_mpv_rep_exp_tipo ON mpv_reporte_exportaciones(tipo_reporte, formato);

-- 2. Índices de Rendimiento y Agregación para Expedientes
CREATE INDEX IF NOT EXISTS idx_mpv_exp_est_fpres ON mpv_expedientes(estado_actual, fecha_presentacion);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_ofi_est ON mpv_expedientes(oficina_responsable_id, estado_actual);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_tram_est ON mpv_expedientes(tipo_tramite_id, estado_actual);
CREATE INDEX IF NOT EXISTS idx_mpv_exp_f_cierre ON mpv_expedientes(fecha_cierre) WHERE fecha_cierre IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_mpv_exp_f_pres ON mpv_expedientes(fecha_presentacion);

-- 3. Índices de Rendimiento para Derivaciones y Observaciones
CREATE INDEX IF NOT EXISTS idx_mpv_deriv_ofi_est ON mpv_derivaciones(oficina_destino_id, estado);
CREATE INDEX IF NOT EXISTS idx_mpv_obs_exp_est ON mpv_observaciones(expediente_id, estado);

-- 4. Sembrado de Permisos Granulares de Reportes, Estadísticas y Supervisión
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_REPORTES_VER', 'Ver reportes y estadísticas de Mesa de Partes', 'VIEW', 'Permite visualizar paneles estadísticos y reportes administrativos'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_REPORTES_OFICINA', 'Ver reportes de la oficina asignada', 'VIEW', 'Restringe la consulta de estadísticas y reportes únicamente al ámbito de su oficina'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_REPORTES_GLOBALES', 'Ver reportes globales de toda la municipalidad', 'VIEW', 'Permite consultar estadísticas consolidadas de todas las dependencias municipales'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_REPORTES_EXPORTAR', 'Exportar reportes en Excel y PDF', 'EXPORT', 'Permite generar y descargar archivos XLSX y PDF de expedientes'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_ESTADISTICAS_VER', 'Visualizar gráficos e indicadores de gestión', 'VIEW', 'Permite el acceso a los gráficos de tendencias, estados y distribución'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion)
SELECT m.id, 'MESAPARTES_SUPERVISION_VER', 'Supervisión de tiempos y productividad operativa', 'MANAGE', 'Permite el monitoreo de carga laboral, plazos vencidos y derivaciones'
FROM modulos_sistema m WHERE m.codigo = 'TRAMITE_DOCUMENTARIO'
ON CONFLICT (codigo) DO NOTHING;

-- 5. Asignación de Permisos a Roles Institucionales

-- Administrador y Superadmin: Todos los permisos de reportes, globales, exportar y supervisión
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo IN (
      'MESAPARTES_REPORTES_VER',
      'MESAPARTES_REPORTES_GLOBALES',
      'MESAPARTES_REPORTES_EXPORTAR',
      'MESAPARTES_ESTADISTICAS_VER',
      'MESAPARTES_SUPERVISION_VER'
  )
ON CONFLICT DO NOTHING;

-- Jefe de Oficina: Reportes, oficina, exportar, estadísticas y supervisión
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo = 'ROLE_JEFE_OFICINA'
  AND p.codigo IN (
      'MESAPARTES_REPORTES_VER',
      'MESAPARTES_REPORTES_OFICINA',
      'MESAPARTES_REPORTES_EXPORTAR',
      'MESAPARTES_ESTADISTICAS_VER',
      'MESAPARTES_SUPERVISION_VER'
  )
ON CONFLICT DO NOTHING;

-- Operador de Mesa de Partes: Reportes, estadísticas, exportar
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.codigo IN ('ROLE_MESA_PARTES', 'ROLE_MESA_PARTES_OPERADOR', 'ROLE_OPERADOR')
  AND p.codigo IN (
      'MESAPARTES_REPORTES_VER',
      'MESAPARTES_REPORTES_OFICINA',
      'MESAPARTES_REPORTES_EXPORTAR',
      'MESAPARTES_ESTADISTICAS_VER'
  )
ON CONFLICT DO NOTHING;
