-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V68: Ajuste y Consolidación Exclusiva de los Cuatro Servicios
-- Oficiales en el Catálogo del Portal Ciudadano
-- ==============================================================================

-- 1. Desactivación no destructiva de los servicios descartados
UPDATE portal_servicios
SET estado = 'INACTIVO'
WHERE codigo NOT IN ('SRV_MESA_PARTES', 'SRV_MERCADOS', 'SRV_SEGURIDAD', 'SRV_RENTAS');

-- 2. Consolidación y actualización del Servicio 1: Mesa de Partes Virtual
INSERT INTO portal_servicios (codigo, nombre, descripcion, icono, orden, estado_disponibilidad, url_acceso, estado)
VALUES (
    'SRV_MESA_PARTES',
    'Mesa de Partes Virtual',
    'Presentación digital de solicitudes, trámites TUPA y documentos oficiales con cargo digital y seguimiento en tiempo real.',
    'file-text',
    1,
    'DISPONIBLE',
    '/mesa-partes',
    'ACTIVO'
)
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    icono = EXCLUDED.icono,
    orden = EXCLUDED.orden,
    estado_disponibilidad = EXCLUDED.estado_disponibilidad,
    url_acceso = EXCLUDED.url_acceso,
    estado = EXCLUDED.estado,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SISTEMA_V68';

-- 3. Consolidación y actualización del Servicio 2: Mercados Municipales
INSERT INTO portal_servicios (codigo, nombre, descripcion, icono, orden, estado_disponibilidad, url_acceso, estado)
VALUES (
    'SRV_MERCADOS',
    'Mercados Municipales',
    'Gestión y control de puestos de abasto, padrón de comerciantes, contratos de concesión y liquidación de sisa comercial.',
    'store',
    2,
    'DISPONIBLE',
    '/portal/comerciante/dashboard',
    'ACTIVO'
)
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    icono = EXCLUDED.icono,
    orden = EXCLUDED.orden,
    estado_disponibilidad = EXCLUDED.estado_disponibilidad,
    url_acceso = EXCLUDED.url_acceso,
    estado = EXCLUDED.estado,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SISTEMA_V68';

-- 4. Consolidación y actualización del Servicio 3: Seguridad Ciudadana y Serenazgo
INSERT INTO portal_servicios (codigo, nombre, descripcion, icono, orden, estado_disponibilidad, url_acceso, estado)
VALUES (
    'SRV_SEGURIDAD',
    'Seguridad Ciudadana y Serenazgo',
    'Central de alertas vecinales de Serenazgo Quinua, reporte directo de emergencias distritales y patrullaje integrado por cuadrantes.',
    'shield-check',
    3,
    'DISPONIBLE',
    '/portal/seguridad/reportar',
    'ACTIVO'
)
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    icono = EXCLUDED.icono,
    orden = EXCLUDED.orden,
    estado_disponibilidad = EXCLUDED.estado_disponibilidad,
    url_acceso = EXCLUDED.url_acceso,
    estado = EXCLUDED.estado,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SISTEMA_V68';

-- 5. Consolidación y actualización del Servicio 4: Recaudación Tributaria Municipal
INSERT INTO portal_servicios (codigo, nombre, descripcion, icono, orden, estado_disponibilidad, url_acceso, estado)
VALUES (
    'SRV_RENTAS',
    'Recaudación Tributaria Municipal',
    'Consulta en línea de predios, cálculo y pago de impuesto predial y arbitrios municipales, estados de cuenta y fraccionamientos.',
    'landmark',
    4,
    'DISPONIBLE',
    '/portal/tributario/dashboard',
    'ACTIVO'
)
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    icono = EXCLUDED.icono,
    orden = EXCLUDED.orden,
    estado_disponibilidad = EXCLUDED.estado_disponibilidad,
    url_acceso = EXCLUDED.url_acceso,
    estado = EXCLUDED.estado,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SISTEMA_V68';

-- 6. Actualización de comunicados para reflejar con veracidad los servicios existentes
UPDATE portal_noticias
SET contenido = 'Se comunica a los vecinos y administrados del distrito de Quinua que la atención de trámites documentarios en Mesa de Partes, trámites de mercados municipales, tributación local y reporte de seguridad ciudadana se realiza en la sede institucional de lunes a viernes de 08:00 a 13:00 y de 14:00 a 17:00 horas, así como a través de nuestros canales digitales oficiales.\n\nAsimismo, se recuerda que cualquier consulta institucional puede ser registrada a través del formulario de contacto oficial de este portal ciudadano, garantizando una recepción oportuna por el equipo municipal.',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SISTEMA_V68'
WHERE slug = 'comunicado-canales-atencion-oficial';

