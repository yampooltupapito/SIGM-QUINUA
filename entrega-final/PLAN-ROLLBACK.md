# PLAN DE ROLLBACK Y REVERSIÓN OPERATIVA — SIGM-QUINUA

## 1. CRITERIOS DE ACTIVACIÓN DEL ROLLBACK
Se activará el procedimiento de reversión si tras una actualización o despliegue en producción se presenta alguna de las siguientes condiciones críticas:
* Imposibilidad de inicio de la aplicación Spring Boot durante más de 15 minutos continuos.
* Inconsistencia financiera en las transacciones de cobro en ventanilla de caja.
* Caída de integridad referencial o corrupción del esquema en PostgreSQL.
* Falla masiva en la generación o descarga de constancias y recibos oficiales con código QR.

---

## 2. PROCEDIMIENTO TÉCNICO DE ROLLBACK

### Paso 1: Notificación y detención inmediata
1. Notificar a las oficinas usuarias (Mesa de Partes, Rentas, Mercados, Serenazgo) del pase a contingencia manual durante 20 minutos.
2. Detener el proceso de la aplicación:
   ```bash
   sudo systemctl stop sigm-quinua
   ```

### Paso 2: Reversión del artefacto ejecutable
1. Restaurar el binario JAR de la versión estable anterior desde la carpeta de custodia:
   ```bash
   cp /opt/sigm-quinua/backups/sigm-quinua-anterior.jar /opt/sigm-quinua/sigm-quinua.jar
   ```

### Paso 3: Reversión de la base de datos (si hubo cambios de esquema)
Si la nueva versión introdujo migraciones fallidas:
1. Restaurar el dump completo generado inmediatamente antes del despliegue:
   ```bash
   pg_restore -h 127.0.0.1 -p 5433 -U sigm_user -d sigm_quinua -c -v /opt/sigm-quinua/backups/pre_deploy_sigm_quinua.dump
   ```

### Paso 4: Reinicio y prueba de humo
1. Iniciar el servicio:
   ```bash
   sudo systemctl start sigm-quinua
   ```
2. Realizar prueba de humo autenticándose como `admin` y verificando la carga del dashboard institucional y el estado de los cuatro servicios.

