-- ==============================================================================
-- SIGM-QUINUA - PROMPT 30 DE 150: REGISTRO DE INCIDENCIAS E INFRACCIONES
-- Módulo: Mercados Municipales - Atención de Incidencias y Evaluación de Infracciones
-- ==============================================================================

-- 1. Secuencias correlativas seguras
CREATE SEQUENCE IF NOT EXISTS mercados_incidencias_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mercados_infracciones_seq START WITH 1 INCREMENT BY 1;

-- 2. Catálogo de Tipos de Incidencia
CREATE TABLE IF NOT EXISTS mercados_tipos_incidencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    prioridad_sugerida VARCHAR(30) NOT NULL DEFAULT 'MEDIA', -- BAJA, MEDIA, ALTA, CRITICA
    requiere_puesto BOOLEAN NOT NULL DEFAULT false,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tipos_incidencia_codigo ON mercados_tipos_incidencia(codigo);
CREATE INDEX IF NOT EXISTS idx_tipos_incidencia_activo ON mercados_tipos_incidencia(activo);

-- 3. Tabla Principal de Incidencias en Mercados
CREATE TABLE IF NOT EXISTS mercados_incidencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_incidencia VARCHAR(50) NOT NULL UNIQUE,
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE RESTRICT,
    sector_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    pabellon_id UUID REFERENCES mercados_pabellones(id) ON DELETE SET NULL,
    puesto_id UUID REFERENCES mercados_puestos(id) ON DELETE SET NULL,
    comerciante_id UUID REFERENCES mercados_comerciantes(id) ON DELETE SET NULL,
    tipo_incidencia_id UUID NOT NULL REFERENCES mercados_tipos_incidencia(id),
    origen VARCHAR(50) NOT NULL, -- PERSONAL_MUNICIPAL, ADMINISTRACION_MERCADO, INSPECCION, COMUNICACION_CIUDADANA, EXPEDIENTE, OTRO
    descripcion TEXT NOT NULL,
    fecha_reporte TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ocurrencia TIMESTAMP WITH TIME ZONE,
    prioridad VARCHAR(30) NOT NULL DEFAULT 'MEDIA', -- BAJA, MEDIA, ALTA, CRITICA
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA', -- REGISTRADA, EN_REVISION, ASIGNADA, EN_ATENCION, PENDIENTE_INFORMACION, RESUELTA, CERRADA, DESCARTADA
    reportante_nombre VARCHAR(150),
    reportante_contacto VARCHAR(100),
    es_anonimo BOOLEAN NOT NULL DEFAULT false,
    reportante_usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    responsable_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    inspeccion_id UUID REFERENCES mercados_inspecciones(id) ON DELETE SET NULL,
    expediente_id UUID,
    expediente_numero VARCHAR(64),
    observaciones TEXT,
    fecha_resolucion TIMESTAMP WITH TIME ZONE,
    motivo_resolucion TEXT,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    motivo_cierre TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_incidencias_codigo ON mercados_incidencias(codigo_incidencia);
