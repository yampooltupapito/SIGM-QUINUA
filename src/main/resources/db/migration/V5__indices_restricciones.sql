-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V5: Índices de Rendimiento y Restricciones de Integridad
-- ==============================================================================

-- 1. Índices para Búsqueda y Validación de Personas
CREATE INDEX IF NOT EXISTS idx_personas_doc ON personas (tipo_documento, numero_documento);
CREATE INDEX IF NOT EXISTS idx_personas_apellidos ON personas (apellido_paterno, apellido_materno);
CREATE INDEX IF NOT EXISTS idx_personas_correo ON personas (correo);
CREATE INDEX IF NOT EXISTS idx_personas_estado ON personas (estado);

-- 2. Índices para Cuentas de Usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios (username);
CREATE INDEX IF NOT EXISTS idx_usuarios_persona ON usuarios (persona_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_enabled ON usuarios (enabled);

-- 3. Índices para Estructura de Oficinas y Jerarquía
CREATE INDEX IF NOT EXISTS idx_oficinas_municipalidad ON oficinas (municipalidad_id);
CREATE INDEX IF NOT EXISTS idx_oficinas_padre ON oficinas (oficina_padre_id);
CREATE INDEX IF NOT EXISTS idx_oficinas_codigo ON oficinas (codigo);
CREATE INDEX IF NOT EXISTS idx_oficinas_estado ON oficinas (estado);

-- Restricción de integridad: Una oficina no puede ser su propio padre
ALTER TABLE oficinas ADD CONSTRAINT chk_oficina_no_autociclo CHECK (oficina_padre_id IS NULL OR oficina_padre_id <> id);

-- 4. Índices para Asignaciones Persona - Oficina
CREATE INDEX IF NOT EXISTS idx_persona_oficinas_persona ON persona_oficinas (persona_id);
CREATE INDEX IF NOT EXISTS idx_persona_oficinas_oficina ON persona_oficinas (oficina_id);
CREATE INDEX IF NOT EXISTS idx_persona_oficinas_cargo ON persona_oficinas (cargo_id);
CREATE INDEX IF NOT EXISTS idx_persona_oficinas_tipo_actor ON persona_oficinas (tipo_actor_id);
CREATE INDEX IF NOT EXISTS idx_persona_oficinas_vigencia ON persona_oficinas (fecha_inicio, fecha_fin);

-- 5. Índices para Control de Acceso (Roles y Permisos)
CREATE INDEX IF NOT EXISTS idx_permisos_modulo ON permisos (modulo_id);
CREATE INDEX IF NOT EXISTS idx_permisos_accion ON permisos (accion);
CREATE INDEX IF NOT EXISTS idx_roles_codigo ON roles (codigo);
CREATE INDEX IF NOT EXISTS idx_usuario_roles_usuario ON usuario_roles (usuario_id);
CREATE INDEX IF NOT EXISTS idx_usuario_roles_rol ON usuario_roles (rol_id);
CREATE INDEX IF NOT EXISTS idx_rol_permisos_rol ON rol_permisos (rol_id);
CREATE INDEX IF NOT EXISTS idx_rol_permisos_permiso ON rol_permisos (permiso_id);
