-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V4: Módulos del Sistema, Roles y Permisos Dinámicos
-- Tablas: modulos_sistema, permisos, roles, usuario_roles, rol_permisos
-- ==============================================================================

-- 1. Tabla de Módulos del Sistema
CREATE TABLE IF NOT EXISTS modulos_sistema (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    icono VARCHAR(50),
    orden INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_modulos_codigo UNIQUE (codigo),
    CONSTRAINT chk_modulo_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. Tabla de Permisos por Módulo y Acción
CREATE TABLE IF NOT EXISTS permisos (
    id BIGSERIAL PRIMARY KEY,
    modulo_id BIGINT NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_permisos_modulo FOREIGN KEY (modulo_id) REFERENCES modulos_sistema(id) ON DELETE RESTRICT,
    CONSTRAINT uq_permisos_codigo UNIQUE (codigo),
    CONSTRAINT chk_permiso_accion CHECK (accion IN ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'ASSIGN', 'EXPORT', 'MANAGE')),
    CONSTRAINT chk_permiso_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 3. Tabla de Roles Institucionales
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_roles_codigo UNIQUE (codigo),
    CONSTRAINT chk_rol_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 4. Tabla de Asignación Usuario - Roles (Muchos a Muchos)
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_roles_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- 5. Tabla de Asignación Rol - Permisos (Muchos a Muchos)
CREATE TABLE IF NOT EXISTS rol_permisos (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permisos_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE RESTRICT,
    CONSTRAINT fk_rol_permisos_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE RESTRICT
);

-- Poblado Estructurado de los 10 Módulos Municipales Oficiales
INSERT INTO modulos_sistema (codigo, nombre, descripcion, icono, orden) VALUES
    ('TRAMITE_DOCUMENTARIO', 'Trámite Documentario y Mesa de Partes', 'Gestión de expedientes, mesa de partes física y digital', 'file-text', 1),
    ('RENTAS_TRIBUTACION', 'Rentas y Administración Tributaria', 'Recaudación de impuesto predial, arbitrios, fraccionamientos', 'dollar-sign', 2),
    ('CATASTRO_URBANO', 'Catastro y Desarrollo Urbano', 'Padrón de predios, licencias de edificación, zonificación', 'map-pin', 3),
    ('REGISTRO_CIVIL', 'Registro Civil y Cementerio', 'Partidas de nacimiento, matrimonio, defunción y nichos', 'book-open', 4),
    ('SEGURIDAD_CIUDADANA', 'Seguridad Ciudadana y Serenazgo', 'Ocurrencias, patrullaje municipal y emergencias comunitarias', 'shield', 5),
    ('SERVICIOS_PUBLICOS', 'Servicios Públicos y Medio Ambiente', 'Gestión de residuos sólidos, áreas verdes y limpieza pública', 'trash-2', 6),
    ('PROGRAMAS_SOCIALES', 'Desarrollo Social y Programas Asistenciales', 'Vaso de Leche, OMAPED, DEMUNA y Centro Integral del Adulto Mayor', 'heart', 7),
    ('DESARROLLO_ECONOMICO', 'Desarrollo Económico y Turismo', 'Comercialización, ferias agropecuarias, licencias de funcionamiento y artesanía de Quinua', 'shopping-bag', 8),
    ('FISCALIZACION_CONTROL', 'Fiscalización y Control Municipal', 'Operativos de control, papeletas de infracción y clausuras', 'clipboard-check', 9),
    ('ADMINISTRACION_SISTEMA', 'Administración del Sistema y Seguridad', 'Usuarios, roles, oficinas, auditoría integral y parámetros', 'settings', 10);

-- Catálogo Base de Roles Institucionales (Sin asignar usuarios predeterminados)
INSERT INTO roles (codigo, nombre, descripcion) VALUES
    ('ROLE_ADMIN', 'Administrador General del Sistema', 'Acceso a la configuración y gestión integral de la plataforma municipal'),
    ('ROLE_JEFE_OFICINA', 'Jefe de Unidad Orgánica', 'Gestión, asignación y aprobación de trámites en su oficina'),
    ('ROLE_OPERADOR', 'Operador Administrativo', 'Registro, procesamiento y emisión de documentos'),
    ('ROLE_MESA_PARTES', 'Operador de Mesa de Partes', 'Recepción, derivación y foliado de expedientes municipales'),
    ('ROLE_FISCALIZADOR', 'Fiscalizador Municipal', 'Registro de actas de inspección, control y notificaciones'),
    ('ROLE_SERENO', 'Efectivo de Serenazgo', 'Registro de intervenciones, patrullajes y novedades de seguridad'),
    ('ROLE_CAJERO', 'Cajero Municipal', 'Emisión de recibos de pago y cierre diario de caja'),
    ('ROLE_CIUDADANO', 'Ciudadano / Administrado', 'Acceso al portal ciudadano para seguimiento de trámites');
