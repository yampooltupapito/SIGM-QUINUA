# LISTA DE VERIFICACIÓN PRE-DESPLIEGUE (CHECKLIST) — SIGM-QUINUA

Marque cada casilla antes de autorizar la apertura del sistema al público y personal municipal:

### 1. Infraestructura y Red
- [ ] Servidor físico o virtual con mínimo 8 GB de RAM y 100 GB SSD.
- [ ] Sistema de respaldo de energía eléctrica (UPS) instalado y operativo.
- [ ] Puerto 8080 cerrado en el firewall externo; únicamente accesible vía Nginx (puertos 80 y 443).
- [ ] Certificado SSL/TLS válido para `sigm.muniquinua.gob.pe` instalado en Nginx.

### 2. Base de Datos PostgreSQL 17
- [ ] Motor PostgreSQL activo en puerto 5433 (o local configurado).
- [ ] Base de datos `sigm_quinua` creada con codificación `UTF8` y cotejamiento `es_PE.UTF-8`.
- [ ] Usuario de base de datos exclusivo con contraseña segura (no usar usuario `postgres` por defecto).
- [ ] Verificación de 67 migraciones Flyway aplicadas sin errores (`flyway_schema_history`).

### 3. Seguridad de la Aplicación
- [ ] Clave `JWT_SECRET` generada con al menos 64 caracteres aleatorios seguros.
- [ ] Contraseña predeterminada del usuario `superadmin` cambiada inmediatamente en el primer inicio de sesión.
- [ ] Cuentas de prueba de desarrollo deshabilitadas o eliminadas de la base de datos de producción.
- [ ] Permisos de lectura/escritura restringidos al usuario `sigm` en el directorio de documentos `/storage`.

### 4. Funcionalidad de los Cuatro Servicios
- [ ] **Mesa de Partes:** Radicación de un expediente de prueba, descarga de constancia PDF y verificación de lectura del código QR.
- [ ] **Mercados:** Visualización de padrón de puestos, asignación de puesto y emisión de boleta de sisa.
- [ ] **Serenazgo:** Registro de incidencia de prueba, despacho y emisión de parte de ocurrencia con datos de víctima enmascarados.
- [ ] **Recaudación:** Apertura de caja con saldo base, cobro de arbitrio de prueba, emisión de recibo PDF y cierre de caja con cuadre conforme.

