# METADATOS TÉCNICOS DEL ARTEFACTO EJECUTABLE — SIGM-QUINUA

## 1. IDENTIFICACIÓN DEL ARTEFACTO CANDIDATO A PRODUCCIÓN

* **Nombre del Archivo:** `sigm-quinua-0.0.1-SNAPSHOT.jar`
* **Ruta de Generación en Repositorio:** `target/sigm-quinua-0.0.1-SNAPSHOT.jar`
* **Tipo de Artefacto:** Fat JAR ejecutable Spring Boot (incluye servidor web Apache Tomcat embebido y dependencias en `BOOT-INF/lib`).
* **Versión del Sistema:** `1.0.0-RC1`
* **Versión de Spring Boot:** `4.1.1` (Spring Framework `7.0.9`)
* **Java Target Bytecode:** `Java 17 LTS` (OpenJDK 64-Bit Server VM)
* **Tamaño del Archivo:** `108,678,500 bytes` (~103.6 MB)
* **Fecha y Hora de Compilación:** 30 de Octubre de 2026, 19:49:19 (Hora de Perú, America/Lima)

---

## 2. HUELLAS CRIPTOGRÁFICAS DE INTEGRIDAD

Para verificar que el archivo ejecutable no ha sido modificado, corrupto o adulterado durante su traslado o almacenamiento, se calcularon las siguientes firmas criptográficas:

### Algoritmo SHA-256 (Oficial):
```text
3F3A30CEC5FF7BB15E8ED72812222C09D9DD710350C95D3274A961E55EB922A6
```

### Comando de Verificación en Windows (PowerShell):
```powershell
Get-FileHash -Path "target\sigm-quinua-0.0.1-SNAPSHOT.jar" -Algorithm SHA256
```

### Comando de Verificación en Linux:
```bash
sha256sum target/sigm-quinua-0.0.1-SNAPSHOT.jar
```

---

## 3. COMPONENTES Y SERVICIOS INTEGRADOS EN EL ARTEFACTO

1. **Núcleo del Sistema y Mesa de Partes Virtual:** Gestión de expedientes, códigos correlativos `EXP-YYYY-XXXXXX`, derivaciones y constancias PDF con QR.
2. **Mercados Municipales:** Puestos, sectores, pabellones, comerciantes, contratos y recaudación de sisa diaria.
3. **Seguridad Ciudadana y Serenazgo:** Turnos, cuadrantes de patrullaje, incidencias ciudadanas, despacho de unidades móviles y partes de ocurrencia en PDF.
4. **Recaudación Tributaria Municipal:** Padrón de contribuyentes, impuesto predial, arbitrios municipales, convenios de fraccionamiento y caja municipal diaria con arqueo.

