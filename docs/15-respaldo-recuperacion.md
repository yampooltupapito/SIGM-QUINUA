# 15. ESTRATEGIA DE RESPALDO Y RECUPERACIÓN ANTE DESASTRES — SIGM-QUINUA

## 1. POLÍTICA DE COPIAS DE SEGURIDAD (BACKUPS)

Para garantizar la continuidad operativa de la Municipalidad Distrital de Quinua y el cumplimiento de las normativas de archivo digital, se define la siguiente política de respaldo:

| Tipo de Backup | Frecuencia | Hora Programada | Destino de Almacenamiento | Retención |
| :--- | :---: | :---: | :--- | :---: |
| **Backup Completo de Base de Datos** | Diario | 23:00 hrs | Almacén NAS secundario / Disco externo | 30 días |
| **Backup de Archivos y Documentos PDF** | Diario | 23:30 hrs | Almacén NAS secundario / Disco externo | 90 días |
| **Respaldo Mensual Consolidado** | Mensual (fin de mes)| 23:59 hrs | Disco offline en custodia segura (bóveda) | 5 años |

---

## 2. PROCEDIMIENTO DE RESPALDO DE BASE DE DATOS (`pg_dump`)

### Comando de Respaldo en Formato Custom (`.dump`):
```powershell
$FECHA = Get-Date -Format "yyyyMMdd_HHmmss"
& "C:\Program Files\PostgreSQL\17\bin\pg_dump.exe" `
    -h localhost `
    -p 5433 `
    -U postgres `
    -F c `
    -b `
    -v `
    -f "D:\Backups_SIGM\sigm_quinua_$FECHA.dump" `
    sigm_quinua
```

### Script de Respaldo de Documentos PDF (`storage/`):
```powershell
$FECHA = Get-Date -Format "yyyyMMdd_HHmmss"
Compress-Archive `
    -Path "c:\Users\ASUS\OneDrive\PROYECTO FINAL DE LENGUAJE DE PROGRAMACION MUNICIPALIDAD DISTRITAL DE QUINUA\SIGM-QUINUA\storage\*" `
    -DestinationPath "D:\Backups_SIGM\storage_docs_$FECHA.zip"
```

---

## 3. PROCEDIMIENTO DE RECUPERACIÓN ANTE DESASTRES (RESTORE)

En caso de fallo de hardware, corrupción de datos o desastre operativo:

### Paso 1: Crear base de datos limpia
```sql
DROP DATABASE IF EXISTS sigm_quinua;
CREATE DATABASE sigm_quinua WITH ENCODING = 'UTF8';
```

### Paso 2: Ejecutar restauración mediante `pg_restore`
```powershell
& "C:\Program Files\PostgreSQL\17\bin\pg_restore.exe" `
    -h localhost `
    -p 5433 `
    -U postgres `
    -d sigm_quinua `
    -v `
    "D:\Backups_SIGM\sigm_quinua_YYYYMMDD_HHMMSS.dump"
```

### Paso 3: Restaurar repositorio de archivos
Descomprimir el archivo `storage_docs_YYYYMMDD_HHMMSS.zip` dentro de la carpeta `storage/documentos`.

### Paso 4: Comprobación de Integridad
1. Iniciar la aplicación Spring Boot.
2. Ingresar con rol de administrador y verificar el dashboard institucional.
3. Consultar un expediente en Mesa de Partes y descargar su constancia PDF para corroborar que el documento y la base de datos coincidan.

