-- ==============================================================================
-- SIGM-QUINUA - MUNICIPALIDAD DISTRITAL DE QUINUA, HUAMANGA, AYACUCHO
-- Migración V8: Incorporación de Rol SUPERADMIN y Permisos de Administración
-- ==============================================================================

-- 1. Insertar Rol ROLE_SUPERADMIN si no existe
INSERT INTO roles (codigo, nombre, descripcion, estado)
SELECT 'ROLE_SUPERADMIN', 'Superadministrador Institucional', 'Control total y absoluto de administración, seguridad y parametrización', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE codigo = 'ROLE_SUPERADMIN');

-- 2. Asignar todos los permisos del módulo de ADMINISTRACION a ROLE_SUPERADMIN
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
JOIN modulos_sistema m ON p.modulo_id = m.id
WHERE r.codigo = 'ROLE_SUPERADMIN'
  AND m.codigo = 'ADMINISTRACION'
  AND NOT EXISTS (
      SELECT 1 FROM rol_permisos rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
