-- ==============================================================================
-- SIGM-QUINUA - PROMPT 29 DE 150: INSPECCIONES Y SUPERVISIÓN DE MERCADOS
-- Módulo: Mercados Municipales - Fiscalización y Control Administrativo
-- ==============================================================================

-- 1. Secuencias para numeración correlativa segura
CREATE SEQUENCE IF NOT EXISTS mercados_inspecciones_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mercados_actas_inspeccion_seq START WITH 1 INCREMENT BY 1;

-- 2. Plantillas de Listas de Verificación
CREATE TABLE IF NOT EXISTS mercados_plantillas_inspeccion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_inspeccion VARCHAR(50) NOT NULL, -- PROGRAMADA, EXTRAORDINARIA, SEGUIMIENTO, DOCUMENTAL, INFRAESTRUCTURA, SALUBRIDAD_HIGIENE
    version INT NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, INACTIVA, HISTORICA
    oficina_responsable_id BIGINT REFERENCES oficinas(id),
    fecha_aprobacion DATE,
    sustento_normativo VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version_lock BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_plantillas_inspeccion_codigo ON mercados_plantillas_inspeccion(codigo);
CREATE INDEX IF NOT EXISTS idx_plantillas_inspeccion_tipo ON mercados_plantillas_inspeccion(tipo_inspeccion);

