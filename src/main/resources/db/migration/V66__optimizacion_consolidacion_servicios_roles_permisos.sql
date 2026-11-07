-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V66: Consolidación Oficial de 4 Servicios Municipales,
--                Depuración de Módulos Descartados, Matriz de Roles y Permisos,
--                e Índices de Alto Rendimiento
-- ==============================================================================

-- 1. REGISTRO Y REORDENAMIENTO DE LOS 4 SERVICIOS MUNICIPALES OFICIALES
-- Servicio 1: Mesa de Partes Virtual (TRAMITE_DOCUMENTARIO)
-- Servicio 2: Mercados Municipales (MERCADOS_MUNICIPALES)
-- Servicio 3: Seguridad Ciudadana y Serenazgo (SEGURIDAD_CIUDADANA)
-- Servicio 4: Recaudación Tributaria Municipal (RENTAS_TRIBUTACION)
-- Componente Compartido: Administración del Sistema y Seguridad (ADMINISTRACION_SISTEMA)

INSERT INTO modulos_sistema (codigo, nombre, descripcion, icono, orden, estado)
VALUES ('MERCADOS_MUNICIPALES', 'Mercados Municipales', 'Gestión de infraestructura, puestos, comerciantes, contratos, recaudación diaria y control operativo de mercados', 'shopping-bag', 2, 'ACTIVO')
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    icono = EXCLUDED.icono,
    orden = 2,
    estado = 'ACTIVO';

-- 2. REASIGNACIÓN DE PERMISOS DE MERCADOS A SU MÓDULO OFICIAL
-- Migrar los permisos que estaban vinculados a DESARROLLO_ECONOMICO
UPDATE permisos
SET modulo_id = (SELECT id FROM modulos_sistema WHERE codigo = 'MERCADOS_MUNICIPALES')
WHERE modulo_id = (SELECT id FROM modulos_sistema WHERE codigo = 'DESARROLLO_ECONOMICO');

-- 3. ACTUALIZACIÓN DE ORDEN Y ESTADO DE LOS SERVICIOS MUNICIPALES
UPDATE modulos_sistema SET orden = 1, estado = 'ACTIVO', nombre = 'Trámite Documentario y Mesa de Partes Virtual' WHERE codigo = 'TRAMITE_DOCUMENTARIO';
UPDATE modulos_sistema SET orden = 2, estado = 'ACTIVO', nombre = 'Mercados Municipales' WHERE codigo = 'MERCADOS_MUNICIPALES';
UPDATE modulos_sistema SET orden = 3, estado = 'ACTIVO', nombre = 'Seguridad Ciudadana y Serenazgo' WHERE codigo = 'SEGURIDAD_CIUDADANA';
UPDATE modulos_sistema SET orden = 4, estado = 'ACTIVO', nombre = 'Recaudación Tributaria Municipal' WHERE codigo = 'RENTAS_TRIBUTACION';
UPDATE modulos_sistema SET orden = 5, estado = 'ACTIVO', nombre = 'Administración del Sistema y Seguridad' WHERE codigo = 'ADMINISTRACION_SISTEMA';

-- Inactivación de módulos descartados (sin tablas ni permisos activos)
UPDATE modulos_sistema SET estado = 'INACTIVO', orden = 99 WHERE codigo IN (
    'CATASTRO_URBANO',
    'REGISTRO_CIVIL',
    'SERVICIOS_PUBLICOS',
    'PROGRAMAS_SOCIALES',
    'DESARROLLO_ECONOMICO',
    'FISCALIZACION_CONTROL'
);

-- 4. CONSOLIDACIÓN DE ROLES INSTITUCIONALES
-- Inactivar rol obsoleto de fiscalización externa
UPDATE roles SET estado = 'INACTIVO' WHERE codigo = 'ROLE_FISCALIZADOR';

-- Asegurar roles operativos de los 4 servicios
INSERT INTO roles (codigo, nombre, descripcion, estado)
VALUES
    ('ROLE_ADMIN_MERCADOS', 'Administrador de Mercados', 'Gestión de infraestructura, comerciantes y contratos de mercados municipales', 'ACTIVO'),
    ('ROLE_SUPERVISOR_SERENAZGO', 'Supervisor de Seguridad Ciudadana', 'Control operativo y despacho de incidencias de serenazgo', 'ACTIVO'),
    ('ROLE_OPERADOR_RENTAS', 'Operador de Recaudación Tributaria', 'Gestión de cuentas corrientes, valores y recaudación municipal', 'ACTIVO')
ON CONFLICT (codigo) DO UPDATE SET estado = 'ACTIVO';

-- 5. ÍNDICES DE RENDIMIENTO PARA OPERACIONES FRECUENTES

-- A. Servicio 1: Núcleo y Mesa de Partes Virtual
CREATE INDEX IF NOT EXISTS idx_mpv_expedientes_estado_fec ON mpv_expedientes(estado_actual, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_mpv_expedientes_numero ON mpv_expedientes(numero_expediente);
CREATE INDEX IF NOT EXISTS idx_mpv_expedientes_codigo_seg ON mpv_expedientes(codigo_seguimiento);
CREATE INDEX IF NOT EXISTS idx_mpv_derivaciones_ofic_est ON mpv_derivaciones(oficina_destino_id, estado);

-- B. Servicio 2: Mercados Municipales
CREATE INDEX IF NOT EXISTS idx_mercados_puestos_sec_est ON mercados_puestos(sector_id, estado);
CREATE INDEX IF NOT EXISTS idx_mercados_comerciantes_cod ON mercados_comerciantes(codigo_comerciante, estado);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_vig ON mercados_contratos(comerciante_id, estado, fecha_fin);
CREATE INDEX IF NOT EXISTS idx_mercados_obligaciones_est ON mercados_obligaciones(estado, fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_mercados_pagos_fec ON mercados_pagos(fecha_pago, caja_sesion_id);

-- C. Servicio 3: Seguridad Ciudadana y Serenazgo
CREATE INDEX IF NOT EXISTS idx_serenazgo_incidencias_fec_est ON serenazgo_incidencias(fecha_reporte DESC, estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_patrullajes_fec_est ON serenazgo_patrullajes(fecha_inicio DESC, estado);
CREATE INDEX IF NOT EXISTS idx_serenazgo_alertas_fec_est ON serenazgo_alertas_ciudadanas(fecha_recepcion DESC, estado);

-- D. Servicio 4: Recaudación Tributaria Municipal
CREATE INDEX IF NOT EXISTS idx_tributaria_contrib_doc ON tributaria_contribuyentes(numero_documento, tipo_documento);
CREATE INDEX IF NOT EXISTS idx_tributaria_predios_cod ON tributaria_predios(codigo_predio, estado);
CREATE INDEX IF NOT EXISTS idx_tributaria_oblig_contrib_est ON tributaria_obligaciones(contribuyente_id, estado, fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_tributaria_pagos_fec_sesion ON tributaria_pagos(fecha_hora_pago, sesion_caja_id);
CREATE INDEX IF NOT EXISTS idx_tributaria_notif_est_fec ON tributaria_notificaciones(estado_administrativo, fecha_programada);
