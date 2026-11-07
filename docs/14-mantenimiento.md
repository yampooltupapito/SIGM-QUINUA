# 14. MANUAL DE MANTENIMIENTO PREVENTIVO Y CORRECTIVO — SIGM-QUINUA

## 1. MANTENIMIENTO PREVENTIVO

### A. Tareas Diarias
1. **Verificación de Logs del Sistema:**  
   Revisar los archivos de registro en `logs/sigm-quinua.log` para identificar excepciones o errores 500 no controlados.
2. **Inspección de Cuadre de Caja:**  
   Confirmar que todas las sesiones de caja diaria en el módulo de Recaudación hayan sido cerradas (`estado = 'CERRADA'`) y conciliadas con los montos reales depositados.
3. **Monitoreo de Espacio en Disco:**  
   Verificar que la partición del sistema y el directorio `/storage` cuenten con al menos un 25% de espacio disponible.

### B. Tareas Semanales
1. **Ejecución de VACUUM y ANALYZE en PostgreSQL:**
   Optimizar los índices y estadísticas del planificador de consultas:
   ```sql
   VACUUM ANALYZE;
   ```
2. **Depuración de Archivos Temporales:**  
   Limpiar reportes intermedios o archivos de carga fallidos en la carpeta temporal.

### C. Tareas Mensuales
1. **Revisión de Intentos Fallidos de Acceso:**  
   Auditar la tabla `auditoria_administrativa` filtrando por eventos de autenticación fallida o bloqueo de cuentas.
2. **Prueba de Restauración de Copia de Seguridad:**  
   Restaurar el último dump de respaldo en un servidor de pruebas para verificar la integridad del backup.

---

## 2. MANTENIMIENTO CORRECTIVO

### A. Bloqueo de Conexiones en PostgreSQL (Deadlocks)
Si se observa congelamiento de transacciones:
```sql
SELECT pid, usename, query, state, age(clock_timestamp(), query_start) 
FROM pg_stat_activity 
WHERE state != 'idle' AND age(clock_timestamp(), query_start) > interval '2 minutes';
```
Para terminar una consulta bloqueante:
```sql
SELECT pg_terminate_backend(PID_BLOQUEANTE);
```

### B. Reinicio Controlado del Servicio
En Windows (PowerShell Administrador):
```powershell
Stop-Process -Name "java" -Force
Start-Process "powershell" -ArgumentList ".\mvnw.cmd spring-boot:run"
```
En Linux Ubuntu Server:
```bash
sudo systemctl restart sigm-quinua
```

