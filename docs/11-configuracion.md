# 11. CONFIGURACIÓN DE ENTORNOS Y PROPIEDADES — SIGM-QUINUA

## 1. ARCHIVOS DE CONFIGURACIÓN DEL SISTEMA

SIGM-QUINUA utiliza la jerarquía de configuración estándar de Spring Boot:
* `src/main/resources/application.properties`: Configuración base compartida.
* `src/main/resources/application-dev.properties`: Configuración de desarrollo local con recarga de plantillas.
* `src/main/resources/application-prod.properties`: Configuración de producción endurecida (cache activo, logs en nivel WARN/ERROR).
* `src/test/resources/application-test.properties`: Configuración aislada para pruebas unitarias y de integración.

---

## 2. VARIABLES DE ENTORNO RECOMENDADAS

Para salvaguardar la seguridad institucional y evitar exponer contraseñas en archivos versionados, se recomienda configurar las siguientes variables de entorno en el servidor:

| Variable de Entorno | Valor Predeterminado | Descripción |
| :--- | :---: | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil de ejecución (`dev`, `prod`, `test`). |
| `SERVER_PORT` | `8080` | Puerto HTTP donde escuchará Tomcat embebido. |
| `DB_HOST` | `localhost` | Dirección IP o host del servidor PostgreSQL. |
| `DB_PORT` | `5433` | Puerto de escucha del motor PostgreSQL. |
| `DB_NAME` | `sigm_quinua` | Nombre de la base de datos relacional. |
| `DB_USERNAME` | `postgres` | Usuario con privilegios sobre la base de datos. |
| `DB_PASSWORD` | *(según entorno)* | Contraseña cifrada del usuario de base de datos. |
| `JWT_SECRET` | *(64 bytes hex)* | Clave criptográfica para firma de tokens JWT (HMAC-SHA256). |
| `STORAGE_LOCATION` | `storage/documentos` | Ruta en disco donde se guardan los archivos PDF adjuntos. |

---

## 3. PARÁMETROS CRÍTICOS EN `application.properties`

```properties
# Nombre de la aplicación
spring.application.name=SIGM-QUINUA

# Conexión a PostgreSQL 17
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:sigm_quinua}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:jkbnmt09}

# Configuración del Pool HikariCP
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000

# JPA y Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Migraciones Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# Límite de carga de documentos PDF (Expedientes y Sustentos)
spring.servlet.multipart.max-file-size=15MB
spring.servlet.multipart.max-request-size=20MB
```