CREATE INDEX IF NOT EXISTS idx_incidencias_mercado ON mercados_incidencias(mercado_id);
CREATE INDEX IF NOT EXISTS idx_incidencias_puesto ON mercados_incidencias(puesto_id);
CREATE INDEX IF NOT EXISTS idx_incidencias_comerciante ON mercados_incidencias(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_incidencias_estado ON mercados_incidencias(estado);
CREATE INDEX IF NOT EXISTS idx_incidencias_prioridad ON mercados_incidencias(prioridad);
CREATE INDEX IF NOT EXISTS idx_incidencias_fecha ON mercados_incidencias(fecha_reporte);
CREATE INDEX IF NOT EXISTS idx_incidencias_responsable ON mercados_incidencias(responsable_id);

-- 4. Actuaciones de Atención y Seguimiento de Incidencias
CREATE TABLE IF NOT EXISTS mercados_incidencias_actuaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incidencia_id UUID NOT NULL REFERENCES mercados_incidencias(id) ON DELETE CASCADE,
    tipo_actuacion VARCHAR(50) NOT NULL, -- REGISTRO, ASIGNACION, REVISION, INSPECCION_SOLICITADA, DILIGENCIA_CAMPO, COMUNICACION, SUBSANACION_OPERATIVA, RESOLUCION, CIERRE, DESCARTE
    descripcion TEXT NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsable_id BIGINT NOT NULL REFERENCES usuarios(id),
    documento_id UUID,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incidencias_actuaciones_inc ON mercados_incidencias_actuaciones(incidencia_id);
CREATE INDEX IF NOT EXISTS idx_incidencias_actuaciones_fecha ON mercados_incidencias_actuaciones(fecha);

-- 5. Evidencias de Incidencias (Fotografías y Documentos)
CREATE TABLE IF NOT EXISTS mercados_incidencias_evidencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incidencia_id UUID NOT NULL REFERENCES mercados_incidencias(id) ON DELETE CASCADE,
    tipo_evidencia VARCHAR(50) NOT NULL, -- FOTOGRAFIA, DOCUMENTO, INFORME, ACTA, OTRO
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    descripcion TEXT,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subido_por_id BIGINT NOT NULL REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_incidencias_evidencias_inc ON mercados_incidencias_evidencias(incidencia_id);

-- 6. Catálogo de Tipificaciones de Infracciones Municipales
CREATE TABLE IF NOT EXISTS mercados_tipificaciones_infraccion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    norma_referencia VARCHAR(255) NOT NULL,
    articulo VARCHAR(100),
    gravedad_base VARCHAR(30) NOT NULL DEFAULT 'LEVE', -- LEVE, MODERADA, GRAVE, MUY_GRAVE
    autoridad_competente VARCHAR(150) NOT NULL,
    fecha_inicio_vigencia DATE NOT NULL,
    fecha_fin_vigencia DATE,
    version_normativa INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, INACTIVO, DEROGADO
    documento_fuente_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tipificaciones_codigo ON mercados_tipificaciones_infraccion(codigo);
CREATE INDEX IF NOT EXISTS idx_tipificaciones_estado ON mercados_tipificaciones_infraccion(estado);

-- 7. Registro y Evaluación de Presuntas Infracciones
CREATE TABLE IF NOT EXISTS mercados_presuntas_infracciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    incidencia_id UUID REFERENCES mercados_incidencias(id) ON DELETE SET NULL,
    inspeccion_id UUID REFERENCES mercados_inspecciones(id) ON DELETE SET NULL,
    hallazgo_id UUID REFERENCES mercados_hallazgos_inspeccion(id) ON DELETE SET NULL,
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE RESTRICT,
    sector_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    pabellon_id UUID REFERENCES mercados_pabellones(id) ON DELETE SET NULL,
    puesto_id UUID REFERENCES mercados_puestos(id) ON DELETE SET NULL,
    comerciante_id UUID REFERENCES mercados_comerciantes(id) ON DELETE SET NULL,
    tipificacion_id UUID REFERENCES mercados_tipificaciones_infraccion(id) ON DELETE SET NULL,
    descripcion_hechos TEXT NOT NULL,
    fecha_deteccion DATE NOT NULL,
    estado_evaluacion VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA', -- REGISTRADA, EN_EVALUACION, PENDIENTE_INFORMACION, DESCARTADA, DERIVADA_A_PROCEDIMIENTO, CERRADA
    responsable_evaluacion_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    informe_evaluacion TEXT,
    fecha_evaluacion TIMESTAMP WITH TIME ZONE,
    motivo_descarte TEXT,
    expediente_id UUID,
    expediente_numero VARCHAR(64),
    tiene_descargo BOOLEAN NOT NULL DEFAULT false,
    fecha_descargo TIMESTAMP WITH TIME ZONE,
    documento_descargo_ref VARCHAR(255),
    resumen_descargo TEXT,
    resolucion_decision_ref VARCHAR(255),
    fecha_decision DATE,
    sentido_decision VARCHAR(50), -- ARCHIVO, ABSOLUCION, SANCION_FORMAL, MEDIDA_COMPLEMENTARIA (Solo registrable si hay acto resolutivo válido emitido por autoridad competente)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_presuntas_infr_codigo ON mercados_presuntas_infracciones(codigo);
CREATE INDEX IF NOT EXISTS idx_presuntas_infr_mercado ON mercados_presuntas_infracciones(mercado_id);
CREATE INDEX IF NOT EXISTS idx_presuntas_infr_puesto ON mercados_presuntas_infracciones(puesto_id);
CREATE INDEX IF NOT EXISTS idx_presuntas_infr_comerciante ON mercados_presuntas_infracciones(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_presuntas_infr_estado ON mercados_presuntas_infracciones(estado_evaluacion);
CREATE INDEX IF NOT EXISTS idx_presuntas_infr_fecha ON mercados_presuntas_infracciones(fecha_deteccion);

-- 8. Carga Inicial de Tipos de Incidencia Operativos
INSERT INTO mercados_tipos_incidencia (codigo, nombre, descripcion, prioridad_sugerida, requiere_puesto, activo)
VALUES
    ('INFRAESTRUCTURA', 'Problema de Infraestructura', 'Daños en techos, pisos, muros, puertas, cerrajería o puestos físicos', 'MEDIA', false, true),
    ('OCUPACION_ESPACIO', 'Conflicto de Ocupación', 'Ocupación de pasadizos comunes, invasión de linderos o mercadería fuera de zona delimitada', 'MEDIA', true, true),
    ('LIMPIEZA_SALUBRIDAD', 'Incidencia de Limpieza y Salubridad', 'Acumulación de residuos, falta de higiene, derrames o manejo inadecuado de desechos', 'ALTA', false, true),
    ('MANTENIMIENTO_SERVICIOS', 'Fallas en Servicios Básicos', 'Averías en redes de agua potable, desagüe, suministro eléctrico o alumbrado común', 'ALTA', false, true),
    ('DOCUMENTAL_PADRON', 'Reporte Documental y Autorizaciones', 'Irregularidades en licencias, autorizaciones, contratos o acreditación de titularidad', 'BAJA', true, true),
    ('RIESGO_SEGURIDAD', 'Riesgo Identificado de Seguridad', 'Obstrucción de rutas de escape, extintores vencidos, cableado expuesto o materiales inflamables', 'CRITICA', false, true),
    ('OTRO_HECHO', 'Otro Hecho Reportado', 'Situaciones operativas no clasificadas previamente que requieran atención municipal', 'MEDIA', false, true)
ON CONFLICT (codigo) DO NOTHING;

-- 9. Carga Inicial de Tipificaciones Normativas Verificadas
INSERT INTO mercados_tipificaciones_infraccion (codigo, descripcion, norma_referencia, articulo, gravedad_base, autoridad_competente, fecha_inicio_vigencia, version_normativa, estado)
VALUES
    ('TIP-MER-001', 'Ocupación de vías de circulación peatonal, accesos o áreas comunes con mercadería o enseres', 'Ley Orgánica de Municipalidades N° 27972', 'Art. 83', 'LEVE', 'Subgerencia de Fiscalización y Control', '2026-01-01', 1, 'ACTIVO'),
    ('TIP-MER-002', 'Comercialización de productos en condiciones antihigiénicas o sin medidas de inocuidad alimentaria', 'D.S. N° 007-98-SA, Reglamento Sanitario de Alimentos', 'Título IV', 'GRAVE', 'Subgerencia de Fiscalización y Control / Salud Ambiental', '2026-01-01', 1, 'ACTIVO'),
    ('TIP-MER-003', 'Alteración no autorizada de la infraestructura física del puesto o instalaciones eléctricas', 'Ley Orgánica de Municipalidades N° 27972', 'Art. 83', 'GRAVE', 'Subgerencia de Fiscalización y Control', '2026-01-01', 1, 'ACTIVO'),
    ('TIP-MER-004', 'Ejercicio de giro comercial distinto al expresamente autorizado en el contrato o autorización', 'Reglamento de Mercados Municipales', 'Art. 24', 'MODERADA', 'Gerencia de Desarrollo Económico', '2026-01-01', 1, 'ACTIVO'),
    ('TIP-MER-005', 'Obstaculización de la labor de fiscalización o negativa injustificada a la supervisión municipal', 'TUO Ley N° 27444, Ley del Procedimiento Administrativo General', 'Art. 240', 'MUY_GRAVE', 'Subgerencia de Fiscalización y Control', '2026-01-01', 1, 'ACTIVO')
ON CONFLICT (codigo) DO NOTHING;

-- 10. Inserción de Permisos Granulares RBAC
INSERT INTO permisos (codigo, nombre, accion, descripcion, modulo_id)
SELECT p.codigo, p.nombre, p.accion, p.descripcion, m.id
FROM (
    VALUES
        ('MERCADOS_INCIDENCIA_VER', 'Ver Incidencias de Mercados', 'VIEW', 'Consultar bandeja, detalle y actuaciones de incidencias', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_CREAR', 'Registrar Incidencia', 'CREATE', 'Registrar nuevas incidencias internas o ciudadanas', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_EDITAR', 'Editar Incidencia', 'UPDATE', 'Actualizar datos de incidencias en revisión o asignadas', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_ASIGNAR', 'Asignar Responsable de Incidencia', 'ASSIGN', 'Designar servidor u oficina responsable de la atención', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_ATENDER', 'Atender y Registrar Actuaciones', 'MANAGE', 'Registrar diligencias y acciones operativas de atención', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_EVIDENCIA_GESTIONAR', 'Gestionar Evidencias de Incidencias', 'MANAGE', 'Subir y administrar fotografías y documentos de sustento', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_RESOLVER', 'Resolver Incidencia', 'APPROVE', 'Registrar resolución operativa con sustento verificable', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_CERRAR', 'Cerrar Incidencia', 'APPROVE', 'Concluir formalmente o descartar incidencias con motivo', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_REPORTES', 'Reportes de Incidencias', 'EXPORT', 'Exportar estadísticas y reportes de incidencias a Excel y PDF', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INFRACCION_VER', 'Ver Presuntas Infracciones', 'VIEW', 'Consultar registro de presuntas infracciones y tipificaciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INFRACCION_REGISTRAR', 'Registrar Presunta Infracción', 'CREATE', 'Registrar presunto incumplimiento originado en incidencia o inspección', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INFRACCION_EVALUAR', 'Evaluar Presunta Infracción', 'APPROVE', 'Elaborar evaluación preliminar técnica y de hechos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INFRACCION_DERIVAR', 'Derivar Infracción a Procedimiento', 'APPROVE', 'Proponer inicio de procedimiento y vincular expediente administrativo', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_TIPIFICACION_GESTIONAR', 'Gestionar Tipificaciones', 'MANAGE', 'Administrar catálogo normativo de infracciones y vigencias', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INCIDENCIA_AUDITORIA_VER', 'Ver Auditoría de Incidencias', 'VIEW', 'Consultar bitácora de trazabilidad de incidencias e infracciones', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_INCIDENCIA_VER',
        'MERCADOS_INCIDENCIA_CREAR',
        'MERCADOS_INCIDENCIA_EDITAR',
        'MERCADOS_INCIDENCIA_ASIGNAR',
        'MERCADOS_INCIDENCIA_ATENDER',
        'MERCADOS_INCIDENCIA_EVIDENCIA_GESTIONAR',
        'MERCADOS_INCIDENCIA_RESOLVER',
        'MERCADOS_INCIDENCIA_CERRAR',
        'MERCADOS_INCIDENCIA_REPORTES',
        'MERCADOS_INFRACCION_VER',
        'MERCADOS_INFRACCION_REGISTRAR',
        'MERCADOS_INFRACCION_EVALUAR',
        'MERCADOS_INFRACCION_DERIVAR',
        'MERCADOS_TIPIFICACION_GESTIONAR',
        'MERCADOS_INCIDENCIA_AUDITORIA_VER'
    )
) p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
