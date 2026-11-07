# GUÍA DE DESPLIEGUE EN PRODUCCIÓN — SIGM-QUINUA

## 1. REQUISITOS DEL SERVIDOR DE PRODUCCIÓN

* **Servidor Físico / VM:** Procesador Quad-Core 2.5 GHz o superior, 8 GB de RAM mínimo, 100 GB SSD.
* **Sistema Operativo:** Ubuntu Server 22.04 LTS o Windows Server 2022.
* **Java:** OpenJDK 17 LTS instalado (`/usr/lib/jvm/java-17-openjdk-amd64` o `C:\Program Files\Java\jdk-17`).
* **Motor de Base de Datos:** PostgreSQL 17 configurado con acceso local.
* **Proxy Inverso:** Nginx con módulo SSL activado.

---

## 2. DESPLIEGUE PASO A PASO EN SERVIDOR LINUX

### Paso 1: Creación de usuario y carpetas de servicio
```bash
sudo useradd -r -s /bin/false sigm
sudo mkdir -p /opt/sigm-quinua/storage/documentos
sudo mkdir -p /opt/sigm-quinua/logs
sudo chown -R sigm:sigm /opt/sigm-quinua
```

### Paso 2: Copia del artefacto JAR y verificación de integridad
Copie el artefacto `target/sigm-quinua-0.0.1-SNAPSHOT.jar` a `/opt/sigm-quinua/sigm-quinua.jar`.  
Verifique que la suma de verificación coincida:
```bash
sha256sum /opt/sigm-quinua/sigm-quinua.jar
# Debe coincidir exactamente con:
# 3F3A30CEC5FF7BB15E8ED72812222C09D9DD710350C95D3274A961E55EB922A6
```

### Paso 3: Configuración de variables de entorno seguras
Cree el archivo `/opt/sigm-quinua/.env`:
```ini
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_HOST=127.0.0.1
DB_PORT=5433
DB_NAME=sigm_quinua
DB_USERNAME=sigm_user
DB_PASSWORD=ContraseñaUltraSeguraGeneradaParaProduccion!
STORAGE_LOCATION=/opt/sigm-quinua/storage/documentos
```
Proteja el archivo:
```bash
sudo chmod 600 /opt/sigm-quinua/.env
sudo chown sigm:sigm /opt/sigm-quinua/.env
```

### Paso 4: Configuración del servicio Systemd
Instale el archivo `/etc/systemd/system/sigm-quinua.service`:
```ini
[Unit]
Description=SIGM-QUINUA Aplicacion Municipal
After=network.target postgresql.service

[Service]
User=sigm
Group=sigm
WorkingDirectory=/opt/sigm-quinua
EnvironmentFile=/opt/sigm-quinua/.env
ExecStart=/usr/bin/java -Xms1024m -Xmx2048m -jar /opt/sigm-quinua/sigm-quinua.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### Paso 5: Inicio y validación
```bash
sudo systemctl daemon-reload
sudo systemctl enable sigm-quinua
sudo systemctl start sigm-quinua
sudo systemctl status sigm-quinua
```
Verifique en el navegador o mediante `curl -I http://127.0.0.1:8080/login` que el servidor responde `HTTP/1.1 200 OK`.

