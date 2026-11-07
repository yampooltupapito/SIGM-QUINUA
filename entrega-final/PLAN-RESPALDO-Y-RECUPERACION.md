# PLAN DE RESPALDO Y RECUPERACIÓN ANTE DESASTRES — SIGM-QUINUA

## 1. CRONOGRAMA AUTOMATIZADO DE COPIAS DE SEGURIDAD

Se programa una tarea periódica en el sistema operativo (mediante crontab en Linux o el Programador de Tareas en Windows):

### Script de Respaldo Diario Nocturno (23:00 hrs):
```bash
#!/bin/bash
BACKUP_DIR="/opt/sigm-quinua/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
mkdir -p "$BACKUP_DIR"

# Respaldo de Base de Datos PostgreSQL
pg_dump -h 127.0.0.1 -p 5433 -U sigm_user -F c -b -v -f "$BACKUP_DIR/sigm_quinua_$TIMESTAMP.dump" sigm_quinua

# Respaldo comprimido de Documentos PDF (/storage)
tar -czf "$BACKUP_DIR/storage_$TIMESTAMP.tar.gz" -C /opt/sigm-quinua/storage .

# Retención: Eliminar respaldos con más de 30 días de antigüedad
find "$BACKUP_DIR" -type f -name "*.dump" -mtime +30 -delete
find "$BACKUP_DIR" -type f -name "*.tar.gz" -mtime +30 -delete
```

---

## 2. PROCEDIMIENTO DE RESTAURACIÓN ANTE DESASTRES

1. Detener el servicio: `sudo systemctl stop sigm-quinua`.
2. Recrear la base de datos limpia:
   ```sql
   DROP DATABASE IF EXISTS sigm_quinua;
   CREATE DATABASE sigm_quinua WITH ENCODING = 'UTF8';
   ```
3. Ejecutar la restauración del dump con `pg_restore`:
   ```bash
   pg_restore -h 127.0.0.1 -p 5433 -U sigm_user -d sigm_quinua -v "/opt/sigm-quinua/backups/sigm_quinua_YYYYMMDD_HHMMSS.dump"
   ```
4. Restaurar el repositorio de documentos:
   ```bash
   tar -xzf "/opt/sigm-quinua/backups/storage_YYYYMMDD_HHMMSS.tar.gz" -C /opt/sigm-quinua/storage
   ```
5. Iniciar la aplicación: `sudo systemctl start sigm-quinua`.

