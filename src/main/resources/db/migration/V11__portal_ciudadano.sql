-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V11: Portal Ciudadano Institucional, Contenidos, Servicios y FAQ
-- Tablas: portal_noticias, portal_faq, portal_contactos, portal_servicios, portal_configuracion
-- ==============================================================================

-- 1. Tabla de Noticias, Comunicados y Publicaciones Institucionales
CREATE TABLE IF NOT EXISTS portal_noticias (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    resumen VARCHAR(500) NOT NULL,
    contenido TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL DEFAULT 'NOTICIA',
    categoria VARCHAR(100) NOT NULL DEFAULT 'INSTITUCIONAL',
    imagen_url VARCHAR(500),
    autor VARCHAR(150) NOT NULL DEFAULT 'Municipalidad Distrital de Quinua',
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    fecha_publicacion TIMESTAMP WITH TIME ZONE,
    destacada BOOLEAN NOT NULL DEFAULT FALSE,
    visitas INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    updated_by VARCHAR(100),
    CONSTRAINT uq_portal_noticias_slug UNIQUE (slug),
    CONSTRAINT chk_portal_noticias_tipo CHECK (tipo IN ('NOTICIA', 'COMUNICADO', 'CONVOCATORIA')),
    CONSTRAINT chk_portal_noticias_estado CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'ARCHIVADO'))
);

CREATE INDEX IF NOT EXISTS idx_portal_noticias_estado_fecha ON portal_noticias(estado, fecha_publicacion DESC);
CREATE INDEX IF NOT EXISTS idx_portal_noticias_tipo ON portal_noticias(tipo);

-- 2. Tabla de Preguntas Frecuentes (FAQ)
CREATE TABLE IF NOT EXISTS portal_faq (
    id BIGSERIAL PRIMARY KEY,
    pregunta VARCHAR(300) NOT NULL,
    respuesta TEXT NOT NULL,
    categoria VARCHAR(100) NOT NULL DEFAULT 'GENERAL',
    orden INTEGER NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    updated_by VARCHAR(100),
    CONSTRAINT chk_portal_faq_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_portal_faq_orden ON portal_faq(categoria, orden ASC);
CREATE INDEX IF NOT EXISTS idx_portal_faq_estado ON portal_faq(estado);

-- 3. Tabla de Mensajes y Consultas de Contacto Ciudadano
CREATE TABLE IF NOT EXISTS portal_contactos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    remitente_nombre VARCHAR(150) NOT NULL,
    remitente_correo VARCHAR(150) NOT NULL,
    remitente_telefono VARCHAR(20),
    asunto VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    respuesta_interna TEXT,
    atendido_por VARCHAR(100),
    atendido_at TIMESTAMP WITH TIME ZONE,
    ip_origen VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100) NOT NULL DEFAULT 'PORTAL_PUBLICO',
    updated_by VARCHAR(100),
    CONSTRAINT uq_portal_contactos_codigo UNIQUE (codigo),
    CONSTRAINT chk_portal_contactos_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'ATENDIDO'))
);

