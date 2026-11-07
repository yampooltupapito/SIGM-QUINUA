-- ============================================================================
-- SIGM-QUINUA: MIGRACIÓN V29 - CONTRATOS, AUTORIZACIONES Y VIGENCIAS DE OCUPACIÓN
-- Módulo 2: Mercados Municipales - Prompt 27
-- ============================================================================

-- 1. Secuencias seguras para códigos correlativos institucionales
CREATE SEQUENCE IF NOT EXISTS seq_mercados_codigo_contrato START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_mercados_codigo_autorizacion START WITH 1 INCREMENT BY 1;

-- 2. Catálogo Configurable: Tipos de Contrato de Ocupación
CREATE TABLE IF NOT EXISTS mercados_tipos_contrato (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    base_normativa TEXT,
    vigencia_meses_defecto INT DEFAULT 12,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO mercados_tipos_contrato (codigo, nombre, descripcion, base_normativa, vigencia_meses_defecto, estado) VALUES
    ('OCUPACION_TEMPORAL', 'Ocupación Temporal de Espacio', 'Contrato para campañas estacionales, ferias agropecuarias o festividades patronales', 'Art. 83 Ley N° 27972', 3, 'ACTIVO'),
    ('USO_ESPACIO_COMERCIAL', 'Uso de Espacio Comercial Municipal', 'Instrumento contractual regular de uso de puesto de mercado municipal', 'Ordenanza Municipal N° 008-2023-MDQ', 12, 'ACTIVO'),
    ('ARRENDAMIENTO_ESPACIO_COMERCIAL', 'Arrendamiento de Espacio Comercial', 'Contrato formal de arrendamiento de locales cerrados o modulares de propiedad edil', 'Código Civil y Ley N° 27972', 12, 'ACTIVO'),
    ('OTRO_CONTRATO', 'Otro Instrumento Contractual Especial', 'Convenios interinstitucionales o cesiones en uso especial autorizadas por Concejo', 'Acuerdo de Concejo Municipal', 6, 'ACTIVO')
ON CONFLICT (codigo) DO NOTHING;

-- 3. Catálogo Configurable: Tipos de Autorización Comercial
CREATE TABLE IF NOT EXISTS mercados_tipos_autorizacion (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    procedimiento_relacionado VARCHAR(150),
    oficina_competente_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    vigencia_meses_defecto INT DEFAULT 12,
    fuente_normativa TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO mercados_tipos_autorizacion (codigo, nombre, descripcion, procedimiento_relacionado, vigencia_meses_defecto, fuente_normativa, estado) VALUES
    ('PADRON_MUNICIPAL', 'Autorización por Padrón Municipal', 'Acto administrativo que reconoce la condición de comerciante habilitado', 'Procedimiento TUPA - Empadronamiento de Comerciantes', 12, 'Reglamento de Mercados Quinua', 'ACTIVO'),
    ('AUTORIZACION_TEMPORAL', 'Autorización Comercial Temporal', 'Permiso provisional de funcionamiento en ferias o festividades locales', 'Procedimiento TUPA - Ferias Temporales', 3, 'Decreto de Alcaldía', 'ACTIVO'),
    ('PERMISO_ESPECIAL', 'Permiso Extraordinario de Funcionamiento', 'Autorización excepcional sujeta a verificación técnica de salubridad y defensa civil', 'Procedimiento TUPA - Permisos Especiales', 6, 'Resolución de Gerencia Municipal', 'ACTIVO'),
    ('LICENCIA_MERCADO', 'Autorización Municipal de Conducción de Puesto', 'Acto administrativo formal de habilitación para el giro comercial adjudicado', 'Procedimiento TUPA - Conducción de Puestos', 24, 'Ordenanza Municipal N° 008-2023-MDQ', 'ACTIVO')
ON CONFLICT (codigo) DO NOTHING;

-- 4. Tabla de Contratos de Ocupación de Puestos Comerciales
CREATE TABLE IF NOT EXISTS mercados_contratos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_contrato VARCHAR(50) NOT NULL UNIQUE,
    asignacion_id UUID NOT NULL REFERENCES mercados_asignaciones(id) ON DELETE RESTRICT,
    comerciante_id UUID NOT NULL REFERENCES mercados_comerciantes(id) ON DELETE RESTRICT,
    puesto_id UUID NOT NULL REFERENCES mercados_puestos(id) ON DELETE RESTRICT,
    tipo_contrato_id BIGINT REFERENCES mercados_tipos_contrato(id) ON DELETE SET NULL,
    tipo_contrato_codigo VARCHAR(50) NOT NULL DEFAULT 'USO_ESPACIO_COMERCIAL',
    fecha_elaboracion DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    expediente_id BIGINT REFERENCES mpv_expedientes(id) ON DELETE SET NULL,
    oficina_responsable_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    responsable_elaboracion VARCHAR(100),
    responsable_aprobacion VARCHAR(100),
    fecha_aprobacion TIMESTAMP WITH TIME ZONE,
    fecha_formalizacion TIMESTAMP WITH TIME ZONE,
    codigo_seguro VARCHAR(100) UNIQUE,
    hash_documento VARCHAR(64),
    motivo_observacion TEXT,
    motivo_suspension TEXT,
    motivo_terminacion TEXT,
    observaciones TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_contrato_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_contrato_estado CHECK (estado IN ('BORRADOR', 'EN_REVISION', 'OBSERVADO', 'APROBADO', 'PENDIENTE_FORMALIZACION', 'VIGENTE', 'SUSPENDIDO', 'VENCIDO', 'TERMINADO', 'ANULADO'))
);

CREATE INDEX IF NOT EXISTS idx_mercados_contratos_asignacion ON mercados_contratos(asignacion_id);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_comerciante ON mercados_contratos(comerciante_id);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_puesto ON mercados_contratos(puesto_id);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_estado ON mercados_contratos(estado);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_expediente ON mercados_contratos(expediente_id);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_fechas ON mercados_contratos(fecha_inicio, fecha_fin);
CREATE INDEX IF NOT EXISTS idx_mercados_contratos_codigo_seguro ON mercados_contratos(codigo_seguro);

-- 5. Historial Inmutable de Contratos de Ocupación
CREATE TABLE IF NOT EXISTS mercados_contratos_historial (
    id BIGSERIAL PRIMARY KEY,
    contrato_id UUID NOT NULL REFERENCES mercados_contratos(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    motivo TEXT,
    usuario VARCHAR(100) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    documento_referencia VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_contratos_historial_contrato ON mercados_contratos_historial(contrato_id);
CREATE INDEX IF NOT EXISTS idx_contratos_historial_fecha ON mercados_contratos_historial(fecha);

-- 6. Actualización y adecuación de mercados_autorizaciones
ALTER TABLE mercados_autorizaciones DROP CONSTRAINT IF EXISTS chk_autorizacion_estado;
ALTER TABLE mercados_autorizaciones ADD CONSTRAINT chk_autorizacion_estado 
    CHECK (estado IN ('BORRADOR', 'EN_EVALUACION', 'OBSERVADA', 'APROBADA', 'EMITIDA', 'VIGENTE', 'SUSPENDIDA', 'VENCIDA', 'REVOCADA', 'ANULADA'));

ALTER TABLE mercados_autorizaciones ALTER COLUMN fecha_inicio DROP NOT NULL;
ALTER TABLE mercados_autorizaciones ALTER COLUMN fecha_fin DROP NOT NULL;

ALTER TABLE mercados_autorizaciones DROP CONSTRAINT IF EXISTS chk_autorizacion_fechas;
ALTER TABLE mercados_autorizaciones ADD CONSTRAINT chk_autorizacion_fechas 
    CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio);

ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS asignacion_id UUID REFERENCES mercados_asignaciones(id) ON DELETE SET NULL;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS codigo_autorizacion VARCHAR(50);
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS fecha_solicitud DATE DEFAULT CURRENT_DATE;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS fecha_emision DATE;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS expediente_id BIGINT REFERENCES mpv_expedientes(id) ON DELETE SET NULL;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS oficina_emisora_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS responsable_evaluacion VARCHAR(100);
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS responsable_aprobacion VARCHAR(100);
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS fecha_evaluacion TIMESTAMP WITH TIME ZONE;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS fecha_aprobacion TIMESTAMP WITH TIME ZONE;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS motivo_observacion TEXT;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS motivo_suspension TEXT;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS motivo_revocacion TEXT;
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS codigo_seguro VARCHAR(100);
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS hash_documento VARCHAR(64);
ALTER TABLE mercados_autorizaciones ADD COLUMN IF NOT EXISTS tipo_autorizacion_id BIGINT REFERENCES mercados_tipos_autorizacion(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_codigo_autorizacion ON mercados_autorizaciones(codigo_autorizacion) WHERE codigo_autorizacion IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_autorizacion_codigo_seguro ON mercados_autorizaciones(codigo_seguro) WHERE codigo_seguro IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_mercados_autorizaciones_asignacion ON mercados_autorizaciones(asignacion_id);
CREATE INDEX IF NOT EXISTS idx_mercados_autorizaciones_expediente ON mercados_autorizaciones(expediente_id);

-- 7. Historial Inmutable de Autorizaciones Comerciales
CREATE TABLE IF NOT EXISTS mercados_autorizaciones_historial (
    id BIGSERIAL PRIMARY KEY,
    autorizacion_id UUID NOT NULL REFERENCES mercados_autorizaciones(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    motivo TEXT,
    usuario VARCHAR(100) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    documento_referencia VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_autorizaciones_historial_aut ON mercados_autorizaciones_historial(autorizacion_id);
CREATE INDEX IF NOT EXISTS idx_autorizaciones_historial_fecha ON mercados_autorizaciones_historial(fecha);

-- 8. Registro de Permisos Granulares del Módulo (Prompt 27)
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT 
    m.id, p.codigo, p.nombre, p.accion, p.descripcion, 'ACTIVO'
FROM (
    VALUES
        ('MERCADOS_CONTRATO_VER', 'Ver Contratos de Mercados', 'VIEW', 'Ver contratos de ocupación de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_CREAR', 'Crear Borrador Contrato', 'CREATE', 'Crear borradores de contratos de ocupación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_EDITAR', 'Editar Borrador Contrato', 'UPDATE', 'Modificar borradores de contratos de ocupación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_REVISAR', 'Revisar Contrato', 'MANAGE', 'Enviar contratos a revisión y registrar observaciones', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_APROBAR', 'Aprobar Contrato', 'APPROVE', 'Aprobar administrativamente contratos de ocupación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_FORMALIZAR', 'Formalizar Contrato', 'MANAGE', 'Registrar formalización y vigencia de contratos', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_SUSPENDER', 'Suspender Contrato', 'MANAGE', 'Suspender vigencia de contratos de ocupación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_CONTRATO_TERMINAR', 'Terminar Contrato', 'MANAGE', 'Registrar terminación de contratos de ocupación', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_VER', 'Ver Autorizaciones Comerciales', 'VIEW', 'Ver autorizaciones comerciales de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_CREAR', 'Crear Solicitud Autorización', 'CREATE', 'Crear solicitudes de autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_EVALUAR', 'Evaluar Autorización', 'MANAGE', 'Evaluar y observar autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_APROBAR', 'Aprobar Autorización', 'APPROVE', 'Aprobar autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_EMITIR', 'Emitir Autorización', 'MANAGE', 'Emitir y formalizar autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_SUSPENDER', 'Suspender Autorización', 'MANAGE', 'Suspender autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_AUTORIZACION_REVOCAR', 'Revocar Autorización', 'MANAGE', 'Revocar o anular autorizaciones comerciales', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_DOCUMENTO_VERIFICAR', 'Verificar Documento Mercado', 'VIEW', 'Verificar autenticidad de documentos de mercados', 'DESARROLLO_ECONOMICO'),
        ('MERCADOS_DOCUMENTO_EXPORTAR', 'Exportar Documentos Mercados', 'EXPORT', 'Exportar reportes de contratos y autorizaciones', 'DESARROLLO_ECONOMICO')
) AS p(codigo, nombre, accion, descripcion, modulo_codigo)
JOIN modulos_sistema m ON m.codigo = p.modulo_codigo
WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = p.codigo);

-- Asignar nuevos permisos a roles administrativos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.permiso_id
FROM roles r
CROSS JOIN (
    SELECT id AS permiso_id FROM permisos WHERE codigo IN (
        'MERCADOS_CONTRATO_VER',
        'MERCADOS_CONTRATO_CREAR',
        'MERCADOS_CONTRATO_EDITAR',
        'MERCADOS_CONTRATO_REVISAR',
        'MERCADOS_CONTRATO_APROBAR',
        'MERCADOS_CONTRATO_FORMALIZAR',
        'MERCADOS_CONTRATO_SUSPENDER',
        'MERCADOS_CONTRATO_TERMINAR',
        'MERCADOS_AUTORIZACION_VER',
        'MERCADOS_AUTORIZACION_CREAR',
        'MERCADOS_AUTORIZACION_EVALUAR',
        'MERCADOS_AUTORIZACION_APROBAR',
        'MERCADOS_AUTORIZACION_EMITIR',
        'MERCADOS_AUTORIZACION_SUSPENDER',
        'MERCADOS_AUTORIZACION_REVOCAR',
        'MERCADOS_DOCUMENTO_VERIFICAR',
        'MERCADOS_DOCUMENTO_EXPORTAR'
    )
) p
WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN')
AND NOT EXISTS (
    SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.permiso_id
);
