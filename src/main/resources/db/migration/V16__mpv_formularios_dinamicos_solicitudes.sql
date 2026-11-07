-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V16: Formularios Dinámicos, Requisitos Documentales e Idempotencia
-- Tablas: mpv_formularios, mpv_formulario_versiones, mpv_formulario_campos,
--         mpv_formulario_opciones, mpv_respuestas, mpv_respuesta_valores,
--         mpv_requisitos_documentales, mpv_presentaciones_idempotencia
-- ==============================================================================

-- 1. Cabecera de Formularios Dinámicos Vinculados a Tipos de Trámite
CREATE TABLE IF NOT EXISTS mpv_formularios (
    id BIGSERIAL PRIMARY KEY,
    tipo_tramite_id BIGINT NOT NULL REFERENCES mpv_tipos_tramite(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_mpv_forms_tramite ON mpv_formularios(tipo_tramite_id);
CREATE INDEX IF NOT EXISTS idx_mpv_forms_codigo ON mpv_formularios(codigo);

-- 2. Versiones de Formularios para Preservar la Inmutabilidad Histórica
CREATE TABLE IF NOT EXISTS mpv_formulario_versiones (
    id BIGSERIAL PRIMARY KEY,
    formulario_id BIGINT NOT NULL REFERENCES mpv_formularios(id) ON DELETE CASCADE,
    version INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'PUBLICADO',
    fecha_publicacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uq_mpv_form_version UNIQUE(formulario_id, version),
    CONSTRAINT chk_mpv_form_ver_estado CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'HISTORICO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_form_ver_form ON mpv_formulario_versiones(formulario_id);
CREATE INDEX IF NOT EXISTS idx_mpv_form_ver_estado ON mpv_formulario_versiones(estado);

-- 3. Definición de Campos Configurables por Versión de Formulario
CREATE TABLE IF NOT EXISTS mpv_formulario_campos (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES mpv_formulario_versiones(id) ON DELETE CASCADE,
    nombre_campo VARCHAR(100) NOT NULL,
    etiqueta VARCHAR(200) NOT NULL,
    tipo_campo VARCHAR(30) NOT NULL,
    obligatorio BOOLEAN NOT NULL DEFAULT FALSE,
    longitud_maxima INT,
    valor_defecto VARCHAR(255),
    placeholder VARCHAR(255),
    mensaje_ayuda VARCHAR(500),
    regla_validacion VARCHAR(255),
    orden INT NOT NULL DEFAULT 1,
    CONSTRAINT uq_mpv_campo_version UNIQUE(version_id, nombre_campo),
    CONSTRAINT chk_mpv_tipo_campo CHECK (tipo_campo IN (
        'TEXTO', 'TEXTO_LARGO', 'NUMERO', 'FECHA', 'SELECCION', 'CHECKBOX', 'DOCUMENTO'
    ))
);

CREATE INDEX IF NOT EXISTS idx_mpv_campos_version ON mpv_formulario_campos(version_id);
CREATE INDEX IF NOT EXISTS idx_mpv_campos_orden ON mpv_formulario_campos(version_id, orden);

-- 4. Opciones para Campos Dinámicos de Tipo Selección
CREATE TABLE IF NOT EXISTS mpv_formulario_opciones (
    id BIGSERIAL PRIMARY KEY,
    campo_id BIGINT NOT NULL REFERENCES mpv_formulario_campos(id) ON DELETE CASCADE,
    valor VARCHAR(100) NOT NULL,
    etiqueta VARCHAR(200) NOT NULL,
    orden INT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_mpv_opciones_campo ON mpv_formulario_opciones(campo_id);

-- 5. Cabecera de Respuestas Dinámicas Asociadas al Expediente
CREATE TABLE IF NOT EXISTS mpv_respuestas (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    version_id BIGINT NOT NULL REFERENCES mpv_formulario_versiones(id) ON DELETE RESTRICT,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mpv_expediente_resp UNIQUE(expediente_id)
);

CREATE INDEX IF NOT EXISTS idx_mpv_resp_exp ON mpv_respuestas(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mpv_resp_ver ON mpv_respuestas(version_id);

-- 6. Valores Detallados Capturados por Campo
CREATE TABLE IF NOT EXISTS mpv_respuesta_valores (
    id BIGSERIAL PRIMARY KEY,
    respuesta_id BIGINT NOT NULL REFERENCES mpv_respuestas(id) ON DELETE CASCADE,
    campo_id BIGINT NOT NULL REFERENCES mpv_formulario_campos(id) ON DELETE RESTRICT,
    valor_texto TEXT,
    CONSTRAINT uq_mpv_resp_campo UNIQUE(respuesta_id, campo_id)
);

CREATE INDEX IF NOT EXISTS idx_mpv_resp_val_resp ON mpv_respuesta_valores(respuesta_id);

-- 7. Catálogo Oficial de Requisitos Documentales por Tipo de Trámite
CREATE TABLE IF NOT EXISTS mpv_requisitos_documentales (
    id BIGSERIAL PRIMARY KEY,
    tipo_tramite_id BIGINT NOT NULL REFERENCES mpv_tipos_tramite(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    formatos_permitidos VARCHAR(255) NOT NULL DEFAULT 'application/pdf,image/jpeg,image/png',
    tamano_maximo_mb INT NOT NULL DEFAULT 15,
    orden INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_mpv_tramite_req UNIQUE(tipo_tramite_id, codigo),
    CONSTRAINT chk_mpv_req_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX IF NOT EXISTS idx_mpv_req_tramite ON mpv_requisitos_documentales(tipo_tramite_id);
CREATE INDEX IF NOT EXISTS idx_mpv_req_estado ON mpv_requisitos_documentales(estado);

-- 8. Control Estricto de Idempotencia de Presentaciones Ciudadanas
CREATE TABLE IF NOT EXISTS mpv_presentaciones_idempotencia (
    id BIGSERIAL PRIMARY KEY,
    clave_idempotencia VARCHAR(100) NOT NULL UNIQUE,
    expediente_id BIGINT NOT NULL REFERENCES mpv_expedientes(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha_presentacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_expediente VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_mpv_idem_clave ON mpv_presentaciones_idempotencia(clave_idempotencia);
CREATE INDEX IF NOT EXISTS idx_mpv_idem_exp ON mpv_presentaciones_idempotencia(expediente_id);

-- ==============================================================================
-- 9. Poblado Inicial de Requisitos Documentales y Formularios para Quinua
-- ==============================================================================

-- Trámite 1: TRAM-GEN (Solicitud General)
INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'FUT_GEN', 'Solicitud Simple o Formulario Único de Trámite (FUT)', 'Documento formal con firma del administrado conteniendo el petitorio concreto.', TRUE, 'application/pdf', 15, 1
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-GEN'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'ANEXO_SUSTENTO', 'Documentos de Sustento Complementarios', 'Copia de documentos, planos, cartas o antecedentes que sustenten la solicitud.', FALSE, 'application/pdf,image/jpeg,image/png', 20, 2
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-GEN'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

-- Formulario y Versión para TRAM-GEN
INSERT INTO mpv_formularios (tipo_tramite_id, codigo, nombre, descripcion, activo)
SELECT t.id, 'FORM-GEN', 'Formulario de Solicitud General', 'Datos complementarios para la atención de solicitudes generales.', TRUE
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-GEN'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_formulario_versiones (formulario_id, version, estado)
SELECT f.id, 1, 'PUBLICADO'
FROM mpv_formularios f WHERE f.codigo = 'FORM-GEN'
ON CONFLICT (formulario_id, version) DO NOTHING;

-- Campos para FORM-GEN
INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'referencia_documento', 'Documento de Referencia (si aplica)', 'TEXTO', FALSE, 100, 'Ej. Oficio N° 012-2026 / Carta s/n', 'Indique si su trámite responde a un documento municipal previo.', 1
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-GEN' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'solicita_audiencia', '¿Solicita audiencia o reunión presencial?', 'CHECKBOX', FALSE, NULL, NULL, 'Marque si requiere coordinación presencial con el despacho municipal.', 2
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-GEN' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

-- Trámite 2: TRAM-POS (Constancia de Posesión de Predio)
INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'PLANO_MEMORIA', 'Plano de Ubicación y Memoria Descriptiva', 'Elaborado y firmado por profesional competente (Arquitecto o Ingeniero Civil colegiado).', TRUE, 'application/pdf', 25, 1
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-POS'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'ACTA_COLINDANTES', 'Declaración Jurada de Vecinos Colindantes', 'Acta o declaración de dos o más vecinos colindantes que certifiquen posesión pacífica.', TRUE, 'application/pdf', 15, 2
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-POS'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

INSERT INTO mpv_formularios (tipo_tramite_id, codigo, nombre, descripcion, activo)
SELECT t.id, 'FORM-POS', 'Formulario para Constancia de Posesión', 'Metadatos del inmueble o predio ubicado en Quinua.', TRUE
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-POS'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_formulario_versiones (formulario_id, version, estado)
SELECT f.id, 1, 'PUBLICADO'
FROM mpv_formularios f WHERE f.codigo = 'FORM-POS'
ON CONFLICT (formulario_id, version) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'direccion_predio', 'Dirección o Ubicación Exacta del Predio', 'TEXTO', TRUE, 200, 'Ej. Jr. Sucre s/n, Barrio San Blas, Quinua', 'Detalle la vía, número, barrio o comunidad rural donde se ubica el predio.', 1
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-POS' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'area_m2', 'Área Aproximada del Predio (m²)', 'NUMERO', TRUE, 10, 'Ej. 180', 'Indique el área superficial del terreno en metros cuadrados.', 2
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-POS' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'tiempo_posesion_anios', 'Tiempo de Posesión Continua (Años)', 'NUMERO', TRUE, 3, 'Ej. 5', 'Años ininterrumpidos ejerciendo la posesión del predio.', 3
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-POS' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

-- Trámite 3: TRAM-LIC (Licencia de Funcionamiento Comercial)
INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'CROQUIS_DIST', 'Croquis de Ubicación y Distribución del Local', 'Plano simple a escala o croquis legible indicando distribución de ambientes y salidas.', TRUE, 'application/pdf', 20, 1
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-LIC'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'DECLARACION_ITSE', 'Declaración Jurada de Condiciones Básicas de Seguridad (ITSE)', 'Formato de cumplimiento de condiciones de seguridad en edificaciones según nivel de riesgo.', TRUE, 'application/pdf', 15, 2
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-LIC'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

INSERT INTO mpv_formularios (tipo_tramite_id, codigo, nombre, descripcion, activo)
SELECT t.id, 'FORM-LIC', 'Formulario de Licencia de Funcionamiento', 'Datos del establecimiento y actividad comercial.', TRUE
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-LIC'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_formulario_versiones (formulario_id, version, estado)
SELECT f.id, 1, 'PUBLICADO'
FROM mpv_formularios f WHERE f.codigo = 'FORM-LIC'
ON CONFLICT (formulario_id, version) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'nombre_comercial', 'Nombre Comercial del Establecimiento', 'TEXTO', TRUE, 150, 'Ej. Alfarería y Cerámica Quinua Linda', 'Rótulo o denominación con la que operará el local.', 1
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-LIC' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'giro_negocio', 'Giro o Actividad Económica Principal', 'SELECCION', TRUE, 50, NULL, 'Seleccione la actividad económica principal.', 2
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-LIC' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

-- Opciones para el campo giro_negocio
INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'ARTESANIA_CERAMICA', 'Taller Artesanal / Venta de Cerámica y Textiles', 1
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-LIC' AND c.nombre_campo = 'giro_negocio';

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'BODEGA_ABARROTES', 'Bodega / Venta de Abarrotes y Productos de Primera Necesidad', 2
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-LIC' AND c.nombre_campo = 'giro_negocio';

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'RESTAURANTE_COMIDA_TIPICA', 'Restaurante / Expendio de Comidas Típicas y Bebidas', 3
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-LIC' AND c.nombre_campo = 'giro_negocio';

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'HOSPEDAJE_TURISTICO', 'Alojamiento / Hospedaje Rural o Turístico', 4
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-LIC' AND c.nombre_campo = 'giro_negocio';

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'OTRO_SERVICIOS', 'Otros Servicios Personales o Profesionales', 5
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-LIC' AND c.nombre_campo = 'giro_negocio';

-- Trámite 4: TRAM-AIP (Acceso a la Información Pública)
INSERT INTO mpv_requisitos_documentales (tipo_tramite_id, codigo, nombre, descripcion, obligatorio, formatos_permitidos, tamano_maximo_mb, orden)
SELECT t.id, 'SUSTENTO_AIP', 'Documento Complementario o Precisión Temática', 'Especificación detallada de la información o expediente administrativo solicitado.', FALSE, 'application/pdf', 15, 1
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-AIP'
ON CONFLICT (tipo_tramite_id, codigo) DO NOTHING;

INSERT INTO mpv_formularios (tipo_tramite_id, codigo, nombre, descripcion, activo)
SELECT t.id, 'FORM-AIP', 'Formulario de Acceso a la Información Pública', 'Modalidad de entrega y período de la información pública peticionada.', TRUE
FROM mpv_tipos_tramite t WHERE t.codigo = 'TRAM-AIP'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mpv_formulario_versiones (formulario_id, version, estado)
SELECT f.id, 1, 'PUBLICADO'
FROM mpv_formularios f WHERE f.codigo = 'FORM-AIP'
ON CONFLICT (formulario_id, version) DO NOTHING;

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'forma_entrega', 'Modalidad Deseada de Entrega', 'SELECCION', TRUE, 50, NULL, 'Seleccione el medio preferido para la puesta a disposición de la información.', 1
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-AIP' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'CORREO_DIGITAL', 'Copia Digital enviada a Correo Electrónico (Gratuito)', 1
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-AIP' AND c.nombre_campo = 'forma_entrega';

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'COPIA_SIMPLE_FISICA', 'Copia Simple Impresa (Recojo en Ventanilla con costo de reproducción TUPA)', 2
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-AIP' AND c.nombre_campo = 'forma_entrega';

INSERT INTO mpv_formulario_opciones (campo_id, valor, etiqueta, orden)
SELECT c.id, 'COPIA_CERTIFICADA', 'Copia Certificada (Recojo en Ventanilla con costo de certificación TUPA)', 3
FROM mpv_formulario_campos c JOIN mpv_formulario_versiones v ON c.version_id = v.id JOIN mpv_formularios f ON v.formulario_id = f.id
WHERE f.codigo = 'FORM-AIP' AND c.nombre_campo = 'forma_entrega';

INSERT INTO mpv_formulario_campos (version_id, nombre_campo, etiqueta, tipo_campo, obligatorio, longitud_maxima, placeholder, mensaje_ayuda, orden)
SELECT v.id, 'periodo_anio', 'Año o Período de la Información Requerida', 'NUMERO', FALSE, 4, 'Ej. 2025', 'Indique el año o ejercicio presupuestal de los documentos.', 2
FROM mpv_formulario_versiones v JOIN mpv_formularios f ON v.formulario_id = f.id WHERE f.codigo = 'FORM-AIP' AND v.version = 1
ON CONFLICT (version_id, nombre_campo) DO NOTHING;
