# MANUAL DEL SUPERADMINISTRADOR (`ROLE_SUPERADMIN`) — SIGM-QUINUA

## 1. PROPÓSITO DEL ROL
El Superadministrador es el responsable de la gobernanza de TI, configuración de parámetros del sistema, gestión de seguridad y supervisión de la auditoría administrativa general de la plataforma SIGM-QUINUA.

---

## 2. PROCEDIMIENTOS OPERATIVOS

### A. Inicio de Sesión y Acceso Seguro
1. Ingrese a la URL oficial: `http://localhost:8080/login`.
2. Digite su nombre de usuario de superadministrador y su contraseña institucional.
3. El sistema verificará las credenciales con BCrypt. Al autenticarse, será dirigido a `/admin/dashboard`.

### B. Gestión de Usuarios y Credenciales
1. Diríjase a la opción **Administración** → **Usuarios** (`/admin/usuarios`).
2. Para dar de alta a un nuevo servidor municipal:
   * Haga clic en **Nuevo Usuario**.
   * Ingrese los datos de la persona (DNI, nombres, apellidos, correo institucional).
   * Asigne el rol institucional correspondiente (`ROLE_MESA_PARTES`, `ROLE_MERCADOS`, `ROLE_SERENO`, `ROLE_CAJERO`, etc.).
   * Establezca una contraseña temporal segura que cumpla con la política (mínimo 8 caracteres, alfanumérica).
3. Para desbloquear un usuario bloqueado por intentos fallidos:
   * Busque al usuario por DNI o username.
   * Haga clic en **Desbloquear Cuenta**. El contador de intentos fallidos volverá a cero.

### C. Auditoría Administrativa Forense
1. Ingrese a **Administración** → **Auditoría** (`/admin/auditoria`).
2. Podrá filtrar las transacciones por usuario, fecha, tipo de operación (`LOGIN`, `REGISTRAR_PAGO`, `ASIGNAR_PUESTO`, `ANULAR_RECIBO`) y estado (`EXITO` / `FALLO`).
3. Toda acción crítica incluye la dirección IP del cliente y la huella de trazabilidad.