-- 3. Ítems de Verificación de las Plantillas
CREATE TABLE IF NOT EXISTS mercados_items_verificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plantilla_id UUID NOT NULL REFERENCES mercados_plantillas_inspeccion(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    categoria VARCHAR(100) NOT NULL, -- INFRAESTRUCTURA, SALUBRIDAD_HIGIENE, ORDEN_COMERCIAL, SEGURIDAD_EXTINTORES, DOCUMENTAL
    descripcion TEXT NOT NULL,
    orden INT NOT NULL DEFAULT 0,
    obligatorio BOOLEAN NOT NULL DEFAULT true,
    referencia_normativa VARCHAR(255),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_plantilla_item_codigo UNIQUE (plantilla_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_items_verificacion_plantilla ON mercados_items_verificacion(plantilla_id);

-- 4. Inspecciones de Mercados Municipales
CREATE TABLE IF NOT EXISTS mercados_inspecciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_inspeccion VARCHAR(50) NOT NULL UNIQUE,
    tipo_inspeccion VARCHAR(50) NOT NULL, -- PROGRAMADA, EXTRAORDINARIA, SEGUIMIENTO, DOCUMENTAL, INFRAESTRUCTURA, SALUBRIDAD_HIGIENE
    mercado_id UUID NOT NULL REFERENCES mercados_mercados(id) ON DELETE RESTRICT,
    sector_id UUID REFERENCES mercados_sectores(id) ON DELETE SET NULL,
    pabellon_id UUID REFERENCES mercados_pabellones(id) ON DELETE SET NULL,
    puesto_id UUID REFERENCES mercados_puestos(id) ON DELETE SET NULL,
    comerciante_id UUID REFERENCES mercados_comerciantes(id) ON DELETE SET NULL,
    inspeccion_origen_id UUID REFERENCES mercados_inspecciones(id) ON DELETE SET NULL, -- Para inspecciones de seguimiento
    plantilla_id UUID REFERENCES mercados_plantillas_inspeccion(id) ON DELETE SET NULL,
    fecha_programada DATE NOT NULL,
    hora_programada TIME,
    fecha_inicio_real TIMESTAMP WITH TIME ZONE,
    fecha_fin_real TIMESTAMP WITH TIME ZONE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR', -- BORRADOR, PROGRAMADA, ASIGNADA, EN_PROCESO, PENDIENTE_REVISION, OBSERVADA, FINALIZADA, CANCELADA
    motivo VARCHAR(500) NOT NULL,
    objetivo TEXT NOT NULL,
    oficina_responsable_id BIGINT REFERENCES oficinas(id),
    inspector_responsable_id BIGINT REFERENCES usuarios(id),
    expediente_id UUID,
    observaciones_generales TEXT,
    resultado_general VARCHAR(30), -- CONFORME, CON_OBSERVACIONES, NO_CONFORME, SUSPENDIDA
    motivo_cancelacion TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_codigo ON mercados_inspecciones(codigo_inspeccion);
CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_mercado ON mercados_inspecciones(mercado_id);
CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_puesto ON mercados_inspecciones(puesto_id);
CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_comerciante ON mercados_inspecciones(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_fecha ON mercados_inspecciones(fecha_programada);
CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_estado ON mercados_inspecciones(estado);
CREATE INDEX IF NOT EXISTS idx_mercados_inspecciones_inspector ON mercados_inspecciones(inspector_responsable_id);

-- 5. Inspectores y Participantes del Equipo de Inspección
CREATE TABLE IF NOT EXISTS mercados_inspecciones_participantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspeccion_id UUID NOT NULL REFERENCES mercados_inspecciones(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    funcion VARCHAR(50) NOT NULL DEFAULT 'INSPECTOR_APOYO', -- INSPECTOR_PRINCIPAL, INSPECTOR_APOYO, ESPECIALISTA_TECNICO, VEEDOR
    fecha_asignacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_inspeccion_participante UNIQUE (inspeccion_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_inspecciones_part_inspeccion ON mercados_inspecciones_participantes(inspeccion_id);
CREATE INDEX IF NOT EXISTS idx_inspecciones_part_usuario ON mercados_inspecciones_participantes(usuario_id);

-- 6. Respuestas de Verificación de la Inspección
CREATE TABLE IF NOT EXISTS mercados_inspecciones_verificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspeccion_id UUID NOT NULL REFERENCES mercados_inspecciones(id) ON DELETE CASCADE,
    item_verificacion_id UUID NOT NULL REFERENCES mercados_items_verificacion(id) ON DELETE RESTRICT,
    resultado VARCHAR(30) NOT NULL, -- CONFORME, NO_CONFORME, NO_APLICA, NO_EVALUADO
    observaciones TEXT,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    inspector_registro_id BIGINT NOT NULL REFERENCES usuarios(id),
    CONSTRAINT uq_inspeccion_item_verif UNIQUE (inspeccion_id, item_verificacion_id)
);

CREATE INDEX IF NOT EXISTS idx_inspecciones_verif_inspeccion ON mercados_inspecciones_verificaciones(inspeccion_id);

-- 7. Hallazgos Técnicos de Inspección
CREATE TABLE IF NOT EXISTS mercados_hallazgos_inspeccion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspeccion_id UUID NOT NULL REFERENCES mercados_inspecciones(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    gravedad_tecnica VARCHAR(30), -- LEVE, MODERADA, GRAVE, MUY_GRAVE
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO', -- ABIERTO, CON_MEDIDA_CORRECTIVA, SUBSANADO, CERRADO
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsable_registro_id BIGINT NOT NULL REFERENCES usuarios(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_inspeccion_hallazgo_codigo UNIQUE (inspeccion_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_hallazgos_inspeccion_inspeccion ON mercados_hallazgos_inspeccion(inspeccion_id);
CREATE INDEX IF NOT EXISTS idx_hallazgos_inspeccion_estado ON mercados_hallazgos_inspeccion(estado);

-- 8. Evidencias Fotográficas y Documentales
CREATE TABLE IF NOT EXISTS mercados_evidencias_inspeccion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspeccion_id UUID NOT NULL REFERENCES mercados_inspecciones(id) ON DELETE CASCADE,
    hallazgo_id UUID REFERENCES mercados_hallazgos_inspeccion(id) ON DELETE SET NULL,
    tipo_evidencia VARCHAR(30) NOT NULL, -- FOTOGRAFIA, DOCUMENTO_SUSTENTO, INFORME_TECNICO
    nombre_archivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tamanio_bytes BIGINT NOT NULL,
    ruta_almacenamiento VARCHAR(500) NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    descripcion TEXT,
    fecha_subida TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_subida_id BIGINT NOT NULL REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_evidencias_inspeccion ON mercados_evidencias_inspeccion(inspeccion_id);
CREATE INDEX IF NOT EXISTS idx_evidencias_hallazgo ON mercados_evidencias_inspeccion(hallazgo_id);

-- 9. Medidas Correctivas
CREATE TABLE IF NOT EXISTS mercados_medidas_correctivas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hallazgo_id UUID NOT NULL REFERENCES mercados_hallazgos_inspeccion(id) ON DELETE CASCADE,
    descripcion TEXT NOT NULL,
    tipo_responsable VARCHAR(50) NOT NULL DEFAULT 'COMERCIANTE_TITULAR', -- COMERCIANTE_TITULAR, ADMINISTRACION_MERCADO, MANTENIMIENTO_MUNICIPAL
    responsable_id BIGINT REFERENCES usuarios(id),
    comerciante_responsable_id UUID REFERENCES mercados_comerciantes(id),
    fecha_asignacion DATE NOT NULL,
    fecha_limite DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, EN_PROCESO, PRESENTADA_PARA_VERIFICACION, SUBSANADA, NO_SUBSANADA, CANCELADA
    observaciones TEXT,
    documento_sustento_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_medida_fechas CHECK (fecha_limite IS NULL OR fecha_limite >= fecha_asignacion)
);

CREATE INDEX IF NOT EXISTS idx_medidas_correctivas_hallazgo ON mercados_medidas_correctivas(hallazgo_id);
CREATE INDEX IF NOT EXISTS idx_medidas_correctivas_estado ON mercados_medidas_correctivas(estado);
CREATE INDEX IF NOT EXISTS idx_medidas_correctivas_fecha_limite ON mercados_medidas_correctivas(fecha_limite);

-- 10. Subsanaciones Presentadas para Medidas Correctivas
CREATE TABLE IF NOT EXISTS mercados_subsanaciones_medida (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medida_correctiva_id UUID NOT NULL REFERENCES mercados_medidas_correctivas(id) ON DELETE CASCADE,
    descripcion_subsanacion TEXT NOT NULL,
    fecha_presentacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    presentado_por_id BIGINT REFERENCES usuarios(id),
    documento_evidencia_id UUID,
    estado_verificacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_REVISION', -- PENDIENTE_REVISION, APROBADA_CONFORME, RECHAZADA_INSUFICIENTE
    fecha_verificacion TIMESTAMP WITH TIME ZONE,
    inspector_verificador_id BIGINT REFERENCES usuarios(id),
    observaciones_verificacion TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subsanaciones_medida ON mercados_subsanaciones_medida(medida_correctiva_id);

-- 11. Actas Oficiales de Inspección
CREATE TABLE IF NOT EXISTS mercados_actas_inspeccion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspeccion_id UUID NOT NULL UNIQUE REFERENCES mercados_inspecciones(id) ON DELETE RESTRICT,
    codigo_acta VARCHAR(50) NOT NULL UNIQUE,
    fecha_elaboracion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resumen TEXT NOT NULL,
    conclusiones TEXT NOT NULL,
    recomendaciones TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR', -- BORRADOR, EN_REVISION, APROBADA, EMITIDA, ANULADA
    documento_id UUID,
    codigo_verificacion VARCHAR(64) NOT NULL UNIQUE,
    hash_sha256 VARCHAR(64) NOT NULL,
    responsable_id BIGINT NOT NULL REFERENCES usuarios(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_actas_inspeccion_codigo ON mercados_actas_inspeccion(codigo_acta);
CREATE INDEX IF NOT EXISTS idx_actas_inspeccion_verificacion ON mercados_actas_inspeccion(codigo_verificacion);

-- 12. Plantilla Semilla Institucional para Quinua (Inspección Sanitaria y de Orden)
INSERT INTO mercados_plantillas_inspeccion (id, codigo, nombre, descripcion, tipo_inspeccion, version, estado, sustento_normativo)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'PLANT-HIGIENE-ORDEN-2026',
    'Lista de Verificación de Salubridad, Orden y Seguridad de Puestos',
    'Plantilla estándar para supervisión de puestos de abastos y locales comerciales de mercados municipales.',
    'SALUBRIDAD_HIGIENE',
    1,
    'ACTIVA',
    'Ordenanza Municipal N° 004-2023-MDQ / D.S. N° 007-98-SA'
) ON CONFLICT (codigo) DO NOTHING;

INSERT INTO mercados_items_verificacion (id, plantilla_id, codigo, categoria, descripcion, orden, obligatorio, referencia_normativa)
VALUES 
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-01', 'DOCUMENTAL', 'Cuenta con Autorización Comercial Municipal vigente y a la vista.', 1, true, 'Art. 12 Ord. 004-MDQ'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-02', 'SALUBRIDAD_HIGIENE', 'Presenta Carné de Sanidad vigente del titular y ayudantes.', 2, true, 'D.S. 007-98-SA'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-03', 'SALUBRIDAD_HIGIENE', 'Uso obligatorio de indumentaria adecuada (mandil, gorro/cofia y mascarilla si manipula alimentos).', 3, true, 'R.M. 449-2006-MINSA'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-04', 'ORDEN_COMERCIAL', 'Limpieza y orden del puesto; libre de desechos fuera del contenedor reglamentario.', 4, true, 'Art. 18 Ord. 004-MDQ'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-05', 'ORDEN_COMERCIAL', 'Respeto irrestricto del metraje asignado; pasadizos libres de mercadería o vitrinas.', 5, true, 'Art. 22 Ord. 004-MDQ'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-06', 'SEGURIDAD_EXTINTORES', 'Extintor operativo de PQS vigente con tarjeta de inspección mensual al día.', 6, true, 'NTP 350.043-1'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'ITEM-07', 'INFRAESTRUCTURA', 'Instalaciones eléctricas empotradas o entubadas, sin cables mellizos expuestos.', 7, true, 'CNE Utilización 2006')
ON CONFLICT DO NOTHING;

-- 13. Registro de Permisos Granulares RBAC para Inspecciones de Mercados
INSERT INTO permisos (codigo, nombre, accion, descripcion, modulo_id)
SELECT p.codigo, p.nombre, p.accion, p.descripcion, m.id
FROM (
    VALUES
        ('MERCADOS_INSPECCION_VER', 'Ver Inspecciones de Mercados', 'VIEW', 'Consultar expedientes, programaciones y actas de inspección', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_CREAR', 'Crear Inspección de Mercados', 'CREATE', 'Registrar nuevas inspecciones ordinarias y extraordinarias', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_PROGRAMAR', 'Programar Inspección de Mercados', 'UPDATE', 'Calendarizar visitas y definir objetivos de supervisión', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_ASIGNAR', 'Asignar Inspectores de Mercados', 'ASSIGN', 'Designar inspectores responsables y conformar equipos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_EJECUTAR', 'Ejecutar Inspección In Situ', 'MANAGE', 'Iniciar visita, registrar respuestas y evaluar condiciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_HALLAZGO_REGISTRAR', 'Registrar Hallazgos Técnicos', 'CREATE', 'Documentar observaciones y hallazgos con gravedad técnica', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_EVIDENCIA_GESTIONAR', 'Gestionar Evidencias Fotográficas', 'MANAGE', 'Subir y asociar fotografías y documentos de prueba', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_MEDIDA_GESTIONAR', 'Gestionar Medidas Correctivas', 'MANAGE', 'Establecer medidas de subsanación y plazos perentorios', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_SUBSANACION_VERIFICAR', 'Verificar Subsanaciones', 'APPROVE', 'Evaluar evidencias de subsanación presentadas por comerciantes', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_ACTA_ELABORAR', 'Elaborar Acta de Inspección', 'CREATE', 'Redactar borrador de acta con conclusiones y recomendaciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_ACTA_APROBAR', 'Aprobar y Emitir Acta', 'APPROVE', 'Aprobar acta final y generar documento oficial con QR', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_REPORTES', 'Reportes de Supervisión de Mercados', 'EXPORT', 'Exportar reportes de inspecciones y medidas a Excel y PDF', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_INSPECCION_AUDITORIA_VER', 'Ver Auditoría de Inspecciones', 'VIEW', 'Consultar bitácora de auditoría y trazabilidad de fiscalización', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_INSPECCION_VER',
        'MERCADOS_INSPECCION_CREAR',
        'MERCADOS_INSPECCION_PROGRAMAR',
        'MERCADOS_INSPECCION_ASIGNAR',
        'MERCADOS_INSPECCION_EJECUTAR',
        'MERCADOS_INSPECCION_HALLAZGO_REGISTRAR',
        'MERCADOS_INSPECCION_EVIDENCIA_GESTIONAR',
        'MERCADOS_INSPECCION_MEDIDA_GESTIONAR',
        'MERCADOS_INSPECCION_SUBSANACION_VERIFICAR',
        'MERCADOS_INSPECCION_ACTA_ELABORAR',
        'MERCADOS_INSPECCION_ACTA_APROBAR',
        'MERCADOS_INSPECCION_REPORTES',
        'MERCADOS_INSPECCION_AUDITORIA_VER'
    )
) p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