CREATE INDEX IF NOT EXISTS idx_portal_contactos_estado ON portal_contactos(estado, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_portal_contactos_codigo ON portal_contactos(codigo);

-- 4. Tabla de Catálogo de Servicios Municipales Visibles
CREATE TABLE IF NOT EXISTS portal_servicios (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    icono VARCHAR(50) NOT NULL,
    orden INTEGER NOT NULL DEFAULT 1,
    estado_disponibilidad VARCHAR(30) NOT NULL DEFAULT 'PROXIMAMENTE',
    url_acceso VARCHAR(255),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    updated_by VARCHAR(100),
    CONSTRAINT uq_portal_servicios_codigo UNIQUE (codigo),
    CONSTRAINT chk_portal_servicios_disponibilidad CHECK (estado_disponibilidad IN ('DISPONIBLE', 'PROXIMAMENTE', 'EN_DESARROLLO')),
    CONSTRAINT chk_portal_servicios_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_portal_servicios_orden ON portal_servicios(orden ASC);
CREATE INDEX IF NOT EXISTS idx_portal_servicios_estado ON portal_servicios(estado);

-- 5. Tabla de Configuración Institucional del Portal
CREATE TABLE IF NOT EXISTS portal_configuracion (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL,
    valor TEXT NOT NULL,
    descripcion VARCHAR(300),
    grupo VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    updated_by VARCHAR(100),
    CONSTRAINT uq_portal_configuracion_clave UNIQUE (clave)
);

-- 6. Registro del Permiso PORTAL_MANAGE en el Catálogo de Permisos
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT id, 'PORTAL_MANAGE', 'Gestión de Contenidos y Portal Ciudadano', 'MANAGE', 'Administración de noticias, FAQ, mensajes ciudadanos y configuración pública', 'ACTIVO'
FROM modulos_sistema WHERE codigo = 'ADMINISTRACION_SISTEMA'
ON CONFLICT (codigo) DO NOTHING;

-- Asignación del Permiso PORTAL_MANAGE a ROLE_ADMIN y ROLE_SUPERADMIN
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
  AND p.codigo = 'PORTAL_MANAGE'
ON CONFLICT DO NOTHING;

-- 7. Poblado de los 10 Servicios Municipales Oficiales
INSERT INTO portal_servicios (codigo, nombre, descripcion, icono, orden, estado_disponibilidad, url_acceso, estado) VALUES
('SRV_MESA_PARTES', 'Mesa de Partes Virtual', 'Presentación digital de solicitudes, trámites y documentos oficiales con recepción de cargos digitales.', 'file-text', 1, 'PROXIMAMENTE', '/mesa-partes', 'ACTIVO'),
('SRV_MERCADOS', 'Mercados Municipales', 'Gestión de puestos comerciales, autorizaciones de venta en ferias y fiscalización sanitaria.', 'store', 2, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_SEGURIDAD', 'Seguridad Ciudadana', 'Central de alertas de Serenazgo, reporte vecinal de emergencias comunitarias y patrullaje integrado.', 'shield', 3, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_RENTAS', 'Recaudación Tributaria', 'Consulta de estados de cuenta del impuesto predial, arbitrios municipales y emisión de constancias.', 'dollar-sign', 4, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_LICENCIAS', 'Licencias de Funcionamiento', 'Trámite simplificado para autorizaciones comerciales e inspecciones técnicas de seguridad en edificaciones.', 'file-check', 5, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_OBRAS', 'Seguimiento de Obras Públicas', 'Visualización transparente de inversiones públicas comunales, estado físico y avance financiero distrital.', 'hard-hat', 6, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_SOCIALES', 'Programas Sociales', 'Atención integral del Programa Vaso de Leche, OMAPED, DEMUNA y Centro Integral del Adulto Mayor.', 'heart-handshake', 7, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_PATRIMONIO', 'Almacén y Patrimonio', 'Inventario de bienes municipales, gestión patrimonial y trazabilidad logística institucional.', 'boxes', 8, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_PUBLICOS', 'Servicios Públicos', 'Rutas de recolección de residuos sólidos, mantenimiento de áreas verdes y ornato distrital.', 'trees', 9, 'PROXIMAMENTE', NULL, 'ACTIVO'),
('SRV_ATENCION', 'Atención Ciudadana y Orientación', 'Canal oficial de orientación vecinal, consultas institucionales y seguimiento presencial.', 'headset', 10, 'DISPONIBLE', '/contacto', 'ACTIVO')
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    icono = EXCLUDED.icono,
    orden = EXCLUDED.orden,
    estado_disponibilidad = EXCLUDED.estado_disponibilidad,
    url_acceso = EXCLUDED.url_acceso;

-- 8. Poblado de Preguntas Frecuentes Iniciales Reales
INSERT INTO portal_faq (pregunta, respuesta, categoria, orden, estado) VALUES
('¿Cuál es el horario y lugar de atención presencial de la Municipalidad?', 'La atención al público se brinda de lunes a viernes en horario de 08:00 a 13:00 horas y de 14:00 a 17:00 horas en la Sede Municipal ubicada frente a la Plaza de Armas de Quinua.', 'GENERAL', 1, 'ACTIVO'),
('¿Dónde se encuentra ubicada la Municipalidad Distrital de Quinua?', 'Nuestra sede institucional se encuentra en la Plaza de Armas s/n, distrito de Quinua, provincia de Huamanga, departamento de Ayacucho, Perú.', 'GENERAL', 2, 'ACTIVO'),
('¿Cómo puedo presentar un documento o solicitud en la Municipalidad?', 'Por el momento, los trámites y expedientes se presentan de manera presencial en la ventanilla única de Mesa de Partes en la Sede Municipal, cumpliendo con los requisitos de la Ley N° 27444. La Mesa de Partes Virtual se encuentra en proceso de implementación técnica.', 'TRAMITES', 3, 'ACTIVO'),
('¿Qué requisitos se necesitan para el Registro Ciudadano en el portal?', 'Para registrarse como vecino o administrado requiere contar con su Documento Nacional de Identidad (DNI) o Carné de Extranjería válido y un correo electrónico activo. El registro le permitirá realizar consultas y seguimiento de trámites en línea.', 'PORTAL', 4, 'ACTIVO'),
('¿Cómo puedo pagar mis arbitrios o impuesto predial?', 'Los pagos tributarios se efectúan directamente en la Oficina de Rentas y Caja de la Municipalidad Distrital de Quinua en el Palacio Municipal. Próximamente se integrará la pasarela de pagos digitales.', 'RENTAS', 5, 'ACTIVO'),
('¿A qué número o canal puedo comunicarme ante una emergencia vecinal?', 'Puede apersonarse a la Central de Seguridad Ciudadana / Serenazgo en el distrito de Quinua o enviar su consulta prioritaria a través de nuestro formulario oficial de Atención Ciudadana.', 'SEGURIDAD', 6, 'ACTIVO');

-- 9. Poblado de Noticias y Comunicados Institucionales Reales
INSERT INTO portal_noticias (titulo, slug, resumen, contenido, tipo, categoria, imagen_url, autor, estado, fecha_publicacion, destacada, visitas) VALUES
(
    'Lanzamiento Oficial del Sistema Integral de Gestión Municipal SIGM-QUINUA',
    'lanzamiento-oficial-sigm-quinua',
    'La Municipalidad Distrital de Quinua inicia el despliegue de su moderna plataforma digital para la transparencia, simplificación administrativa y atención ciudadana.',
    'La Municipalidad Distrital de Quinua, en el marco de la modernización de la gestión pública y la Ley de Gobierno Digital, presenta a la comunidad el Sistema Integral de Gestión Municipal (SIGM-QUINUA).\n\nEsta plataforma integrará de manera progresiva los principales servicios municipales, iniciando con el Portal Ciudadano, la administración institucional, la futura Mesa de Partes Virtual y los módulos de recaudación tributaria y seguridad ciudadana, promoviendo una atención ágil, transparente y descentralizada para todos los vecinos de nuestro histórico distrito.',
    'NOTICIA',
    'INSTITUCIONAL',
    NULL,
    'Gerencia Municipal',
    'PUBLICADO',
    CURRENT_TIMESTAMP,
    TRUE,
    15
),
(
    'Comunicado: Canales de Atención Oficial y Trámites Presenciales en Sede Central',
    'comunicado-canales-atencion-oficial',
    'Se informa a la ciudadanía los canales oficiales y horarios de atención al administrado en el Palacio Municipal de Quinua.',
    'Se comunica a los vecinos y administrados del distrito de Quinua que la atención de trámites documentarios, licencias, registros y orientación vecinal se realiza en ventanilla de la sede institucional de lunes a viernes de 08:00 a 13:00 y de 14:00 a 17:00 horas.\n\nAsimismo, se recuerda que cualquier consulta institucional puede ser registrada a través del formulario de contacto oficial de este portal ciudadano, garantizando una recepción oportuna por el equipo municipal.',
    'COMUNICADO',
    'INSTITUCIONAL',
    NULL,
    'Oficina de Secretaría General',
    'PUBLICADO',
    CURRENT_TIMESTAMP,
    FALSE,
    8
),
(
    'Promoción del Desarrollo Turístico y Artesanal en la Histórica Villa de Quinua',
    'desarrollo-turistico-artesanal-quinua',
    'Acciones municipales para fortalecer la identidad cultural, la alfarería tradicional y la reactivación económica local.',
    'El distrito de Quinua, reconocido por su gloriosa historia en la Pampa de Ayacucho y su tradición alfarera declarada Patrimonio Cultural de la Nación, continúa fortaleciendo espacios de promoción turística, gastronómica y artesanal.\n\nLa Municipalidad reafirma su compromiso de coordinar con los maestros artesanos y productores locales para impulsar el turismo sostenible y el bienestar de las familias quinuinas.',
    'NOTICIA',
    'DESARROLLO_ECONOMICO',
    NULL,
    'Subgerencia de Desarrollo Económico',
    'PUBLICADO',
    CURRENT_TIMESTAMP,
    FALSE,
    12
)
ON CONFLICT (slug) DO NOTHING;

-- 10. Poblado de Parámetros de Configuración Institucional
INSERT INTO portal_configuracion (clave, valor, descripcion, grupo) VALUES
('institucion_nombre', 'Municipalidad Distrital de Quinua', 'Nombre oficial de la entidad municipal', 'INSTITUCIONAL'),
('institucion_sigla', 'MDQ', 'Sigla institucional', 'INSTITUCIONAL'),
('institucion_provincia', 'Huamanga', 'Provincia de ubicación', 'INSTITUCIONAL'),
('institucion_departamento', 'Ayacucho', 'Departamento de ubicación', 'INSTITUCIONAL'),
('institucion_direccion', 'Plaza de Armas s/n, Quinua, Huamanga, Ayacucho, Perú', 'Dirección física de la sede municipal', 'CONTACTO'),
('institucion_telefono', '(Atención presencial en Palacio Municipal)', 'Teléfono de contacto institucional', 'CONTACTO'),
('institucion_correo', 'mesadepartes@muniquinua.gob.pe', 'Correo electrónico para mesa de partes y avisos', 'CONTACTO'),
('institucion_horario', 'Lunes a Viernes: 08:00 a 13:00 y 14:00 a 17:00 horas', 'Horario oficial de atención presencial', 'CONTACTO'),
('mision_institucional', 'Promover el desarrollo integral, sostenible y armónico del distrito de Quinua, brindando servicios públicos de calidad con transparencia, inclusión social y preservación de nuestro patrimonio histórico y cultural.', 'Misión institucional conforme al Plan de Desarrollo', 'INSTITUCIONAL'),
('vision_institucional', 'Ser un distrito modelo en la región Ayacucho, con una gestión moderna, eficiente y participativa que revaloriza su identidad histórica, turística y artesanal al servicio de todos los ciudadanos.', 'Visión de futuro distrital', 'INSTITUCIONAL')
ON CONFLICT (clave) DO UPDATE SET
    valor = EXCLUDED.valor,
    descripcion = EXCLUDED.descripcion,
    grupo = EXCLUDED.grupo;
