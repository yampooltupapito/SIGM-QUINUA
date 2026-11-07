# SIGM-QUINUA — Sistema Integral de Gestión Municipal
**Municipalidad Distrital de Quinua**  
*Provincia de Huamanga, Departamento de Ayacucho, Perú*

---

## 1. Descripción del Sistema

**SIGM-QUINUA** es la plataforma tecnológica empresarial concebida para modernizar, digitalizar y centralizar la administración pública de la Municipalidad Distrital de Quinua. Diseñado bajo un enfoque de **Monolito Modular**, el sistema integra en una única base de código y base de datos relacional la totalidad de los procesos administrativos, tributarios, documentales, asistenciales y de seguridad ciudadana del distrito.

El sistema garantiza trazabilidad de trámites, transparencia ante los ciudadanos, recaudación eficiente y simplificación administrativa conforme a los estándares de gobierno digital del Estado Peruano.

---

## 2. Tecnologías Utilizadas

- **Lenguaje:** Java 17 LTS.
- **Framework Principal:** Spring Boot 4.1.1 (Spring Framework 7.0.9).
- **Gestión de Dependencias:** Apache Maven 3.10.0 (vía Maven Wrapper `mvnw`).
- **Capa de Persistencia:** Spring Data JPA + Hibernate ORM 7.4.
- **Base de Datos:** PostgreSQL 17 (puerto 5433, base de datos `sigm_quinua`).
- **Migraciones de Esquema:** Flyway 12.4.
- **Seguridad Criptográfica:** Spring Security 7.1 + BCrypt + JJWT 0.12.6.
- **Motor de Vistas:** Thymeleaf + Thymeleaf Layout Dialect.
- **Estilos y Diseño:** Tailwind CSS 3.4 (compilado localmente, sin CDN).
- **Iconografía:** Lucide Icons (bundle JavaScript UMD local, sin dependencias externas).
- **Validaciones:** Jakarta Bean Validation 3.1 (Hibernate Validator 9.1).
- **Productividad:** Project Lombok 1.18.46.
- **Reportes:** Apache POI 5.4 (Excel) y LibrePDF OpenPDF 2.0.3 (PDF).
- **Pruebas Automatizadas:** JUnit Jupiter 6.0, Mockito 5.23, AssertJ 3.27, Testcontainers 1.20.

---

## 3. Requisitos de Ejecución

1. **Java Development Kit (JDK):** Versión 17 LTS instalada (`JAVA_HOME` configurado).
2. **Servidor PostgreSQL:** Versión 15 o superior (instalado y activo en el puerto 5433 o configurable por entorno).
3. **Node.js y npm (para desarrollo frontend):** Node.js v18+ y npm v9+ (utilizados para compilar Tailwind CSS).
4. **Memoria RAM mínima:** 2 GB recomendados para ejecución en desarrollo.

---

## 4. Arquitectura General del Sistema

El sistema implementa una arquitectura de **Monolito Modular** por responsabilidades de dominio, estructurado en capas bien definidas:

```
[ Petición HTTP ] 
       │
       ▼
[ SecurityFilterChain (CSRF, JWT / Sesión) ]
       │
       ▼
[ Controller (web / REST) ] ── (DTOs / ViewModels)
       │
       ▼
[ Service (Lógica de Negocio y Reglas Municipales) ]
       │
       ▼
[ Repository (Spring Data JPA) ]
       │
       ▼
[ PostgreSQL Database (sigm_quinua) ]
```

### Reglas de Diseño:
- **Flujo Estricto:** Controller → Service → Repository → Base de Datos.
- **Desacoplamiento:** Cada módulo municipal cuenta con su propio paquete aislado. Se prohíben referencias circulares entre módulos.
- **DTOs y Mappers:** Las entidades persistentes nunca se exponen directamente a las vistas ni a las APIs públicas.
- **Auditoría Automática:** Todas las tablas de negocio heredan de `AuditableEntity` para registrar fecha de creación, modificación y servidor municipal responsable.

---

## 5. Estructura de Paquetes

```text
pe.gob.municipalidad.sigm/
├── SigmQuinuaApplication.java         # Clase de arranque con @EnableJpaAuditing
│
├── config/                            # Configuraciones transversales del sistema
│   ├── LocaleConfig.java              # Configuración i18n (es_PE)
│   ├── TimeConfig.java                # Zona horaria oficial (America/Lima)
│   └── WebConfig.java                 # Ruteo web MVC y recursos estáticos
│
├── common/                            # Componentes y utilidades compartidas
│   ├── audit/                         # Superclase AuditableEntity y AuditorAware
│   ├── constants/                     # Constantes institucionales y de seguridad
│   ├── exception/                     # Excepciones de negocio y recursos no encontrados
│   ├── response/                      # Envoltorios estándar ApiResponse y ErrorResponse
│   ├── util/                          # Utilidades de fecha y formato peruano
│   └── validation/                    # Validadores personalizados (@Dni, @Ruc)
│
├── security/                          # Capa de seguridad institucional
│   ├── config/SecurityConfig.java     # Reglas de firewall web y beans de cifrado
│   ├── authentication/                # Componentes de autenticación
│   ├── authorization/                 # Control de accesos y roles (RBAC)
│   ├── filter/                        # Interceptores de tokens y sesiones
│   ├── jwt/                           # Utilidades de firma y claims JWT
│   └── service/                       # Servicios de usuario (UserDetailsService)
│
├── modules/                           # Los 10 Módulos de Gestión Municipal
│   ├── mesapartes/                    # Módulo 1: Mesa de Partes Virtual
│   ├── mercados/                      # Módulo 2: Mercados Municipales
│   ├── serenazgo/                     # Módulo 3: Seguridad Ciudadana
│   ├── tributacion/                   # Módulo 4: Recaudación Tributaria
│   ├── licencias/                     # Módulo 5: Licencias de Funcionamiento
│   ├── obras/                         # Módulo 6: Obras Públicas
│   ├── programassociales/             # Módulo 7: Programas Sociales
│   ├── patrimonio/                    # Módulo 8: Almacén y Patrimonio
│   ├── serviciospublicos/             # Módulo 9: Servicios Públicos
│   └── administracion/                # Módulo 10: Administración Integral
│
└── web/                               # Capa de presentación y controladores
    ├── advice/GlobalExceptionHandler  # Manejo centralizado y seguro de errores
    └── controller/                    # Controladores web (/ping, /verify)
```

---

## 6. Los 10 Módulos Municipales

1. **Mesa de Partes Virtual:** Recepción de solicitudes ciudadanas, generación de expedientes y código único de trámite, derivación a gerencias/subgerencias y seguimiento en línea.
2. **Mercados Municipales:** Padrón de comerciantes del mercado de Quinua, arriendo de puestos, inspecciones sanitarias y recaudación de merced conductiva.
3. **Seguridad Ciudadana y Serenazgo:** Registro geolocalizado de incidencias, cuadrantes y turnos de patrullaje, coordinación comunitaria y respuesta a emergencias.
4. **Recaudación Tributaria:** Padrón de contribuyentes del distrito, liquidación de impuesto predial y arbitrios municipales, estados de cuenta, control de morosidad y caja.
5. **Licencias de Funcionamiento:** Trámite para establecimientos comerciales y artesanales de Quinua, inspecciones de Defensa Civil (ITSE) y emisión de resoluciones.
6. **Obras Públicas:** Banco de inversiones y fichas técnicas, control de ejecución presupuestal y física (curva S), valorizaciones y supervisión de infraestructura.
7. **Programas Sociales:** Gestión del Programa Vaso de Leche (PVL), Comedores Populares, OMAPED y focalización de beneficiarios en situación vulnerable.
8. **Almacén y Patrimonio:** Inventario de bienes muebles e inmuebles institucionales, generación de notas de entrada/salida (PECOSA) y asignación a funcionarios.
9. **Servicios Públicos:** Rutas de recolección de residuos sólidos, limpieza pública, mantenimiento de áreas verdes y parques distritales.
10. **Administración Integral:** Gestión centralizada de servidores municipales, estructura orgánica (oficinas y áreas), roles, permisos dinámicos y logs de auditoría.

---

## 7. Perfiles de Ejecución y Variables de Entorno

El proyecto soporta tres perfiles preconfigurados:
- `dev`: Desarrollo local activo por defecto.
- `test`: Suite de pruebas unitarias y de integración.
- `prod`: Despliegue en servidor institucional con directivas de alta seguridad.

### Variables de Entorno Disponibles:

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Perfil de configuración activo | `dev` |
| `SERVER_PORT` | Puerto de escucha HTTP | `8080` |
| `DB_HOST` | Host del servidor PostgreSQL | `localhost` |
| `DB_PORT` | Puerto del servidor PostgreSQL | `5433` |
| `DB_NAME` | Nombre de la base de datos | `sigm_quinua` |
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | Configurable / `jkbnmt09` |
| `JPA_DDL_AUTO` | Estrategia DDL de Hibernate | `validate` (en producción) |
| `FLYWAY_ENABLED` | Habilitación de migraciones SQL | `true` |
| `COOKIE_SECURE` | Flag HTTPS para cookies | `false` en dev, `true` en prod |

---

## 8. Instrucciones de Compilación y Ejecución

### Compilar y Validar el Proyecto:
```powershell
.\mvnw.cmd clean compile
```

### Ejecutar las Pruebas Automatizadas:
```powershell
.\mvnw.cmd test
```

### Generar el Paquete de Distribución (JAR):
```powershell
.\mvnw.cmd package
```
El archivo ejecutable se generará en: `target/sigm-quinua-0.0.1-SNAPSHOT.jar`.

### Compilar Estilos Frontend (Tailwind CSS):
```powershell
npm run build:css
```
O para desarrollo con recarga continua de estilos:
```powershell
npm run watch:css
```

### Iniciar la Aplicación en Modo Desarrollo:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```
O ejecutando directamente el JAR empaquetado:
```powershell
java -jar target/sigm-quinua-0.0.1-SNAPSHOT.jar
```

---

## 9. Endpoints de Verificación Técnica

Una vez iniciada la aplicación, verificar la operatividad mediante:

1. **Health Check (HTTP Plain Text):**
   - URL: `http://localhost:8080/ping`
   - Respuesta esperada: `SIGM-QUINUA OK`
2. **Página de Verificación Thymeleaf:**
   - URL: `http://localhost:8080/verify` (o acceso directo a `http://localhost:8080/`)
   - Muestra el dashboard técnico con el estado de la arquitectura, paleta institucional oscura, Lucide Icons y el resumen de los 10 módulos preparados.
3. **Métricas Actuator:**
   - URL: `http://localhost:8080/actuator/health`

---

## 10. Núcleo Común y Validaciones Profesionales (Prompt 2)

El paquete `pe.gob.municipalidad.sigm.common` proporciona la infraestructura transversal compartida por los 10 módulos municipales:

```text
pe.gob.municipalidad.sigm.common/
├── response/                          # Respuestas estandarizadas
│   ├── ApiResponse.java               # Envoltorio genérico JSON para éxitos (ok, created, noContent, paginated)
│   └── ErrorResponse.java             # Envoltorio canónico para errores con mapa de campos y traceId
│
├── exception/                         # Excepciones de negocio y HTTP
│   ├── BusinessException.java         # Violación de regla de negocio municipal (HTTP 422)
│   ├── ResourceNotFoundException.java # Entidad o registro inexistente (HTTP 404)
│   ├── DuplicateResourceException.java# Conflicto por recurso duplicado (HTTP 409)
│   ├── ConflictException.java         # Conflicto con estado actual de la entidad (HTTP 409)
│   ├── InvalidRequestException.java   # Solicitud de negocio inválida o parámetros ilegales (HTTP 400)
│   └── UnauthorizedException.java     # Credenciales inválidas o sesión expirada (HTTP 401)
│
├── validation/                        # Validaciones profesionales Bean Validation
│   ├── Dni.java / DniValidator.java   # Validación de DNI peruano (8 dígitos numéricos exactos)
│   ├── Ruc.java / RucValidator.java   # Validación de RUC peruano (11 dígitos + módulo 11 SUNAT)
│   ├── ValidEmail.java / EmailValidator.java # Validación sintáctica RFC 5322 (longitud máx 254)
│   └── PositiveId.java / PositiveIdValidator.java # Validación de identificador numérico > 0
│
├── pagination/                        # Paginación y ordenamiento seguro
│   ├── PaginationRequest.java         # DTO con page, size (1..100), sort y validación de campos permitidos
│   └── PagedResponse.java             # Metadatos seguros (totalElements, totalPages, first, last, hasNext)
│
├── audit/                             # Trazabilidad de entidades
│   ├── AuditableEntity.java           # MappedSuperclass con createdAt, updatedAt, createdBy, updatedBy
│   └── AuditAwareImpl.java            # Integración con SecurityContextHolder (sin usuarios ficticios)
│
├── util/                              # Utilidades compartidas
│   ├── TraceContext.java              # Manejo de traceId mediante SLF4J MDC
│   ├── TraceIdFilter.java             # Filtro HTTP (Ordered.HIGHEST_PRECEDENCE) con cabecera X-Trace-Id y limpieza en finally
│   ├── DateUtils.java                 # Manejo estricto de fechas en America/Lima con Clock inyectable
│   └── StringUtils.java               # Sanitización y enmascaramiento seguro (DNI, RUC, Correo)
│
└── constants/                         # Constantes canónicas
    ├── ErrorCodes.java                # Códigos estables de error (ERR_RESOURCE_NOT_FOUND, etc.)
    ├── PaginationConstants.java       # Límites de paginación (defecto 10, máximo 100)
    └── AppConstants.java              # Constantes de zona horaria y formatos de fecha
```

### Respuestas Estandarizadas JSON

Todas las respuestas exitosas de la API REST siguen el formato:
```json
{
  "success": true,
  "message": "Operación realizada correctamente",
  "data": { ... },
  "timestamp": "2026-10-14T22:30:00-05:00",
  "traceId": "d3b07384-d113-4f9e-bc47-0985a9efd9ef"
}
```

Para respuestas `204 No Content`, se emite `ApiResponse.noContent()` sin cuerpo.

### Manejo Global de Excepciones

`GlobalExceptionHandler` captura excepciones REST y peticiones web Thymeleaf:
```json
{
  "success": false,
  "message": "Los datos enviados no son válidos",
  "errorCode": "ERR_VALIDATION",
  "status": 400,
  "path": "/api/v1/tramite",
  "errors": {
    "dni": "El DNI debe tener exactamente 8 dígitos numéricos válidos.",
    "email": "El correo electrónico ingresado no tiene un formato válido."
  },
  "timestamp": "2026-10-14T22:30:00-05:00",
  "traceId": "d3b07384-d113-4f9e-bc47-0985a9efd9ef"
}
```
*Garantía de Seguridad:* Las excepciones internas nunca exponen stack traces, rutas internas, credenciales o sentencias SQL.

---

## 11. Base de Datos PostgreSQL y Migraciones Flyway (Prompt 3)

### Diagrama Entidad-Relación Institucional (Mermaid)

```mermaid
erDiagram
    MUNICIPALIDADES ||--o{ OFICINAS : "posee"
    OFICINAS ||--o{ OFICINAS : "contiene (jerarquia)"
    OFICINAS ||--o{ PERSONA_OFICINAS : "asigna"
    CARGOS ||--o{ PERSONA_OFICINAS : "asigna"
    TIPOS_ACTORES ||--o{ PERSONA_OFICINAS : "clasifica"
    PERSONAS ||--o{ PERSONA_OFICINAS : "desempena"
    PERSONAS ||--|| USUARIOS : "autentica"
    USUARIOS ||--o{ USUARIO_ROLES : "tiene"
    ROLES ||--o{ USUARIO_ROLES : "asignado a"
    ROLES ||--o{ ROL_PERMISOS : "contiene"
    PERMISOS ||--o{ ROL_PERMISOS : "pertenece a"
    MODULOS_SISTEMA ||--o{ PERMISOS : "agrupa"

    MUNICIPALIDADES {
        bigint id PK
        varchar nombre
        varchar nombre_corto
        varchar ubigeo
        varchar departamento
        varchar provincia
        varchar distrito
        varchar estado
    }

    OFICINAS {
        bigint id PK
        bigint municipalidad_id FK
        bigint oficina_padre_id FK
        varchar codigo UK
        varchar nombre
        varchar sigla
        int nivel_jerarquico
        varchar estado
    }

    CARGOS {
        bigint id PK
        varchar codigo UK
        varchar nombre
        int nivel_jerarquico
        varchar estado
    }

    TIPOS_ACTORES {
        bigint id PK
        varchar codigo UK
        varchar nombre
        varchar estado
    }

    PERSONAS {
        bigint id PK
        varchar tipo_documento
        varchar numero_documento
        varchar nombres
        varchar apellido_paterno
        varchar apellido_materno
        varchar correo
        varchar telefono
        varchar estado
    }

    USUARIOS {
        bigint id PK
        bigint persona_id FK,UK
        varchar username UK
        varchar password_hash
        boolean enabled
        boolean account_locked
        int failed_attempts
    }

    PERSONA_OFICINAS {
        bigint id PK
        bigint persona_id FK
        bigint oficina_id FK
        bigint cargo_id FK
        bigint tipo_actor_id FK
        date fecha_inicio
        date fecha_fin
        boolean principal
        varchar estado
    }

    MODULOS_SISTEMA {
        bigint id PK
        varchar codigo UK
        varchar nombre
        varchar icono
        int orden
        varchar estado
    }

    PERMISOS {
        bigint id PK
        bigint modulo_id FK
        varchar codigo UK
        varchar nombre
        varchar accion
        varchar estado
    }

    ROLES {
        bigint id PK
        varchar codigo UK
        varchar nombre
        varchar estado
    }
```

### Migraciones Versionadas Flyway

Las migraciones se encuentran en `src/main/resources/db/migration/`:

1. `V1__estructura_institucional.sql`: Creación de `municipalidades`, `oficinas` (jerarquía sin ciclos) y `cargos`.
2. `V2__personas_usuarios.sql`: Creación de `personas` con documento unívoco (`tipo_documento`, `numero_documento`) y `usuarios` (1:1 con personas).
3. `V3__actores.sql`: Creación de `tipos_actores`, `persona_oficinas` y catálogo inicial de tipos de actores.
4. `V4__roles_permisos.sql`: Creación de `modulos_sistema`, `permisos`, `roles`, `usuario_roles`, `rol_permisos` y siembra de los 10 módulos municipales oficiales.
5. `V5__indices_restricciones.sql`: Índices para optimización de consultas B-tree y restricción CHECK de no autociclo en oficinas (`oficina_padre_id <> id`).
6. `V6__seguridad_tokens_auditoria.sql`: Tablas `refresh_tokens` e `auditoria_seguridad` con hashes SHA-256, llaves foráneas e índices de revocación por familia.

### Variables de Entorno de Base de Datos

| Variable | Descripción | Valor Predeterminado Local |
| :--- | :--- | :--- |
| `DB_HOST` | Host del servidor PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de conexión | `5433` (personalizado) o `5432` |
| `DB_NAME` | Nombre de la base de datos | `sigm_quinua` |
| `DB_USERNAME` | Usuario de base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña | Configurable vía entorno (no en código) |
| `JPA_DDL_AUTO` | Estrategia DDL de Hibernate | `validate` |
| `FLYWAY_ENABLED` | Habilitación de Flyway | `true` |
| `FLYWAY_BASELINE_ON_MIGRATE` | Baseline automático | `false` |

---

## 11. Seguridad, Autenticación, JWT y Control de Acceso (Prompt 4)

### Arquitectura de Seguridad
La capa de seguridad está modularizada bajo `pe.gob.municipalidad.sigm.security` con separación estricta de responsabilidades:
- **`config/`**: `SecurityConfig` (Spring Security 7 stateless, filter chain, CSRF selectivo para tokens).
- **`authentication/`**: `AuthController`, `UserDetailsServiceImpl`, `CustomUserDetails`.
- **`authorization/`**: `OfficeSecurityService` (`@officeSecurity` para aislamiento por unidad orgánica).
- **`jwt/`**: `JwtTokenProvider` (JJWT 0.12.6, HMAC-SHA256, claims seguros).
- **`filter/`**: `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint` (401), `JwtAccessDeniedHandler` (403).
- **`service/`**: `AuthService`, `RefreshTokenService`, `PasswordService`, `SecurityAuditService`.
- **`dto/`**: DTOs de login, refresh, perfil de usuario y respuestas seguras.
- **`model/` & `repository/`**: Entidades y repositorios JPA para `RefreshToken` y `AuditoriaSeguridad`.

### Flujo de Autenticación
1. **Petición de Login (`POST /api/auth/login`):** Recibe `username` y `password` en JSON.
2. **Validación BCrypt (costo 12):** Spring Security valida las credenciales contra el hash en PostgreSQL.
3. **Control de Intentos Fallidos:** Tras 5 intentos fallidos consecutivos, la cuenta se bloquea preventivamente (`account_locked = true`) y se registra alerta de seguridad.
4. **Emisión Híbrida de Credenciales:**
   - Devuelve `accessToken` (15 min) y `refreshToken` rotativo en el payload JSON.
   - Adjunta cookies `HttpOnly`, `SameSite=Lax` y `Secure` (`SIGM_AUTH_TOKEN`, `SIGM_REFRESH_TOKEN`) para consumo transparente desde Thymeleaf y frontend.
5. **Auditoría:** Registro automático de `LOGIN_SUCCESS` o `LOGIN_FAILED` con IP, User-Agent y `traceId`.

### Rotación de Refresh Tokens y Detección de Reutilización Maliciosa
- **Almacenamiento Seguro:** En base de datos nunca se guarda el texto plano del refresh token, sino su hash criptográfico **SHA-256**.
- **Rotación en Cada Uso:** Cada llamada a `/api/auth/refresh` revoca el token actual y emite uno nuevo en la misma familia (`familyId`).
- **Detección de Reutilización:** Si un token ya revocado vuelve a ser presentado, el sistema detecta un posible robo de sesión, invalida inmediatamente **toda la familia de tokens** del usuario en base de datos (`revokeFamily`), registra un evento de alerta en auditoría y rechaza la petición.

### Control de Acceso por Oficina y Permisos Dinámicos
- **Permisos Dinámicos:** Mapeo de `ROLE_*` institucionales y códigos de acción (`MESAPARTES_VIEW`, `ADMINISTRACION_MANAGE`, etc.) como `GrantedAuthority`.
- **Aislamiento por Oficina (`@officeSecurity`):** Los trabajadores solo pueden acceder a registros de su unidad orgánica asignada activa (`canAccessResourceOfOffice(#oficinaId)`). Los administradores (`ROLE_ADMIN` o `ADMINISTRACION_MANAGE`) cuentan con acceso transversal autorizado.

### Variables de Entorno de Seguridad

| Variable | Descripción | Valor Predeterminado Local |
| :--- | :--- | :--- |
| `JWT_SECRET` | Clave secreta criptográfica HMAC-SHA256 (mínimo 256 bits) | Clave de desarrollo generada en properties |
| `JWT_EXPIRATION_MS` | Duración del Access Token | `900000` (15 minutos) |
| `JWT_REFRESH_EXPIRATION_MS` | Duración del Refresh Token | `604800000` (7 días) |
| `JWT_ISSUER` | Emisor institucional del token | `SIGM-QUINUA` |
| `SECURITY_MAX_FAILED_ATTEMPTS` | Intentos fallidos antes de bloqueo preventivo | `5` |
| `COOKIE_SECURE` | Flag Secure para cookies (habilitar en producción HTTPS) | `false` en dev, `true` en prod |

### Ejemplos de Solicitudes y Respuestas (Postman / cURL)

#### 1. Iniciar Sesión (`POST /api/auth/login`)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "operador.mesa",
    "password": "PasswordSeguro123!"
  }'
```
**Respuesta Exitosa (HTTP 200):**
```json
{
  "success": true,
  "message": "Autenticación realizada exitosamente.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "4a7b9c1d-8e2f-...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "username": "operador.mesa",
    "nombreCompleto": "Carlos Quispe Mendoza",
    "roles": ["ROLE_OPERADOR"],
    "permisos": ["MESAPARTES_VIEW", "MESAPARTES_CREATE"]
  },
  "timestamp": "2026-10-15T04:45:00-05:00",
  "traceId": "trace-auth-login-01"
}
```

#### 2. Consultar Perfil de Usuario (`GET /api/auth/me`)
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

#### 3. Renovar Sesión (`POST /api/auth/refresh`)
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "4a7b9c1d-8e2f-..."
  }'
```

#### 4. Cerrar Sesión (`POST /api/auth/logout`)
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "4a7b9c1d-8e2f-..."
  }'
```

---

## 13. Módulo de Administración Dinámica (Prompt 5)

El paquete `pe.gob.municipalidad.sigm.modules.administracion` implementa el subsistema integral de identidades, accesos, parametrización orgánica y auditoría institucional de SIGM-QUINUA.

### Componentes Administrables:
1. **Usuarios y Cuentas:**
   - Registro con DNI validado, BCrypt y correo institucional/personal.
   - Activación, desactivación y bloqueo preventivo de cuentas.
   - Prevención de auto-desactivación y auto-bloqueo para administradores.
   - Restablecimiento administrativo seguro de contraseñas con invalidación inmediata de tokens.
2. **Roles Dinámicos:**
   - Creación y edición de roles institucionales en base de datos PostgreSQL.
   - Protección de roles del sistema (`ROLE_SUPERADMIN`, `ROLE_ADMIN`).
   - Prevención de auto-revocación de privilegios de administración.
3. **Matriz Visual de Permisos Granulares:**
   - Permisos organizados matricialmente por Módulos Institucionales (10) y Tipos de Acción (`VIEW`, `CREATE`, `UPDATE`, `DELETE`, `APPROVE`, `ASSIGN`, `EXPORT`, `MANAGE`).
   - Asignación dinámica en bloque con interfaz de checkboxes y revocación inmediata de sesiones activas (`RefreshTokenService.revokeAllUserTokens`).
4. **Estructura Orgánica y Oficinas:**
   - Catálogo jerárquico de unidades orgánicas municipales.
   - Algoritmo de detección exhaustiva y prevención de ciclos jerárquicos cerrados.
   - Validación de dependencias de personal antes de permitir la desactivación de oficinas.
   - Visualización del organigrama en vista árbol (`OficinaTreeDto`).
5. **Cargos Municipales:**
   - Catálogo de puestos institucionales con niveles de responsabilidad y jerarquía.
6. **Tipos de Actores:**
   - Clasificación extensible de personas que interactúan con el municipio (Ciudadano, Servidor, Comerciante, Sereno, Fiscalizador, etc.).
7. **Pistas de Auditoría Administrativa:**
   - Registro transaccional independiente (`REQUIRES_NEW`) de toda operación administrativa.
   - Trazabilidad con `traceId`, IP de origen, User-Agent, usuario responsable y resultado.
   - Filtros avanzados implementados con `JpaSpecificationExecutor`.

### Aprovisionamiento Seguro del Primer Superadministrador:
- Operación restringida fuera de endpoints públicos mediante `SuperAdminProvisioningService`.
- Verificación de clave secreta (`sigm.security.provisioning-secret`).
- Control de unicidad que impide duplicar el rol `ROLE_SUPERADMIN`.

### Endpoints REST Administrativos (`/api/admin/**`):
- `GET /api/admin/usuarios` | `POST /api/admin/usuarios` | `PUT /api/admin/usuarios/{id}`
- `PATCH /api/admin/usuarios/{id}/activar` | `PATCH /api/admin/usuarios/{id}/desactivar`
- `PATCH /api/admin/usuarios/{id}/bloquear` | `PATCH /api/admin/usuarios/{id}/desbloquear`
- `POST /api/admin/usuarios/{id}/reset-password` | `PUT /api/admin/usuarios/{id}/roles`
- `GET /api/admin/roles` | `POST /api/admin/roles` | `PUT /api/admin/roles/{id}`
- `PUT /api/admin/roles/{id}/permisos` (actualización de matriz de permisos)
- `GET /api/admin/permisos` | `GET /api/admin/permisos/matriz`
- `GET /api/admin/oficinas` | `GET /api/admin/oficinas/arbol` | `POST /api/admin/oficinas`
- `POST /api/admin/oficinas/asignar-trabajador`
- `GET /api/admin/cargos` | `POST /api/admin/cargos`
- `GET /api/admin/actores` | `POST /api/admin/actores`
- `GET /api/admin/auditoria`

### Vistas Web Administrativas (`/admin/**`):
- `/admin/usuarios` — Gestión integral de usuarios y modales funcionales.
- `/admin/roles` — Gestión de roles dinámicos y estados.
- `/admin/permisos` — Matriz visual de permisos con selección rápida por módulo.
- `/admin/oficinas` — Listado general y organigrama institucional en árbol.
- `/admin/cargos` — Catálogo y asignación de cargos.
- `/admin/actores` — Catálogo de tipos de actores.
- `/admin/auditoria` — Trazabilidad inmutable de cambios administrativos.

---

## 14. Login Institucional Profesional, Autenticación Real y Recuperación de Acceso (Prompt 6)

### 1. Interfaz de Inicio de Sesión Institucional (`/login`)
- **Diseño Visual de Calidad Gubernamental:**
  - Fondo: `#101827`
  - Panel oscuro: `#1e293b`
  - Tarjetas: `#202c3d`
  - Azul institucional: `#1d4ed8`
  - Texto principal: `#e2e8f0`
  - Texto secundario: `#94a3b8`
  - Bordes: `#334155`
- **Composición Desktop:** Dos columnas equilibradas. Columna izquierda con identidad de la Municipalidad Distrital de Quinua, reseña histórica de la Cuna de la Libertad Americana y marcador visual provisional institucional del escudo oficial. Columna derecha con formulario de acceso.
- **Composición Móvil:** Columna única vertical, fluida, adaptada a pantallas táctiles y sin desplazamiento horizontal.
- **Formulario y Accesibilidad:**
  - Atributos estándar `autocomplete="username"` y `autocomplete="current-password"` compatibles con gestores de contraseñas.
  - Botón interactivo para alternar visibilidad de contraseña con iconos Lucide (`eye` y `eye-off`), sin emojis.
  - Botón de submit con spinner animado ("Autenticando...") y bloqueo anti-doble envío.
  - Alertas accesibles (`role="alert"`, `aria-live`) para credenciales inválidas, bloqueos y confirmaciones de logout o reset.

### 2. Flujo de Autenticación Real y Redirección Inteligente
1. El usuario envía credenciales mediante `POST /login` (con protección CSRF activa).
2. Spring Security autentica contra PostgreSQL usando `BCryptPasswordEncoder(12)`.
3. Se verifican estados `enabled` y `account_locked`.
4. Si las credenciales son válidas:
   - Se emite el token de acceso JWT y refresh token en cookies `HttpOnly`, `SameSite=Lax` y `Secure` (`SIGM_AUTH_TOKEN`, `SIGM_REFRESH_TOKEN`).
   - Se restablece el contador de intentos fallidos.
   - Se determina el destino autorizado:
     - Administradores (`ROLE_ADMIN`, `ROLE_SUPERADMIN`, `ADMINISTRACION_MANAGE`): Redirección inmediata a `/admin/usuarios`.
     - Servidores u operadores municipales: Redirección a `/bienvenida` (portal institucional autenticado).
5. **Protección Anti-Open Redirect:** Si existe un parámetro `?redirect=...`, el sistema valida rigurosamente que sea una ruta relativa local que inicie con `/` (descartando `//`, esquemas externos `http:`/`https:` o secuencias de escape).

### 3. Recuperación Segura de Contraseña (`/forgot-password` y `/reset-password`)
- **Prevención de Enumeración de Usuarios:** Al ingresar un usuario o correo en `/forgot-password`, el sistema responde siempre con un mensaje genérico idéntico: *"Si los datos ingresados corresponden a una cuenta activa registrada, se ha enviado un enlace seguro de recuperación."*
- **Huella Criptográfica SHA-256:** En PostgreSQL nunca se almacena el token en texto claro, únicamente su hash SHA-256 (`token_hash` en tabla `password_reset_tokens`).
- **Un Solo Uso y Expiración:** Cada enlace tiene una vigencia máxima de 30 minutos y queda invalidado inmediatamente tras su primer uso (`used = true`).
- **Invalidación en Cascada:** Al completar el cambio de contraseña en `/reset-password`:
  1. Se actualiza el hash de contraseña en PostgreSQL mediante BCrypt.
  2. Si la cuenta estaba bloqueada por intentos fallidos, se desbloquea automáticamente.
  3. **Se revocan de forma inmediata todas las sesiones activas del usuario** (`RefreshTokenService.revokeAllUserTokens`) forzando reautenticación en todos sus dispositivos.
  4. Se audita el evento `PASSWORD_RESET_SUCCESS`.

### 4. Cierre de Sesión Seguro (`/logout`)
- Soporta peticiones `GET /logout` y `POST /logout` (con CSRF).
- Invalida los refresh tokens en base de datos.
- Sobrescribe las cookies `SIGM_AUTH_TOKEN` y `SIGM_REFRESH_TOKEN` con `Max-Age=0`.
- Limpia el `SecurityContextHolder`.
- Redirige al login con notificación de cierre: `/login?logout=true`.

---

## 15. Dashboard Administrativo Profesional, Estadísticas Reales y Navegación Dinámica (Prompt 7)

### 1. Arquitectura y Componentes
El paquete `pe.gob.municipalidad.sigm.modules.administracion.dashboard` implementa el centro de comando institucional de SIGM-QUINUA, conectando métricas operativas reales desde PostgreSQL con visualizaciones de alto nivel para la toma de decisiones municipales.

- **Controlador Web (`DashboardWebController`):** Sirve `/admin/dashboard` y `/dashboard` inyectando métricas agregadas, datos de gráficos, actividad reciente y datos de sesión institucional en horario oficial de Perú (`America/Lima`).
- **Controlador REST (`DashboardApiController`):** Endpoints JSON `/api/dashboard/stats`, `/api/dashboard/charts` y `/api/dashboard/actividad` para actualizaciones asíncronas y filtros dinámicos.
- **Servicio Central (`DashboardService` / `DashboardServiceImpl`):** Consolida consultas de conteo, distribuciones y agrupaciones evitando sobrecarga de memoria o problemas N+1.
- **Controlador Global de Atributos (`AdminLayoutControllerAdvice`):** Inyecta automáticamente el perfil del usuario autenticado, iniciales y bandeja de notificaciones en todas las vistas de administración.

### 2. Sidebar Dinámico y Estructura de los 10 Módulos Municipales
- **Colapsable y Adaptable:** Alternancia fluida entre modo expandido y contraído (desktop) y cajón fuera de pantalla (*off-canvas*) con telón de fondo táctil (*backdrop*) en dispositivos móviles y tabletas.
- **Mapeo de los 10 Módulos Oficiales de SIGM-QUINUA:**
  1. *Mesa de Partes Virtual* (`file-text`) — [Próximamente]
  2. *Recaudación Tributaria* (`dollar-sign`) — [Próximamente]
  3. *Catastro Urbano y Desarrollo* (`map-pin`) — [Próximamente]
  4. *Registro Civil y Cementerio* (`book-open`) — [Próximamente]
  5. *Seguridad Ciudadana y Serenazgo* (`shield`) — [Próximamente]
  6. *Servicios Públicos y Medio Ambiente* (`trash-2`) — [Próximamente]
  7. *Desarrollo Social y Programas Asistenciales* (`heart`) — [Próximamente]
  8. *Mercados Municipales y Turismo* (`shopping-bag`) — [Próximamente]
  9. *Fiscalización y Control Municipal* (`clipboard-check`) — [Próximamente]
  10. *Administración Integral y Seguridad* (`settings`) — **[Operativo]** con accesos directos a Usuarios, Roles, Permisos, Oficinas, Cargos, Actores y Auditoría.
- **Identidad Institucional:** Emblema heráldico provisional oficial de Quinua y subtítulo institucional (*Provincia de Huamanga, Ayacucho*).

### 3. Topbar Institucional y Navegación
- **Buscador Contextual:** Entrada de búsqueda con atajo de teclado accesible (`Alt + /`).
- **Bandeja de Notificaciones Desplegable:**
  - Contador numérico dinámico de notificaciones no leídas con badge de color destacado.
  - Menú interactivo con listado cronológico de eventos del sistema.
  - Acciones para marcar notificaciones individuales o marcar todas como leídas en bloque vía AJAX.
  - Estados vacíos con iconografía Lucide.
- **Perfil de Usuario:**
  - Iniciales generadas dinámicamente y etiqueta de cargo y dependencia.
  - Menú desplegable con acceso a "Mi Perfil Institucional" (modal emergente), "Cambiar Contraseña", "Registro de Auditoría" y "Cerrar Sesión".

### 4. Indicadores Reales y Gráficos Interactivos (Chart.js Local)
- **Tarjetas Estadísticas Reales (PostgreSQL):**
  - Total de Usuarios Activos (`usuarios.enabled = true`)
  - Total de Oficinas y Dependencias (`oficinas.estado = 'ACTIVO'`)
  - Total de Roles Dinámicos (`roles.estado = 'ACTIVO'`)
  - Padrón de Personas Registradas (`personas`)
  - Tipos de Actores Municipales (`tipos_actores.estado = 'ACTIVO'`)
  - Cuentas Bloqueadas Preventivamente (`usuarios.account_locked = true`)
  - Indicadores de módulos en desarrollo claramente identificados con *"Módulo pendiente"* o *"Sin datos disponibles"*, sin recurrir a datos ficticios.
- **Gráficos Interactivos:**
  - Gráfico 1: *Estado de Cuentas de Acceso* (Doughnut chart: Activos vs Inactivos vs Bloqueados).
  - Gráfico 2: *Personal Asignado por Oficina* (Bar chart: Cantidad de servidores por unidad orgánica).
  - Gráfico 3: *Distribución de Usuarios por Rol* (Bar chart: Perfiles de seguridad asignados).
  - Gráfico 4: *Operaciones Administrativas Registradas* (Bar chart: Frecuencia de acciones en bitácora).

### 5. Bitácora de Actividad Reciente y Filtros
- Conectada a la tabla `auditoria_administrativa`.
- Muestra fecha/hora en Perú, usuario responsable, acción, entidad y badges de resultado (`Éxito`, `Alerta`, `Fallo`).
- Filtros interactivos por período temporal (*Todos*, *Hoy*, *Últimos 7 días*, *Últimos 30 días*, *Mes actual*) y por estado de resultado.
- Paginación integrada en servidor mediante Spring Data.

### 6. Sistema de Notificaciones (`V10__notificaciones.sql`)
- Entidad `Notificacion` con claves foráneas hacia `usuarios`, índices por `usuario_id`, `leida` y `fecha_creacion DESC`.
- Endpoints REST `/api/notificaciones`, `/api/notificaciones/{id}/leer`, `/api/notificaciones/marcar-todas`.
- Protección estricta: Cada usuario únicamente puede consultar o modificar sus propias notificaciones.

---

## 16. Portal Ciudadano Institucional y Acceso Público (Prompt 8)

El módulo `pe.gob.municipalidad.sigm.modules.portal` implementa el portal web institucional público y accesible de la Municipalidad Distrital de Quinua, diferenciado visualmente con una paleta moderna, limpia y clara (fondos blancos y grises tenues con acentos `#1d4ed8`), optimizada según los estándares WCAG 2.2 nivel AA.

```text
pe.gob.municipalidad.sigm.modules.portal/
├── controller/
│   ├── PortalWebController.java             # Vistas públicas (GET /, /servicios, /nosotros, etc.)
│   ├── PortalPublicRestController.java      # APIs públicas (/api/portal/faqs/search, /api/portal/noticias/destacadas)
│   ├── PortalAdminWebController.java        # Vistas de gestión (/admin/portal/*) con permiso PORTAL_MANAGE
│   └── PortalAdminRestController.java       # APIs REST administrativas para CRUD de contenidos
│
├── service/
│   ├── PortalContenidoService.java          # Gestión de noticias, comunicados, FAQs y configuración
│   ├── PortalContactoService.java           # Recepción, honeypot, tasa límite y atención de consultas
│   ├── PortalServicioService.java           # Catálogo de 10 servicios con disponibilidad real
│   └── CiudadanoRegistroService.java        # Alta pública segura con BCrypt y asignación estricta de ROLE_CIUDADANO
│
├── entity/
│   ├── PortalNoticia.java                   # Tabla: portal_noticias
│   ├── PortalFaq.java                       # Tabla: portal_faq
│   ├── PortalContacto.java                  # Tabla: portal_contactos
│   ├── PortalServicio.java                  # Tabla: portal_servicios
│   └── PortalConfiguracion.java             # Tabla: portal_configuracion
│
├── repository/
│   ├── PortalNoticiaRepository.java
│   ├── PortalFaqRepository.java
│   ├── PortalContactoRepository.java
│   ├── PortalServicioRepository.java
│   └── PortalConfiguracionRepository.java
│
└── dto/
    ├── PortalNoticiaDtos.java
    ├── PortalFaqDtos.java
    ├── PortalContactoDtos.java
    ├── PortalServicioDtos.java
    └── CiudadanoRegistroDto.java
```

### 1. Migración de Base de Datos (`V11__portal_ciudadano.sql`)
- **`portal_noticias`:** Publicaciones institucionales con slugs únicos, categoría, estado (`BORRADOR`, `PUBLICADO`, `ARCHIVADO`), visitas y fecha de publicación.
- **`portal_faq`:** Preguntas frecuentes organizadas por categoría, orden y estado activo.
- **`portal_contactos`:** Mensajes ciudadanos protegidos con código único canónico `CNS-YYYY-XXXXX`, estado de atención y trazabilidad.
- **`portal_servicios`:** Catálogo de 10 servicios municipales con estado de disponibilidad real (`DISPONIBLE`, `PROXIMAMENTE`, `EN_DESARROLLO`).
- **`portal_configuracion`:** Parámetros institucionales (horarios, teléfono, correo oficial, portal de transparencia).
- **Seguridad RBAC:** Inserción del permiso `PORTAL_MANAGE` asignado a `ROLE_ADMIN` y `ROLE_SUPERADMIN`.

### 2. Rutas Públicas Implementadas
- `GET /`: Portada principal con carrusel de servicios, accesos rápidos a Mesa de Partes, consulta de expedientes, noticias destacadas y FAQs.
- `GET /servicios`: Catálogo interactivo de los 10 servicios municipales identificando claramente su disponibilidad real (sin enlaces engañosos).
- `GET /nosotros`: Historia institucional de Quinua, Misión, Visión, reseña cívica y directorio dinámico de unidades orgánicas.
- `GET /mesa-partes`: Guía informativa oficial del procedimiento administrativo según TUO Ley N° 27444, requisitos y estado de digitalización.
- `GET /tramites/consulta`: Interfaz de consulta de expedientes con advertencia de seguridad y protección de datos personales.
- `GET /noticias` y `GET /noticias/{slug}`: Portal de noticias con buscador, filtros por categoría/tipo, paginación y sanitización XSS.
- `GET /faq`: Acordeón interactivo con categorías dinámicas y filtro de búsqueda instantánea.
- `GET /contacto` y `POST /contacto`: Formulario seguro con honeypot antispam, tasa límite por IP (máx. 5/hora), validación de correo RFC 5322 y código de trazabilidad único.
- `GET /registro` y `POST /registro`: Registro seguro de ciudadanos con validación de DNI peruano (8 dígitos), contraseñas robustas BCrypt, asignación estricta de `ROLE_CIUDADANO` y prevención absoluta de escalamiento de privilegios.

### 3. Vistas y APIs de Gestión Administrativa (`/admin/portal/*`)
- **Noticias (`/admin/portal/noticias`):** Modal de creación/edición, cambio de estado (Publicar/Despublicar) y borrado lógico/físico.
- **Preguntas Frecuentes (`/admin/portal/faq`):** Gestión de categorías, orden de prelación y respuestas oficiales.
- **Consultas Ciudadanas (`/admin/portal/contactos`):** Bandeja de entrada privada (no expuesta al público) con formulario para registrar respuesta oficial interna y fecha de atención.
- **Catálogo de Servicios (`/admin/portal/servicios`):** Activación/desactivación de servicios y configuración de estado de disponibilidad.

### 4. Pruebas Automatizadas (29 tests)
- `PortalWebIntegrationTest` (18 pruebas): Renderizado de todas las vistas públicas, validación de formularios, rechazo de spam/honeypot, protección CSRF, sanitización XSS y estados vacíos.
- `PortalAdminSecurityIntegrationTest` (11 pruebas): Control de acceso RBAC, redirección de anónimos a login, rechazo HTTP 403 a usuarios con `ROLE_CIUDADANO` al intentar acceder a rutas administrativas, y pruebas completas de APIs REST CRUD con JWT.

---

## 17. Gestión de Perfiles, Datos Personales y Configuración de Cuentas (Prompt 9)

El módulo `pe.gob.municipalidad.sigm.modules.administracion.perfil` implementa la gestión integral del perfil institucional y personal, configuración de cuentas, seguridad de acceso y personalización para todos los usuarios autenticados del SIGM-QUINUA (servidores públicos y ciudadanos).

```text
pe.gob.municipalidad.sigm.modules.administracion.perfil/
├── controller/
│   ├── PerfilWebController.java             # Vistas Thymeleaf (GET /perfil, GET /admin/perfil)
│   └── PerfilRestController.java            # Endpoints REST (/api/perfil/**)
│
├── service/
│   └── PerfilService.java                   # Lógica de perfil, validaciones, contraseñas, avatars y sesiones
│
├── entity/
│   └── UsuarioPreferencia.java              # Entidad usuario_preferencias (tema, idioma, alertas, sidebar)
│
├── repository/
│   └── UsuarioPreferenciaRepository.java    # Spring Data JPA para preferencias de usuario
│
└── dto/
    └── PerfilDtos.java                      # DTOs: PerfilResponse, ActualizarDatosPersonalesRequest,
                                             # CambiarPasswordRequest, UsuarioPreferenciaDto, SesionActivaDto,
                                             # PermisosConsolidadosDto, AsignacionOficinaDto
```

### 1. Migración de Base de Datos (`V12__usuario_perfil_preferencias.sql`)
- **Columna `foto_url` en `personas`:** Almacenamiento seguro de ruta o identificador interno de avatar.
- **Tabla `usuario_preferencias`:** Clave primaria/foránea vinculada a `usuarios(id)`, con preferencias visuales (`tema_visual`: dark/light/system, `idioma`: es, `notificaciones_email`, `notificaciones_sistema`, `tamano_pagina`, `sidebar_compacto`).

### 2. Funcionalidades del Módulo de Perfil
- **Consulta de Perfil Propio:** Visualización de fotografía, nombres y apellidos completos, tipo de documento con DNI parcialmente enmascarado (`4567****`), correo electrónico, teléfono, dirección, estado de cuenta, fecha de alta, roles asignados, oficina principal y cargo institucional.
- **Edición de Datos Personales:** Modificación segura de teléfono, correo electrónico (con validación de duplicados en la base de datos) y dirección domiciliaria. Los datos de identidad legal (DNI, nombres) y relaciones institucionales (oficina, cargo, roles) permanecen protegidos y no editables por el usuario común.
- **Cambio Seguro de Contraseña:** Verificación de contraseña actual mediante BCrypt, validación de complejidad (mínimo 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales), coincidencia de confirmación, revocación automática de otras sesiones activas y registro de auditoría.
- **Matriz de Roles y Permisos (Solo Lectura):** Desglose transparente de roles asignados, módulos autorizados y acciones permitidas, impidiendo estrictamente la autoasignación o escalamiento de privilegios.
- **Asignaciones Institucionales:** Consulta de oficinas y cargos asignados con indicación de oficina principal.
- **Preferencias Personales:** Persistencia de tema visual (oscuro `#101827`, claro, sistema), tamaño de página en tablas y comportamiento del sidebar.
- **Gestión de Fotografía de Perfil:** Carga de avatares en formatos JPEG, PNG y WebP con validación estricta de Magic Bytes (firmas de archivo), límite de 2 MB, generación de identificadores aleatorios seguros para evitar path traversal, almacenamiento fuera del classpath ejecutable y eliminación segura.
- **Sesiones Activas:** Integración directa con `RefreshTokenRepository` para visualizar sesiones activas por IP y navegador, permitir la revocación remota de una sesión específica o la revocación simultánea de todas las demás sesiones activas.
- **Actividad de Seguridad y Auditoría:** Paginación de eventos de seguridad registrados en `AuditoriaAdministrativa` asociados exclusivamente al usuario autenticado.

### 3. Prevención Estricta de Vulnerabilidades (IDOR y Escalamiento)
- Todos los controladores extraen el identificador y nombre de usuario directamente desde el `SecurityContextHolder` / JWT autenticado.
- Ningún endpoint de perfil acepta un `userId` en el payload o URL para mutaciones sobre el perfil propio, anulando cualquier posibilidad de Insecure Direct Object Reference (IDOR).
- Las operaciones sensibles generan registros inmediatos en la tabla `auditoria_administrativa`.

### 4. Pruebas Automatizadas
- `PerfilIntegrationTest`: Cobertura exhaustiva de 19 casos de prueba (consulta web/REST, rechazo de anónimos, edición de datos, validación de correo duplicado, cambio de contraseña, rechazo de contraseña incorrecta o no coincidente, revocación de sesiones, permisos consolidados, asignaciones, preferencias de usuario, carga de avatares válidos y rechazo de archivos maliciosos/grandes, revocación individual y masiva de sesiones remotas, auditoría y prevención de IDOR).

---

## 18. Sistema de Notificaciones Institucionales, Correo Electrónico y Alertas Internas (Prompt 10)

El sistema implementa una infraestructura centralizada, asíncrona y segura de notificaciones internas y mensajería transaccional por correo electrónico, transversal a los 10 módulos municipales del SIGM-QUINUA:

```text
pe.gob.municipalidad.sigm.common.notification/
├── controller/
│   ├── NotificacionWebController.java             # Centro de notificaciones (/notificaciones)
│   └── NotificacionApiController.java             # REST API (/api/notificaciones/**)
│
├── service/
│   ├── CentralNotificationService.java            # Contrato transversal de notificaciones
│   ├── CentralNotificationServiceImpl.java        # Orquestación interna, oficina, programación
│   ├── EmailService.java                          # Interfaz de gestión y outbox de correos
│   └── EmailServiceImpl.java                      # Encolado persistente, reintentos y despacho
│
├── provider/
│   ├── MailProvider.java                          # Contrato desacoplado de proveedor de correo
│   ├── MockMailProvider.java                      # Proveedor simulado para pruebas y desarrollo
│   ├── SmtpMailProvider.java                      # Proveedor SMTP para servidores reales
│   └── SendResult.java                            # Resultado estructurado del envío
│
├── template/
│   └── NotificationTemplateEngine.java            # Motor de plantillas con escape HTML y anti-XSS
│
├── event/
│   ├── NotificacionSeguridadEvent.java            # Evento desacoplado de auditoría y seguridad
│   └── NotificacionEventListener.java             # Listener asíncrono para eventos institucionales
│
├── scheduler/
│   └── NotificationQueueScheduler.java            # Worker periódico para Outbox y envíos programados
│
├── entity/
│   ├── NotificacionPreferencias.java              # Tabla: notificacion_preferencias
│   ├── NotificacionPlantilla.java                 # Tabla: notificacion_plantillas
│   ├── NotificacionEnvio.java                     # Tabla: notificacion_envios (Outbox)
│   ├── NotificacionIntento.java                   # Tabla: notificacion_intentos (Historial)
│   └── NotificacionProgramacion.java              # Tabla: notificacion_programaciones
│
├── repository/
│   ├── NotificacionPreferenciasRepository.java
│   ├── NotificacionPlantillaRepository.java
│   ├── NotificacionEnvioRepository.java
│   ├── NotificacionIntentoRepository.java
│   └── NotificacionProgramacionRepository.java
│
└── dto/
    └── NotificationDtos.java                      # DTOs: NotificacionFiltroRequest, PreferenciasRequest,
                                                   # ProgramarNotificacionRequest, EnviarOficinaRequest, etc.
```

### 1. Migración de Base de Datos (`V13__notificaciones_sistema_email.sql`)
- **Evolución de `notificaciones`:** Incorporación de columnas `categoria`, `referencia_url`, `leida_en`, `expira_en`, `origen_modulo`, `metadata_json` y restricciones de integridad.
- **Tabla `notificacion_preferencias`:** Configuración por usuario (`canal_sistema`, `canal_email`, `alertas_seguridad`, `aviso_tramites`, `aviso_pagos`, `aviso_institucional`).
- **Tabla `notificacion_plantillas`:** Almacén institucional de plantillas HTML/texto con variables dinámicas (`{{nombreDestinatario}}`, `{{mensaje}}`, `{{enlaceAccion}}`, etc.).
- **Tabla `notificacion_envios`:** Cola persistente (Patrón Outbox transaccional) con clave de idempotencia (`clave_idempotencia`), intentos ejecutados, próximo reintento y estado (`PENDIENTE`, `EN_PROCESO`, `ENVIADO`, `FALLIDO_REINTENTABLE`, `FALLIDO_DEFINITIVO`).
- **Tabla `notificacion_intentos`:** Historial detallado de cada intento de envío con proveedor utilizado, código de respuesta y mensaje de error/éxito.
- **Tabla `notificacion_programaciones`:** Envíos programados a futuro evaluados en la zona horaria oficial `America/Lima`.

### 2. Categorías de Eventos y Notificaciones
Soporta las 11 categorías institucionales:
`INFORMACION`, `EXITO`, `ADVERTENCIA`, `ERROR`, `SEGURIDAD`, `TRAMITE`, `DOCUMENTO`, `PAGO`, `APROBACION`, `OBSERVACION`, `SISTEMA`.

### 3. Centro de Notificaciones y Campana del Dashboard
- **Campana Topbar:** Componente interactivo en el layout administrativo con ícono Lucide Bell, badge reactivo con contador en tiempo real de mensajes no leídos, menú desplegable con vista previa de notificaciones recientes, botón directo para marcar como leída y acceso al centro completo.
- **Centro de Notificaciones (`/notificaciones`):** Interfaz dark-mode adaptada (`#101827`, `#1e293b`, `#202c3d`, `#1d4ed8`) con pestañas ("Todas", "No leídas", "Trámites", "Seguridad", "Preferencias"), buscador por texto, filtro por categoría/estado, paginación dinámica, modal de detalle accesible y panel de activación/desactivación de canales.
- **Seguridad en Preferencias:** El interruptor `alertas_seguridad` es de carácter obligatorio y el sistema impide estrictamente su desactivación para garantizar la trazabilidad de eventos críticos.

### 4. Servicio de Correo Electrónico y Patrón Outbox
- **Abstracción `MailProvider`:** Desacoplamiento total de proveedores externos. El sistema arranca de manera predeterminada con `MockMailProvider` (que registra envíos en memoria e inspección de pruebas sin enviar spam ni requerir credenciales reales).
- **Proveedor `SmtpMailProvider`:** Integración lista para producción con Jakarta Mail / Spring Mail mediante variables de entorno estándar.
- **Patrón Outbox y Reintentos con Backoff:** Los correos se persisten de forma atómica en `notificacion_envios` antes de ser despachados. En caso de fallas transitorias de red, el planificador `NotificationQueueScheduler` aplica reintentos con retroceso exponencial (`nextAttemptAt = now + (2^intentos * 60) segundos`) hasta un máximo de 5 intentos.
- **Claves de Idempotencia:** Prevención de envíos duplicados ante reintentos de peticiones HTTP concurrentes.

### 5. Motor de Plantillas y Seguridad XSS
- **`NotificationTemplateEngine`:** Renderiza mensajes institucionales escapando rigurosamente caracteres especiales HTML y saneando URLs de botones de acción para neutralizar vectores de inyección XSS.
- **10 Plantillas Oficiales:**
  1. `BIENVENIDA_SISTEMA`: Bienvenida al portal municipal.
  2. `VERIFICACION_CORREO`: Confirmación de dirección electrónica.
  3. `RECUPERACION_PASSWORD`: Enlace de restablecimiento seguro.
  4. `CAMBIO_PASSWORD`: Alerta preventiva de cambio de contraseña.
  5. `ALERTA_SEGURIDAD`: Detección de actividad sensible de cuenta.
  6. `NUEVA_NOTIFICACION`: Aviso de nueva notificación interna.
  7. `AVISO_INSTITUCIONAL`: Comunicados oficiales de la alcaldía o gerencias.
  8. `ACTUALIZACION_TRAMITE`: Estado de expedientes en trámite.
  9. `DOCUMENTO_OBSERVADO`: Subsanación de observaciones documentales.
  10. `TRAMITE_APROBADO`: Notificación de acto administrativo favorable.

### 6. Variables de Entorno para Correo Real

Para habilitar el envío real a través de un servidor SMTP institucional (ej. Gmail Workspace, Amazon SES, SendGrid, servidor municipal propio):

| Variable | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `MAIL_HOST` | Host del servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Puerto de conexión | `587` |
| `MAIL_USERNAME` | Usuario o correo institucional | `notificaciones@muniquinua.gob.pe` |
| `MAIL_PASSWORD` | Contraseña de aplicación o token | `clave_app_segura` |
| `MAIL_FROM` | Remitente oficial autorizado | `Municipalidad Distrital de Quinua <no-reply@muniquinua.gob.pe>` |
| `MAIL_ENABLED` | Activa el proveedor SMTP real | `true` (por defecto `false` usa Mock) |

### 7. Endpoints REST Implementados (`/api/notificaciones/**`)
- `GET /api/notificaciones`: Consulta paginada con filtros por `leida`, `categoria` y `termino`.
- `GET /api/notificaciones/no-leidas/count`: Contador numérico de no leídas para la campana.
- `GET /api/notificaciones/{id}`: Detalle de una notificación (con validación estricta de propiedad IDOR).
- `PATCH /api/notificaciones/{id}/leer`: Marcar una notificación individual como leída.
- `PATCH /api/notificaciones/leer-todas`: Marcar todas las notificaciones propias como leídas.
- `GET /api/notificaciones/preferencias`: Obtener preferencias de canales del usuario autenticado.
- `PATCH /api/notificaciones/preferencias`: Actualizar preferencias respetando alertas de seguridad obligatorias.
- `POST /api/notificaciones/programar`: Programar notificación con fecha y hora (`America/Lima`).
- `POST /api/notificaciones/oficina`: Envío masivo autorizado a servidores de una unidad orgánica municipal.
- `DELETE /api/notificaciones/programadas/{id}`: Cancelación de envíos programados pendientes.

### 8. Pruebas Automatizadas (210 tests en el proyecto)
- `TemplateEngineAndMailUnitTest` (4 pruebas unitarias): Renderizado de plantillas, escape HTML anti-XSS, envío vía mock provider y fallback con error controlado.
- `NotificacionIntegrationTest` (12 pruebas de integración): Ciclo completo de creación, consulta paginada, conteo, aislamiento IDOR (rechazo 403 a notificaciones ajenas), marcar como leída y todas leídas, actualización de preferencias inmutables de seguridad, encolado en Outbox, reintentos con backoff, envío programado y difusión por oficina.

---

## 19. Sistema Centralizado de Gestión Documental, Archivos Seguros, PDF y Comprobantes (Prompt 11)

El paquete `pe.gob.municipalidad.sigm.common.document` implementa el subsistema transversal de administración documental, almacenamiento seguro desacoplado, cálculo de huella criptográfica SHA-256, versionado inmutable, generación de constancias/comprobantes en PDF con código QR y auditoría de accesos para la Municipalidad Distrital de Quinua.

### Estructura de Paquetes (`pe.gob.municipalidad.sigm.common.document`):
```text
pe.gob.municipalidad.sigm.common.document/
├── controller/
│   ├── DocumentoRestController.java               # API REST (/api/documentos/**)
│   └── DocumentoWebController.java                # Controlador Web (/dashboard/documentos)
│
├── service/
│   ├── DocumentoService.java                      # Interfaz de gestión documental
│   └── DocumentoServiceImpl.java                  # Implementación desacoplada y transaccional
│
├── storage/
│   ├── DocumentStorageService.java                # Abstracción de almacenamiento físico
│   └── FileSystemDocumentStorageService.java      # Implementación local con contención anti-traversal
│
├── validation/
│   ├── DocumentFileValidator.java                 # Validación de MIME, tamaño, extensiones y magic bytes
│   └── Sha256Calculator.java                      # Cálculo de huellas criptográficas SHA-256
│
├── pdf/
│   ├── PdfGeneratorService.java                   # Generador OpenPDF de constancias, reportes y oficios
│   └── QrCodeGenerator.java                       # Generador de códigos QR con ZXing
│
├── security/
│   ├── DigitalSignatureService.java               # Punto de extensión para firma digital criptográfica
│   └── NoOpDigitalSignatureService.java           # Implementación base documentada (no simulada)
│
├── audit/
│   └── DocumentoAuditService.java                 # Registro transaccional de accesos y descargas
│
├── entity/
│   ├── Documento.java                             # Metadatos del documento principal
│   ├── DocumentoVersion.java                      # Historial de versiones del archivo
│   ├── DocumentoRelacion.java                     # Vinculación modular polimórfica
│   ├── DocumentoVerificacion.java                 # Registro histórico de verificaciones SHA-256
│   └── DocumentoAcceso.java                       # Trazas de auditoría (IP, User-Agent, acción)
│
├── repository/
│   ├── DocumentoRepository.java
│   ├── DocumentoVersionRepository.java
│   ├── DocumentoRelacionRepository.java
│   ├── DocumentoVerificacionRepository.java
│   └── DocumentoAccesoRepository.java
│
└── dto/
    ├── DocumentDtos.java                          # DTOs de peticiones, respuestas y estadísticas
    └── DocumentoMapper.java                       # Mapeador de entidades a DTOs
```

### 1. Migración de Base de Datos (`V14__gestion_documental.sql`)
- **Tabla `documentos`:** Metadatos principales, identificador público UUID no predecible, nombre original saneado, nombre interno UUID, tipo MIME, tamaño en bytes, hash SHA-256, categoría documental, clasificación de acceso (`PUBLICO`, `INTERNO`, `CONFIDENCIAL`, `RESTRINGIDO`), versión vigente, estado (`ACTIVO`, `ARCHIVADO`, `ELIMINADO_LOGICO`) y bandera `bloqueo_eliminacion`.
- **Tabla `documento_versiones`:** Registro inmutable por versión con número correlativo, hash SHA-256, motivo de cambio, tamaño, tipo MIME y usuario responsable.
- **Tabla `documento_relaciones`:** Enlace desacoplado con módulos institucionales futuros (expedientes, comerciantes, licencias, pagos, obras, etc.) mediante `modulo`, `tipo_entidad`, `entidad_id` y `rol_documento`.
- **Tabla `documento_verificaciones`:** Registro de auditoría criptográfica con hash esperado, hash calculado y resultado (`INTEGRO`, `ALTERADO`, `NO_ENCONTRADO`, `ERROR`).
- **Tabla `documento_accesos`:** Auditoría de cada descarga, vista previa, creación y cambio de estado con dirección IP, User-Agent y usuario.
- **Permisos Sembrados:** `DOCUMENTO_VER`, `DOCUMENTO_CREAR`, `DOCUMENTO_EDITAR`, `DOCUMENTO_ELIMINAR`, `DOCUMENTO_ADMIN`.

### 2. Almacenamiento Seguro y Prevención de Vulnerabilidades
- **Directorio Privado Configurable:** Los archivos nunca se almacenan dentro del código fuente ni en carpetas web públicas (`src/main/resources/static`).
- **Variable de Entorno:**
  ```properties
  sigm.document.storage.path=${DOCUMENT_STORAGE_PATH:./storage/documentos}
  sigm.document.max-file-size=${DOCUMENT_MAX_FILE_SIZE:26214400}
  sigm.document.allowed-extensions=pdf,jpeg,jpg,png,webp,docx,xlsx
  ```
- **Nombre Físico No Predecible:** Los archivos se renombran a `{UUID}.{ext}` impidiendo el descubrimiento por enumeración o el sobreescrito accidental.
- **Validación Binaria Estricta (`DocumentFileValidator`):**
  - Rechazo de archivos vacíos (0 bytes) y que excedan los 25 MB configurados.
  - Saneamiento contra Path Traversal (`../`, `..\`, secuencias nulas `%00`).
  - Bloqueo incondicional de extensiones ejecutables o de script (`.exe`, `.bat`, `.cmd`, `.sh`, `.vbs`, `.js`, `.jsp`, `.jar`, `.msi`, `.dll`, `.zip`, `.rar`, etc.).
  - Comprobación de **firmas binarias (magic bytes)** reales:
    - PDF: `%PDF-` (`0x25 0x50 0x44 0x46`)
    - PNG: `0x89 0x50 0x4E 0x47`
    - JPEG: `0xFF 0xD8 0xFF`
    - WebP: `RIFF....WEBP`
    - DOCX / XLSX: Cabeceras ZIP OpenXML válidas.
  - Política de contención y aislamiento para documentos en cuarentena.

### 3. Integridad Criptográfica SHA-256
- Cada archivo subido o generado calcula su hash criptográfico SHA-256 (`Sha256Calculator`) durante la transferencia mediante `MessageDigest`.
- Endpoint de verificación periódica (`GET /api/documentos/{id}/integridad`) que recalcula el hash del archivo físico en disco y lo compara con el metadato en base de datos.
- Detecta alteraciones accidentales, corrupciones de disco o modificaciones no autorizadas en el almacenamiento.

### 4. Generación de PDF Institucional con Código QR
- **Servicio `PdfGeneratorService`:** Construido con OpenPDF para generar Constancias, Comprobantes, Reportes y Oficios con diseño formal municipal.
- **Estructura Oficial:** Encabezado con nombre institucional ("MUNICIPALIDAD DISTRITAL DE QUINUA"), título central, código documental, destinatario, asunto, cuerpo del documento, código QR al pie y pie de página con aviso legal.
- **Código QR (`QrCodeGenerator` con ZXing):**
  - Contiene URL HTTPS segura de verificación pública (`https://muniquinua.gob.pe/verificar/{uuid}`).
  - No expone datos personales sensibles ni DNI en el payload del QR.
- **Aviso Legal Criptográfico:** La integridad SHA-256 y el código QR garantizan la correspondencia digital del contenido, pero no sustituyen una firma digital con certificado digital X.509 de persona jurídica/natural de acuerdo a la Ley N° 27269 de Firmas y Certificados Digitales del Perú.

### 5. Puntos de Extensión para Firma Digital
- Interfaz `DigitalSignatureService` lista para integrar proveedores autorizados por INDECOPI / RENIEC en etapas posteriores.
- Implementación base `NoOpDigitalSignatureService` documentada expresamente para evitar confundir firmas electrónicas simuladas o imágenes manuscritas pegadas con firmas digitales válidas.

### 6. Interfaz Web Administrativa (`/dashboard/documentos` y `/admin/documentos`)
- Integrada con el layout administrativo y protegida por Spring Security.
- **Paleta de Colores Gubernamental Oscura:** Fondo `#101827`, Sidebar `#1e293b`, Tarjetas `#202c3d`, Botones de acción `#1d4ed8`, Texto `#e2e8f0`.
- **Componentes:**
  - 4 Tarjetas de resumen (Total documentos, Activos, Espacio consumido en MB, Bloqueados por conservación).
  - Barra de búsqueda y filtros combinados por Estado, Categoría y Clasificación.
  - Tabla documental responsiva con identificadores UUID, tamaños formateados, etiquetas estilizadas y menús de acciones.
  - Modal de carga interactivo con previsualización, selector de oficina y categoría.
  - Modal de historial completo de versiones con descarga independiente de cada versión previa.
  - Modal de verificación de integridad SHA-256 en vivo con badges de estado (`INTEGRO` en esmeralda o `ALTERADO` en carmesí).
  - Modal de vista previa en iframe para archivos PDF e imágenes con cabeceras `nosniff` y `DENY`.
  - Modales seguros de archivado y eliminación lógica con ingreso obligatorio de motivo.

### 7. Catálogo de Endpoints REST (`/api/documentos/**`)
| Método | Endpoint | Permiso Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/documentos` | `DOCUMENTO_CREAR` | Subida segura multipart con metadatos y version 1 |
| `GET` | `/api/documentos` | Autenticado | Listado paginado con filtros de búsqueda y permisos |
| `GET` | `/api/documentos/{id}` | Autenticado / Anti-IDOR | Consulta de metadatos por ID numérico |
| `GET` | `/api/documentos/uuid/{uuid}` | Autenticado / Anti-IDOR | Consulta de metadatos por identificador público UUID |
| `GET` | `/api/documentos/{id}/descargar` | Autenticado / Anti-IDOR | Descarga segura con cabeceras `nosniff` y `Content-Disposition` |
| `GET` | `/api/documentos/{id}/vista-previa` | Autenticado / Anti-IDOR | Previsualización inline de documentos PDF e imágenes |
| `GET` | `/api/documentos/{id}/versiones` | Autenticado / Anti-IDOR | Historial cronológico de versiones del documento |
| `POST` | `/api/documentos/{id}/versiones` | `DOCUMENTO_EDITAR` | Carga de nueva versión con motivo e incremento correlativo |
| `GET` | `/api/documentos/{id}/integridad` | Autenticado / Anti-IDOR | Verificación criptográfica SHA-256 en tiempo real |
| `GET` | `/api/documentos/{id}/auditoria` | `DOCUMENTO_ADMIN` | Trazabilidad completa de accesos, descargas y cambios |
| `PATCH`| `/api/documentos/{id}` | `DOCUMENTO_EDITAR` | Actualización de descripción, categoría o clasificación |
| `POST` | `/api/documentos/{id}/archivar` | `DOCUMENTO_ELIMINAR` | Archivado institucional del documento con motivo |
| `DELETE`| `/api/documentos/{id}` | `DOCUMENTO_ELIMINAR` | Eliminación lógica (respeta bloqueo de conservación) |
| `PATCH`| `/api/documentos/{id}/conservacion` | `DOCUMENTO_ADMIN` | Activa o desactiva el bloqueo de conservación documental |
| `GET` | `/api/documentos/estadisticas` | Autenticado | Métricas numéricas de documentos activos y almacenamiento |
| `POST` | `/api/documentos/generar-pdf` | `DOCUMENTO_CREAR` | Generación oficial de constancia o comprobante en PDF con QR |

### 8. Pruebas Automatizadas (229 tests en el proyecto)
- `DocumentValidationAndPdfUnitTest` (6 pruebas unitarias):
  - Detección y bloqueo de nombres maliciosos con path traversal (`../../../etc/passwd.pdf`).
  - Validación binaria exitosa de archivo PDF válido.
  - Bloqueo de extensiones peligrosas (.exe) y validación de magic bytes.
  - Cálculo consistente de huellas criptográficas SHA-256.
  - Generación de código QR con dimensiones y codificación válidas.
  - Generación de constancia institucional en formato PDF binario.
- `DocumentoIntegrationTest` (13 pruebas de integración con MockMvc y PostgreSQL):
  - Subida de documento oficial y registro de versión 1.
  - Rechazo de archivos vacíos (HTTP 422).
  - Rechazo de archivos ejecutables (HTTP 422).
  - Consulta de metadatos por ID y UUID.
  - Descarga con cabeceras seguras (`X-Content-Type-Options: nosniff`).
  - Previsualización inline de archivos PDF.
  - Verificación criptográfica de integridad SHA-256 (`INTEGRO`).
  - Creación de nueva versión e incremento del correlativo.
  - Generación de PDF oficial con bloqueo de conservación.
  - Rechazo de eliminación bajo bloqueo institucional de conservación (HTTP 422).
  - Archivado y eliminación lógica de documentos desbloqueados.
  - Control de accesos Anti-IDOR (rechazo 403 Forbidden a documentos confidenciales ajenos).
  - Renderizado completo de la vista administrativa Thymeleaf `/dashboard/documentos`.

---

## 20. Módulo 1: Mesa de Partes Virtual (Prompt 12)

El paquete `pe.gob.municipalidad.sigm.modules.mesapartes` implementa el núcleo de persistencia, reglas de negocio, motor de transiciones de estados, numeración correlativa concurrente y servicios de dominio para la **Mesa de Partes Virtual (MPV)** de la Municipalidad Distrital de Quinua.

### 1. Modelo de Datos y Migración Flyway (`V15__mesa_partes_virtual.sql`)
- **`mpv_tipos_tramite`:** Catálogo institucional de procedimientos con código, nombre, oficina responsable, requisitos documentales, bandera de admisión virtual, plazo en días (`plazo_dias`) y unidad de plazo (`DIAS_HABILES` o `DIAS_CALENDARIO`).
- **`mpv_correlativos`:** Tabla de control para numeración anual atómica con restricción `UNIQUE(prefijo, anio)`.
- **`mpv_expedientes`:** Entidad central del trámite administrativo municipal con identificador UUID público, número correlativo (`MPV-YYYY-NNNNNN`), solicitante (`Persona`), oficina responsable (`Oficina`), tipo de trámite, estado actual, canal de ingreso (`VIRTUAL`, `PRESENCIAL`), prioridad (`NORMAL`, `ALTA`, `URGENTE`), confidencialidad y plazos.
- **`mpv_expediente_historial`:** Trazabilidad inmutable de cada cambio de estado, acción ejecutada, motivo, usuario responsable, oficina y correlación UUID.
- **`mpv_expediente_documentos`:** Relación N:M entre el expediente y la entidad centralizada `Documento` (Prompt 11) con tipificación de relación (`PRINCIPAL`, `ANEXO`, `SUBSANACION`, `INFORME`, `OFICIO`, `RESOLUCION`, `CONSTANCIA`).
- **`mpv_derivaciones`:** Registro formal de transferencias entre unidades orgánicas con estado (`PENDIENTE`, `RECIBIDA`, `RECHAZADA`, `FINALIZADA`), motivo y fechas.
- **`mpv_observaciones`:** Observaciones administrativas formuladas por operadores u oficinas responsables con plazo perentorio de subsanación.
- **`mpv_subsanaciones`:** Respuestas formales, descargos y adjuntos complementarios presentados por los ciudadanos para levantar observaciones.
- **Permisos Sembrados y Mapeados a Roles:**
  - `MESAPARTES_EXPEDIENTE_CREAR`, `MESAPARTES_EXPEDIENTE_VER_PROPIO`, `MESAPARTES_EXPEDIENTE_VER_OFICINA`, `MESAPARTES_EXPEDIENTE_VER_TODOS`.
  - `MESAPARTES_EXPEDIENTE_RECIBIR`, `MESAPARTES_EXPEDIENTE_DERIVAR`, `MESAPARTES_EXPEDIENTE_OBSERVAR`, `MESAPARTES_EXPEDIENTE_SUBSANAR`.
  - `MESAPARTES_EXPEDIENTE_RESOLVER`, `MESAPARTES_EXPEDIENTE_ARCHIVAR`, `MESAPARTES_CONFIGURAR_TRAMITES`, `MESAPARTES_ADMIN`.

### 2. Diagrama Entidad-Relación (Mermaid)

```mermaid
erDiagram
    MUNICIPALIDAD ||--o{ OFICINA : organiza
    OFICINA ||--o{ TIPO_TRAMITE : atiende
    PERSONA ||--o{ EXPEDIENTE : solicita
    OFICINA ||--o{ EXPEDIENTE : despacha
    TIPO_TRAMITE ||--o{ EXPEDIENTE : tipifica
    USUARIO ||--o{ EXPEDIENTE : registra
    EXPEDIENTE ||--o{ EXPEDIENTE_HISTORIAL : registra
    EXPEDIENTE ||--o{ EXPEDIENTE_DOCUMENTO : asocia
    DOCUMENTO ||--o{ EXPEDIENTE_DOCUMENTO : referencia
    EXPEDIENTE ||--o{ DERIVACION : deriva
    EXPEDIENTE ||--o{ OBSERVACION : observa
    OBSERVACION ||--o{ SUBSANACION : responde
    CORRELATIVO_EXPEDIENTE {
        bigint id PK
        varchar prefijo
        int anio
        int ultimo_numero
    }
    EXPEDIENTE {
        bigint id PK
        uuid uuid UK
        varchar numero_expediente UK
        int anio
        varchar asunto
        text descripcion
        varchar estado_actual
        timestamptz fecha_presentacion
        timestamptz fecha_recepcion
        timestamptz fecha_limite
        timestamptz fecha_cierre
    }
```

### 3. Diagrama de Transición de Estados (Mermaid)

```mermaid
stateDiagram-v2
    [*] --> BORRADOR : Ciudadano inicia registro
    BORRADOR --> PRESENTADO : Ciudadano presenta formalmente
    BORRADOR --> ANULADO : Desistimiento o anulación
    PRESENTADO --> RECIBIDO : Operador MPV recibe conforme
    PRESENTADO --> OBSERVADO : Observación previa a recepción
    PRESENTADO --> ANULADO : Anulación administrativa
    RECIBIDO --> EN_REVISION : Oficina inicia evaluación
    RECIBIDO --> DERIVADO : Derivación a otra oficina
    RECIBIDO --> OBSERVADO : Observación de requisitos
    EN_REVISION --> OBSERVADO : Observación técnica / legal
    EN_REVISION --> DERIVADO : Remisión a otra unidad
    EN_REVISION --> EN_ATENCION : Actuación administrativa
    EN_REVISION --> RESUELTO : Dictamen / Resolución
    OBSERVADO --> EN_SUBSANACION : Ciudadano subsana y adjunta
    OBSERVADO --> ARCHIVADO : Vencimiento de plazo sin subsanación
    OBSERVADO --> ANULADO : Anulación
    EN_SUBSANACION --> EN_REVISION : Retorno para reevaluación
    DERIVADO --> EN_ATENCION : Unidad destino avoca conocimiento
    DERIVADO --> OBSERVADO : Unidad destino formula reparos
    DERIVADO --> RESUELTO : Unidad destino resuelve
    EN_ATENCION --> RESUELTO : Emisión de acto resolutivo
    EN_ATENCION --> OBSERVADO : Requerimiento adicional
    RESUELTO --> NOTIFICADO : Notificación formal ciudadana
    RESUELTO --> ARCHIVADO : Cierre administrativo
    NOTIFICADO --> ARCHIVADO : Archivo definitivo central
    ARCHIVADO --> [*]
    ANULADO --> [*]
```

### 4. Generación Atómica y Concurrente de Numeración (`NumeracionExpedienteService`)
- Formato configurable garantizado: `MPV-{AÑO}-{CORRELATIVO:06d}` (ejemplo: `MPV-2026-000001`).
- **Control Anticolisión:** Nunca depende de `COUNT(*) + 1`. Emplea la tabla de control `mpv_correlativos`.
- **Prevención de Abortos de Transacción:** Ejecuta una sentencia nativa `INSERT ... ON CONFLICT (prefijo, anio) DO NOTHING` previa a la adquisición del bloqueo con `@Lock(LockModeType.PESSIMISTIC_WRITE)`. Esto garantiza la correcta serialización de solicitudes concurrentes de múltiples ciudadanos simultáneos sin colisiones de clave única ni bloqueos muertos.

### 5. Cómputo de Plazos Administrativos (`PlazoAdministrativoService`)
- Desarrollado de conformidad con el Texto Único Ordenado de la Ley N° 27444 (Ley del Procedimiento Administrativo General - LPAG).
- Diferencia estrictamente entre `DIAS_CALENDARIO` y `DIAS_HABILES`.
- En cómputo por días hábiles:
  - El cómputo inicia el día hábil siguiente a la notificación o presentación formal.
  - Excluye automáticamente sábados y domingos.
  - Excluye feriados nacionales oficiales del Perú (D. Leg. 713), incluyendo la fecha histórica de Quinua: el **9 de Diciembre (Batalla de Ayacucho)** y las Fiestas Patrias (28 y 29 de Julio).

### 6. Desacoplamiento y Seguridad Anti-IDOR
- **Anti-IDOR Estricto:**
  - Los usuarios con rol `ROLE_CIUDADANO` solo pueden consultar y actuar sobre expedientes donde figuren como solicitantes (`solicitante.id == usuario.persona.id`). Intentos de consultar o alterar expedientes ajenos son bloqueados con `AccessDeniedException` (HTTP 403 Forbidden).
  - Los operadores y jefes de oficina solo pueden gestionar trámites asignados a sus unidades orgánicas, salvo personal directivo con `MESAPARTES_EXPEDIENTE_VER_TODOS` o administradores del sistema.
- **Integración con Gestión Documental:**
  - Reutiliza la tabla `documentos` (Prompt 11) sin redundancia de almacenamiento físico ni duplicidad binaria.
- **Integración Transaccional con Notificaciones:**
  - Emisión de eventos de dominio (`ExpedientePresentadoEvent`, `ExpedienteRecibidoEvent`, `ExpedienteObservadoEvent`, `ExpedienteDerivadoEvent`, `ExpedienteResueltoEvent`, `ExpedienteArchivadoEvent`).
  - El listener `MpvNotificationListener` procesa en fase `TransactionPhase.AFTER_COMMIT`, impidiendo que errores de mensajería afecten la transacción o que se notifiquen transacciones revertidas.

### 7. Catálogo de Endpoints REST (`/api/mesa-partes/**`)
| Método | Endpoint | Autorización | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mesa-partes/tipos-tramite` | Público / Autenticado | Catálogo de tipos de trámites disponibles |
| `GET` | `/api/mesa-partes/tipos-tramite/{id}` | Autenticado | Detalle de un tipo de trámite |
| `POST` | `/api/mesa-partes/tipos-tramite` | `MESAPARTES_CONFIGURAR_TRAMITES` | Creación de nuevos tipos de trámites |
| `PUT` | `/api/mesa-partes/tipos-tramite/{id}` | `MESAPARTES_CONFIGURAR_TRAMITES` | Actualización de configuración de trámite |
| `GET` | `/api/mesa-partes/expedientes` | Autenticado / Anti-IDOR | Listado paginado con filtros de estado y oficina |
| `GET` | `/api/mesa-partes/expedientes/{id}` | Autenticado / Anti-IDOR | Detalle completo de expediente con docs, historial y derivaciones |
| `GET` | `/api/mesa-partes/expedientes/{id}/historial` | Autenticado / Anti-IDOR | Trazabilidad inmutable de movimientos del expediente |
| `GET` | `/api/mesa-partes/expedientes/{id}/documentos` | Autenticado / Anti-IDOR | Lista de documentos principales y anexos |
| `GET` | `/api/mesa-partes/estadisticas` | Funcionarios / Admin | Métricas de expedientes por estado y oficina |
| `POST` | `/api/mesa-partes/expedientes/borradores` | Autenticado | Registro de nuevo borrador de expediente |
| `PUT` | `/api/mesa-partes/expedientes/{id}/borrador` | Autenticado / Anti-IDOR | Modificación de borrador |
| `POST` | `/api/mesa-partes/expedientes/{id}/presentar` | Autenticado / Anti-IDOR | Presentación formal y generación de correlativo único |
| `POST` | `/api/mesa-partes/expedientes/{id}/documentos` | Autenticado / Anti-IDOR | Vinculación de documento activo al expediente |
| `DELETE`| `/api/mesa-partes/expedientes/{id}/documentos/{docId}` | Autenticado / Anti-IDOR | Desvinculación de documento en etapa de borrador |
| `POST` | `/api/mesa-partes/expedientes/{id}/recibir` | `MESAPARTES_EXPEDIENTE_RECIBIR` | Recepción formal por Mesa de Partes |
| `POST` | `/api/mesa-partes/expedientes/{id}/derivar` | `MESAPARTES_EXPEDIENTE_DERIVAR` | Derivación formal a unidad orgánica de destino |
| `POST` | `/api/mesa-partes/expedientes/{id}/observar` | `MESAPARTES_EXPEDIENTE_OBSERVAR` | Formulación de observaciones con plazo de subsanación |
| `POST` | `/api/mesa-partes/expedientes/{id}/subsanar` | Autenticado / Solicitante | Respuesta a observaciones con nuevos recaudos |
| `POST` | `/api/mesa-partes/expedientes/{id}/resolver` | `MESAPARTES_EXPEDIENTE_RESOLVER` | Resolución formal del expediente |
| `POST` | `/api/mesa-partes/expedientes/{id}/archivar` | `MESAPARTES_EXPEDIENTE_ARCHIVAR` | Conclusión y envío a archivo central |
| `POST` | `/api/mesa-partes/expedientes/{id}/anular` | `ROLE_ADMIN` / `ROLE_SUPERADMIN` | Anulación justificada de expediente |

---

## 21. Mesa de Partes Virtual — Solicitudes Ciudadanas, Formularios Dinámicos, Idempotencia y Constancias PDF

Implementado en el marco del **Prompt 13 (Módulo 1: Mesa de Partes Virtual)** para permitir a los ciudadanos presentar solicitudes digitales completas a la Municipalidad Distrital de Quinua con total validez técnica y administrativa.

### 1. Flujo de Presentación Ciudadana en 5 Pasos
La interfaz de usuario (`/mesa-partes/nueva-solicitud`) implementa un asistente wizard responsivo y accesible (paleta institucional azul `#1d4ed8`, `#101827`, texto `#0f172a`, bordes `#e2e8f0` y componentes Lucide Icons):
1. **Paso 1 — Tipo de Trámite:** Selección interactiva desde el catálogo oficial de la Municipalidad, mostrando oficina responsable, plazos legales y requisitos documentales asociados.
2. **Paso 2 — Datos del Solicitante:** Carga automática y blindada de la identidad ciudadana desde Spring Security (`SecurityContextHolder`), con validación de datos de contacto (teléfono y correo).
3. **Paso 3 — Información y Campos Dinámicos:** Asunto, descripción y renderizado en tiempo real de los campos configurados dinámicamente para el trámite seleccionado.
4. **Paso 4 — Carga Documental:** Carga de documentos requeridos y anexos mediante el servicio de gestión documental (Prompt 11), validando extensiones autorizadas (PDF, DOCX, PNG, JPEG), tamaño máximo y obligatoriedad.
5. **Paso 5 — Revisión, Guardado de Borrador y Presentación:** Resumen consolidado de la solicitud, opción de guardar y continuar como borrador, y presentación definitiva con confirmación y descarga inmediata de la constancia.

### 2. Arquitectura de Formularios Dinámicos Versionados
Permite a la administración municipal configurar formularios específicos para cada tipo de trámite sin modificar código fuente ni reiniciar el servidor:
- **Estructura Relacional (PostgreSQL):**
  - `mpv_formularios`: Definición de formularios vinculados a tipos de trámite.
  - `mpv_formulario_versiones`: Control de versiones inmutables (`BORRADOR`, `PUBLICADO`, `HISTORICO`). Cuando se publica una nueva versión, las solicitudes históricas mantienen su correspondencia exacta con la versión en la que fueron presentadas.
  - `mpv_formulario_campos`: Campos configurables con tipo de dato (`TEXTO`, `TEXTO_LARGO`, `NUMERO`, `FECHA`, `SELECCION`, `CHECKBOX`, `DOCUMENTO`), orden, obligatoriedad, longitud máxima, expresiones regulares de validación y textos de ayuda.
  - `mpv_formulario_opciones`: Opciones predefinidas para campos de tipo selección.
  - `mpv_respuestas` y `mpv_respuesta_valores`: Almacenamiento tipificado de las respuestas del administrado asociadas al expediente.
- **Validación Backend Reforzada:** Las reglas de validación (obligatoriedad, formato numérico, rango de fechas, regex de texto y pertenencia de opciones) se validan estrictamente en el servidor en `FormularioDinamicoService` para evitar inyecciones o bypass de validaciones frontend.

### 3. Idempotencia y Prevención de Doble Envío
- **Tabla de Control `mpv_presentaciones_idempotencia`:** Al presentar una solicitud, el cliente envía o genera una clave de idempotencia única (UUID o token de cliente).
- Si la presentación ya fue procesada exitosamente con dicha clave, el sistema retorna inmediatamente la respuesta original sin reasignar número correlativo ni generar un nuevo expediente.
- Si una solicitud idéntica se encuentra en procesamiento concurrente (bloqueo transaccional), se previene la duplicación de trámites por doble clic.

### 4. Constancia de Presentación PDF con Código QR y Verificación Criptográfica
- **Generación en Memoria con OpenPDF:** El servicio `ConstanciaPresentacionService` genera una constancia institucional de presentación en formato PDF (hoja A4).
- **Contenido Oficial:** Escudo y datos de la Municipalidad Distrital de Quinua, número único de expediente (`MPV-2026-XXXXXX`), fecha y hora exacta de presentación, datos del solicitante, asunto, resumen del trámite, lista de documentos recaudos anexados y declaración formal de presentación (advirtiendo que la presentación no implica admisión ni aprobación automática).
- **Código QR Dinámico:** Renderizado directo mediante ZXing apuntando a `/verify?code={codigoVerificacion}`, permitiendo a funcionarios o terceros validar la autenticidad del documento escaneando la constancia impresa o digital.
- **Integridad:** Código de verificación alfanumérico derivado de la huella SHA-256 del expediente y la transacción.

### 5. Bandeja Ciudadana "Mis Solicitudes" (`/mesa-partes/mis-solicitudes`)
- Visualización de expedientes y solicitudes del ciudadano autenticado.
- Filtros por estado administrativo (`BORRADOR`, `PRESENTADO`, `RECIBIDO`, `EN_REVISION`, `OBSERVADO`, `SUBSANADO`, `RESUELTO`, etc.) y búsqueda por asunto o número de expediente.
- Acciones directas: Continuar edición de borradores pendientes, visualizar seguimiento histórico en tiempo real y descargar la constancia de presentación en PDF.
- Aislamiento estricto de seguridad anti-IDOR: los ciudadanos únicamente acceden a sus propios expedientes.

### 6. Catálogo de Endpoints de Solicitudes Ciudadanas (`/api/mesa-partes/**`)
| Método | Endpoint | Autorización | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mesa-partes/tipos-tramite` | Autenticado | Catálogo de tipos de trámite habilitados para presentación virtual |
| `GET` | `/api/mesa-partes/tipos-tramite/{id}/formulario` | Autenticado | Definición y campos de la versión publicada del formulario dinámico |
| `POST` | `/api/mesa-partes/expedientes/borradores` | Autenticado | Guardar nuevo borrador de solicitud ciudadana |
| `PUT` | `/api/mesa-partes/expedientes/borradores/{id}` | Autenticado / Propietario | Actualizar datos y respuestas de un borrador existente |
| `GET` | `/api/mesa-partes/expedientes/borradores/{id}` | Autenticado / Propietario | Recuperar un borrador para continuar su llenado |
| `POST` | `/api/mesa-partes/expedientes/{id}/documentos` | Autenticado / Propietario | Adjuntar documento probatorio al expediente |
| `POST` | `/api/mesa-partes/expedientes/{id}/presentar` | Autenticado / Propietario | Presentación definitiva, validación completa, idempotencia y correlativo |
| `GET` | `/api/mesa-partes/mis-solicitudes` | Autenticado | Listado paginado de solicitudes y expedientes del solicitante |
| `GET` | `/api/mesa-partes/expedientes/{id}/constancia` | Autenticado / Propietario | Descarga del documento PDF de constancia de presentación con QR |

---

## 22. Mesa de Partes Virtual — Recepción de Expedientes, Bandeja Institucional y Observaciones (Prompt 14)

Implementado en el marco del **Prompt 14 (Módulo 1: Mesa de Partes Virtual)** para dotar a los operadores y jefes de unidad orgánica de la Municipalidad Distrital de Quinua de una bandeja administrativa integral y profesional, revisión de recaudos documentales, verificación técnica de requisitos configurados, recepción formal con numeración diferenciada, registro de observaciones internas y ciudadanas, requerimientos de subsanación y generación de constancias oficiales de recepción en PDF con código QR.

### 1. Bandeja de Entrada Institucional (`/dashboard/mesa-partes/bandeja`)
- **Diseño Administrativo Consistente:** Paleta oscura institucional (`#101827`, sidebar `#1e293b`, tarjetas `#202c3d`, acento `#1d4ed8`, texto `#e2e8f0` y bordes `#334155`), tipografía moderna y Lucide Icons sin emojis.
- **Acceso Autorizado por Permisos Dinámicos:** Restringido a servidores públicos con `MESAPARTES_BANDEJA`, `MESAPARTES_EXPEDIENTE_VER_TODOS` o roles de gestión municipal (`ROLE_ADMIN`, `ROLE_OPERADOR_MPV`). Los ciudadanos que intentan acceder a la bandeja institucional reciben denegación estricta (HTTP 403 Forbidden).
- **Filtros Multi-Criterio Paginados en PostgreSQL:**
  - Búsqueda en texto completo (`q`): número de expediente, asunto, nombres, apellidos o documento de identidad del administrado.
  - Estado administrativo (`PRESENTADO`, `RECIBIDO`, `EN_REVISION`, `OBSERVADO`, `EN_SUBSANACION`, `DERIVADO`, `RESUELTO`, `ARCHIVADO`).
  - Tipo de trámite del catálogo oficial.
  - Oficina responsable (respetando el ámbito jerárquico del funcionario).
  - Prioridad (`NORMAL`, `ALTA`, `URGENTE`).
  - Canal de presentación (`VIRTUAL`, `PRESENCIAL`).
  - Rango de fechas de presentación/registro (`fechaDesde`, `fechaHasta`).
  - Paginación dinámica en PostgreSQL mediante `JpaSpecificationExecutor<Expediente>` para consultas sin sobrecarga de memoria.

### 2. Tarjetas Estadísticas Reales y Agregadas
- Indicadores en tiempo real calculados directamente en PostgreSQL sin cargar listas completas en memoria:
  - **Expedientes Nuevos / Presentados:** Solicitudes recibidas virtualmente a la espera de revisión preliminar.
  - **Expedientes Recibidos:** Trámites formalmente recepcionados por Mesa de Partes.
  - **En Revisión:** En proceso de verificación técnica o documental.
  - **Observados:** Expedientes con observaciones registradas.
  - **Pendientes de Subsanación:** En espera de descargo o recaudos del administrado.
  - **Resueltos / Concluidos:** Trámites que finalizaron su ciclo de atención.

### 3. Detalle Integral del Expediente y Revisión en 5 Pestañas (`/dashboard/mesa-partes/expedientes/{id}`)
1. **Información General:** Resumen de identificación, solicitante, trámite, oficina, canal, fechas de presentación y recepción formal, y gestión autorizada de prioridad.
2. **Documentos:** Integrado con el servicio documental (Prompt 11), permitiendo previsualización segura de PDF/imágenes, verificación de hash criptográfico y descarga auditada.
3. **Requisitos:** Matriz de verificación de recaudos según la configuración oficial del trámite.
4. **Observaciones:** Registro diferenciado de observaciones internas y notificaciones al ciudadano.
5. **Historial de Movimientos:** Línea de tiempo cronológica e inmutable de eventos con usuario responsable, fecha y detalle del movimiento.

### 4. Verificación de Requisitos Documentales (`mpv_revision_requisitos`)
- Evaluación individualizada de cada requisito del trámite con los estados:
  - `PENDIENTE_REVISION`
  - `CONFORME`
  - `OBSERVADO`
  - `NO_APLICA`
- Permite asociar el documento probatorio examinado, observaciones puntuales y trazabilidad del operador revisor y fecha de revisión.

### 5. Recepción Administrativa Formal y Diferenciación Temporal
- **Acción Formal de Recepción (`POST /api/mesa-partes/expedientes/{id}/recibir`):**
  - Validación de estado actual (`PRESENTADO`).
  - Asignación de fecha y hora exacta de recepción administrativa (`fechaRecepcion`), conservando intacta la fecha de presentación electrónica (`fechaPresentacion`) conforme a la LPAG (Ley N° 27444).
  - Registro del usuario funcionario receptor.
  - Transición de estado a `RECIBIDO` mediante el workflow documental con registro inmutable en `mpv_historial`.
  - Notificación transaccional al administrado (en base de datos y correo si está configurado).
  - Control de concurrencia y prevención de doble recepción (HTTP 422 en intentos duplicados).

### 6. Observaciones con Visibilidad Dual y Requerimientos de Subsanación
- **Visibilidad Dual Estricta:**
  - `INTERNA`: Notas técnicas de coordinación entre funcionarios u oficinas; los ciudadanos jamás pueden visualizarlas (filtradas a nivel de servicio y en queries de repositorio).
  - `CIUDADANO`: Observaciones formales dirigidas al administrado, notificadas en su portal y bandeja de solicitudes.
- **Solicitud de Subsanación con Cómputo Legal Oficial:**
  - Cálculo de fecha límite mediante `PlazoAdministrativoService` (días hábiles, excluyendo fines de semana y feriados oficiales peruanos, como el 9 de Diciembre - Batalla de Ayacucho).
  - Actualización automática del expediente a estado `OBSERVADO`.
- **Respuesta Ciudadana y Reingreso:**
  - El administrado visualiza únicamente sus observaciones notificadas en `/mesa-partes/mis-solicitudes/{id}`.
  - Presenta subsanación con respuesta sustentada y nuevos documentos anexos (`POST /api/mesa-partes/expedientes/{id}/subsanar`).
  - El expediente transiciona a `EN_SUBSANACION`, notificando al operador para reanudar la revisión.

### 7. Constancia Oficial de Recepción en PDF con Código QR
- Generación bajo demanda en memoria con OpenPDF (`GET /api/mesa-partes/expedientes/{id}/constancia-recepcion`).
- Contiene: Membrete de la Municipalidad Distrital de Quinua, número de expediente, fecha y hora de presentación, fecha y hora de recepción formal, identificación del solicitante, oficina competente, estado y código hash de verificación digital SHA-256.
- Código QR dinámico generado mediante ZXing (`QrCodeGenerator`) enlazando a `/verify?code={hash}`.

### 8. Gestión de Prioridades y Auditoría (`mpv_cambios_prioridad`)
- Niveles: `NORMAL`, `ALTA`, `URGENTE`.
- Endpoint protegido `POST /api/mesa-partes/expedientes/{id}/prioridad` con permiso `MESAPARTES_EXPEDIENTE_PRIORIDAD` o `ROLE_ADMIN`.
- Bitácora inmutable en tabla `mpv_cambios_prioridad` que registra prioridad anterior, prioridad nueva, motivo justificado, usuario operador y fecha.

### 9. Catálogo de Endpoints de Recepción, Revisión y Observaciones (`/api/mesa-partes/**`)
| Método | Endpoint | Autorización | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mesa-partes/bandeja` | Operadores / Admin | Bandeja institucional paginada con filtros multi-criterio |
| `GET` | `/api/mesa-partes/bandeja/estadisticas` | Operadores / Admin | Indicadores agregados reales por estado y oficina |
| `GET` | `/api/mesa-partes/expedientes/{id}/requisitos` | Autenticado | Lista de requisitos y su estado de verificación |
| `POST` | `/api/mesa-partes/expedientes/{id}/requisitos/{reqId}/verificar` | Operador revisor | Calificar requisito (CONFORME, OBSERVADO, NO_APLICA) |
| `POST` | `/api/mesa-partes/expedientes/{id}/recibir` | `MESAPARTES_EXPEDIENTE_RECIBIR` | Registrar recepción formal de expediente presentado |
| `POST` | `/api/mesa-partes/expedientes/{id}/observaciones` | `MESAPARTES_EXPEDIENTE_OBSERVAR` | Registrar observación (INTERNA o CIUDADANO) y requerir subsanación |
| `GET` | `/api/mesa-partes/expedientes/{id}/observaciones` | Autenticado / Anti-IDOR | Consultar observaciones (ciudadano solo ve visibilidad CIUDADANO) |
| `POST` | `/api/mesa-partes/expedientes/{id}/prioridad` | `MESAPARTES_EXPEDIENTE_PRIORIDAD` | Actualizar prioridad (NORMAL, ALTA, URGENTE) con bitácora |
| `GET` | `/api/mesa-partes/expedientes/{id}/constancia-recepcion` | Autenticado / Interesado | Descargar constancia oficial de recepción en PDF con QR |

---

## 24. MÓDULO 1 — MESA DE PARTES VIRTUAL: DERIVACIÓN DE EXPEDIENTES, ASIGNACIÓN Y SEGUIMIENTO INTERNO (PROMPT 15)

Implementado en el marco del **Prompt 15 de 150** para dotar a la Municipalidad Distrital de Quinua de un sistema de circulación documental interna, derivación controlada entre oficinas, recepción y devolución justificada, asignación y reasignación de trabajadores responsables, registro de actuaciones administrativas internas con documentos anexos (SHA-256), seguimiento 360°, mapa de recorrido administrativo entre oficinas, semáforo de plazos LPAG y panel directivo del jefe de oficina.

### 1. Diagrama de Flujo de Derivación, Recepción, Asignación y Actuaciones
```mermaid
graph TD
    MP[Mesa de Partes General] -->|1. Derivar con motivo obligatorio| DER[Expediente en estado DERIVADO<br/>Derivación en estado PENDIENTE]
    DER -->|2. Rechazar o Devolver con Justificación| DEV[Derivación DEVUELTA<br/>Expediente retorna a RECIBIDO en oficina origen]
    DER -->|2. Aceptar Recepción| REC[Derivación RECIBIDA<br/>Expediente en EN_ATENCION en oficina destino]
    REC -->|3. Jefe Asigna Responsable| ASIG[Asignación ACTIVA<br/>Responsable Actual asignado]
    ASIG -->|4. Reasignación Justificada| REASIG[Historial actualizado<br/>Nuevo Responsable Asignado]
    ASIG -->|5. Registrar Actuaciones| ACT[Actuación Administrativa<br/>Informes, Memorandos, Evaluaciones SHA-256]
    ACT -->|6. Derivar a Siguiente Oficina| DER
    ACT -->|7. Seguimiento 360°| SEG[Línea de tiempo + Mapa de Recorrido + Control de Plazos]
```

### 2. Diferenciación Conceptual de Estados
Se garantiza la estricta separación de conceptos según las directivas del Sistema de Gestión Documental:
- **Estado del Expediente (`EstadoExpediente`):** `PRESENTADO`, `RECIBIDO`, `EN_REVISION`, `DERIVADO`, `EN_ATENCION`, `OBSERVADO`, `EN_SUBSANACION`, `RESUELTO`, `NOTIFICADO`, `ARCHIVADO`, `ANULADO`.
- **Estado de la Derivación (`EstadoDerivacion`):** `PENDIENTE`, `RECIBIDA`, `RECHAZADA`, `DEVUELTA`, `FINALIZADA`, `CANCELADA`.
- **Estado de la Asignación (`EstadoAsignacion`):** `ACTIVA`, `FINALIZADA`, `REASIGNADA`, `CANCELADA`.

### 3. Circulación Documental y Control de Concurrencia
- **Validación de Reglas de Negocio:**
  - No se permite derivar a la misma oficina de origen (HTTP 422).
  - No se permite derivación duplicada si ya existe una derivación `PENDIENTE` para el mismo expediente (HTTP 422).
  - Verificación estricta de que la oficina de destino esté activa en el catálogo institucional.
  - Bloqueo optimista mediante columna `@Version` en `mpv_derivaciones` y `mpv_asignaciones`.
  - Reversión consistente en una única transacción: si una derivación es devuelta o rechazada, el expediente retorna a la oficina de origen en estado `RECIBIDO` con trazabilidad completa en `mpv_expediente_historial` y `mpv_derivacion_eventos`.

### 4. Asignación y Reasignación de Trabajadores Responsables
- **Asignación (`POST /api/mesa-partes/expedientes/{id}/asignar`):**
  - Validación de que el trabajador esté habilitado y pertenezca activamente a la oficina responsable (`persona_oficinas`).
  - Actualización del campo `responsable_actual_id` en `mpv_expedientes`.
  - Notificación en tiempo real al trabajador con tipo `TRAMITE` y correo electrónico informativo.
- **Reasignación (`POST /api/mesa-partes/expedientes/{id}/reasignar`):**
  - Cierre formal de la asignación anterior (`fechaFin`, estado `REASIGNADA`).
  - Creación de la nueva asignación con auditoría de motivo, usuario reasignador y fechas.
  - Notificación simultánea al nuevo responsable y al responsable cesante.

### 5. Actuaciones Administrativas Internas (`mpv_actuaciones`)
- Tipos tipificados: `REVISION`, `INFORME`, `EVALUACION`, `OBSERVACION_INTERNA`, `SOLICITUD_INFORMACION`, `REMISION`, `OTRA`.
- Visibilidad dual (`INTERNA` vs `CIUDADANO`).
- Vinculación documental inmutable a través de `mpv_actuacion_documentos` con cálculo de hash SHA-256 e integración con el servicio central de almacenamiento (`DocumentStorageService`).

### 6. Seguimiento 360°, Línea de Tiempo y Recorrido Administrativo
- **Línea de Tiempo Integral:** Consolida en estricto orden cronológico los movimientos del expediente, derivaciones, eventos de recepción, asignaciones de personal y actuaciones administrativas.
- **Mapa de Recorrido Administrativo:** Generación secuencial de los nodos del trayecto entre unidades orgánicas (p. ej. *Mesa de Partes General* → *Gerencia de Desarrollo Urbano* → *Subgerencia de Obras Privadas*), indicando oficina actual, responsable y fecha de salida/entrada.
- **Semáforo de Plazos Legales (LPAG Ley N° 27444):** Cálculo automatizado de días transcurridos y restantes excluyendo días no laborables y feriados mediante `PlazoAdministrativoService`, con estados: `SIN_PLAZO_CONFIGURADO`, `EN_PLAZO`, `PROXIMO_VENCIMIENTO`, `VENCIDO`, `SUSPENDIDO`.

### 7. Vistas Web Thymeleaf + Tailwind CSS
- `/dashboard/mesa-partes/derivaciones`: Bandeja general de derivaciones emitidas y en trámite.
- `/dashboard/mesa-partes/derivaciones/recibidas`: Bandeja de recepción para despachos de oficina (con modales para Aceptar, Devolver y Rechazar).
- `/dashboard/mesa-partes/mis-expedientes`: Bandeja personal del trabajador público con sus expedientes asignados.
- `/dashboard/mesa-partes/panel-oficina`: Panel directivo del jefe de oficina con balance de carga por servidor, expedientes sin responsable y pendientes de asignación.
- `/dashboard/mesa-partes/expedientes/{id}/seguimiento`: Visor 360° del expediente con línea de tiempo interactiva, mapa de recorrido y registro de actuaciones.

### 8. Endpoints REST (`/api/mesa-partes/**`)
| Método | Endpoint | Permiso Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mesa-partes/derivaciones` | `MESAPARTES_DERIVACION_VER` | Lista paginada de derivaciones con filtros |
| `POST` | `/api/mesa-partes/expedientes/{id}/derivar` | `MESAPARTES_DERIVACION_CREAR` | Registrar derivación de expediente a otra oficina |
| `GET` | `/api/mesa-partes/derivaciones/recibidas` | `MESAPARTES_DERIVACION_VER` | Listar derivaciones recibidas en la oficina del usuario |
| `GET` | `/api/mesa-partes/derivaciones/{id}` | `MESAPARTES_DERIVACION_VER` | Obtener detalle de una derivación |
| `POST` | `/api/mesa-partes/derivaciones/{id}/recibir` | `MESAPARTES_DERIVACION_RECIBIR` | Aceptar formalmente recepción de derivación |
| `POST` | `/api/mesa-partes/derivaciones/{id}/rechazar` | `MESAPARTES_DERIVACION_RECHAZAR` | Rechazar derivación con motivo obligatorio |
| `POST` | `/api/mesa-partes/derivaciones/{id}/devolver` | `MESAPARTES_DERIVACION_DEVOLVER` | Devolver derivación a oficina remitente |
| `POST` | `/api/mesa-partes/expedientes/{id}/asignar` | `MESAPARTES_ASIGNACION_CREAR` | Asignar trabajador responsable de la oficina |
| `POST` | `/api/mesa-partes/expedientes/{id}/reasignar` | `MESAPARTES_ASIGNACION_MODIFICAR` | Reasignar expediente a otro trabajador |
| `GET` | `/api/mesa-partes/mis-expedientes` | Autenticado | Consultar expedientes asignados al usuario autenticado |
| `POST` | `/api/mesa-partes/expedientes/{id}/actuaciones` | `MESAPARTES_ACTUACION_CREAR` | Registrar actuación administrativa interna |
| `GET` | `/api/mesa-partes/expedientes/{id}/actuaciones` | `MESAPARTES_SEGUIMIENTO_VER` | Listar actuaciones administrativas del expediente |
| `GET` | `/api/mesa-partes/expedientes/{id}/seguimiento` | `MESAPARTES_SEGUIMIENTO_VER` | Obtener ficha de seguimiento 360° |
| `GET` | `/api/mesa-partes/derivaciones/estadisticas` | `MESAPARTES_ESTADISTICAS_OFICINA` | Consultar métricas agregadas de derivación |
| `GET` | `/api/mesa-partes/derivaciones/panel-oficina` | `MESAPARTES_ESTADISTICAS_OFICINA` | Panel directivo del jefe de oficina |
| `GET` | `/api/mesa-partes/oficinas-activas` | Autenticado | Catálogo de oficinas disponibles para derivación |
| `GET` | `/api/mesa-partes/oficinas/{id}/trabajadores`| Autenticado | Trabajadores activos habilitados por oficina |

---

## 25. MÓDULO 1 — MESA DE PARTES VIRTUAL: ATENCIÓN, RESOLUCIÓN, NOTIFICACIÓN Y CIERRE DE EXPEDIENTES (PROMPT 16)

Implementado en el marco del **Prompt 16 de 150** para dotar a la Municipalidad Distrital de Quinua del ciclo completo de sustanciación, evaluación técnica/legal, formulación y revisión de propuestas de decisión, emisión formal de actos administrativos resolutivos, notificación conforme a la Ley del Procedimiento Administrativo General (LPAG Ley N° 27444), cierre formal de expedientes, archivo en custodia física/digital y procedimiento extraordinario de reapertura administrativa.

### 1. Diagrama de Flujo del Procedimiento Administrativo de Atención y Resolución
```mermaid
graph TD
    EXP[Expediente en Trámite<br/>RECIBIDO o DERIVADO] -->|1. Iniciar Atención Formal| ATN[Estado: EN_ATENCION<br/>Fecha de Inicio Registrada]
    ATN -->|2. Conducir Evaluaciones| EVAL[Evaluación Técnica / Legal<br/>mpv_evaluaciones + Informes SHA-256]
    EVAL -->|3. Finalizar Análisis| PROP[Formulación de Propuesta de Decisión<br/>Proyectista Especialista]
    PROP -->|4. Elevar a Despacho| REV[Propuesta en Revisión<br/>Jefe de Unidad Orgánica]
    REV -->|5a. Observar / Solicitar Ajustes| PROP
    REV -->|5b. Separación Estricta de Funciones<br/>Aprobar Propuesta| APR[Propuesta APROBADA_INTERNAMENTE]
    APR -->|6. Autoridad Competente Formaliza Decisión| DEC[Decisión Administrativa Registrada<br/>Correlativo Oficial Reservado]
    DEC -->|7. Compilar PDF Institucional| PDF[Documento Oficial en PDF con Código QR<br/>Membrete, Vistos, Considerandos, Resuelve]
    PDF -->|8. Registrar Emisión / Firma Formal| RES[Estado: RESUELTO<br/>Firma Manuscrita o Digital Acreditada]
    RES -->|9. Generar Cédula de Notificación| NOTIF[Cédula / Aviso de Notificación<br/>Casilla Electrónica, Personal o Correo]
    NOTIF -->|10. Validar Acuse de Notificación LPAG| ACUSE[Estado: NOTIFICADO<br/>Acuse de Recibo Probatorio y Válido]
    ACUSE -->|11. Proceder al Cierre Administrativo| CIE[Cierre Formal del Expediente<br/>mpv_cierres]
    CIE -->|12. Remisión a Archivo Central| ARC[Estado: ARCHIVADO<br/>Código de Archivador, Estante, Caja, Tomo]
    ARC -.->|13. Reapertura Extraordinaria Justificada| REAP[Reapertura Administrativa<br/>Resolución de Alcaldía / Mandato Judicial]
    REAP -.-> ATN
```

### 2. Principios de Legalidad y Separación Funcional (LPAG Ley N° 27444)
1. **Separación Estricta de Funciones:** El servidor proyectista que elabora la propuesta de decisión técnica no puede auto-aprobarse ni formalizar el acto resolutivo. La revisión y aprobación final está reservada exclusivamente a la autoridad competente (Jefe de Oficina o Gerente Municipal).
2. **Sin Simulaciones de Firmas:** No se generan firmas electrónicas automáticas arbitrarias. El sistema permite compilar el documento formal, registrar la evidencia de la firma manuscrita/digital debidamente estampada por la autoridad competente y dejar trazabilidad fidedigna.
3. **Validez Legal de la Notificación:** Se distingue taxativamente entre un correo electrónico de cortesía meramente informativo y una **notificación administrativa con eficacia legal**. La notificación solo transiciona a legalmente válida cuando se acredita la entrega efectiva mediante acuse de recibo firmado, constancia de lectura en casilla electrónica o acta de visita domiciliaria con firma del receptor o testigo.
4. **Cierre y Custodia Estricta:** No existen cierres automáticos ni arbitrarios. El cierre exige resolución emitida, notificación formalmente perfeccionada o acto de desistimiento/abandono documentado.

### 3. Modelo de Datos y Migración Flyway (`V19__mpv_atencion_resolucion_cierre.sql`)
- `mpv_evaluaciones`: Evaluaciones técnicas, legales o colegiadas del expediente con estado (`BORRADOR`, `FINALIZADA`).
- `mpv_evaluacion_documentos`: Anexos, planos e informes técnicos evaluados con hash criptográfico SHA-256.
- `mpv_propuestas_decision`: Propuestas de decisión elaboradas por el personal sustanciador (`BORRADOR`, `EN_REVISION`, `APROBADA_INTERNAMENTE`, `OBSERVADA`, `RECHAZADA`).
- `mpv_revision_propuestas`: Bitácora inmutable de revisiones jerárquicas con observaciones y fechas.
- `mpv_decisiones`: Actos resolutivos institucionales (`RESOLUCION_ALCALDIA`, `RESOLUCION_GERENCIAL`, `RESOLUCION_SUBGERENCIAL`, `DECRETO`, `OFICIO_RESPUESTA`) con numeración correlativa oficial.
- `mpv_correlativos_documentales`: Control transaccional correlativo por tipo de acto, año fiscal y unidad orgánica emisora.
- `mpv_notificaciones_administrativas`: Cédulas de notificación, intentos de entrega, acuse de recibo y acreditación de validez legal LPAG.
- `mpv_cierres`: Actas de conclusión de expediente administrativo con motivo legal tipificado.
- `mpv_archivos`: Registro de remisión a custodia pasiva con ubicación física (estante, nivel, caja, archivador, tomo/folio).
- `mpv_reaperturas`: Procedimiento extraordinario de reactivación de expedientes archivados con justificación y acto administrativo habilitante.

### 4. Generación de Resoluciones en PDF y Código QR Institucional
- Motor OpenPDF con diseño municipal formal:
  - Escudo institucional y membrete oficial de la Municipalidad Distrital de Quinua.
  - Título formal y número correlativo oficial generado por `NumeracionDocumentalService`.
  - Estructura LPAG: **VISTO**, **CONSIDERANDO** (motivación fáctica y fundamentos de derecho) y **SE RESUELVE** (parte resolutiva expresa).
  - Pie de página institucional con código QR generado en tiempo real que apunta a la verificación pública de autenticidad.

### 5. Catálogo de Permisos Granulares Sembrados
| Permiso | Acción | Descripción |
| :--- | :--- | :--- |
| `MESAPARTES_ATENCION_VER` | `VIEW` | Ver expedientes en sustanciación y bandejas de trabajo |
| `MESAPARTES_ATENCION_INICIAR` | `UPDATE` | Iniciar formalmente la atención de expedientes |
| `MESAPARTES_EVALUACION_CREAR` | `CREATE` | Registrar evaluaciones técnicas y adjuntar informes |
| `MESAPARTES_EVALUACION_FINALIZAR` | `UPDATE` | Finalizar y cerrar dictámenes técnicos |
| `MESAPARTES_PROPUESTA_CREAR` | `CREATE` | Proyectar propuestas de decisión administrativa |
| `MESAPARTES_PROPUESTA_REVISAR` | `UPDATE` | Examinar propuestas elevadas al despacho |
| `MESAPARTES_PROPUESTA_APROBAR` | `APPROVE` | Aprobar internamente proyectos de resolución |
| `MESAPARTES_DECISION_EMITIR` | `CREATE` | Registrar decisiones y actos administrativos resolutivos |
| `MESAPARTES_DOCUMENTO_GENERAR` | `CREATE` | Confeccionar resoluciones oficiales en PDF |
| `MESAPARTES_DOCUMENTO_FORMALIZAR` | `UPDATE` | Registrar la expedición formal y firma de resoluciones |
| `MESAPARTES_NOTIFICACION_GESTIONAR` | `MANAGE` | Practicar y validar formalmente notificaciones LPAG |
| `MESAPARTES_EXPEDIENTE_CERRAR` | `UPDATE` | Declarar concluido el procedimiento administrativo |
| `MESAPARTES_EXPEDIENTE_ARCHIVAR` | `UPDATE` | Enviar expedientes al archivo pasivo institucional |
| `MESAPARTES_EXPEDIENTE_REABRIR` | `UPDATE` | Reaperturas extraordinarias por mandato normativo |

### 6. Catálogo de Endpoints REST de Atención, Resolución y Cierre (`/api/mesa-partes/**`)
| Método | Endpoint | Permiso Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/mesa-partes/expedientes/{id}/iniciar-atencion` | `MESAPARTES_ATENCION_INICIAR` | Iniciar formalmente la atención del expediente |
| `GET` | `/api/mesa-partes/expedientes/{id}/atencion` | `MESAPARTES_ATENCION_VER` | Obtener estado detallado y plazos de atención |
| `GET` | `/api/mesa-partes/bandeja-atencion` | `MESAPARTES_ATENCION_VER` | Bandeja paginada de expedientes en atención por oficina |
| `POST` | `/api/mesa-partes/expedientes/{id}/evaluaciones` | `MESAPARTES_EVALUACION_CREAR` | Registrar nueva evaluación técnica/legal en borrador |
| `GET` | `/api/mesa-partes/expedientes/{id}/evaluaciones` | `MESAPARTES_ATENCION_VER` | Listar evaluaciones practicadas en el expediente |
| `POST/PUT` | `/api/mesa-partes/evaluaciones/{id}/finalizar` | `MESAPARTES_EVALUACION_FINALIZAR` | Finalizar y cerrar evaluación técnica |
| `POST` | `/api/mesa-partes/evaluaciones/{id}/documentos` | `MESAPARTES_EVALUACION_CREAR` | Adjuntar informe o plano a la evaluación (SHA-256) |
| `POST` | `/api/mesa-partes/expedientes/{id}/propuestas` | `MESAPARTES_PROPUESTA_CREAR` | Proyectar propuesta de decisión administrativa |
| `GET` | `/api/mesa-partes/expedientes/{id}/propuestas` | `MESAPARTES_PROPUESTA_REVISAR` | Listar propuestas de decisión del expediente |
| `GET` | `/api/mesa-partes/propuestas` | `MESAPARTES_PROPUESTA_REVISAR` | Bandeja paginada de despacho para revisión de propuestas |
| `POST` | `/api/mesa-partes/propuestas/{id}/enviar-revision`| `MESAPARTES_PROPUESTA_CREAR` | Elevar propuesta a revisión de la jefatura |
| `POST` | `/api/mesa-partes/propuestas/{id}/revisar` | `MESAPARTES_PROPUESTA_APROBAR` | Aprobar u observar internamente la propuesta |
| `POST` | `/api/mesa-partes/expedientes/{id}/decisiones` | `MESAPARTES_DECISION_EMITIR` | Registrar acto formal resolutivo (número correlativo) |
| `GET` | `/api/mesa-partes/expedientes/{id}/decisiones` | `MESAPARTES_ATENCION_VER` | Listar actos resolutivos emitidos |
| `POST` | `/api/mesa-partes/decisiones/{id}/generar-documento`| `MESAPARTES_DOCUMENTO_GENERAR` | Compilar documento PDF oficial con QR institucional |
| `POST` | `/api/mesa-partes/decisiones/{id}/registrar-emision` | `MESAPARTES_DOCUMENTO_FORMALIZAR` | Registrar emisión formal; expediente pasa a RESUELTO |
| `GET` | `/api/mesa-partes/decisiones/{id}/descargar` | Autenticado | Descargar archivo binario de la resolución oficial |
| `POST` | `/api/mesa-partes/expedientes/{id}/notificaciones` | `MESAPARTES_NOTIFICACION_GESTIONAR` | Generar cédula de notificación administrativa |
| `GET` | `/api/mesa-partes/expedientes/{id}/notificaciones` | `MESAPARTES_ATENCION_VER` | Listar cédulas y notificaciones del trámite |
| `POST` | `/api/mesa-partes/notificaciones/{id}/intento` | `MESAPARTES_NOTIFICACION_GESTIONAR` | Registrar intento de entrega o envío de aviso |
| `POST` | `/api/mesa-partes/notificaciones/{id}/acreditar` | `MESAPARTES_NOTIFICACION_GESTIONAR` | Acreditar acuse legal de notificación; pasa a NOTIFICADO |
| `POST` | `/api/mesa-partes/expedientes/{id}/cerrar` | `MESAPARTES_EXPEDIENTE_CERRAR` | Proceder al cierre administrativo formal |
| `POST` | `/api/mesa-partes/expedientes/{id}/archivar` | `MESAPARTES_EXPEDIENTE_ARCHIVAR` | Enviar a archivo pasivo (estante, caja, tomo) |
| `POST` | `/api/mesa-partes/expedientes/{id}/reabrir` | `MESAPARTES_EXPEDIENTE_REABRIR` | Reapertura extraordinaria por resolución justificada |
| `GET` | `/api/mesa-partes/atencion/estadisticas` | `MESAPARTES_ATENCION_VER` | Estadísticas agregadas del ciclo de sustanciación |

### 7. Vistas Web Integradas (Thymeleaf + Tailwind CSS)
- `/dashboard/mesa-partes/atencion`: Bandeja principal de sustanciación con filtros por oficina, estado, plazos de atención y acceso rápido a gestión.
- `/dashboard/mesa-partes/atencion/{id}`: Centro de control 360° de sustanciación con 6 módulos operativos:
  1. Estado actual y control de plazos LPAG.
  2. Dictámenes y evaluaciones técnicas con informes adjuntos.
  3. Formulación y elevación de propuestas de decisión.
  4. Decisiones y actos administrativos resolutivos con visor y descarga de PDF.
  5. Cédulas de notificación administrativa y acreditación de acuses legales.
  6. Acciones directivas de cierre, remisión a archivo pasivo y reapertura legal.
- `/dashboard/mesa-partes/propuestas`: Bandeja directiva para jefes y gerentes con modales para aprobación y observaciones de propuestas elevadas.
- `/dashboard/mesa-partes/decisiones/{id}/descargar-pdf`: Generador y visor directo de resoluciones oficiales con código QR.

---

## 25. Seguimiento Ciudadano, Consulta de Expedientes y Verificación Documental (Prompt 17)

El Módulo de Mesa de Partes Virtual implementa el portal público y privado de seguimiento ciudadano, consulta de expedientes mediante identificadores seguros de alta entropía y el servicio público de verificación documental con validación de código QR e integridad criptográfica SHA-256.

### 1. Portal de Seguimiento Ciudadano y Consulta Pública Limitada
- **Consulta Pública (`/mesa-partes/seguimiento`):**
  - Permite a cualquier ciudadano o administrado verificar el estado y progreso general de su expediente ingresando su **Código Seguro de Seguimiento** (`SEG-XXXXXXXX`).
  - **Protección de Datos Personales (Ley N° 29733):** La consulta anónima no expone DNI, domicilio, teléfono, correo ni nombres completos del administrado, limitándose al número de expediente, tipo de trámite, oficina responsable, fecha de presentación, estado ciudadano traducido y la barra de progreso de 6 etapas.
  - **Prevención de Enumeración y Fuerza Bruta:** Control de tasa de solicitudes (máximo 30 consultas cada 5 minutos por IP) y bloqueo temporal del código tras 5 intentos fallidos consecutivos.
- **Detalle Integral para Administrados Autenticados (`/mesa-partes/seguimiento/{id}`):**
  - Validación estricta de titularidad (**Anti-IDOR**): solo el ciudadano titular o el personal administrativo autorizado puede consultar el expediente completo.
  - Muestra datos generales del administrado, oficina responsable, fechas clave (presentación, recepción, plazo legal restante en días hábiles).
  - Herramienta para copiar al portapapeles el código seguro de seguimiento.
  - Enlace directo a la descarga de la Constancia Oficial de Presentación en PDF.

### 2. Códigos Seguros de Seguimiento
- **Generación Criptográficamente Segura:** Prefijo institucional `SEG-` seguido de 8 caracteres alfanuméricos no correlativos de alta entropía generados con `SecureRandom` excluyendo caracteres ambiguos (`23456789ABCDEFGHJKLMNPQRSTUVWXYZ`).
- **Almacenamiento Protegido:** Hash criptográfico SHA-256 almacenado en `mpv_codigos_seguimiento`.
- **Gestión del Ciclo de Vida:** Control de caducidad (12 meses por defecto), revocación administrativa con motivo registrado y rotación segura en caso de sospecha de compromiso.

### 3. Barra de Progreso y Línea de Tiempo Ciudadana
- **Barra de Progreso Oficial (6 Etapas):**
  1. *Presentado:* Solicitud ingresada por el ciudadano mediante el portal.
  2. *Recibido:* Revisado preliminarmente y admitido con número correlativo oficial.
  3. *En Revisión:* En examen de requisitos por la unidad orgánica competente.
  4. *En Atención:* En proceso de evaluación técnica y formulación de resolución.
  5. *Resuelto:* Acto resolutivo formal emitido y formalizado.
  6. *Cerrado:* Procedimiento concluido y remitido al archivo institucional.
  - Se adapta transparentemente ante estados alternativos (`OBSERVADO`, `EN_SUBSANACION`, `REABIERTO`) sin fingir porcentajes lineales ficticios.
- **Línea de Tiempo Ciudadana Transparente:**
  - Visualización cronológica de hitos administrativos autorizados (presentación, recepción, derivación comunicable, observaciones notificadas, subsanaciones, resolución y cierre).
  - Ocultación garantizada de nombres de funcionarios internos, notas reservadas y comentarios de gestión interna.

### 4. Observaciones, Subsanaciones y Documentos Disponibles
- **Observaciones Notificadas:** Visualización exclusiva de requerimientos destinados al ciudadano (`VisibilidadObservacion.CIUDADANO`), con indicación de requisito observado, fecha de notificación, fecha límite, días hábiles restantes y botón directo para subsanar dentro del plazo legal.
- **Subsanaciones Presentadas:** Historial de respuestas y recaudos presentados por el ciudadano con estado de revisión.
- **Documentos Oficiales Disponibles:** Tabla de documentos con hash SHA-256 de integridad, tipo documental, fecha de emisión y enlace seguro de descarga (`/api/mesa-partes/mis-solicitudes/{id}/documentos/{docId}/descargar`) protegido con control de acceso por titularidad.

### 5. Verificación Pública de Documentos Oficiales y Validación QR
- **Página Pública (`/verificar-documento`):**
  - Permite verificar cualquier constancia o acto resolutivo emitido por la Municipalidad Distrital de Quinua mediante código alfanumérico o lectura de código QR.
  - Muestra ficha técnica mínima: tipo documental, número de documento, fecha de emisión, entidad emisora, funcionario emisor, estado (`VIGENTE`, `ANULADO`) y huella SHA-256.
  - **Aviso Legal Expreso:** Aclara que la verificación acredita la autenticidad e integridad registral en los sistemas de la Municipalidad Distrital de Quinua conforme al TUO de la Ley N° 27444, no sustituyendo una firma digital con certificado digital salvo que el documento cuente con acreditación conforme a la Ley N° 27269.
  - No expone datos sensibles ni permite la descarga del archivo binario a terceros no autenticados.

### 6. Estructura de Base de Datos (Migración Flyway `V20__mpv_seguimiento_verificacion.sql`)
- `mpv_codigos_seguimiento`: Códigos seguros, hash SHA-256, vigencia, contador de intentos fallidos, bloqueos y auditoría de accesos.
- `mpv_verificaciones_documentales`: Registro de constancias y resoluciones emitidas con código de verificación, token QR, hash SHA-256, estado documental (`VIGENTE`, `ANULADO`) y contador de consultas.
- `mpv_consultas_seguimiento`: Pista inmutable de auditoría para consultas de seguimiento y verificación (IP, User-Agent, criterio, resultado y tiempo de respuesta en ms).
- Modificación en `mpv_expedientes`: incorporación de columna `codigo_seguimiento` indexada de forma única.

### 7. Permisos Granulares Sembrados (Prompt 17)
| Permiso | Acción | Descripción |
| :--- | :--- | :--- |
| `MESAPARTES_SEGUIMIENTO_CIUDADANO` | `VIEW` | Consultar el avance integral, observaciones y documentos de expedientes propios |
| `MESAPARTES_DOCUMENTO_VERIFICAR` | `VIEW` | Verificar la autenticidad e integridad de documentos municipales emitidos |

### 8. Catálogo de Endpoints REST y Web
| Método | Endpoint | Acceso / Permiso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/mesa-partes/seguimiento` | Público | Página web de consulta ciudadana por código seguro |
| `POST` | `/api/publico/mesa-partes/consultar` | Público (Rate-Limited) | API pública de avance y progreso mínimo |
| `GET` | `/verificar-documento` | Público | Página web de verificación documental y QR |
| `POST` | `/api/publico/documentos/verificar` | Público (Rate-Limited) | API pública de verificación documental y hash SHA-256 |
| `GET` | `/mesa-partes/seguimiento/{id}` | Autenticado (Titular) | Vista web integral de seguimiento para el administrado |
| `GET` | `/api/mesa-partes/mis-solicitudes` | `MESAPARTES_SOLICITUD_CONSULTAR` | Bandeja paginada de solicitudes con códigos de seguimiento |
| `GET` | `/api/mesa-partes/mis-solicitudes/{id}/seguimiento` | `MESAPARTES_SEGUIMIENTO_CIUDADANO` | API de detalle integral de seguimiento (Anti-IDOR) |
| `GET` | `/api/mesa-partes/mis-solicitudes/{id}/historial` | `MESAPARTES_SEGUIMIENTO_CIUDADANO` | API de línea de tiempo comunicable para el ciudadano |
| `GET` | `/api/mesa-partes/mis-solicitudes/{id}/documentos` | `MESAPARTES_SEGUIMIENTO_CIUDADANO` | API de documentos oficiales disponibles para descarga |
| `GET` | `/api/mesa-partes/mis-solicitudes/{id}/documentos/{docId}/descargar` | `MESAPARTES_SEGUIMIENTO_CIUDADANO` | Descarga protegida de archivo documental oficial |
| `GET` | `/api/mesa-partes/mis-solicitudes/{id}/constancia` | Autenticado (Titular) | Descarga directa de Constancia Oficial de Recepción en PDF |


---

## 26. Mesa de Partes Virtual: Reportes, Estadísticas, Indicadores, Excel, PDF y Supervisión Municipal (Prompt 18)

El módulo de **Reportes, Estadísticas y Supervisión Municipal** de Mesa de Partes Virtual proporciona inteligencia de negocios y analítica administrativa institucional para la Municipalidad Distrital de Quinua. El sistema calcula indicadores basados en información real de PostgreSQL, distinguiendo métricas de flujo y de existencias, aplicando control de acceso Anti-IDOR y permitiendo la exportación profesional a hojas de cálculo Excel (Apache POI) y documentos PDF (OpenPDF).

### 1. Arquitectura de Servicios y Capas
- **`ReporteMesaPartesService`:** Consolidación de resumen estadístico de estados, bandeja paginada de expedientes con filtros avanzados y resolución de ámbito de oficina.
- **`IndicadoresMesaPartesService`:** Cálculo de indicadores de eficiencia institucional (tiempo promedio, tiempo mediano, porcentajes de resolución, observación y pendientes) e indicadores de plazos legales según el TUO de la Ley N° 27444.
- **`EstadisticasExpedienteService`:** Generación de estructuras de datos para gráfico de estados y evolución mensual (12 meses o rango cronológico real por eventos de presentación, resolución y cierre).
- **`EstadisticasOficinaService`:** Agregación institucional de expedientes recibidos, en atención, resueltos y pendientes por unidad orgánica.
- **`EstadisticasTramiteService`:** Distribución de carga documental y porcentajes por tipo de trámite oficial.
- **`SupervisionMesaPartesService`:** Monitoreo de flujo de derivaciones inter-oficinas, trazabilidad de observaciones/subsanaciones ciudadanas y agregación de productividad operativa institucional (evitando rankings individuales punitivos).
- **`ExportacionExcelService`:** Generación de libros XLSX de alta fidelidad con Apache POI, paleta slate institucional, congelación de paneles, autosize y protección contra inyección de fórmulas (`=`, `+`, `-`, `@`).
- **`ExportacionPdfService`:** Generación de reportes ejecutivos en formato PDF apaisado (A4 Landscape) con OpenPDF, membrete institucional, tarjetas resumen KPI, tabla paginada con alternancia de filas y pie de página con aviso legal.

### 2. Panel de Supervisión Web y Frontend Responsive
- **Ruta Web Principal:** `/dashboard/mesa-partes/reportes` (con alias `/dashboard/mesa-partes/estadisticas` y `/dashboard/mesa-partes/supervision`).
- **Diseño Institucional Oscuro:** Paleta corporativa `#101827` (fondo), `#1e293b` (sidebar y tablas), `#202c3d` (tarjetas KPI), `#1d4ed8` (primario azul) y `#e2e8f0` (texto).
- **Librería de Iconos:** Lucide Icons (bundle JavaScript local `data-lucide`, sin emojis como iconos).
- **Interactividad JavaScript Modular:**
  - `reportes-filtros.js`: Manejo de selectores de período (`MES_ACTUAL`, `MES_ANTERIOR`, `TRIMESTRE_ACTUAL`, `ANIO_ACTUAL`, `PERSONALIZADO`), rango de fechas, oficina, tipo de trámite, estado y canal.
  - `reportes-graficos.js`: Renderizado de gráficos interactivos de dona/barras y evolución cronológica.
  - `reportes.js`: Orquestador con loaders asíncronos, debounce en búsquedas, tabs de navegación y botones de exportación Excel / PDF con retroalimentación inmediata.

### 3. Fórmulas y Métricas de Gestión Institucional
- **Métricas de Existencias (Stock) vs Flujo:** Se distingue con rigor entre expedientes ingresados en el período (flujo) y expedientes que permanecen activos o pendientes de ejercicios anteriores (stock).
- **Tiempo Promedio de Atención:** $\frac{\sum (\text{Fecha Cierre} - \text{Fecha Presentación})}{\text{Total de Expedientes Resueltos}}$ (en días calendario transcurridos).
- **Tiempo Mediano de Atención:** Cálculo estadístico exacto sobre la serie ordenada de días transcurridos para eliminar distorsiones causadas por valores atípicos (outliers).
- **Porcentaje de Resolución:** $\frac{\text{Expedientes Resueltos}}{\text{Total Atendibles en Período}} \times 100$.
- **Porcentaje de Observación:** $\frac{\text{Expedientes con Observación}}{\text{Total Atendibles en Período}} \times 100$.
- **Manejo Seguro de División por Cero:** En caso de no existir registros concluidos, el sistema indica explícitamente `datosSuficientes: false` sin arrojar errores aritméticos ni mostrar valores espurios.

### 4. Indicadores de Plazos Administrativos (TUO Ley N° 27444)
- **En Plazo:** Expedientes en trámite cuya fecha límite legal es superior a la fecha actual y faltan más de 3 días para vencer.
- **Próximos a Vencer:** Expedientes en trámite que se encuentran a 3 o menos días de su fecha límite legal.
- **Vencidos:** Expedientes en trámite que han superado el plazo legal sin acto resolutivo.
- **Suspendidos:** Expedientes con observaciones pendientes de subsanación ciudadana (plazo suspendido de conformidad con la normativa).
- **Sin Plazo Configurado:** Trámites que no cuentan con plazo legal tasado en el TUPA.

### 5. Exportaciones Seguras a Excel y PDF
- **Protección contra Inyección de Fórmulas en Excel (CSV/Formula Injection):** Todo campo de texto ingresado por usuarios o administrados (asunto, nombre del solicitante, número de documento, etc.) es evaluado; si inicia con `=`, `+`, `-` o `@`, se neutraliza anteponiendo un apóstrofe `'` (`ReporteFilterHelper.sanitizarParaExcel`).
- **Tope de Volumen:** Se establecen límites de exportación de hasta 10,000 registros para XLSX y 2,000 registros para PDF para salvaguardar la memoria y estabilidad del servidor.
- **Auditoría de Exportaciones:** Cada descarga genera un registro inmutable en `mpv_reporte_exportaciones` con usuario solicitante, tipo de reporte, formato, filtros aplicados, cantidad de registros, IP, tiempo de generación y resultado.

### 6. Estructura de Base de Datos (Migración Flyway `V21__mpv_reportes_estadisticas_supervision.sql`)
- `mpv_reporte_exportaciones`: Tabla de auditoría de descargas con usuario, formato (`EXCEL`, `PDF`), filtros JSON, IP, User-Agent, duración en ms y estado.
- Índices de rendimiento agregados:
  - `idx_mpv_exp_reportes_busqueda`: `(estado_actual, fecha_presentacion, oficina_responsable_id)`.
  - `idx_mpv_exp_reportes_tramite`: `(tipo_tramite_id, fecha_presentacion)`.
  - `idx_mpv_exp_reportes_cierre`: `(fecha_cierre, oficina_responsable_id)`.
  - `idx_mpv_deriv_reportes`: `(oficina_destino_id, estado, fecha_derivacion)`.
  - `idx_mpv_obs_reportes`: `(expediente_id, fecha_registro)`.

### 7. Matriz de Permisos Granulares
| Permiso | Acción | Descripción |
| :--- | :--- | :--- |
| `MESAPARTES_REPORTES_VER` | `VIEW` | Visualizar el panel de reportes y estadísticas administrativas |
| `MESAPARTES_REPORTES_OFICINA` | `VIEW` | Consultar métricas y expedientes de la unidad orgánica propia |
| `MESAPARTES_REPORTES_GLOBALES` | `VIEW` | Consultar reportes institucionales globales y de todas las oficinas |
| `MESAPARTES_REPORTES_EXPORTAR` | `EXPORT` | Exportar reportes administrativos a formatos Excel (XLSX) y PDF |
| `MESAPARTES_ESTADISTICAS_VER` | `VIEW` | Consultar dashboards cuantitativos, evolución mensual e indicadores |
| `MESAPARTES_SUPERVISION_VER` | `VIEW` | Supervisar derivaciones, observaciones y carga institucional agregada |

### 8. Catálogo de Endpoints REST y Web
| Método | Endpoint | Permiso Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/dashboard/mesa-partes/reportes` | `MESAPARTES_REPORTES_VER` | Panel interactivo web de supervisión y reportes |
| `GET` | `/api/mesa-partes/reportes/resumen` | `MESAPARTES_REPORTES_VER` | Resumen cuantitativo por estados para tarjetas KPI |
| `GET` | `/api/mesa-partes/reportes/indicadores-eficiencia` | `MESAPARTES_REPORTES_VER` | Promedio, mediana de atención y ratios de resolución |
| `GET` | `/api/mesa-partes/reportes/plazos` | `MESAPARTES_REPORTES_VER` | Indicadores de cumplimiento legal y semáforo de plazos |
| `GET` | `/api/mesa-partes/reportes/por-estado` | `MESAPARTES_REPORTES_VER` | Distribución de expedientes para gráfico de dona/barras |
| `GET` | `/api/mesa-partes/reportes/por-oficina` | `MESAPARTES_REPORTES_VER` | Métricas de expedientes por unidad orgánica |
| `GET` | `/api/mesa-partes/reportes/por-tramite` | `MESAPARTES_REPORTES_VER` | Distribución y porcentajes por tipo de trámite |
| `GET` | `/api/mesa-partes/reportes/evolucion-mensual` | `MESAPARTES_REPORTES_VER` | Serie temporal mensual de expedientes presentados y resueltos |
| `GET` | `/api/mesa-partes/reportes/derivaciones` | `MESAPARTES_SUPERVISION_VER` | Análisis institucional de derivaciones inter-oficinas |
| `GET` | `/api/mesa-partes/reportes/observaciones` | `MESAPARTES_SUPERVISION_VER` | Trazabilidad de observaciones y subsanaciones ciudadanas |
| `GET` | `/api/mesa-partes/reportes/productividad` | `MESAPARTES_SUPERVISION_VER` | Productividad operativa y pendientes por oficina |
| `GET` | `/api/mesa-partes/reportes/comparativo` | `MESAPARTES_REPORTES_VER` | Comparación porcentual y absoluta entre períodos |
| `GET` | `/api/mesa-partes/reportes/expedientes` | `MESAPARTES_REPORTES_VER` | Tabla paginada y filtrable de expedientes detallados |
| `GET` | `/api/mesa-partes/reportes/exportar/excel` | `MESAPARTES_REPORTES_EXPORTAR` | Descarga de reporte en formato Excel XLSX |
| `GET` | `/api/mesa-partes/reportes/exportar/pdf` | `MESAPARTES_REPORTES_EXPORTAR` | Descarga de reporte institucional en formato PDF |

## 27. Mesa de Partes Virtual: Configuración Administrativa, Catálogo de Trámites, Requisitos, Plazos y Formularios Dinámicos (Prompt 19)

El módulo de **Configuración Administrativa de Mesa de Partes Virtual** proporciona al personal directivo y de gestión documental de la Municipalidad Distrital de Quinua el control centralizado, flexible y trazable de los procedimientos administrativos, requisitos documentales, formularios dinámicos, plazos legales, calendarios no laborables, correlativos anuales y plantillas oficiales. Cumple de manera rigurosa con la Ley N° 27972 (Ley Orgánica de Municipalidades), el TUO de la Ley N° 27444 (Ley del Procedimiento Administrativo General) y el Decreto Legislativo N° 1203 / D.S. N° 004-2019-JUS sobre el TUPA digital.

### 1. Arquitectura de Servicios y Dominio
El módulo implementa una arquitectura modular desacoplada con 9 servicios especializados:
- **`CatalogoTramitesService`:** Gestión integral del ciclo de vida de los tipos de trámite: creación en borrador, actualización de metadatos TUPA, pase a revisión técnica, aprobación formal, publicación con versión inmutable, suspensión temporal y desactivación.
- **`RequisitosTramiteService`:** Administración granular de requisitos documentales por trámite (obligatoriedad, formatos MIME permitidos, tamaño máximo en MB, orden de prelación y estado activo/inactivo).
- **`AdministracionFormulariosService`:** Diseñador y versionador de esquemas JSON de formularios dinámicos asociados a trámites, soporte de vista previa en tiempo real y versionado inmutable al publicar.
- **`ConfiguracionPlazosService`:** Configuración de plazos normativos en días hábiles o calendario, tipo de silencio administrativo (`POSITIVO`, `NEGATIVO`, `NO_APLICA`) y base legal asociada.
- **`CalendarioAdministrativoService`:** Mantenimiento de días no laborables, feriados nacionales, días cívico-religiosos distritales de Quinua (e.g. 9 de Diciembre - Batalla de Quinua) y decretos de días no laborables con recálculo dinámico en el conteo de plazos.
- **`ConfiguracionNumeracionService`:** Parametrización del correlativo anual institucional (`EXP-{ANIO}-{CORRELATIVO}`), reinicio automático o manual por ejercicio fiscal, prefijos y sufijos oficiales.
- **`PlantillasDocumentalesService`:** Repositorio de plantillas documentales (`DECRETO_INICIO`, `CARGO_RECEPCION`, `OFICIO_OBSERVACION`, `RESOLUCION_FINAL`) con soporte de versionado y tokens dinámicos (`{NUMERO_EXPEDIENTE}`, `{SOLICITANTE_NOMBRE}`, `{FECHA_INGRESO}`, etc.).
- **`ConfiguracionHistorialService`:** Registro de auditoría administrativa inmutable con trazabilidad de usuario, IP, motivo justificado, cambios JSON y transición de versiones.
- **`PlazoAdministrativoService` (Integración):** Actualizado para consultar dinámicamente `CalendarioAdministrativoRepository` e ignorar fines de semana y días no laborables configurados en la base de datos municipal.

### 2. Ciclo de Vida y Versionado Inmutable de Trámites
Para garantizar la seguridad jurídica del administrado y de la entidad, los trámites siguen una máquina de estados estricta. Todo expediente histórico mantiene inmutable la versión del trámite con la que fue radicado.

```mermaid
stateDiagram-v2
    [*] --> BORRADOR: Creación inicial
    BORRADOR --> EN_REVISION: Envío a revisión técnica
    EN_REVISION --> BORRADOR: Retorno por observaciones
    EN_REVISION --> APROBADO: Aprobación formal
    APROBADO --> PUBLICADO: Publicación oficial (Genera TramiteVersion inmutable)
    PUBLICADO --> SUSPENDIDO: Suspensión temporal (por contingencia municipal)
    SUSPENDIDO --> PUBLICADO: Reactivación
    PUBLICADO --> BORRADOR: Nueva versión (Incrementa versión menor/mayor)
    PUBLICADO --> DESACTIVADO: Desactivación definitiva
    DESACTIVADO --> [*]
```

### 3. Panel de Administración Web Responsive
- **Ruta Web Principal:** `/dashboard/mesa-partes/configuracion` (con subrutas directas `/tramites`, `/requisitos`, `/formularios`, `/plazos`, `/calendario`, `/numeracion`, `/plantillas`, `/historial`).
- **Diseño Dark Slate Institucional:** Paleta de alto contraste `#101827` (fondo primario), `#1e293b` (superficies y tarjetas), `#202c3d` (módulos y encabezados), `#1d4ed8` (acentos y acciones primarias) y `#e2e8f0` (tipografía legible).
- **Librería de Iconos:** Lucide Icons cargado localmente (`data-lucide`, sin emojis como iconos).
- **Constructor Visual de Formularios:** Diseñador intuitivo para añadir dinámicamente campos (texto, número, email, teléfono, select, radio, checkbox, textarea, fecha, archivo), definir validaciones y generar esquemas JSON estándar.
- **Seguridad Frontend:** Integración transparente con tokens CSRF en peticiones AJAX/Fetch y modales de confirmación obligatoria para publicación y suspensión.

### 4. Estructura de Base de Datos (Migración Flyway `V22__mpv_configuracion_tramites_plazos_formularios.sql`)
1. **Ampliación de `mpv_tipos_tramite`:** Columnas `codigo_tupa`, `version_actual`, `estado_catalogo`, `tasa_tramitacion`, `cuenta_bancaria_recaudacion`, `permite_canal_virtual`, `permite_canal_presencial`, `publicado_por`, `fecha_publicacion`, `aprobado_por`, `fecha_aprobacion`.
2. **`mpv_tramite_versiones`:** Historial de versiones publicadas con snapshot de requisitos JSON, esquema de formulario JSON, plazos y tasa al momento de publicación.
3. **`mpv_calendarios_administrativos`:** Registro de feriados nacionales y días cívicos locales con año, fecha, tipo (`FERIADO_NACIONAL`, `CIVICO_LOCAL`, `NO_LABORABLE_COMPENSABLE`), recurrencia anual y estado.
4. **`mpv_plantillas_documentales`:** Gestión de plantillas con código, tipo documental, título, cuerpo con tokens dinámicos, versión y estado.
5. **`mpv_configuracion_historial`:** Auditoría de configuración con entidad (`TRAMITE`, `REQUISITO`, `FORMULARIO`, `PLAZO`, `CALENDARIO`, `PLANTILLA`, `NUMERACION`), tipo de cambio (`CREACION`, `MODIFICACION`, `REVISION`, `ENVIADO_REVISION`, `APROBACION`, `PUBLICACION`, `SUSPENSION`, `DESACTIVACION`, `VERSIONADO`), motivo justificado, IP y diferencias JSON.
6. **Integración con `configuracion_sistema`:** Parámetros institucionales `MPV_EXP_PREFIJO`, `MPV_EXP_LONGITUD_CORRELATIVO` y `MPV_EXP_REINICIO_ANUAL`.

### 5. Matriz de Permisos Granulares de Configuración
| Permiso | Acción | Descripción |
| :--- | :--- | :--- |
| `MESAPARTES_CONFIG_VER` | `VIEW` | Acceso al panel general y pestañas de configuración de MPV |
| `MESAPARTES_TRAMITE_CREAR` | `CREATE` | Crear nuevos tipos de trámite en estado borrador |
| `MESAPARTES_TRAMITE_EDITAR` | `UPDATE` | Modificar datos generales, plazos y tasas de trámites en borrador |
| `MESAPARTES_TRAMITE_REVISAR` | `MANAGE` | Enviar trámite a revisión técnica o devolver a borrador |
| `MESAPARTES_TRAMITE_APROBAR` | `APPROVE` | Aprobar técnicamente un trámite revisado |
| `MESAPARTES_TRAMITE_PUBLICAR` | `APPROVE` | Publicar trámite en catálogo oficial y generar versión inmutable |
| `MESAPARTES_TRAMITE_SUSPENDER` | `MANAGE` | Suspender temporalmente o reactivar trámites |
| `MESAPARTES_REQUISITO_GESTIONAR` | `MANAGE` | Crear, editar y ordenar requisitos documentales de trámites |
| `MESAPARTES_FORMULARIO_GESTIONAR` | `MANAGE` | Diseñar, guardar y versionar formularios dinámicos |
| `MESAPARTES_PLAZOS_CONFIGURAR` | `UPDATE` | Configurar días hábiles, régimen de silencio y base legal |
| `MESAPARTES_CALENDARIO_GESTIONAR` | `MANAGE` | Gestionar días no laborables y feriados locales en Quinua |
| `MESAPARTES_NUMERACION_CONFIGURAR` | `UPDATE` | Parametrizar correlativos, prefijos y secuencias de expedientes |
| `MESAPARTES_PLANTILLAS_GESTIONAR` | `MANAGE` | Crear y editar plantillas oficiales de documentos |
| `MESAPARTES_HISTORIAL_VER` | `VIEW` | Consultar la auditoría completa de cambios de configuración |
| `MESAPARTES_VERSIONES_VER` | `VIEW` | Ver histórico y snapshots inmutables de versiones publicadas |

### 6. Catálogo de Endpoints REST y Web
| Método | Endpoint | Permiso Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/dashboard/mesa-partes/configuracion` | `MESAPARTES_CONFIG_VER` | Vista web del panel centralizado con pestañas |
| `GET` | `/api/mesa-partes/configuracion/tramites` | `MESAPARTES_CONFIG_VER` | Listar catálogo paginado con filtros de estado y búsqueda |
| `POST` | `/api/mesa-partes/configuracion/tramites` | `MESAPARTES_TRAMITE_CREAR` | Registrar nuevo tipo de trámite en borrador |
| `PUT` | `/api/mesa-partes/configuracion/tramites/{id}` | `MESAPARTES_TRAMITE_EDITAR` | Actualizar borrador de trámite |
| `POST` | `/api/mesa-partes/configuracion/tramites/{id}/enviar-revision` | `MESAPARTES_TRAMITE_REVISAR` | Pasar trámite a estado revisión técnica |
| `POST` | `/api/mesa-partes/configuracion/tramites/{id}/aprobar` | `MESAPARTES_TRAMITE_APROBAR` | Aprobar trámite revisado |
| `POST` | `/api/mesa-partes/configuracion/tramites/{id}/publicar` | `MESAPARTES_TRAMITE_PUBLICAR` | Publicar trámite y generar versión inmutable |
| `POST` | `/api/mesa-partes/configuracion/tramites/{id}/suspender` | `MESAPARTES_TRAMITE_SUSPENDER` | Suspender temporalmente trámite publicado |
| `GET` | `/api/mesa-partes/configuracion/requisitos/tramite/{id}` | `MESAPARTES_CONFIG_VER` | Listar requisitos documentales de un trámite |
| `POST` | `/api/mesa-partes/configuracion/requisitos` | `MESAPARTES_REQUISITO_GESTIONAR` | Crear nuevo requisito documental |
| `PUT` | `/api/mesa-partes/configuracion/requisitos/{id}` | `MESAPARTES_REQUISITO_GESTIONAR` | Modificar requisito existente |
| `GET` | `/api/mesa-partes/configuracion/formularios/tramite/{id}` | `MESAPARTES_CONFIG_VER` | Obtener esquema de formulario dinámico de un trámite |
| `POST` | `/api/mesa-partes/configuracion/formularios` | `MESAPARTES_FORMULARIO_GESTIONAR` | Guardar o actualizar versión de formulario dinámico |
| `PUT` | `/api/mesa-partes/configuracion/plazos/tramite/{id}` | `MESAPARTES_PLAZOS_CONFIGURAR` | Actualizar plazos, tipo de silencio y base legal |
| `GET` | `/api/mesa-partes/configuracion/calendario` | `MESAPARTES_CONFIG_VER` | Listar días feriados y no laborables por año |
| `POST` | `/api/mesa-partes/configuracion/calendario` | `MESAPARTES_CALENDARIO_GESTIONAR` | Registrar o actualizar día no laborable |
| `GET` | `/api/mesa-partes/configuracion/numeracion` | `MESAPARTES_CONFIG_VER` | Consultar configuración y vista previa de numeración |
| `PUT` | `/api/mesa-partes/configuracion/numeracion` | `MESAPARTES_NUMERACION_CONFIGURAR` | Modificar parámetros de correlativo institucional |
| `GET` | `/api/mesa-partes/configuracion/plantillas` | `MESAPARTES_CONFIG_VER` | Listar plantillas documentales oficiales |
| `POST` | `/api/mesa-partes/configuracion/plantillas` | `MESAPARTES_PLANTILLAS_GESTIONAR` | Crear o actualizar plantilla documental con versión |
| `GET` | `/api/mesa-partes/configuracion/historial` | `MESAPARTES_HISTORIAL_VER` | Consultar auditoría inmutable de cambios con filtros |

---

## 21. Módulo 1 — Mesa de Partes Virtual: Integración Final, Pruebas E2E y Cierre del Módulo (Prompt 20)

### 1. Resumen Ejecutivo de Cierre
El **Módulo 1: Mesa de Partes Virtual** de SIGM-QUINUA ha sido formalmente integrado, probado y cerrado técnica y funcionalmente. El módulo opera sobre **PostgreSQL 17** (`sigm_quinua`), garantizando la trazabilidad institucional completa y el cumplimiento riguroso del **Texto Único Ordenado de la Ley N° 27444 (Ley del Procedimiento Administrativo General)** y los lineamientos de interoperabilidad y gobierno digital de la Presidencia del Consejo de Ministros (PCM) del Perú.

### 2. Diagrama de Estados del Ciclo de Vida del Expediente (Mermaid)
```mermaid
stateDiagram-v2
    [*] --> BORRADOR: Registro por Administrado
    BORRADOR --> PRESENTADO: Presentación formal (Atómica + Idempotencia)
    PRESENTADO --> RECIBIDO: Recepción conforme por Mesa de Partes
    PRESENTADO --> OBSERVADO: Notificación de Reparo documental (10 días)
    OBSERVADO --> EN_SUBSANACION: Subsanación presentada por Administrado
    EN_SUBSANACION --> RECIBIDO: Levantamiento de observación validado
    RECIBIDO --> DERIVADO: Derivación a Unidad Orgánica competente
    DERIVADO --> RECIBIDO: Aceptación de pase inter-oficinas
    RECIBIDO --> EN_ATENCION: Asignación a Especialista Técnico e Inicio formal
    EN_ATENCION --> EN_EVALUACION: Actuaciones y Evaluación técnica
    EN_EVALUACION --> EN_REVISION: Propuesta de Decisión elevada a Jefatura
    EN_REVISION --> RESUELTO: Aprobación interna y Emisión formal de Resolución
    RESUELTO --> NOTIFICADO: Notificación electrónica / física con constancia legal
    NOTIFICADO --> CERRADO: Cierre administrativo motivado (LPAG)
    CERRADO --> ARCHIVADO: Custodia permanente en Archivo Central Municipal
    ARCHIVADO --> [*]
```

### 3. Diagrama de Componentes de Arquitectura
```mermaid
graph TD
    subgraph Portal_Ciudadano [Portal Ciudadano Responsivo]
        A1[Registro y Borrador] --> A2[Adjuntos y Formularios Dinámicos]
        A2 --> A3[Presentación e Idempotencia]
        A3 --> A4[Constancia PDF y Consulta Mis Solicitudes]
        A4 --> A5[Seguimiento Ciudadano y Verificación QR]
    end

    subgraph Mesa_Partes_Virtual [Bandeja y Recepción Institucional]
        B1[Bandeja de Entrada] --> B2[Revisión de Requisitos TUPA]
        B2 --> B3[Sello de Recepción Digital]
        B2 --> B4[Emisión de Observaciones Formales]
    end

    subgraph Gestion_Interna [Gestión Orgánica y Trámite Documentario]
        C1[Derivación Inter-oficinas] --> C2[Recepción en Dependencia Destino]
        C2 --> C3[Asignación de Especialista]
        C3 --> C4[Actuaciones Administrativas]
        C4 --> C5[Evaluación y Propuesta de Decisión]
        C5 --> C6[Revisión y Aprobación Jefatural]
        C6 --> C7[Emisión Formal de Actos Administrativos]
    end

    subgraph Cierre_Archivo [Cierre, Notificación y Custodia]
        D1[Notificación al Administrado] --> D2[Cierre Administrativo LPAG]
        D2 --> D3[Remisión a Archivo Central y Foliado]
    end

    subgraph Inteligencia_Supervision [Supervisión y Reportes]
        E1[Tarjetas KPI y Doughnut de Estados] --> E2[Control de Plazos Ley 27444]
        E2 --> E3[Exportación Excel XLSX POI]
        E2 --> E4[Exportación PDF Apaisado OpenPDF]
    end

    Portal_Ciudadano --> Mesa_Partes_Virtual
    Mesa_Partes_Virtual --> Gestion_Interna
    Gestion_Interna --> Cierre_Archivo
    Gestion_Interna --> Inteligencia_Supervision
```

### 4. Matriz de Permisos Institucionales por Rol de Mesa de Partes
| Rol Institucional | Código de Rol | Permisos Principales | Competencia y Alcance |
| :--- | :--- | :--- | :--- |
| **Ciudadano / Administrado** | `ROLE_CIUDADANO` | `MESAPARTES_EXPEDIENTE_VER_PROPIO`, `MESAPARTES_EXPEDIENTE_SUBSANAR` | Acceso restringido exclusivamente a sus propios expedientes (Anti-IDOR estricto) |
| **Operador de Mesa de Partes** | `ROLE_MESA_PARTES` / `ROLE_OPERADOR` | `MESAPARTES_BANDEJA`, `MESAPARTES_EXPEDIENTE_RECIBIR`, `MESAPARTES_EXPEDIENTE_OBSERVAR`, `MESAPARTES_DERIVACION_CREAR` | Recepción, validación formal de requisitos TUPA, observación y derivación inicial |
| **Jefe de Unidad Orgánica** | `ROLE_JEFE_OFICINA` | `MESAPARTES_DERIVACION_RECIBIR`, `MESAPARTES_ASIGNACION_CREAR`, `MESAPARTES_PROPUESTA_APROBAR`, `MESAPARTES_DECISION_EMITIR`, `MESAPARTES_EXPEDIENTE_CERRAR`, `MESAPARTES_EXPEDIENTE_ARCHIVAR` | Aceptación de derivaciones, asignación funcional, aprobación de resoluciones y cierre |
| **Especialista / Servidor Técnico** | `ROLE_OPERADOR` / `ROLE_TRABAJADOR` | `MESAPARTES_ATENCION_INICIAR`, `MESAPARTES_PROPUESTA_CREAR`, `MESAPARTES_ACTUACION_CREAR`, `MESAPARTES_ATENCION_VER` | Sustanciación del trámite, inspecciones, actuaciones y formulación de propuestas |
| **Administrador del Sistema** | `ROLE_ADMIN` / `ROLE_SUPERADMIN` | `MESAPARTES_CONFIG_VER`, `MESAPARTES_CONFIGURAR_TRAMITES`, `MESAPARTES_REPORTES_VER`, `MESAPARTES_REPORTES_EXPORTAR`, `MESAPARTES_SUPERVISION_VER` | Supervisión general, configuración de TUPA, formularios, calendario y reportes ejecutivos |

### 5. Suite de Pruebas E2E (`MesaPartesLifecycleE2ETest`)
Verificación de los 8 escenarios de extremo a extremo que recorren el ciclo documental completo:
- **Escenario A (Solicitud Ciudadana):** Borrador dinámico, asociación de documentos, presentación atómica, correlativo único institucional (`MPV-2026-XXXXXX` / `EXP-2026-XXXXXX`), constancia PDF y consulta en *Mis Solicitudes*.
- **Escenario B (Recepción):** Consulta de bandeja, revisión de admisibilidad TUPA, sello de recepción digital y constancia oficial de recepción.
- **Escenario C (Observación y Subsanación):** Notificación de reparo administrativo con plazo legal, consulta ciudadana del requerimiento y subsanación con documentos adjuntos.
- **Escenario D (Derivación y Asignación):** Validación de oficinas activas, pase inter-oficinas, recepción de pase por jefatura de destino, asignación técnica a especialista y registro de actuación.
- **Escenario E (Atención y Resolución):** Inicio formal de atención, propuesta técnica motivada, elevación a revisión, aprobación interna de jefatura, emisión formal de acto resolutivo, generación de documento PDF y formalización con libro oficial.
- **Escenario F (Notificación, Cierre y Archivo):** Cédula de notificación con casilla electrónica, validación de estado resuelto para cierre por atención concluida (LPAG), y remisión física con codificación al Archivo Central Municipal.
- **Escenario G (Seguimiento Ciudadano y Verificación QR):** Ficha integral de seguimiento ciudadano, línea de tiempo comunicable, vista pública de verificación y validación criptográfica de autenticidad documental (GET/POST).
- **Escenario H (Reportes y Supervisión):** Indicadores cuantitativos de estado, cálculo de plazos de ley (TUO Ley 27444), exportación oficial a Excel XLSX con Apache POI y reporte ejecutivo a PDF apaisado con LibrePDF.

### 6. Métricas de Calidad y Resultados de Ejecución
- **Total de Pruebas Automatizadas:** 399 pruebas (100% pasando, 0 fallos, 0 errores, 0 omitidas).
- **Compilación y Empaquetado:** JAR ejecutable generado exitosamente (`sigm-quinua-0.0.1-SNAPSHOT.jar`).
- **Base de Datos:** PostgreSQL 17 conectado, Flyway migrado limpiamente hasta la versión V22.
- **Seguridad:** Control estricto RBAC, Anti-IDOR en todas las capas, tokens JWT en cookies HttpOnly seguras, protección CSRF y sanitización de fórmulas Excel.

---

## 22. Módulo 2 — Mercados Municipales: Arquitectura y Base de Datos Inicial (Prompt 21)

### 1. Resumen de Arquitectura del Módulo
El **Módulo 2: Mercados Municipales** de SIGM-QUINUA implementa la infraestructura de datos, servicios transaccionales, seguridad y presentación para la gestión del padrón de comerciantes, puestos comerciales, sectores, pabellones, asignaciones contractuales y autorizaciones municipales.

El diseño sigue el principio de **reutilización del núcleo institucional**:
- **Personas:** Los comerciantes se asocian mediante clave foránea a la entidad central `Persona` (`personas`), admitiendo tanto personas naturales (DNI) como jurídicas (RUC y representante legal) sin duplicar datos personales ni padrones de contacto.
- **Usuarios y Autenticación:** Se reutiliza `Usuario` y `Spring Security`. Un comerciante puede o no tener credenciales de sistema; cuando se habilite el portal de comerciantes, se vinculará a la cuenta existente sin duplicar tablas de acceso.
- **Oficinas Administradoras:** Cada mercado municipal se vincula formalmente con la oficina municipal correspondiente de la estructura orgánica institucional (`Oficina`).
- **Gestión Documental:** Reutilización de `Documento` para el sustento legal de asignaciones y futuras autorizaciones.
- **Auditoría:** Todas las entidades implementan `AuditableEntity` y las operaciones relevantes se registran en `AuditoriaAdministrativaService`.

### 2. Diagrama Entidad-Relación (Mermaid)
```mermaid
erDiagram
    OFICINA ||--o{ MERCADO_MUNICIPAL : "administra"
    MERCADO_MUNICIPAL ||--o{ SECTOR_MERCADO : "contiene"
    MERCADO_MUNICIPAL ||--o{ PABELLON_MERCADO : "contiene"
    MERCADO_MUNICIPAL ||--o{ PUESTO_COMERCIAL : "alberga"
    SECTOR_MERCADO ||--o{ PABELLON_MERCADO : "agrupa (opcional)"
    SECTOR_MERCADO ||--o{ PUESTO_COMERCIAL : "clasifica (opcional)"
    PABELLON_MERCADO ||--o{ PUESTO_COMERCIAL : "ubica (opcional)"
    TIPO_PUESTO_COMERCIAL ||--o{ PUESTO_COMERCIAL : "tipifica"

    PERSONA ||--o| COMERCIANTE : "se vincula a"
    COMERCIANTE ||--o{ COMERCIANTE_ACTIVIDAD : "ejerce"
    ACTIVIDAD_COMERCIAL ||--o{ COMERCIANTE_ACTIVIDAD : "clasifica"

    PUESTO_COMERCIAL ||--o{ ASIGNACION_PUESTO : "es asignado en"
    COMERCIANTE ||--o{ ASIGNACION_PUESTO : "recibe"
    DOCUMENTO ||--o| ASIGNACION_PUESTO : "sustenta"

    PUESTO_COMERCIAL ||--o{ HISTORIAL_OCUPACION : "registra historial"
    COMERCIANTE ||--o{ HISTORIAL_OCUPACION : "figura en historial"
    USUARIO ||--o{ HISTORIAL_OCUPACION : "responsable"

    COMERCIANTE ||--o{ AUTORIZACION_COMERCIAL : "obtiene"
    PUESTO_COMERCIAL ||--o| AUTORIZACION_COMERCIAL : "asocia (opcional)"
    DOCUMENTO ||--o| AUTORIZACION_COMERCIAL : "documento oficial"

    MERCADO_MUNICIPAL {
        uuid id PK
        string codigo UK
        string nombre
        string direccion
        string referencia_ubicacion
        string distrito
        string provincia
        string departamento
        decimal latitud
        decimal longitud
        integer capacidad_referencial
        string estado
        uuid oficina_administradora_id FK
    }

    SECTOR_MERCADO {
        uuid id PK
        uuid mercado_id FK
        string codigo
        string nombre
        string tipo_sector
        string estado
        integer orden_visual
    }

    PABELLON_MERCADO {
        uuid id PK
        uuid mercado_id FK
        uuid sector_id FK
        string codigo
        string nombre
        string nivel
        string estado
        integer orden_visual
    }

    PUESTO_COMERCIAL {
        uuid id PK
        uuid mercado_id FK
        uuid sector_id FK
        uuid pabellon_id FK
        string codigo
        string numero
        decimal area_m2
        string estado
        uuid tipo_puesto_id FK
    }

    COMERCIANTE {
        uuid id PK
        uuid persona_id FK,UK
        string codigo_comerciante UK
        date fecha_registro
        string estado
        text observaciones
    }

    ASIGNACION_PUESTO {
        uuid id PK
        uuid puesto_id FK
        uuid comerciante_id FK
        date fecha_inicio
        date fecha_fin
        string estado
        string tipo_asignacion
        uuid documento_sustento_id FK
    }

    HISTORIAL_OCUPACION {
        uuid id PK
        uuid puesto_id FK
        uuid comerciante_id FK
        string tipo_movimiento
        timestamp fecha_movimiento
        uuid usuario_responsable_id FK
        string estado_anterior
        string estado_nuevo
    }
```

### 3. Migración Flyway (V23)
- **Archivo:** `V23__mercados_municipales_arquitectura_inicial.sql`.
- **Tablas:**
  - `mercados_mercados`: Mercados municipales con código institucional único y referencia geoespacial.
  - `mercados_sectores`: Sectores dentro del mercado (alimentos, abarrotes, artesanías, etc.) con unicidad `(mercado_id, codigo)`.
  - `mercados_pabellones`: Pabellones o naves físicas por mercado y sector opcional.
  - `mercados_tipos_puesto`: Catálogo de tipos de puesto (puesto fijo, módulo, local, espacio temporal).
  - `mercados_puestos`: Puestos comerciales con código único por mercado, área en m² (check `area_m2 > 0`) y estado físico/operativo.
  - `mercados_actividades`: Catálogo institucional de actividades comerciales.
  - `mercados_comerciantes`: Padrón de comerciantes vinculado a `Persona`.
  - `mercados_comerciante_actividades`: Asociación comerciante - actividades comerciales.
  - `mercados_asignaciones`: Asignaciones contractuales de puestos a comerciantes.
  - `mercados_historial_ocupacion`: Historial inmutable de movimientos y cambios de ocupación.
  - `mercados_autorizaciones`: Estructura para futuras autorizaciones comerciales municipales.
- **Control PostgreSQL de Ocupaciones Vigentes Simultáneas:**
  ```sql
  CREATE UNIQUE INDEX idx_uq_puesto_asignacion_vigente 
  ON mercados_asignaciones(puesto_id) 
  WHERE estado = 'VIGENTE';
  ```
  Garantiza a nivel de base de datos relacional la imposibilidad de solapamiento de dos ocupaciones activas en el mismo puesto.

### 4. Permisos de Seguridad Institucionales
| Permiso | Descripción |
| :--- | :--- |
| `MERCADOS_MODULO_ACCEDER` | Acceso al módulo y visualización del Dashboard |
| `MERCADOS_MERCADO_VER` | Consulta de mercados municipales |
| `MERCADOS_MERCADO_CREAR` | Creación y registro de nuevo mercado |
| `MERCADOS_MERCADO_EDITAR` | Actualización de datos de mercado |
| `MERCADOS_SECTOR_GESTIONAR` | Administración de sectores |
| `MERCADOS_PABELLON_GESTIONAR` | Administración de pabellones |
| `MERCADOS_PUESTO_VER` | Consulta de puestos y disponibilidad |
| `MERCADOS_PUESTO_GESTIONAR` | Registro y mantenimiento de puestos |
| `MERCADOS_COMERCIANTE_VER` | Consulta del padrón de comerciantes |
| `MERCADOS_COMERCIANTE_GESTIONAR` | Registro y actualización de comerciantes |
| `MERCADOS_ASIGNACION_VER` | Consulta de asignaciones y contratos de puestos |
| `MERCADOS_ASIGNACION_GESTIONAR` | Creación, suspensión y finalización de asignaciones |

### 5. Endpoints y Vistas
- **Vista Web:** `GET /dashboard/mercados` (Dashboard institucional con métricas reales y estado vacío).
- **REST API:**
  - `GET /api/mercados` — Listado paginado de mercados.
  - `GET /api/mercados/{id}` — Detalle de un mercado.
  - `GET /api/mercados/{id}/sectores` — Sectores del mercado.
  - `GET /api/mercados/{id}/pabellones` — Pabellones del mercado.
  - `GET /api/mercados/{id}/puestos` — Puestos comerciales con filtro por estado.
  - `GET /api/mercados/comerciantes` — Padrón paginado de comerciantes.
  - `POST /api/mercados` — Creación de mercado con validación y auditoría.
  - `POST /api/mercados/comerciantes` — Registro de comerciante vinculado a persona.
  - `POST /api/mercados/puestos/{puestoId}/asignaciones` — Asignación transaccional con historial.

---

## 23. Módulo 2 — Mercados Municipales: Registro y Administración Integral (Prompt 22)

El Prompt 22 implementa la administración integral de mercados municipales para la Municipalidad Distrital de Quinua, incluyendo ciclo de vida operativo, auditoría inmutable, control de concurrencia optimista, gestión documental y reportes de exportación:

### 1. Migración de Base de Datos Flyway (`V24__mercados_administracion_avanzada.sql`)
- **Control de Cambios de Estado (`mercados_cambios_estado`):**
  - Registro histórico inmutable de transiciones de estado operativo (`estado_anterior`, `estado_nuevo`, `motivo`, `usuario_responsable_id`, `usuario_username`, `fecha_cambio`).
  - Índices B-tree optimizados: `idx_mercados_cambios_mercado` y `idx_mercados_cambios_fecha`.
- **Asociación de Documentos Institucionales (`mercados_documentos`):**
  - Enlace con el módulo transversal de documentos (`documento_id` FK a `documentos_comunes`).
  - Categorización por `tipo_documento`: `RESOLUCION_CREACION`, `REGLAMENTO_INTERNO`, `PLANO_DISTRIBUCION`, `CERTIFICADO_DEFENSA_CIVIL`, `INSPECCION_SANITARIA`, `OTRO`.
  - Índice compuesto único `idx_uq_mercado_documento` y check `chk_mercado_documento_tamano`.
- **Permisos de Seguridad Nuevos:**
  - `MERCADOS_MERCADO_CAMBIAR_ESTADO`: Autoriza la modificación del estado operativo con motivo obligatorio.
  - `MERCADOS_MERCADO_EXPORTAR`: Autoriza la descarga de padrones y reportes en Excel y PDF.
  - `MERCADOS_MERCADO_DOCUMENTOS`: Autoriza la asociación y visualización de expedientes institucionales.
  - `MERCADOS_MERCADO_HISTORIAL_VER`: Autoriza la auditoría cronológica de cambios operativos.

### 2. Capacidades de Negocio y Reglas Operativas
1. **Registro y Validación Integral:**
   - Código alfanumérico normalizado en mayúsculas (`trim().toUpperCase()`) con validación de duplicidad (`DuplicateResourceException`).
   - Coordenadas geográficas validadas en rangos geodésicos estándar: Latitud [-90.0, 90.0], Longitud [-180.0, 180.0].
   - Capacidad referencial entera no negativa (`>= 0`).
   - Validación y asociación con oficinas activas de la municipalidad (`Oficina`).
2. **Edición con Concurrencia Optimista (`@Version`):**
   - Detección de colisiones mediante anotación `@Version private Long version;`.
   - Manejo de `OptimisticLockingFailureException` y emisión de `ConflictException` (HTTP 409) con advertencia de recarga de datos concurrentes.
3. **Máquina de Estados Operativos:**
   - Estados: `ACTIVO`, `INACTIVO`, `EN_MANTENIMIENTO`, `SUSPENDIDO`.
   - Validación contra cambios idempotentes (mismo estado).
   - Verificación de asignaciones contractuales vigentes previo a transiciones críticas, retornando advertencias sin bloqueo destructivo.
   - Historial inmutable de trazabilidad por usuario autenticado.
4. **Métricas en Tiempo Real:**
   - Estadísticas globales y resúmenes de puestos (`total`, `disponibles`, `ocupados`, `reservados`, `enMantenimiento`, `inhabilitados`) agregados por consultas nativas optimizadas de PostgreSQL.
5. **Exportación Blindada:**
   - **Excel (Apache POI):** Protección contra inyección de fórmulas (Formula Injection / CSV Injection) mediante escape de caracteres de riesgo (`=`, `+`, `-`, `@`, `\t`, `\r`) anteponiendo apóstrofe `'`. Encabezados estilizados con la paleta municipal `#1e293b`.
   - **PDF (LibrePDF):** Formato apaisado (Landscape A4) con paginación dinámica, metadatos institucionales y tabla estructurada.

### 3. Vistas Thymeleaf y Experiencia Frontend
- **Diseño Visual Institucional:** Implementado en Tailwind CSS con paleta oscura (#101827 fondo general, #1e293b tarjetas, #202c3d bordes, #1d4ed8 acento primario, #e2e8f0 tipografía, #334155 estados neutros) y Lucide Icons (sin emojis como íconos).
- **Rutas Web:**
  - `GET /dashboard/mercados/listado`: Tabla con búsqueda en vivo, filtros combinados (estado, oficina, fechas), selector de ordenamiento seguro por whitelist y paginación reactiva.
  - `GET /dashboard/mercados/nuevo`: Formulario de registro con geolocalización, capacidad y selector de oficina.
  - `GET /dashboard/mercados/{id}`: Vista detallada con 5 pestañas operativas (Información General, Sectores y Pabellones, Puestos Comerciales, Documentos Institucionales, Historial Operativo).
  - `GET /dashboard/mercados/{id}/editar`: Formulario de edición con versión oculta para concurrencia.
  - Modal reactivo de cambio de estado (`modal-estado.html`) con selector de estado y justificación técnica obligatoria.

### 4. Cobertura de Pruebas Automatizadas (30/30 - 100% Exitosas)
1. **Pruebas de Servicio (`MercadoMunicipalAdministrationServiceTest` - 25 puntos):**
   - `test01_crearMercadoValido`: Creación de mercado con campos válidos y valores por defecto.
   - `test02_rechazarCodigoDuplicado`: Rechazo de código existente con `DuplicateResourceException`.
   - `test03_rechazarNombreVacio`: Validación de nombre obligatorio.
   - `test04_validarDireccionObligatoria`: Validación de dirección física obligatoria.
   - `test05_validarCoordenadasFueraDeRango`: Rechazo de latitudes/longitudes fuera de rango geodésico.
   - `test06_validarCapacidadNoNegativa`: Rechazo de capacidad referencial negativa.
   - `test07_asociarOficinaExistenteYActiva`: Asociación correcta de oficina administradora activa.
   - `test08_rechazarOficinaInexistenteOInactiva`: Rechazo de oficina inexistente con HTTP 404.
   - `test09_listarMercadosPaginados`: Paginación consistente de listado.
   - `test10_buscarPorCodigo`: Búsqueda exacta y parcial por código.
   - `test11_buscarPorNombre`: Búsqueda insensible a mayúsculas/minúsculas por nombre.
   - `test12_filtrarPorEstado`: Filtro multi-criterio por estado operativo.
   - `test13_filtrarPorOficina`: Filtro multi-criterio por oficina asignada.
   - `test14_ordenarMercadosWhitelistSeguro`: Validación de ordenamiento seguro contra inyección SQL.
   - `test15_paginacionConsistente`: Verificación de metadatos de paginación (first, last, totalPages).
   - `test16_consultarDetalleCompleto`: Consulta de detalle con sectores, puestos y documentos.
   - `test17_editarMercado`: Edición de datos generales con incremento de versión.
   - `test18_controlConcurrenciaOptimista`: Conflicto de versión optimista (`OptimisticLockingFailureException`).
   - `test19_cambiarEstadoOperativo`: Transición válida de estado con registro de motivo.
   - `test20_rechazarCambioMismoEstado`: Rechazo de transición idempotente hacia el mismo estado.
   - `test21_conservarHistorialInmutable`: Inmutabilidad y trazabilidad de los cambios de estado.
   - `test22_contarPuestosYResumenOperativo`: Cálculo agregado de puestos disponibles, ocupados y reservados.
   - `test23_exportarExcelConProteccionFormulaInjection`: Generación de Excel XLSX con sanitización de inyección de fórmulas.
   - `test24_exportarPdfApaisado`: Generación de PDF institucional apaisado.
   - `test29_asociarDocumentoMercadoConAuditoria`: Vinculación de documento institucional con auditoría.
2. **Pruebas de Seguridad y Web (`MercadoMunicipalWebSecurityTest` - 5 puntos):**
   - `test25_verificarPermisosEnEndpoints`: Validación de roles y permisos `@PreAuthorize` en endpoints API y Web.
   - `test26_rechazarAccesoNoAutorizado`: Retorno de HTTP 403 Forbidden para usuarios sin permisos.
   - `test27_prevenirIdorMercadoInexistente`: Prevención de IDOR retornando HTTP 404 en recursos inexistentes.
   - `test28_proteccionCsrfEnEndpointsMutables`: Rechazo HTTP 403 en solicitudes POST/PUT sin token CSRF.
   - `test30_renderizadoThymeleafVistas`: Renderizado HTML 200 OK con fragmentos y expresiones SpEL válidas en listado, formulario y detalle.

---

## 24. Organización Física, Sectores, Pabellones, Zonas Comerciales y Estructura Jerárquica de Mercados (Prompt 23)

### 1. Migración de Base de Datos Flyway (`V25__mercados_organizacion_fisica.sql`)
- **Zonas Comerciales y Áreas Comunes (`mercados_zonas`):**
  - Registro físico y delimitación funcional por tipos: `COMERCIAL`, `CIRCULACION`, `SERVICIOS_COMUNES`, `ALMACENAMIENTO`, `MANTENIMIENTO`, `OTRO`.
  - Diferenciación explícita de áreas asignables (`es_asignable = true`) vs áreas comunes de tránsito y servicios (`es_asignable = false`).
  - Restricción de unicidad compuesta por mercado y código `uq_mercados_zonas_mercado_codigo`.
  - Control de concurrencia optimista mediante columna `version BIGINT NOT NULL DEFAULT 0`.
- **Catálogo Administrable de Categorías Comerciales (`mercados_categorias_comerciales`):**
  - Catálogo municipal unificado (`codigo`, `nombre`, `descripcion`, `orden_visual`, `estado`).
  - Pre-población con giros tradicionales de Quinua: Abarrotes, Carnes y Embutidos, Frutas y Verduras, Gastronomía Tradicional, Artesanías y Cerámica, Ropa y Calzado, Servicios Básicos.
- **Historial Inmutable de Cambios de Estado:**
  - `mercados_sectores_cambios_estado`: Trazabilidad cronológica de modificaciones de estado en sectores (`estado_anterior`, `estado_nuevo`, `motivo`, `usuario_responsable`, `usuario_username`, `fecha_cambio`).
  - `mercados_pabellones_cambios_estado`: Trazabilidad cronológica de modificaciones de estado en pabellones de mercado.
- **Ampliación de Entidades Existentes:**
  - `mercados_sectores`: Ampliado con columna `categoria_comercial_id` (FK opcional), restricción check ampliada de estados (`ACTIVO`, `INACTIVO`, `EN_MANTENIMIENTO`, `SUSPENDIDO`) y columna `version BIGINT NOT NULL DEFAULT 0`.
  - `mercados_pabellones`: Restricción check ampliada de estados y columna `version BIGINT NOT NULL DEFAULT 0`.
- **Permisos Granulares de Estructura Física:**
  - `MERCADOS_SECTOR_VER`: Consulta y listado de sectores comerciales.
  - `MERCADOS_SECTOR_GESTIONAR`: Creación, edición, cambio de estado y reordenamiento de sectores.
  - `MERCADOS_PABELLON_VER`: Consulta y listado de pabellones.
  - `MERCADOS_PABELLON_GESTIONAR`: Creación, edición, cambio de estado y reordenamiento de pabellones.
  - `MERCADOS_ESTRUCTURA_VER`: Acceso a la vista de árbol jerárquico y cálculo de estadísticas estructurales.
  - `MERCADOS_ZONA_GESTIONAR`: Registro y administración de zonas comerciales y áreas comunes.

### 2. Arquitectura de Dominio y Servicios
- **Entidades JPA & Estructura DTOs:**
  - Mapeo de `ZonaComercial`, `CategoriaComercial`, `SectorCambioEstado` y `PabellonCambioEstado`.
  - Implementación de DTOs en Java 17 puro (evitando stubs incompletos en el entorno IDE): `SectorRequest`, `SectorActualizacionRequest`, `SectorResponse`, `PabellonRequest`, `PabellonActualizacionRequest`, `PabellonResponse`, `ZonaRequest`, `ZonaResponse`, `EstructuraMercadoResponse`, `EstructuraEstadisticasResponse`, `CambioEstadoEstructuraRequest`, `ReordenarItemRequest`.
- **Reglas de Negocio Implementadas:**
  - Unicidad de código por mercado (permite códigos idénticos entre diferentes mercados).
  - Coherencia estructural: Si un pabellón se vincula a un sector, dicho sector debe pertenecer obligatoriamente al mismo mercado municipal (prevención de inconsistencias entre recintos).
  - Flexibilidad arquitectónica: Admite sectores con pabellones, sectores con puestos directos (sin pabellón), pabellones independientes directamente bajo el mercado (sin sector), puestos directos y zonas comunes.
  - Cálculo de estadísticas consolidadas sin duplicación: Conteo exacto de puestos únicos sin duplicar puestos asociados tanto a sector como a pabellón.
  - Máquina de estados con justificación técnica obligatoria (5 a 500 caracteres) y alertas operativas si existen puestos ocupados o activos asociados.

### 3. Endpoints REST y Rutas Web
- **Endpoints REST (`/api/mercados/**`):**
  - `GET /api/mercados/{mercadoId}/sectores` (listado paginado con filtros)
  - `POST /api/mercados/{mercadoId}/sectores` (crear sector)
  - `GET /api/mercados/{mercadoId}/sectores/{sectorId}` (detalle)
  - `PUT /api/mercados/{mercadoId}/sectores/{sectorId}` (editar sector con `@Version`)
  - `PATCH /api/mercados/{mercadoId}/sectores/{sectorId}/estado` (cambio de estado con motivo)
  - `PUT /api/mercados/{mercadoId}/sectores/reordenar` (reordenamiento visual en bloque)
  - `GET /api/mercados/{mercadoId}/sectores/{sectorId}/historial` (historial inmutable de estados)
  - `GET /api/mercados/{mercadoId}/pabellones` (listado paginado con filtros por sector, piso y estado)
  - `POST /api/mercados/{mercadoId}/pabellones` (crear pabellón)
  - `GET /api/mercados/{mercadoId}/pabellones/{pabellonId}` (detalle)
  - `PUT /api/mercados/{mercadoId}/pabellones/{pabellonId}` (editar pabellón con `@Version`)
  - `PATCH /api/mercados/{mercadoId}/pabellones/{pabellonId}/estado` (cambio de estado con motivo)
  - `PUT /api/mercados/{mercadoId}/pabellones/reordenar` (reordenamiento visual en bloque)
  - `GET /api/mercados/{mercadoId}/pabellones/{pabellonId}/historial` (historial inmutable de estados)
  - `GET /api/mercados/{mercadoId}/zonas` (listado paginado de zonas físicas)
  - `POST /api/mercados/{mercadoId}/zonas` (crear zona comercial o área común)
  - `GET /api/mercados/{mercadoId}/zonas/{zonaId}` (detalle de zona)
  - `PUT /api/mercados/{mercadoId}/zonas/{zonaId}` (actualizar zona)
  - `PATCH /api/mercados/{mercadoId}/zonas/{zonaId}/estado` (cambiar estado de zona)
  - `GET /api/mercados/{mercadoId}/estructura` (árbol jerárquico completo en JSON)
  - `GET /api/mercados/{mercadoId}/estructura/estadisticas` (métricas agregadas sin duplicados)
  - `GET /api/mercados/categorias-comerciales` (catálogo activo de categorías)
- **Vistas Web Thymeleaf (`/dashboard/mercados/**`):**
  - `/dashboard/mercados/{mercadoId}/sectores` — Listado con búsqueda, filtro por categoría y estado, badges y modal reactivo de estado.
  - `/dashboard/mercados/{mercadoId}/sectores/nuevo` — Formulario institucional de registro de sector.
  - `/dashboard/mercados/{mercadoId}/sectores/{sectorId}` — Detalle con tarjetas estadísticas, pestañas (Info General, Pabellones Vinculados, Historial de Estados).
  - `/dashboard/mercados/{mercadoId}/sectores/{sectorId}/editar` — Edición con control de versión.
  - `/dashboard/mercados/{mercadoId}/pabellones` — Listado con filtros por sector, nivel/piso y estado.
  - `/dashboard/mercados/{mercadoId}/pabellones/nuevo` — Formulario de registro de pabellón con selector de sector opcional y nivel.
  - `/dashboard/mercados/{mercadoId}/pabellones/{pabellonId}` — Detalle con métricas y auditoría.
  - `/dashboard/mercados/{mercadoId}/pabellones/{pabellonId}/editar` — Edición con control de versión.
  - `/dashboard/mercados/{mercadoId}/estructura` — Árbol interactivo jerárquico colapsable/expandible, búsqueda en tiempo real, desglose por sectores, pabellones, puestos y zonas.

### 4. Cobertura de Pruebas Automatizadas (30/30 - 100% Exitosas)
- **Pruebas de Servicio (`MercadoEstructuraFisicaServiceTest` - 24 puntos):**
  1. `testCrearSectorExitoso`: Crear sector con datos válidos.
  2. `testRechazarCodigoDuplicadoMismoMercado`: Rechazar código duplicado dentro del mercado.
  3. `testPermitirCodigoIdenticoEnDistintoMercado`: Permitir código equivalente en otro mercado.
  4. `testRechazarMercadoInexistente`: Validar mercado inexistente con HTTP 404.
  5. `testRechazarNombreObligatorio`: Validar nombre obligatorio.
  6. `testEditarSector`: Editar sector con campos válidos.
  7. `testCambiarEstadoSector`: Cambiar estado con motivo obligatorio registrado.
  8. `testRechazarTransicionMismoEstado`: Rechazar transición inválida hacia el mismo estado.
  9. `testCrearPabellonExitoso`: Crear pabellón.
  10. `testAsociarPabellonConSector`: Asociar pabellón con sector del mismo mercado.
  11. `testRechazarSectorDeOtroMercado`: Rechazar asociación de sector de otro mercado.
  12. `testCrearPabellonSinSector`: Crear pabellón independiente sin sector.
  13. `testEditarPabellon`: Editar pabellón.
  14. `testCambiarEstadoPabellon`: Cambiar estado de pabellón con motivo.
  15. `testConsultarEstructuraCompleta`: Consultar estructura jerárquica completa.
  16. `testContarSectores`: Contar sectores activos e inactivos.
  17. `testContarPabellones`: Contar pabellones activos e inactivos.
  18. `testContarPuestosSinDuplicados`: Contar puestos sin duplicados vinculados a sector y pabellón.
  19. `testReordenarSectores`: Reordenar sectores visualmente.
  20. `testReordenarPabellones`: Reordenar pabellones visualmente.
  21. `testControlConcurrenciaOptimista`: Controlar concurrencia con `@Version` (HTTP 409).
  22. `testConsultarHistorialInmutable`: Consultar historial cronológico inmutable.
  23. `testFiltrarSectores`: Filtrar sectores por término y estado.
  24. `testPaginarPabellones`: Paginación consistente de pabellones.
- **Pruebas de Seguridad y Web (`MercadoEstructuraWebSecurityTest` - 6 puntos):**
  25. `testPermisoCrearSectorAutorizado`: Verificar permisos RBAC con autoridades y token JWT.
  26. `testRechazarAccesoNoAutorizado`: Rechazar acceso no autorizado (HTTP 403 Forbidden).
  27. `testPrevenirIdorSectorMercadoCruzado`: Prevenir IDOR en URLs cruzadas (HTTP 404).
  28. `testProteccionCsrfRechazaSinToken`: Protección CSRF rechazando mutaciones sin token.
  29. `testRenderizadoThymeleafVistas`: Renderizado Thymeleaf de vistas web respondiendo 200 OK.
  30. `testIntegracionPostgreSqlPersistencia`: Integración con PostgreSQL 17 verificando persistencia real.

---

## PROMPT 24: Registro y Administración de Puestos Comerciales (Módulo 2: Mercados Municipales)

### 1. Resumen de Implementación
El **Prompt 24** implementa el sistema integral de catastro, registro, administración, seguimiento de disponibilidad, historial de traslados y transiciones de estado para los **Puestos Comerciales (`PuestoComercial`)** de los mercados municipales de la Municipalidad Distrital de Quinua.

### 2. Base de Datos y Migración Flyway V26
- **Archivo de Migración:** `V26__mercados_puestos_administracion.sql`
- **Tablas Creadas / Adaptadas:**
  - `mercados_puestos_documentos`: Tabla relacional con FK central a `documentos(id)` (sistema unificado de gestión documental del SIGM), asociando actas, planos, contratos o resoluciones de inspección técnica.
  - `mercados_puestos_cambios_estado`: Registro inmutable de transiciones de estado físico (`OPERATIVO`, `EN_MANTENIMIENTO`, `INHABILITADO`), almacenando estado anterior, nuevo estado, motivo administrativo, observaciones, usuario responsable y timestamp.
  - `mercados_puestos_cambios_ubicacion`: Trazabilidad inmutable de traslados espaciales entre sectores, pabellones o niveles.
- **Permisos Granulares Sembrados (`permisos` & `rol_permisos`):**
  - `MERCADOS_PUESTO_VER`: Consulta de puestos, fichas técnicas, disponibilidad y estadísticas.
  - `MERCADOS_PUESTO_CREAR`: Alta de nuevos puestos en el padrón municipal.
  - `MERCADOS_PUESTO_EDITAR`: Modificación de dimensiones, codificación y referencias.
  - `MERCADOS_PUESTO_CAMBIAR_ESTADO`: Transición de estado físico con justificación técnica.
  - `MERCADOS_PUESTO_CAMBIAR_UBICACION`: Modificación controlada de sector/pabellón/zona.
  - `MERCADOS_PUESTO_HISTORIAL_VER`: Auditoría cronológica de cambios.
  - `MERCADOS_PUESTO_EXPORTAR`: Generación de padrones en Excel y PDF.
  - `MERCADOS_PUESTO_GESTIONAR`: Permiso maestro de administración de puestos.

### 3. Modelo de Dominio y Reglas de Negocio
- **Entidad `PuestoComercial`:**
  - Identificación dual: `UUID id` interno y `codigo` comercial alfanumérico único por mercado (ej. `P-001`, `A-12`, configurable).
  - Jerarquía espacial estricta: Validación transversal de que el `sectorId`, `pabellonId` y `zonaId` pertenezcan al mismo mercado municipal del puesto (prevención de IDOR estructural).
  - Dimensiones controladas: Validación de `areaMetrosCuadrados`, `anchoMetros`, `largoMetros` (positivos, no nulos cuando se declaran, con cálculo automático o validación de concordancia).
  - Desacoplamiento de Estado Físico y Ocupación:
    - **Estado Físico (`estadoFisico`):** `OPERATIVO`, `EN_MANTENIMIENTO`, `INHABILITADO`.
    - **Disponibilidad / Ocupación (`estado`):** `DISPONIBLE`, `OCUPADO`, `RESERVADO`, `NO_ASIGNABLE`.
    - **Regla de Disponibilidad:** Un puesto es `DISPONIBLE` únicamente si el mercado está operativo, su estado físico es `OPERATIVO`, no posee asignaciones vigentes incompatibles y no está bajo reserva administrativa.
  - Historial Inmutable: Toda transición física o de ubicación se audita en tablas dedicadas sin sobrescritura de datos históricos.
  - Concurrencia Optimista: Manejo estricto de concurrencia mediante `@Version` para evitar condiciones de carrera entre administradores.

### 4. Endpoints REST (`/api/mercados/**`)
- `GET /api/mercados/{mercadoId}/puestos`: Listado paginado con especificaciones JPA dinámicas (`PuestoComercialFilter`: sector, pabellón, tipo, estado físico, disponibilidad, niveles, rango de área).
- `POST /api/mercados/{mercadoId}/puestos`: Creación con validación de unicidad de código y pertenencia jerárquica.
- `GET /api/mercados/{mercadoId}/puestos/{puestoId}`: Ficha técnica básica del puesto.
- `GET /api/mercados/{mercadoId}/puestos/{puestoId}/detalle`: Ficha integral con dimensiones, ubicación, documentos asociados e historial cronológico de cambios.
- `PUT /api/mercados/{mercadoId}/puestos/{puestoId}`: Actualización con control optimista `@Version`.
- `PATCH /api/mercados/{mercadoId}/puestos/{puestoId}/estado`: Transición de estado físico con motivo obligatorio (5 a 500 caracteres).
- `PATCH /api/mercados/{mercadoId}/puestos/{puestoId}/ubicacion`: Reubicación interna con validación de recinto e impacto operativo.
- `GET /api/mercados/{mercadoId}/puestos/{puestoId}/disponibilidad`: Evaluación reactiva de disponibilidad en tiempo real.
- `GET /api/mercados/{mercadoId}/puestos/{puestoId}/historial`: Historial inmutable de estados y traslados.
- `GET /api/mercados/{mercadoId}/puestos/estadisticas`: Tarjetas estadísticas calculadas en PostgreSQL (totales, disponibles, ocupados, mantenimiento, inhabilitados, desglose por sector y tipo).
- `GET /api/mercados/{mercadoId}/puestos/exportar/excel`: Exportación a Microsoft Excel (.xlsx) con Apache POI, celdas formateadas y protección contra inyección de fórmulas (`=`, `+`, `-`, `@`).
- `GET /api/mercados/{mercadoId}/puestos/exportar/pdf`: Padrón institucional en PDF con OpenPDF, cabeceras oficiales, metadatos y tabla paginada.

### 5. Interfaces Web Thymeleaf & Tailwind CSS Local
- **Rutas Web (`/dashboard/mercados/**`):**
  - `/dashboard/mercados/{mercadoId}/puestos`: Padrón interactivo con alternancia de vista (Tabla / Tarjetas), filtros reactivos, badges de estado accesibles y paginación server-side.
  - `/dashboard/mercados/{mercadoId}/puestos/nuevo`: Formulario de alta con selectores dependientes en cascada (Mercado → Sectores → Pabellones), validaciones en tiempo real y cálculo de área.
  - `/dashboard/mercados/{mercadoId}/puestos/{puestoId}`: Vista detallada con pestañas operativas (Ficha Técnica, Ubicación, Documentos, Historial de Estados, Traslados).
  - `/dashboard/mercados/{mercadoId}/puestos/{puestoId}/editar`: Formulario de edición con persistencia de versión concurrente.
- **Componentes y Modales:**
  - `componentes/modal-estado.html`: Modal accesible para cambio de estado físico con advertencias de asignaciones vigentes y campo de motivo obligatorio.
  - `componentes/filtros.html`: Panel de filtros avanzados colapsable.
- **JavaScript Modular:**
  - `puestos-listado.js`: Manejo de vistas tabla/tarjeta, consumo AJAX de estadísticas y modal de estado.
  - `puestos-formulario.js`: Carga dinámica de pabellones según sector seleccionado y cálculo automático de m².
  - `puestos-detalle.js`: Gestión de pestañas y visor de historial cronológico.

### 6. Cobertura de Pruebas Automatizadas (34/34 - 100% Exitosas)
- **Pruebas de Servicio (`MercadoPuestosServiceTest` - 28 casos):**
  1. `test01_crearPuestoValido`: Registro exitoso con todos los campos válidos.
  2. `test02_rechazarCodigoDuplicado`: Rechazo de código repetido en el mismo mercado.
  3. `test03_permitirMismoCodigoEnMercadosDistintos`: Aceptación de código idéntico en mercados diferentes.
  4. `test04_validarMercadoInexistente`: Rechazo al asociar puesto a un mercado inexistente (HTTP 404).
  5. `test05_validarSectorDeOtroMercado`: Rechazo al intentar asociar un sector ajeno al mercado (inconsistencia jerárquica).
  6. `test06_validarPabellonDeOtroMercado`: Rechazo al asociar un pabellón de otro mercado.
  7. `test07_validarTipoDePuestoInexistente`: Validación de catálogo de tipos de puesto.
  8. `test08_validarAreaPositiva`: Validación de área mayor a cero.
  9. `test09_validarDimensiones`: Validación de congruencia técnica entre ancho, largo y área.
  10. `test10_consultarPuesto`: Recuperación de puesto por ID y mercado.
  11. `test11_editarPuesto`: Modificación de datos descriptivos y dimensionales.
  12. `test12_controlarConcurrenciaOptimista`: Detección de conflicto concurrente con `@Version`.
  13. `test13_cambiarEstadoFisico`: Transición de estado físico y registro inmutable en auditoría.
  14. `test14_rechazarEstadoInvalido`: Rechazo de motivos vacíos o estados nulos.
  15. `test15_cambiarUbicacion`: Traslado de sector/pabellón con validación de recinto y registro de historial.
  16. `test16_registrarHistorial`: Verificación de auditoría cronológica inmutable.
  17. `test17_consultarDisponibilidad`: Evaluación de puesto como disponible.
  18. `test18_consultarOcupacion`: Verificación de disponibilidad restringida al inhabilitar.
  19. `test19_filtrarPorSector`: Filtrado específico por sector.
  20. `test20_filtrarPorPabellon`: Filtrado por pabellón.
  21. `test21_filtrarPorEstado`: Filtrado por estado físico.
  22. `test22_filtrarPorTipo`: Filtrado por tipo de puesto comercial.
  23. `test23_paginar`: Paginación eficiente a nivel de PostgreSQL.
  24. `test24_ordenar`: Ordenamiento por código, número y área.
  25. `test25_contarPuestos`: Cálculo agregado de métricas.
  26. `test26_evitarConteosDuplicados`: Garantía de conteo sin duplicidad entre sectores y pabellones.
  27. `test27_exportarExcel`: Generación correcta de archivo binario `.xlsx` con encabezados y datos.
  28. `test28_exportarPdf`: Generación correcta de archivo binario `.pdf` con estructura tabular.
- **Pruebas de Seguridad y Web (`MercadoPuestosWebSecurityTest` - 6 casos):**
  29. `test29_verificarPermisosAutorizado`: Acceso permitido con credenciales JWT y permisos asignados.
  30. `test30_rechazarAccesoNoAutorizado`: Rechazo con HTTP 403 Forbidden para usuarios sin permisos.
  31. `test31_prevenirIdorMercadoPuesto`: Prevenir IDOR cuando un puesto no pertenece al mercado indicado en la URL (HTTP 404).
  32. `test32_proteccionCsrfRechazaSinToken`: Verificación de protección CSRF en mutaciones web.
  33. `test33_renderizadoThymeleafVistas`: Verificación de renderizado de vistas HTML (`listado`, `formulario`, `detalle`) respondiendo HTTP 200.
  34. `test34_integracionPostgresql`: Persistencia transaccional real contra PostgreSQL 17.

---

## 33. Registro y Administración de Comerciantes Municipales (Prompt 25)

El **Prompt 25 de 150** consolida el padrón oficial de comerciantes del Sistema de Mercados Municipales de la Municipalidad Distrital de Quinua (`Comerciante`), admitiendo personas naturales (reutilizando `Persona` del núcleo común) y personas jurídicas (`PersonaJuridica`), con gestión de representantes legales, validación matemática oficial de DNI y RUC (Módulo 11 SUNAT), asociación de actividades comerciales, vinculación documental, auditoría inmutable de cambios de estado y protección de datos personales.

### 1. Migración de Base de Datos Flyway (`V27__mercados_comerciantes_administracion.sql`)
- **Secuencia Segura de Códigos Internos:**
  - `seq_mercados_codigo_comerciante`: Genera identificadores correlativos con formato configurable e inmutable (ej. `COM-2026-000001`), evitando concurrencia sucia por `COUNT(*) + 1`.
- **Entidad Institucional Reutilizable `personas_juridicas`:**
  - `id UUID PRIMARY KEY`, `ruc VARCHAR(11) UNIQUE`, `razon_social`, `nombre_comercial`, `domicilio_fiscal`, `telefono`, `correo`, `estado_registro_interno`, `version`, auditoría.
  - Constraint CHECK de formato numérico de 11 dígitos y estados internos (`PENDIENTE_VERIFICACION`, `VERIFICADO`, `OBSERVADO`, `INACTIVO`).
- **Tabla `personas_juridicas_representantes`:**
  - `id BIGSERIAL PRIMARY KEY`, `persona_juridica_id UUID`, `persona_id BIGINT` (titular natural), `tipo_representacion` (`GERENTE_GENERAL`, `APODERADO`, `ADMINISTRADOR`, `REPRESENTANTE_LEGAL`, `OTRO`), `fecha_inicio`, `fecha_fin`, `documento_sustento_id BIGINT`, `estado` (`ACTIVO`, `INACTIVO`, `REVOCADO`).
- **Adaptación Integral de `mercados_comerciantes`:**
  - Modificación de columna `persona_id` a nullable.
  - Agregado de `persona_juridica_id UUID REFERENCES personas_juridicas(id)`.
  - Agregado de `tipo_comerciante VARCHAR(20) NOT NULL` (`PERSONA_NATURAL`, `PERSONA_JURIDICA`).
  - Restricción de integridad CHECK: Exactamente una identidad titular válida (si es natural, `persona_id` requerido y `persona_juridica_id` nulo; si es jurídica, viceversa).
  - Índices únicos condicionales para prevenir que una misma persona o empresa sea duplicada como comerciante.
  - Ampliación de constraint CHECK de estado para admitir `PENDIENTE_VERIFICACION`, `ACTIVO`, `SUSPENDIDO`, `INACTIVO`.
- **Historial Inmutable de Cambios de Estado (`mercados_comerciantes_cambios_estado`):**
  - Auditoría de cada transición de estado: `estado_anterior`, `estado_nuevo`, `motivo`, `documento_sustento_id`, `usuario_id`, `usuario_username`, `fecha_cambio`.
- **Gestión Documental Centralizada (`mercados_comerciantes_documentos`):**
  - Vinculación sin duplicidad con el acervo documental municipal (`documentos(id)`), registrando tipo de sustento, observaciones y metadatos de subida.
- **Relación de Actividades Comerciales:**
  - Agregado de columna `estado` en `mercados_comerciante_actividades`.

### 2. Matriz de Permisos Granulares Sembrados en Base de Datos
- `MERCADOS_COMERCIANTE_VER`: Consulta de comerciantes, fichas y actividades.
- `MERCADOS_COMERCIANTE_GESTIONAR`: Administración integral del padrón de comerciantes.
- `MERCADOS_COMERCIANTE_CREAR`: Registro de comerciantes naturales y jurídicos.
- `MERCADOS_COMERCIANTE_EDITAR`: Modificación de datos de contacto y comerciales autorizados.
- `MERCADOS_COMERCIANTE_CAMBIAR_ESTADO`: Transición de estados con registro obligatorio de motivo.
- `MERCADOS_COMERCIANTE_DOCUMENTOS`: Asociación y descarga de documentos sustentatorios.
- `MERCADOS_COMERCIANTE_DATOS_SENSIBLES`: Visualización de DNI, RUC y contactos sin enmascaramiento.
- `MERCADOS_COMERCIANTE_EXPORTAR`: Generación de padrones en Microsoft Excel y PDF.
- `MERCADOS_COMERCIANTE_HISTORIAL_VER`: Auditoría cronológica de estados y transiciones.

### 3. Validación de Identidad y Abstracción de Servicios (RENIEC / SUNAT)
- **`IdentidadVerificationService`:**
  - `validarFormatoDni`: Validación exacta de 8 dígitos numéricos, normalización y detección de secuencias inválidas.
  - `validarFormatoRuc`: Algoritmo matemático oficial de Módulo 11 de SUNAT (factores `[5, 4, 3, 2, 7, 6, 5, 4, 3, 2]`, validación de prefijos autorizados `10`, `15`, `17`, `20` y cálculo exacto del dígito verificador).
- **Abstracción de Proveedores Externos:**
  - Interfaces desacopladas `ReniecVerificationProvider` y `SunatVerificationProvider` preparadas para consumo de servicios oficiales autorizados. En ausencia de credenciales gubernamentales activas, no se simulan respuestas ficticias y el registro se etiqueta transparentemente como `PENDIENTE_VERIFICACION`.

### 4. Protección de Datos Personales (Enmascaramiento Dinámico)
- Cumplimiento de la Ley N° 29733 (Ley de Protección de Datos Personales del Perú):
  - En tablas y respuestas generales, los documentos de identidad se enmascaran:
    - **DNI:** `****1234` (primeros 4 dígitos ocultos).
    - **RUC:** `20******123` (6 dígitos centrales ocultos).
    - **Teléfonos y correos:** Enmascaramiento de prefijos o subdominios para usuarios estándar.
  - Solo los usuarios autorizados con el privilegio `MERCADOS_COMERCIANTE_DATOS_SENSIBLES` acceden a los números de identidad y contactos completos.

### 5. Exportaciones Seguras (Excel y PDF)
- **Excel (.xlsx) con Apache POI:**
  - Encabezados institucionales con paleta oficial (`#1d4ed8`), tipografía en negrita y alineación automática.
  - **Protección contra Inyección de Fórmulas:** Desinfección estricta de cadenas que inician con caracteres de ejecución (`=`, `+`, `-`, `@`, `\t`, `\r`) anteponiendo comilla simple `'`.
- **PDF con OpenPDF (iText LGPL):**
  - Documento horizontal orientado (Landscape A4) con membrete municipal, fecha/hora de generación y tabla formateada con colores `#1e293b`.

### 6. Endpoints REST (`/api/mercados/comerciantes/**`)
| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/mercados/comerciantes` | Padrón paginado con filtros dinámicos (término, tipo, estado, actividad, fechas) |
| `POST` | `/api/mercados/comerciantes/natural` | Registro de comerciante persona natural (reutiliza o crea `Persona`) |
| `POST` | `/api/mercados/comerciantes/juridico` | Registro de comerciante persona jurídica con representante legal opcional |
| `GET` | `/api/mercados/comerciantes/{id}` | Ficha básica del comerciante |
| `GET` | `/api/mercados/comerciantes/{id}/detalle` | Ficha técnica completa con todas sus pestañas operativas |
| `PUT` | `/api/mercados/comerciantes/{id}` | Actualización de contacto y actividades con control `@Version` |
| `PATCH` | `/api/mercados/comerciantes/{id}/estado` | Transición de estado (`PENDIENTE_VERIFICACION`, `ACTIVO`, `SUSPENDIDO`, `INACTIVO`) |
| `GET` | `/api/mercados/comerciantes/{id}/actividades` | Listado de actividades comerciales asociadas |
| `POST` | `/api/mercados/comerciantes/{id}/actividades` | Asociación de nuevas actividades comerciales del catálogo |
| `DELETE` | `/api/mercados/comerciantes/{id}/actividades/{actividadId}` | Desasociación de actividad comercial |
| `GET` | `/api/mercados/comerciantes/{id}/representantes` | Consulta de representantes legales vigentes e históricos |
| `POST` | `/api/mercados/comerciantes/{id}/representantes` | Registro de nuevo representante legal |
| `PATCH` | `/api/mercados/comerciantes/{id}/representantes/{repId}/revocar` | Revocatoria formal de representación legal |
| `POST` | `/api/mercados/comerciantes/{id}/documentos` | Asociación de documento sustentatorio central |
| `GET` | `/api/mercados/comerciantes/{id}/puestos` | Puestos y asignaciones comerciales relacionadas |
| `GET` | `/api/mercados/comerciantes/{id}/autorizaciones` | Licencias y autorizaciones comerciales asociadas |
| `GET` | `/api/mercados/comerciantes/{id}/expedientes` | Expedientes vinculados de Mesa de Partes Virtual |
| `GET` | `/api/mercados/comerciantes/{id}/historial` | Pistas cronológicas inmutables de cambios de estado |
| `GET` | `/api/mercados/comerciantes/estadisticas` | Métricas en tiempo real calculadas en PostgreSQL |
| `GET` | `/api/mercados/comerciantes/exportar/excel` | Descarga de reporte en Microsoft Excel (.xlsx) |
| `GET` | `/api/mercados/comerciantes/exportar/pdf` | Descarga de padrón oficial en PDF |

### 7. Vistas Web Thymeleaf y JavaScript Modular
- **Rutas Web (`/dashboard/mercados/comerciantes/**`):**
  - `/dashboard/mercados/comerciantes`: Padrón general con tarjetas de indicadores, tabla paginada, enmascaramiento visual de documentos, filtros y exportaciones directas.
  - `/dashboard/mercados/comerciantes/nuevo`: Formulario reactivo y dinámico que conmuta entre Persona Natural y Persona Jurídica, con validación de DNI/RUC en tiempo real y buscador de personas existentes en el sistema central para evitar duplicados.
  - `/dashboard/mercados/comerciantes/{id}`: Ficha integral con 9 pestañas operativas (Información General, Contacto, Actividades, Representantes Legales, Documentos, Puestos Asociados, Autorizaciones, Expedientes MPV, Historial Inmutable).
  - `/dashboard/mercados/comerciantes/{id}/editar`: Formulario de actualización de datos autorizados con trazabilidad concurrente.
- **Componentes Modulares:**
  - `componentes/modal-estado.html`: Modal accesible para cambio de estado con motivo sustentatorio obligatorio y confirmación.
  - `componentes/formulario-natural.html`: Subformulario específico de persona natural.
  - `componentes/formulario-juridico.html`: Subformulario específico de persona jurídica con representante legal.
- **Archivos JavaScript:**
  - `static/js/mercados/comerciantes-listado.js`: Filtros reactivos, cambio de páginas y apertura de modal de estado.
  - `static/js/mercados/comerciantes-formulario.js`: Alternancia dinámica natural/jurídica, verificación previa de identidad y persistencia AJAX.
  - `static/js/mercados/comerciantes-detalle.js`: Navegación entre pestañas, adición de actividades y revocación de representantes.

### 8. Diagrama Entidad-Relación Actualizado (Módulo Mercados: Comerciantes)
```mermaid
erDiagram
    PERSONAS ||--o| MERCADOS_COMERCIANTES : "titular natural"
    PERSONAS_JURIDICAS ||--o| MERCADOS_COMERCIANTES : "titular juridico"
    PERSONAS_JURIDICAS ||--o{ PERSONAS_JURIDICAS_REPRESENTANTES : "tiene"
    PERSONAS ||--o{ PERSONAS_JURIDICAS_REPRESENTANTES : "actua como representante"
    DOCUMENTOS ||--o{ PERSONAS_JURIDICAS_REPRESENTANTES : "sustento"
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_COMERCIANTE_ACTIVIDADES : "registra"
    ACTIVIDADES_COMERCIALES ||--o{ MERCADOS_COMERCIANTE_ACTIVIDADES : "asociada a"
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_COMERCIANTES_CAMBIOS_ESTADO : "auditoria de estado"
    DOCUMENTOS ||--o{ MERCADOS_COMERCIANTES_CAMBIOS_ESTADO : "sustento de cambio"
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_COMERCIANTES_DOCUMENTOS : "posee"
    DOCUMENTOS ||--o{ MERCADOS_COMERCIANTES_DOCUMENTOS : "archivo digital"
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_ASIGNACIONES : "ocupa"
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_AUTORIZACIONES : "autorizado en"
```

### 9. Cobertura de Pruebas Automatizadas (34/34 - 100% Exitosas)
- **`MercadoComerciantesServiceTest` (28 pruebas unitarias y de integración):**
  1. `test01_registrarComercianteNatural`: Registro exitoso con generación de código correlativo `COM-2026-XXXXXX`.
  2. `test02_registrarComercianteJuridico`: Registro con RUC y validación de razón social.
  3. `test03_validarDniValido`: Validación de formato de 8 dígitos numéricos.
  4. `test04_rechazarDniInvalido`: Rechazo inmediato de DNI con caracteres alfanuméricos o longitud errónea.
  5. `test05_validarRucValido`: Verificación matemática estricta con algoritmo oficial de Módulo 11 de SUNAT.
  6. `test06_rechazarRucInvalido`: Rechazo por prefijo no autorizado o dígito verificador erróneo.
  7. `test07_reutilizarPersonaExistente`: Asociación a comerciante reutilizando registro de `Persona` sin duplicidad.
  8. `test08_rechazarComercianteDuplicado`: Rechazo de segundo registro de comerciante para una misma persona o RUC.
  9. `test09_registrarRepresentanteLegal`: Alta de representante con validación de DNI y cargo formal.
  10. `test10_validarFechasRepresentante`: Rechazo si la fecha de fin es anterior a la fecha de inicio.
  11. `test11_registrarActividadComercial`: Asociación de actividad comercial existente.
  12. `test12_asociarVariasActividades`: Asociación de múltiples actividades comerciales sin duplicación de registros.
  13. `test13_consultarComercianteDetalle`: Verificación de ficha integral con pestañas.
  14. `test14_editarContacto`: Modificación autorizada de teléfono, correo y dirección con enmascaramiento dinámico.
  15. `test15_protegerCambioDeIdentidad`: Detección de colisión duplicada ante intento de sobrescritura de código.
  16. `test16_cambiarEstadoComerciante`: Transición auditada de `PENDIENTE_VERIFICACION` a `ACTIVO`.
  17. `test17_rechazarTransicionMismoEstado`: Rechazo de cambio a un estado ya asignado.
  18. `test18_consultarDocumentos`: Vinculación y recuperación de documentos desde el repositorio central.
  19. `test19_consultarPuestos`: Consulta de puestos asociados (lista vacía cuando no hay asignaciones vigentes).
  20. `test20_consultarAutorizaciones`: Consulta de licencias y permisos comerciales.
  21. `test21_buscarPorCodigo`: Búsqueda exacta e insensible a mayúsculas por código correlativo.
  22. `test22_buscarPorNombre`: Búsqueda por nombres, apellidos o razón social.
  23. `test23_filtrarPorTipo`: Filtrado específico de personas naturales y jurídicas.
  24. `test24_filtrarPorEstado`: Filtrado por estado operativo.
  25. `test25_paginacion`: Paginación eficiente server-side en base de datos.
  26. `test26_estadisticasComerciantes`: Métricas cuantitativas agregadas por PostgreSQL.
  27. `test27_exportarExcel`: Generación de archivo binario `.xlsx` con desinfección de inyección de fórmulas.
  28. `test28_exportarPdf`: Generación de padrón institucional en PDF con OpenPDF.
- **`MercadoComerciantesWebSecurityTest` (6 pruebas de seguridad y presentación):**
  29. `test29_enmascararDatosSensibles`: Verificación de datos de contacto y documentos enmascarados para usuarios sin privilegio de datos sensibles.
  30. `test30_verificarPermisosAutorizado`: Acceso permitido a endpoints protegidos con credenciales válidas y permisos asignados.
  31. `test31_rechazarAccesoNoAutorizado`: Rechazo con HTTP 403 Forbidden para usuarios sin permisos.
  32. `test32_prevenirIdor`: Manejo seguro de identificadores UUID inexistentes o ajenos respondiendo HTTP 404 Not Found.
  33. `test33_proteccionCsrfRechazaSinToken`: Verificación de protección CSRF en mutaciones web.
  34. `test35_renderizadoThymeleafVistas`: Verificación de renderizado de vistas HTML (`listado`, `formulario`, `detalle`) respondiendo HTTP 200.

---

## 34. ASIGNACIÓN DE PUESTOS A COMERCIANTES, CONTROL DE OCUPACIÓN Y TRAZABILIDAD (PROMPT 26 DE 150)

### 1. Resumen Ejecutivo
Se implementó el ciclo de vida administrativo y operativo completo de asignación de puestos comerciales (`AsignacionPuesto`) a comerciantes registrados (`Comerciante`), garantizando:
- Consulta y filtrado de puestos con disponibilidad real y control de restricciones de inhabilitación física.
- Máquina de estados formal de 10 etapas administrativas sin saltos arbitrarios.
- Control estricto de concurrencia y protección transaccional contra doble ocupación simultánea incompatible en el mismo puesto comercial mediante PostgreSQL.
- Regla institucional de límite máximo configurable de puestos comerciales activos por comerciante.
- Evaluación técnica, observaciones administrativas subsanables, aprobación y rechazo fundado.
- Activación de ocupación efectiva pasando la asignación a `VIGENTE` y el puesto comercial automáticamente a `OCUPADO`.
- Control de vigencias temporales, alertas de vencimiento, suspensión con o sin reserva de puesto, y finalización formal.
- Registro de devolución física del puesto comercial con acta de recepción, inspección de estado físico y derivación a mantenimiento si corresponde.
- Trazabilidad inmutable de cada hito en `HistorialOcupacion` y auditoría de seguridad.
- Emisión de Constancia Oficial de Asignación en PDF con código QR de verificación registral (ZXing).
- Exportación institucional en Excel/CSV (con sanitización contra inyección de fórmulas) y PDF.

### 2. Máquina de Estados del Ciclo de Vida de Asignación
```mermaid
stateDiagram-v2
    [*] --> BORRADOR: Registro inicial
    BORRADOR --> SOLICITADA: Presentar para evaluación
    SOLICITADA --> EN_EVALUACION: Admitir a trámite
    EN_EVALUACION --> OBSERVADA: Formular observación técnica
    OBSERVADA --> EN_EVALUACION: Subsanar observación
    EN_EVALUACION --> APROBADA: Aprobación por funcionario competente
    EN_EVALUACION --> RECHAZADA: Rechazo fundado (fin trámite)
    APROBADA --> VIGENTE: Activación de ocupación efectiva
    VIGENTE --> SUSPENDIDA: Suspensión administrativa fundada
    SUSPENDIDA --> VIGENTE: Reanudación de asignación
    VIGENTE --> FINALIZADA: Conclusión formal de periodo
    VIGENTE --> ANULADA: Nulidad de oficio / revocación
    APROBADA --> ANULADA: Nulidad antes de ocupación
    FINALIZADA --> DEVOLUCION_REGISTRADA: Recepción física de puesto
    RECHAZADA --> [*]
    ANULADA --> [*]
    DEVOLUCION_REGISTRADA --> [*]
```

### 3. Diagrama Entidad-Relación del Módulo de Asignaciones (Mermaid)
```mermaid
erDiagram
    MERCADOS_PUESTOS ||--o{ MERCADOS_ASIGNACIONES : "asignado en"
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_ASIGNACIONES : "titular de"
    MERCADOS_ASIGNACIONES ||--o{ MERCADOS_HISTORIAL_OCUPACION : "trazabilidad"
    MERCADOS_ASIGNACIONES ||--o{ MERCADOS_ASIGNACIONES_EVALUACIONES : "evaluaciones"
    MERCADOS_ASIGNACIONES ||--o| MERCADOS_ASIGNACIONES_DEVOLUCIONES : "recepción física"
    MPV_EXPEDIENTES ||--o{ MERCADOS_ASIGNACIONES : "expediente sustento"
    GEN_DOCUMENTOS ||--o{ MERCADOS_ASIGNACIONES : "documento sustento"

    MERCADOS_ASIGNACIONES {
        uuid id PK
        uuid puesto_id FK
        uuid comerciante_id FK
        varchar codigo_asignacion UK
        varchar estado
        varchar tipo_asignacion
        date fecha_solicitud
        date fecha_inicio
        date fecha_fin
        bigint documento_sustento_id FK
        bigint expediente_id FK
        varchar motivo
        varchar usuario_registro
        varchar usuario_evaluacion
        timestamp fecha_evaluacion
        varchar usuario_aprobacion
        timestamp fecha_aprobacion
        text observaciones
        text motivo_rechazo
        text motivo_suspension
        text motivo_finalizacion
        bigint version
    }

    MERCADOS_ASIGNACIONES_EVALUACIONES {
        bigserial id PK
        uuid asignacion_id FK
        varchar tipo_evaluacion
        varchar resultado
        text motivo
        bigint documento_sustento_id FK
        varchar usuario_evaluador
        timestamp fecha_evaluacion
    }

    MERCADOS_ASIGNACIONES_DEVOLUCIONES {
        bigserial id PK
        uuid asignacion_id FK
        uuid puesto_id FK
        timestamp fecha_devolucion
        varchar responsable_recepcion
        varchar estado_fisico_puesto
        text observaciones
        bigint documento_acta_id FK
    }
```

### 4. Permisos Granulares Sembrados (Prompt 26)
| Permiso | Acción | Descripción |
| :--- | :--- | :--- |
| `MERCADOS_ASIGNACION_VER` | `VIEW` | Consultar bandeja y detalles de asignaciones de puestos |
| `MERCADOS_ASIGNACION_GESTIONAR` | `MANAGE` | Gestión administrativa general de asignaciones |
| `MERCADOS_ASIGNACION_CREAR` | `CREATE` | Registrar nuevas solicitudes de asignación de puestos |
| `MERCADOS_ASIGNACION_EVALUAR` | `MANAGE` | Evaluar técnicamente solicitudes presentadas |
| `MERCADOS_ASIGNACION_OBSERVAR` | `MANAGE` | Formular observaciones técnicas a subsanar |
| `MERCADOS_ASIGNACION_APROBAR` | `MANAGE` | Aprobar solicitudes de asignación cumpliendo requisitos |
| `MERCADOS_ASIGNACION_RECHAZAR` | `MANAGE` | Rechazar solicitudes de asignación fundadamente |
| `MERCADOS_ASIGNACION_ACTIVAR` | `MANAGE` | Activar ocupación efectiva pasando puesto a OCUPADO |
| `MERCADOS_ASIGNACION_SUSPENDER` | `MANAGE` | Suspender temporalmente asignaciones vigentes |
| `MERCADOS_ASIGNACION_FINALIZAR` | `MANAGE` | Concluir periodo de asignación y liberar puesto |
| `MERCADOS_ASIGNACION_DEVOLUCION` | `MANAGE` | Registrar acta y entrega física del espacio comercial |
| `MERCADOS_ASIGNACION_HISTORIAL_VER` | `VIEW` | Consultar la línea de tiempo e historial de ocupación |
| `MERCADOS_ASIGNACION_EXPORTAR` | `EXPORT` | Exportar reportes de asignaciones a Excel y PDF |

### 5. Catálogo de Endpoints REST y Web
| Método | Endpoint | Permiso / Seguridad | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/dashboard/mercados/asignaciones` | `MERCADOS_ASIGNACION_VER` | Vista Thymeleaf del listado de asignaciones |
| `GET` | `/dashboard/mercados/asignaciones/nueva` | `MERCADOS_ASIGNACION_CREAR` | Formulario web para registrar solicitud |
| `GET` | `/dashboard/mercados/asignaciones/{id}` | `MERCADOS_ASIGNACION_VER` | Ficha integral con timeline, modales y acciones |
| `GET` | `/api/mercados/asignaciones` | `MERCADOS_ASIGNACION_VER` | Listado paginado y filtrado de asignaciones |
| `POST` | `/api/mercados/asignaciones` | `MERCADOS_ASIGNACION_CREAR` | Crear borrador o presentar solicitud formal |
| `GET` | `/api/mercados/asignaciones/{id}` | `MERCADOS_ASIGNACION_VER` | Obtener detalle integral con trazabilidad |
| `PUT` | `/api/mercados/asignaciones/{id}` | `MERCADOS_ASIGNACION_CREAR` | Actualizar solicitud en estado borrador |
| `POST` | `/api/mercados/asignaciones/{id}/presentar` | `MERCADOS_ASIGNACION_CREAR` | Presentar solicitud para evaluación |
| `POST` | `/api/mercados/asignaciones/{id}/evaluar` | `MERCADOS_ASIGNACION_EVALUAR` | Registrar dictamen o evaluación técnica |
| `POST` | `/api/mercados/asignaciones/{id}/observar` | `MERCADOS_ASIGNACION_OBSERVAR` | Formular observaciones técnicas |
| `POST` | `/api/mercados/asignaciones/{id}/subsanar` | `MERCADOS_ASIGNACION_CREAR` | Subsanar observaciones administrativas |
| `POST` | `/api/mercados/asignaciones/{id}/aprobar` | `MERCADOS_ASIGNACION_APROBAR` | Aprobar asignación de puesto |
| `POST` | `/api/mercados/asignaciones/{id}/rechazar` | `MERCADOS_ASIGNACION_RECHAZAR` | Rechazar asignación con motivo obligatorio |
| `POST` | `/api/mercados/asignaciones/{id}/activar` | `MERCADOS_ASIGNACION_ACTIVAR` | Activar ocupación efectiva (`VIGENTE`) |
| `POST` | `/api/mercados/asignaciones/{id}/suspender` | `MERCADOS_ASIGNACION_SUSPENDER` | Suspender temporalmente ocupación |
| `POST` | `/api/mercados/asignaciones/{id}/reanudar` | `MERCADOS_ASIGNACION_SUSPENDER` | Reanudar ocupación suspendida |
| `POST` | `/api/mercados/asignaciones/{id}/finalizar` | `MERCADOS_ASIGNACION_FINALIZAR` | Concluir ocupación liberando puesto |
| `POST` | `/api/mercados/asignaciones/{id}/devolucion`| `MERCADOS_ASIGNACION_DEVOLUCION`| Registrar acta de entrega física del puesto |
| `POST` | `/api/mercados/asignaciones/{id}/anular` | `MERCADOS_ASIGNACION_GESTIONAR` | Anular asignación formalmente |
| `GET` | `/api/mercados/asignaciones/{id}/constancia`| `MERCADOS_ASIGNACION_VER` | Generar constancia en PDF con código QR |
| `GET` | `/api/mercados/asignaciones/{id}/historial` | `MERCADOS_ASIGNACION_HISTORIAL_VER` | Historial inmutable de movimientos |
| `GET` | `/api/mercados/asignaciones/estadisticas` | `MERCADOS_ASIGNACION_VER` | Métricas consolidadas en base de datos |
| `GET` | `/api/mercados/asignaciones/exportar/excel` | `MERCADOS_ASIGNACION_EXPORTAR` | Exportar padrón a CSV/Excel sanitizado |
| `GET` | `/api/mercados/asignaciones/exportar/pdf` | `MERCADOS_ASIGNACION_EXPORTAR` | Exportar padrón a documento PDF |

### 6. Cobertura de Pruebas Automatizadas (31/31 - 100% Exitosas)
- **`MercadoAsignacionServiceTest` (24 pruebas de servicio y base de datos PostgreSQL):**
  1. `test01_crearBorrador_solicitudAsignacion`: Registro de borrador sin bloquear disponibilidad del puesto.
  2. `test02_presentarSolicitud_pasaASolicitada`: Presentación formal con cambio de estado y registro en historial.
  3. `test03_validarMercadoInactivo_rechazaSolicitud`: Rechazo de solicitud sobre mercado cerrado/inactivo.
  4. `test04_validarComercianteNoActivo_rechazaSolicitud`: Rechazo de asignación para comerciantes suspendidos.
  5. `test05_consultarDisponibilidad_puestoDisponible`: Verificación de disponibilidad libre.
  6. `test06_rechazarPuestoEnMantenimiento`: Rechazo de solicitud para puestos en mantenimiento físico.
  7. `test07_evaluarSolicitud_pasaAEnEvaluacion`: Evaluación técnica con persistencia de dictamen.
  8. `test08_formularObservacion_pasaAObservada`: Formulación de requerimientos y cambio de estado a OBSERVADA.
  9. `test09_subsanarObservacion_pasaAEnEvaluacion`: Subsanación de observaciones retomando estado EN_EVALUACION.
  10. `test10_aprobarSolicitud_pasaAAprobada`: Aprobación administrativa con registro de sustento formal.
  11. `test11_rechazarSolicitud_pasaARechazada`: Rechazo administrativo fundado con motivo obligatorio.
  12. `test12_activarOcupacion_pasaAVigenteYPuestoAOccupado`: Activación que actualiza estado del puesto a OCUPADO.
  13. `test13_rechazarDobleAsignacionSimultanea`: Detección transaccional de colisión y conflicto ante doble asignación activa.
  14. `test14_suspenderYReanudarAsignacion`: Suspensión y reanudación operativa de asignaciones.
  15. `test15_finalizarAsignacion_liberaPuesto`: Finalización administrativa con liberación inmediata del puesto comercial.
  16. `test16_registrarDevolucionFisica_maloPasaAMantenimiento`: Devolución con deterioro físico derivando el puesto a EN_MANTENIMIENTO.
  17. `test17_trazabilidadHistorialOcupacion`: Verificación de inmutabilidad y registro secuencial de movimientos.
  18. `test18_validarFechas_fechaFinInvalida`: Validación estricta que impide fecha fin anterior a la de inicio.
  19. `test19_generarConstanciaPdf_conCodigoQR`: Emisión de PDF con código QR verificable generado con ZXing.
  20. `test20_estadisticas_calculaTotalesCorrectos`: Cálculo server-side de métricas de ocupación y asignaciones.
  21. `test21_exportarExcel_sanitizaInyeccionFormulas`: Sanitización con prefijo de apóstrofe ante comandos `=`, `+`, `-`, `@`.
  22. `test22_exportarPdf_generaBytesValidos`: Verificación de documento PDF binario con cabecera `%PDF`.
  23. `test23_anularAsignacion_liberaPuesto`: Nulidad de oficio con liberación de puesto a DISPONIBLE.
  24. `test24_paginacionYFiltrado`: Paginación y filtrado server-side sin errores de tipado en PostgreSQL.
- **`MercadoAsignacionWebSecurityTest` (7 pruebas de seguridad, CSRF, IDOR y Thymeleaf):**
  25. `test01_verificarPermisos_accesoAutorizado`: Acceso permitido con token JWT y permisos asignados.
  26. `test02_rechazarAccesoNoAutorizado`: Rechazo con HTTP 403 Forbidden para usuarios sin rol o permiso suficiente.
  27. `test03_prevenirIdor_asignacionInexistente`: Manejo seguro de UUID inexistente retornando HTTP 404 Not Found.
  28. `test04_proteccionCsrf_rechazaMutacionSinToken`: Rechazo HTTP 403 Forbidden ante solicitudes POST sin token CSRF.
  29. `test05_renderizarThymeleaf_listadoAsignaciones`: Renderizado exitoso de vista HTML de bandeja con layout institucional.
  30. `test06_renderizarThymeleaf_formularioNueva`: Renderizado de vista de formulario con selectores en cascada.
  31. `test07_crearSolicitudViaApi_exitoso`: Mutación exitosa vía API REST con token CSRF válido (HTTP 201 Created).

---

## 36. Módulo de Mercados Municipales: Contratos, Autorizaciones y Control de Vigencias (Prompt 27)

Implementación del subsistema de formalización documental, contractual y habilitación administrativa para la ocupación y ejercicio de actividades comerciales en los mercados municipales del distrito de Quinua.

```text
pe.gob.municipalidad.sigm.modules.mercados/
├── entity/
│   ├── ContratoOcupacion.java             # Entidad JPA para contratos de ocupación
│   ├── ContratoHistorial.java             # Bitácora inmutable de eventos contractuales
│   ├── TipoContrato.java                  # Catálogo de modalidades contractuales
│   ├── AutorizacionComercial.java         # Entidad JPA para autorizaciones comerciales
│   ├── AutorizacionHistorial.java         # Bitácora inmutable de actos administrativos
│   └── TipoAutorizacion.java              # Catálogo normativo de autorizaciones (TUPA)
│
├── enums/
│   ├── EstadoContrato.java                # 10 estados de la máquina de estados contractual
│   └── EstadoAutorizacion.java            # 9 estados del procedimiento administrativo
│
├── repository/
│   ├── ContratoOcupacionRepository.java   # Consultas JPA y nativas con detección de solapamiento
│   ├── ContratoHistorialRepository.java   # Auditoría de estados contractuales
│   ├── TipoContratoRepository.java        # Modalidades de contratación
│   ├── AutorizacionComercialRepository.java # Consultas de autorizaciones y vigencias
│   ├── AutorizacionHistorialRepository.java # Auditoría de actos habilitantes
│   └── TipoAutorizacionRepository.java    # Procedimientos y requisitos TUPA
│
├── service/
│   ├── ContratoOcupacionService.java      # Ciclo de vida: borrador, revisión, aprobación, formalización, vigencia
│   ├── AutorizacionComercialService.java  # Procedimiento: solicitud, evaluación, aprobación, emisión, revocación
│   ├── VigenciaOcupacionService.java      # Monitoreo centralizado de vencimientos y cálculo de estadísticas
│   ├── VerificacionDocumentoMercadoService.java # Verificación pública tokenizada con anonimización (Ley 29733)
│   ├── ContratoExportService.java         # Exportación segura de contratos a CSV y PDF
│   └── AutorizacionExportService.java     # Exportación segura de autorizaciones a CSV y PDF
│
├── controller/
│   ├── ContratoWebController.java         # Vistas Thymeleaf: listado, formulario, detalle
│   ├── ContratoRestController.java        # Endpoints REST protegidos para gestión de contratos
│   ├── AutorizacionWebController.java     # Vistas Thymeleaf: listado, formulario, detalle
│   ├── AutorizacionRestController.java    # Endpoints REST protegidos para autorizaciones
│   ├── MercadoDocumentoWebController.java # Control de vencimientos y verificación pública Web
│   └── MercadoDocumentoRestController.java# APIs de vencimientos, estadísticas y verificación QR
│
├── dto/
│   ├── ContratoCreateRequest.java / ContratoUpdateRequest.java / ContratoEstadoRequest.java
│   ├── ContratoResponse.java / ContratoDetailResponse.java
│   ├── AutorizacionCreateRequest.java / AutorizacionUpdateRequest.java / AutorizacionEstadoRequest.java
│   ├── AutorizacionResponse.java / AutorizacionDetailResponse.java
│   ├── VerificacionDocumentoResponse.java # DTO público con enmascaramiento de datos personales
│   ├── DocumentoEstadisticasResponse.java # Métricas de vigencias y estados documentales
│   └── DocumentoFormalizacionRequest.java # Formalización física y archivo documental
│
└── templates/admin/mercados/
    ├── contratos/ (listado.html, formulario.html, detalle.html)
    ├── autorizaciones/ (listado.html, formulario.html, detalle.html)
    └── documentos/ (vencimientos.html, verificacion.html)
```

### 1. Distinción Jurídico-Administrativa
- **Contrato de Ocupación:** Instrumento sinalagmático y bilateral que formaliza la relación de uso del espacio comercial entre la Municipalidad Distrital de Quinua y el comerciante adjudicatario, previa asignación válida. Requiere elaboración de borrador, revisión técnico-legal, aprobación y formalización física documentada con hash criptográfico SHA-256.
- **Autorización Comercial:** Acto administrativo habilitante unilateral emitido por la autoridad municipal competente (Subgerencia de Comercialización), sustentado en el TUPA institucional y previa evaluación de cumplimiento normativo.

```mermaid
stateDiagram-v2
    [*] --> BORRADOR
    BORRADOR --> EN_REVISION: Enviar a Revisión
    EN_REVISION --> OBSERVADO: Observar Términos
    OBSERVADO --> BORRADOR: Corregir Observaciones
    EN_REVISION --> APROBADO: Aprobar Términos
    APROBADO --> PENDIENTE_FORMALIZACION: Trámite Notarial/Firmas
    APROBADO --> VIGENTE: Formalizar y Activar
    PENDIENTE_FORMALIZACION --> VIGENTE: Formalizar y Activar
    VIGENTE --> SUSPENDIDO: Medida Cautelar / Suspensión
    SUSPENDIDO --> VIGENTE: Levantar Suspensión
    VIGENTE --> VENCIDO: Plazo Expirado
    VIGENTE --> TERMINADO: Conclusión Anticipada / Mutuo Disenso
    BORRADOR --> ANULADO: Desistimiento / Vicio
    EN_REVISION --> ANULADO: Nulidad de Oficio
    APROBADO --> ANULADO: Nulidad de Oficio
```

### 2. Máquina de Estados de Autorizaciones Comerciales
- `BORRADOR` $\rightarrow$ `EN_EVALUACION` $\rightarrow$ (`OBSERVADA` $\leftrightarrow$ `EN_EVALUACION`) $\rightarrow$ `APROBADA` $\rightarrow$ `VIGENTE` (Emitida con código QR público tokenizado) $\rightarrow$ `SUSPENDIDA` / `VENCIDA` / `REVOCADA` / `ANULADA`.

### 3. Seguridad, Verificación QR y Protección de Datos Personales
- **Ruta Pública Tokenizada (`/verificar/mercados/documento/{codigoSeguro}`):** Cualquier ciudadano puede escanear el código QR del documento PDF generado. El sistema no expone identificadores primarios (UUID), DNI, RUC ni domicilios. El titular se presenta anonimizado (ej. `C***o Q***e`) conforme a la **Ley de Protección de Datos Personales N° 29733**.
- **Hash Criptográfico SHA-256:** Cada documento PDF formalizado o emitido incluye su firma hash inmutable calculada sobre sus metadatos y contenido seguro, garantizando integridad documental.
- **Prevención de Solapamiento:** Validación de concurrencia y restricción temporal en PostgreSQL para impedir contratos o autorizaciones vigentes superpuestas sobre el mismo puesto comercial.

### 4. Permisos Granulares RBAC
- `MERCADOS_CONTRATO_VER`, `MERCADOS_CONTRATO_CREAR`, `MERCADOS_CONTRATO_EDITAR`, `MERCADOS_CONTRATO_REVISAR`, `MERCADOS_CONTRATO_APROBAR`, `MERCADOS_CONTRATO_FORMALIZAR`, `MERCADOS_CONTRATO_SUSPENDER`, `MERCADOS_CONTRATO_TERMINAR`.
- `MERCADOS_AUTORIZACION_VER`, `MERCADOS_AUTORIZACION_CREAR`, `MERCADOS_AUTORIZACION_EVALUAR`, `MERCADOS_AUTORIZACION_APROBAR`, `MERCADOS_AUTORIZACION_EMITIR`, `MERCADOS_AUTORIZACION_SUSPENDER`, `MERCADOS_AUTORIZACION_REVOCAR`.
- `MERCADOS_DOCUMENTO_VERIFICAR`, `MERCADOS_DOCUMENTO_EXPORTAR`.

### 5. Cobertura de Pruebas Automatizadas (21/21 - 100% Exitosas)
- **`MercadoContratoServiceTest` (8 pruebas):**
  1. `testCrearBorradorContrato`: Registro inicial con correlativo anual institucional (`CONT-YYYY-NNNNNN`).
  2. `testCicloDeVidaCompletoContrato`: Transiciones `BORRADOR` $\rightarrow$ `EN_REVISION` $\rightarrow$ `OBSERVADO` $\rightarrow$ `APROBADO` $\rightarrow$ `VIGENTE` $\rightarrow$ `SUSPENDIDO` $\rightarrow$ `TERMINADO`.
  3. `testRechazarTransicionInvalida`: Imposibilidad de formalizar contratos en estado borrador.
  4. `testValidarFechasIncompatibles`: Rechazo de fecha fin anterior a fecha de inicio.
  5. `testDetectarSolapamientoContratosVigentes`: Bloqueo inmediato de contratos superpuestos sobre el mismo puesto.
  6. `testGenerarPdfContrato`: Generación de PDF oficial con cabecera `%PDF-`, código QR y hash SHA-256.
  7. `testControlVencimientos`: Consulta de contratos vencidos y cálculo de alertas de expiración.
  8. `testExportarContratos`: Generación de padrón exportable en CSV y PDF binario.
- **`MercadoAutorizacionServiceTest` (6 pruebas):**
  9. `testCrearBorradorAutorizacion`: Solicitud con numeración `AUT-YYYY-NNNNNN`.
  10. `testCicloDeVidaAutorizacion`: Transiciones `BORRADOR` $\rightarrow$ `EN_EVALUACION` $\rightarrow$ `OBSERVADA` $\rightarrow$ `APROBADA` $\rightarrow$ `VIGENTE` $\rightarrow$ `REVOCADA`.
  11. `testGenerarPdfAutorizacion`: Emisión de resolución habilitante con QR público.
  12. `testVerificacionDocumentalTokenizada`: Validación pública con token alfanumérico seguro.
  13. `testMonitoreoVencimientosAutorizaciones`: Detección de autorizaciones con vigencia por expirar.
  14. `testExportarAutorizaciones`: Exportación de autorizaciones a CSV y PDF.
- **`MercadoDocumentoWebSecurityTest` (7 pruebas):**
  15. `test01_rutaPublicaVerificacionQr_sinAutenticacion`: Verificación QR accesible anónimamente (200 OK) protegiendo datos personales.
  16. `test02_accesoAutorizadoListarContratos`: Acceso con token JWT y permiso `MERCADOS_CONTRATO_VER`.
  17. `test03_rechazarAccesoSinPermiso`: Rechazo 403 Forbidden para usuarios sin permisos de mercados.
  18. `test04_proteccionCsrfRechazaPostSinToken`: Rechazo 403 ante mutaciones POST sin token CSRF.
  19. `test05_renderizadoWebBandejaContratos`: Renderizado de vista Thymeleaf `/dashboard/mercados/contratos`.
  20. `test06_renderizadoWebBandejaAutorizaciones`: Renderizado de vista Thymeleaf `/dashboard/mercados/autorizaciones`.
  21. `test07_renderizadoWebControlVencimientos`: Renderizado de vista Thymeleaf `/dashboard/mercados/documentos/vencimientos`.

---

## 38. Módulo de Mercados — Control de Pagos, Cuotas y Obligaciones Comerciales (Prompt 28)

En el **Prompt 28**, se implementó el submódulo financiero y de recaudación comercial de mercados municipales para la **Municipalidad Distrital de Quinua**. Este componente administra de manera inmutable y transaccional las obligaciones económicas, el calendario de cuotas, las sesiones de caja, los pagos totales o parciales, los reversos autorizados, las cuentas corrientes y los recibos de pago con verificación mediante código QR.

### 1. Principios y Reglas de Negocio Financiero
- **Aislamiento Comercial vs. Tributario:** Este submódulo administra exclusivamente conceptos comerciales (uso de puesto comercial, servicios de mantenimiento, limpieza, derechos de renovación). No sustituye ni se confunde con el módulo de Recaudación Tributaria (impuesto predial, arbitrios).
- **Inmutabilidad de Pagos:** Todo pago registrado en caja es estrictamente inmutable. No se permite la edición ni eliminación de pagos confirmados. En caso de error operativo justificado, se aplica un **Reverso Autorizado** que genera un movimiento compensatorio en la cuenta corriente.
- **Precisión Monetaria Estricta:** Uso mandatario de `BigDecimal` con escala a 2 decimales y redondeo bancario (`RoundingMode.HALF_UP`). Prohibido el uso de tipos de coma flotante (`double`, `float`).
- **Control de Sobrepago:** No se aceptan pagos que excedan el saldo exigible de las obligaciones comerciales asignadas, previniendo saldos negativos arbitrarios.
- **Trazabilidad de Caja y Arqueo:** Todas las operaciones de cobro en efectivo o bancario se vinculan a una sesión de caja activa abierta por el cajero responsable. Al momento del cierre, se genera el balance con cálculo de faltantes o sobrantes declarados.

### 2. Diagrama de Flujo Económico y Estados de Obligaciones
```mermaid
stateDiagram-v2
    [*] --> PENDIENTE: Emisión de Obligación
    PENDIENTE --> PARCIALMENTE_PAGADA: Pago Parcial (< Saldo)
    PARCIALMENTE_PAGADA --> PARCIALMENTE_PAGADA: Abono Parcial Subsecuente
    PENDIENTE --> PAGADA: Pago Total (Saldo = 0)
    PARCIALMENTE_PAGADA --> PAGADA: Cancelación Total de Saldo
    PENDIENTE --> VENCIDA: Fecha Actual > Fecha Vencimiento
    PARCIALMENTE_PAGADA --> VENCIDA: Saldo Impago al Vencimiento
    PENDIENTE --> ANULADA: Anulación Administrativa Sustentada
    VENCIDA --> PAGADA: Regularización de Deuda Vencida
    PAGADA --> PENDIENTE: Reverso Autorizado de Pago
```

### 3. Modelo de Entidades y Base de Datos (Migración `V30`)
- **`ConceptoCobroMercado` (`mercados_conceptos_cobro`):** Catálogo administrable de conceptos autorizados (`USO_PUESTO_COMERCIAL`, `SERVICIO_AUTORIZADO`, `OBLIGACION_CONTRACTUAL`, `DERECHO_ADMINISTRATIVO`, `OTRO`).
- **`TarifaMercado` (`mercados_tarifas`):** Estructura tarifaria con control de vigencias temporales (`fecha_inicio_vigencia`, `fecha_fin_vigencia`), sustento normativo y vinculación opcional por mercado y tipo de puesto comercial.
- **`ObligacionMercado` (`mercados_obligaciones`):** Título de deuda comercial con correlativo anual (`OBL-YYYY-NNNNNN`), comerciante titular, puesto asociado, monto original, saldo y estado financiero.
- **`CuotaObligacionMercado` (`mercados_cuotas_obligacion`):** Cronograma de pagos programados con número de cuota, fecha de vencimiento y saldos individuales amortizables.
- **`CajaMercado` y `SesionCajaMercado` (`mercados_cajas`, `mercados_cajas_sesiones`):** Control físico y lógico de puntos de recaudación municipal, aperturas y arqueos de cierre.
- **`PagoMercado` y `AplicacionPagoMercado` (`mercados_pagos`, `mercados_pagos_aplicaciones`):** Comprobante de pago con correlativo inmutable (`REC-YYYY-NNNNNN`), medio de pago (`EFECTIVO`, `TRANSFERENCIA_BANCARIA`, `DEPOSITO_CUENTA`, etc.), aplicaciones atómicas a cuotas u obligaciones y hash criptográfico SHA-256.
- **`ReversoPagoMercado` (`mercados_pagos_reversos`):** Acto administrativo de compensación con motivo, resolución sustentatoria y fecha de ejecución.
- **`MovimientoCuentaCorrienteMercado` (`mercados_cuenta_corriente_movimientos`):** Libro mayor de cuenta corriente comercial por comerciante con discriminación de cargos (emisión de deuda) y abonos (pagos y reversos).

### 4. Seguridad, Verificación QR y Prevención de Fraude
- **Ruta Pública de Verificación:** `/verificar/mercados/recibo/{codigoVerificacion}`. El código QR impreso en la constancia de pago permite a cualquier fiscalizador o comerciante consultar la autenticidad del pago directamente en el servidor municipal sin requerir sesión iniciada.
- **Protección de Datos Personales (Ley N° 29733):** La verificación pública anonimiza al titular (ej. `J*** P***z`) y nunca expone cuentas bancarias ni documentos de identidad completos.
- **Integridad Hash:** Se genera una firma SHA-256 inmutable almacenada en base de datos para validar que el recibo no ha sufrido adulteraciones en sus importes.
- **Protección Anticoncurrencia e Idempotencia:** Bloqueos pesimistas en base de datos (`PESSIMISTIC_WRITE`) y restricciones únicas en PostgreSQL para evitar dobles cobros o aplicaciones simultáneas del mismo pago.

### 5. Permisos Granulares RBAC Implementados
1. `MERCADOS_FINANZAS_VER`
2. `MERCADOS_CONCEPTO_GESTIONAR`
3. `MERCADOS_TARIFA_GESTIONAR`
4. `MERCADOS_OBLIGACION_VER`
5. `MERCADOS_OBLIGACION_CREAR`
6. `MERCADOS_OBLIGACION_AJUSTAR`
7. `MERCADOS_PAGO_VER`
8. `MERCADOS_PAGO_REGISTRAR`
9. `MERCADOS_PAGO_REVERSAR`
10. `MERCADOS_CAJA_ABRIR`
11. `MERCADOS_CAJA_CERRAR`
12. `MERCADOS_CONCILIACION_GESTIONAR`
13. `MERCADOS_FINANZAS_EXPORTAR`
14. `MERCADOS_FINANZAS_AUDITORIA_VER`

### 6. Cobertura de Pruebas Automatizadas (23/23 - 100% Exitosas)
- **`MercadoFinanzasServiceTest` (9 pruebas):**
  1. `test01_crearConceptoCobro`: Registro de concepto comercial y validación de unicidad de código.
  2. `test02_crearTarifaMercado_montoNegativo_lanzaExcepcion`: Rechazo estricto de importes menores o iguales a cero.
  3. `test03_generarObligacionConCuotas`: Creación de obligación con desglose en 3 cuotas sincronizadas con el monto programado.
  4. `test04_registrarPagoParcial`: Pago parcial que amortiza saldo y transiciona la obligación a `PARCIALMENTE_PAGADA`.
  5. `test05_registrarPagoCompleto`: Pago del saldo exacto que transiciona la obligación a `PAGADA` con saldo 0.00.
  6. `test06_rechazarSobrepago`: Rechazo de pagos superiores a la deuda pendiente de la obligación.
  7. `test07_aplicarPagoAVariasObligaciones`: Distribución multi-obligación del monto recibido respetando el saldo de cada una.
  8. `test08_reversarPagoAutorizado`: Ejecución de reverso administrativo, restauración del saldo pendiente y registro compensatorio.
  9. `test09_cuentaCorrienteComerciante`: Consulta de saldos, obligaciones exigibles e historial cronológico de movimientos.
- **`MercadoCajaYReporteTest` (6 pruebas):**
  10. `test01_abrirSesionCaja`: Apertura válida con monto inicial asignado al cajero.
  11. `test02_abrirSesionCaja_duplicada_lanzaExcepcion`: Rechazo de aperturas simultáneas en una misma caja o por el mismo cajero.
  12. `test03_cerrarSesionCaja_conDiferencia`: Cierre de caja con balance entre efectivo recaudado y monto final declarado.
  13. `test04_generarConstanciaPdf`: Generación de recibo oficial en formato OpenPDF con verificación QR y firma hash.
  14. `test05_verificacionPublicaQr`: Consulta pública del estado y autenticidad del recibo mediante código seguro.
  15. `test06_estadisticasYReportesFinancieros`: Cálculo de indicadores de recaudación consolidada y exportación en CSV y PDF.
- **`MercadoFinanzasWebSecurityTest` (8 pruebas):**
  16. `test01_dashboardFinanciero_autorizado`: Renderizado de vista Thymeleaf `/dashboard/mercados/finanzas` para usuario con `MERCADOS_FINANZAS_VER`.
  17. `test02_obligacionesListado_autorizado`: Renderizado de vista Thymeleaf `/dashboard/mercados/obligaciones`.
  18. `test03_pagosListado_autorizado`: Renderizado de vista Thymeleaf `/dashboard/mercados/pagos`.
  19. `test04_cajaListado_autorizado`: Renderizado de vista Thymeleaf `/dashboard/mercados/caja`.
  20. `test05_apiConceptos_autorizado`: Acceso a la API REST de conceptos con token JWT y permisos.
  21. `test06_apiConceptos_rechazaSinPermiso`: Rechazo 403 Forbidden para usuario sin privilegios financieros.
  22. `test07_apiPagos_rechazaSinPermiso`: Bloqueo de cobros para roles no autorizados.
  23. `test08_proteccionCsrf_postSinToken`: Bloqueo 403 ante peticiones de mutación financiera sin token CSRF.

---

## 39. Módulo de Mercados — Inspecciones y Supervisión de Mercados Municipales (Prompt 29)

En el **Prompt 29**, se implementó el sistema integral de **Inspecciones y Supervisión de Mercados Municipales** para la **Municipalidad Distrital de Quinua** (`pe.gob.municipalidad.sigm.modules.mercados`), diseñado bajo estrictos criterios de fiscalización administrativa peruana, no punitiva por defecto, garantizando la trazabilidad probatoria, debida motivación y el debido procedimiento administrativo.

### 1. Principios y Reglas de Negocio de Supervisión
- **Diferenciación de Alcances Técnicos:** Se distinguen 5 tipos de inspección configurables (`PROGRAMADA`, `EXTRAORDINARIA`, `SEGUIMIENTO`, `DOCUMENTAL`, `INFRAESTRUCTURA`). No todas las inspecciones tienen naturaleza sancionadora ni sustituyen las Inspecciones Técnicas de Seguridad en Edificaciones (ITSE) de competencia provincial o de Defensa Civil.
- **Integridad Territorial y Jerárquica:** Se valida estrictamente que los sectores, pabellones, puestos y comerciantes pertenezcan al mismo mercado municipal inspeccionado. Se permite supervisar todo el mercado o un ámbito específico (sector, pabellón o puesto individual).
- **Listas de Verificación y Versionado Institucional:** Catálogo de plantillas administrables (`mercados_plantillas_inspeccion`) con ítems ponderados y categorizados. Cada inspección congela y preserva la versión utilizada, sin alterar auditorías históricas ante eventuales modificaciones de plantillas.
- **Evidencias Probatorias con Hash de Integridad SHA-256:** Registro seguro de archivos fotográficos y documentales con cálculo de hash SHA-256 inmutable, validación MIME, cuotas de tamaño y almacenamiento privado (sin Base64 en tablas operativas).
- **Medidas Correctivas y Doble Instancia de Verificación:** Los hallazgos con gravedad técnica fundamentada originan medidas correctivas con fecha límite perentoria. El comerciante o responsable presenta la subsanación con descargos y evidencias; sin embargo, la aprobación o rechazo de la subsanación es potestad exclusiva del inspector competente autorizado mediante verificación in situ o documental independiente.
- **Inspecciones de Seguimiento Vinculadas:** Permite generar nuevas visitas de fiscalización vinculadas a la inspección de origen (`inspeccion_origen_id`) sin alterar el acta original.
- **Actas Oficiales con OpenPDF, Firma Hash y Verificación QR Pública:** Emisión formal del Acta de Inspección numerada (`ACTA-INS-YYYY-NNNNNN`), con resumen, conclusiones, recomendaciones, código QR seguro y ruta pública de validación tokenizada: `/verificar/mercados/acta/{codigoVerificacion}`.

### 2. Diagrama de Flujo de Estados
```mermaid
stateDiagram-v2
    [*] --> BORRADOR: Registro Inicial
    BORRADOR --> PROGRAMADA: Fijar Fecha, Hora y Objetivo
    PROGRAMADA --> ASIGNADA: Asignar Inspectores y Equipo
    ASIGNADA --> EN_PROCESO: Inicio In Situ de la Supervisión
    EN_PROCESO --> PENDIENTE_REVISION: Conclusión de Verificaciones y Hallazgos
    PENDIENTE_REVISION --> OBSERVADA: Observaciones de Control Interno
    OBSERVADA --> EN_PROCESO: Subsanación de Diligencia
    PENDIENTE_REVISION --> FINALIZADA: Aprobación y Emisión de Acta Oficial
    PROGRAMADA --> CANCELADA: Cancelación Motivada
    ASIGNADA --> CANCELADA: Cancelación Motivada
    BORRADOR --> CANCELADA: Desistimiento
```

### 3. Diagrama de Estados de Medidas Correctivas
```mermaid
stateDiagram-v2
    [*] --> PENDIENTE: Registro del Hallazgo
    PENDIENTE --> EN_PROCESO: Notificación e Inicio de Plazo
    EN_PROCESO --> PRESENTADA_PARA_VERIFICACION: Descargo y Evidencias Presentadas
    PRESENTADA_PARA_VERIFICACION --> SUBSANADA: Verificación Favorable por Inspector
    PRESENTADA_PARA_VERIFICACION --> NO_SUBSANADA: Rechazo Técnico por Insuficiencia
    NO_SUBSANADA --> EN_PROCESO: Prórroga o Reiteración de Medida
    PENDIENTE --> CANCELADA: Dejar Sin Efecto por Sustento
    EN_PROCESO --> CANCELADA: Dejar Sin Efecto por Sustento
```

### 4. Modelo de Datos y Migración (`V31__mercados_inspecciones_supervision.sql`)
- **Secuencias Operativas Seguras:** `mercados_inspecciones_seq`, `mercados_actas_inspeccion_seq`.
- **`PlantillaInspeccion` (`mercados_plantillas_inspeccion`):** Plantillas institucionales con código, nombre, tipo de inspección, versión, oficina responsable y sustento normativo.
- **`ItemVerificacionPlantilla` (`mercados_items_verificacion`):** Criterios evaluables con categoría, orden, obligatoriedad y referencia legal.
- **`InspeccionMercado` (`mercados_inspecciones`):** Registro maestro de la diligencia con código (`INS-MER-YYYY-NNNNNN`), alcance territorial, fechas programadas/reales, equipo inspector y estado.
- **`InspeccionParticipante` (`mercados_inspecciones_participantes`):** Equipos multidisciplinarios de fiscalización con roles (`INSPECTOR_LIDER`, `INSPECTOR_ADJUNTO`, `ESPECIALISTA_TECNICO`, `SECRETARIO_ACTUARIO`).
- **`RespuestaVerificacionInspeccion` (`mercados_inspecciones_verificaciones`):** Respuestas registradas (`CONFORME`, `NO_CONFORME`, `NO_APLICA`, `NO_EVALUADO`) con observaciones de campo.
- **`HallazgoInspeccionMercado` (`mercados_hallazgos_inspeccion`):** Hallazgos detectados con código `HAL-NNN`, categoría técnica, gravedad (`LEVE`, `MODERADA`, `GRAVE`, `MUY_GRAVE`) y estado.
- **`EvidenciaInspeccionMercado` (`mercados_evidencias_inspeccion`):** Fotografías y documentos probatorios con hash SHA-256, tipo MIME y georreferenciación/descripción.
- **`MedidaCorrectivaMercado` (`mercados_medidas_correctivas`):** Obligaciones de adecuación con fecha límite, responsable asignado y control de plazos vencidos.
- **`SubsanacionMedidaMercado` (`mercados_subsanaciones_medida`):** Registro inmutable de descargos, evidencias de subsanación y dictamen técnico de verificación.
- **`ActaInspeccionMercado` (`mercados_actas_inspeccion`):** Instrumento documental formal con código `ACTA-INS-YYYY-NNNNNN`, hash SHA-256, token QR y estado (`BORRADOR`, `EN_REVISION`, `APROBADA`, `EMITIDA`, `ANULADA`).

### 5. Permisos Granulares RBAC Implementados
1. `MERCADOS_INSPECCION_VER`
2. `MERCADOS_INSPECCION_CREAR`
3. `MERCADOS_INSPECCION_PROGRAMAR`
4. `MERCADOS_INSPECCION_ASIGNAR`
5. `MERCADOS_INSPECCION_EJECUTAR`
6. `MERCADOS_INSPECCION_HALLAZGO_REGISTRAR`
7. `MERCADOS_INSPECCION_EVIDENCIA_GESTIONAR`
8. `MERCADOS_INSPECCION_MEDIDA_GESTIONAR`
9. `MERCADOS_INSPECCION_SUBSANACION_VERIFICAR`
10. `MERCADOS_INSPECCION_ACTA_ELABORAR`
11. `MERCADOS_INSPECCION_ACTA_APROBAR`
12. `MERCADOS_INSPECCION_REPORTES`
13. `MERCADOS_INSPECCION_AUDITORIA_VER`

### 6. Cobertura de Pruebas Automatizadas (23/23 - 100% Exitosas)
- **`MercadoInspeccionServiceTest` (9 pruebas unitarias y de negocio):**
  1. `test01_crearInspeccion_valida`: Registro ordinario de inspección con correlativo `INS-MER-2026-000001` y estado `PROGRAMADA`.
  2. `test02_crearInspeccion_incompatibilidadMercadoSector_lanzaExcepcion`: Rechazo estricto ante inconsistencia territorial entre mercado y sector.
  3. `test03_asignarInspectores`: Asignación de inspector principal y colaboradores sin duplicados.
  4. `test04_iniciarInspeccion`: Transición operativa a `EN_PROCESO` registrando fecha/hora in situ.
  5. `test05_rechazarInicio_inspeccionCancelada`: Bloqueo de inicio sobre inspecciones canceladas o finalizadas.
  6. `test06_registrarVerificaciones`: Calificación de ítems de la lista de verificación institucional.
  7. `test07_registrarHallazgo`: Registro de hallazgo técnico categorizado y con gravedad objetiva.
  8. `test08_registrarMedidaCorrectiva`: Determinación de medida correctiva con fecha límite perentoria.
  9. `test09_presentarYVerificarSubsanacion`: Ciclo de presentación de descargos ciudadanos y verificación técnica aprobatoria por inspector.
- **`MercadoActaYReporteTest` (6 pruebas de actas, exportaciones y métricas):**
  10. `test01_elaborarBorradorActa`: Redacción de acta en estado `BORRADOR` con correlativo institucional.
  11. `test02_aprobarYEmitirActa`: Emisión oficial del acta, cálculo del hash SHA-256 y finalización de la inspección.
  12. `test03_generarPdfActa`: Generación del documento binario OpenPDF con firma `%PDF-`, código QR y hash embebido.
  13. `test04_verificacionPublicaActa`: Consulta tokenizada sin autenticación para corroborar autenticidad institucional.
  14. `test05_estadisticasSupervision`: Consolidación estadística de indicadores de gestión, hallazgos y medidas vencidas.
  15. `test06_exportarExcel`: Exportación tabular estructurada en formato XLSX con Apache POI.
- **`MercadoInspeccionWebSecurityTest` (8 pruebas de integración web y seguridad):**
  16. `test01_dashboardSupervision_autorizado`: Renderizado Thymeleaf de `/dashboard/mercados/inspecciones/dashboard`.
  17. `test02_inspeccionesListado_autorizado`: Renderizado Thymeleaf de la bandeja principal con filtros y paginación.
  18. `test03_calendario_autorizado`: Renderizado de vista de calendario de supervisiones programadas.
  19. `test04_seguimientoMedidas_autorizado`: Renderizado de la consola de seguimiento de medidas correctivas.
  20. `test05_apiInspecciones_autorizado`: Acceso a la API REST `/api/mercados/inspecciones` con JWT y rol administrativo.
  21. `test06_apiInspecciones_rechazaSinPermiso`: Rechazo 403 Forbidden para ciudadano sin privilegios fiscalizadores.
  22. `test07_verificacionPublicaQr_sinAutenticacion`: Verificación pública anónima (200 OK) protegiendo datos sensibles.
  23. `test08_proteccionCsrf_postSinToken`: Bloqueo 403 ante mutaciones HTTP POST sin token CSRF.

---

## 38. Registro de Incidencias y Presuntas Infracciones en Mercados Municipales (Prompt 30)

### 1. Marco Normativo y Principios Rectores
El módulo de **Incidencias y Presuntas Infracciones en Mercados Municipales** de SIGM-QUINUA implementa la fiscalización, supervisión operativa y control de conductas en los centros de abasto municipales de Quinua, bajo el estricto cumplimiento de:
- **TUO de la Ley N° 27444 — Ley del Procedimiento Administrativo General (D.S. N° 004-2019-JUS):** Principios de legalidad, debido procedimiento, tipicidad, presunción de licitud y verdad material.
- **Ley N° 27972 — Ley Orgánica de Municipalidades:** Autonomía municipal en la regulación y administración de mercados públicos y salubridad.
- **Régimen de Aplicación de Sanciones Administrativas (RASA) y Cuadro Único de Infracciones y Sanciones (CUIS) de Quinua.**

> [!IMPORTANT]
> **Garantía del Debido Procedimiento y Separación de Fases:**
> El registro de una presunta infracción en este módulo **NO genera multas automáticas ni deudas económicas**. Constituye una diligencia de constatación preliminar que, tras evaluación motivada y recepción de descargos voluntarios, puede ser derivada a la Subgerencia de Fiscalización Administrativa para el inicio del Procedimiento Administrativo Sancionador (PAS) formal.

---

### 2. Diagrama de Flujo del Ciclo de Vida de Incidencias
```mermaid
stateDiagram-v2
    [*] --> REGISTRADA: Reporte In Situ / Mesa de Partes / Inspección
    REGISTRADA --> EN_ATENCION: Asignación de Responsable y Oficina
    EN_ATENCION --> EN_ATENCION: Actuaciones y Diligencias de Campo
    EN_ATENCION --> DERIVADA_A_INFRACCION: Presunción Fundada de Faltas al Reglamento
    EN_ATENCION --> RESUELTA: Subsanación / Reparación / Atención Concluida
    REGISTRADA --> DESCARTADA: Inconsistencia / Hecho No Acreditado
    EN_ATENCION --> DESCARTADA: Inconsistencia / Hecho No Acreditado
    RESUELTA --> CERRADA: Conformidad Administrativa de la Administración
    RESUELTA --> EN_ATENCION: Reapertura Motivada por Recurrencia
    CERRADA --> [*]
    DESCARTADA --> [*]
```

---

### 3. Diagrama de Flujo de Presuntas Infracciones y Debido Proceso
```mermaid
stateDiagram-v2
    [*] --> REGISTRADA: Detección en Supervisión / Derivación de Incidencia
    REGISTRADA --> EN_EVALUACION: Inicio de Verificación Preliminar
    EN_EVALUACION --> PENDIENTE_INFORMACION: Requerimiento de Datos Complementarios
    PENDIENTE_INFORMACION --> EN_EVALUACION: Información Aportada
    EN_EVALUACION --> EN_EVALUACION: Registro de Descargo y Pruebas del Comerciante
    EN_EVALUACION --> DERIVADA_A_PROCEDIMIENTO: Decisión: Derivación Formal al PAS (Fiscalización)
    EN_EVALUACION --> SUBSANADA_PREVIAMENTE: Decisión: Subsanación Voluntaria Oportuna Acreditada
    EN_EVALUACION --> ARCHIVADA_DESESTIMADA: Decisión: Hecho No Configurado / Falta de Pruebas
    EN_EVALUACION --> DESCARTADA: Decisión: Descarte Justificado
    DERIVADA_A_PROCEDIMIENTO --> [*]
    SUBSANADA_PREVIAMENTE --> [*]
    ARCHIVADA_DESESTIMADA --> [*]
    DESCARTADA --> [*]
```

---

### 4. Modelo de Datos y Migración (`V32__mercados_incidencias_infracciones.sql`)
- **Secuencias Correlativas:** `mercados_incidencias_seq`, `mercados_infracciones_seq`.
- **`TipoIncidenciaMercado` (`mercados_tipos_incidencia`):** Catálogo clasificado de incidencias (Salubridad, Seguridad, Infraestructura, Convivencia, etc.) con prioridad sugerida y exigencia de puesto.
- **`IncidenciaMercado` (`mercados_incidencias`):** Entidad central con código institucional (`INC-MER-YYYY-NNNNNN`), vinculación territorial (mercado, sector, pabellón, puesto, comerciante), origen (`INSPECCION_IN_SITU`, `RECLAMO_COMERCIANTE`, `DENUNCIA_VECINAL`, `OFICIO_INTERNO`, `OPERATIVO_MUNICIPAL`), estado y fechas de atención.
- **`ActuacionIncidenciaMercado` (`mercados_incidencias_actuaciones`):** Bitácora inmutable de diligencias y actuaciones operativas (`NOTIFICACION`, `INSPECCION_CAMPO`, `SUBSANACION`, `DERIVACION`, etc.).
- **`EvidenciaIncidenciaMercado` (`mercados_incidencias_evidencias`):** Registro de fotos, actas escaneadas y documentos con hash SHA-256 y descripción probatoria.
- **`TipificacionInfraccion` (`mercados_tipificaciones_infraccion`):** Catálogo formal de conductas infractoras con base normativa municipal (código, base legal, gravedad: `LEVE`, `GRAVE`, `MUY_GRAVE`, medidas preventivas aplicables).
- **`PresuntaInfraccionMercado` (`mercados_presuntas_infracciones`):** Constatación preliminar con código `INF-MER-YYYY-NNNNNN`, hechos descritos, medidas preventivas adoptadas, registro de descargos y sentido final de decisión.

---

### 5. Permisos Granulares RBAC (15 Permisos Especializados)
1. `MERCADOS_INCIDENCIA_VER` — Visualizar bandejas, fichas y expedientes de incidencias.
2. `MERCADOS_INCIDENCIA_CREAR` — Registrar nuevos reportes de incidencia.
3. `MERCADOS_INCIDENCIA_ASIGNAR` — Asignar servidores y oficinas responsables de atención.
4. `MERCADOS_INCIDENCIA_ATENDER` — Registrar actuaciones, diligencias y avances.
5. `MERCADOS_INCIDENCIA_RESOLVER` — Declarar subsanada o atendida la incidencia.
6. `MERCADOS_INCIDENCIA_CERRAR` — Dar cierre definitivo y conformidad administrativa.
7. `MERCADOS_INCIDENCIA_DESCARTAR` — Descartar motivadamente reportes improcedentes.
8. `MERCADOS_INCIDENCIA_REAPERTURAR` — Reabrir incidencias cerradas o resueltas ante reincidencia.
9. `MERCADOS_INCIDENCIA_DERIVAR` — Promover la incidencia a expediente de presunta infracción.
10. `MERCADOS_INCIDENCIA_REPORTES` — Acceder a estadísticas y exportaciones Excel/PDF.
11. `MERCADOS_INFRACCION_VER` — Consultar catálogo y expedientes de presuntas infracciones.
12. `MERCADOS_INFRACCION_REGISTRAR` — Levantar actas de presunta infracción en mercados.
13. `MERCADOS_INFRACCION_EVALUAR` — Calificar antecedentes, tipicidad y pruebas preliminares.
14. `MERCADOS_INFRACCION_DESCARGO` — Registrar descargos formales y alegatos del administrado.
15. `MERCADOS_INFRACCION_DECIDIR` — Emitir dictamen motivado (Derivación al PAS, Subsanada, Archivada o Descartada).

---

### 6. Catálogo de Endpoints Implementados

#### API REST (`/api/mercados/**`)
- `GET /api/mercados/tipos-incidencia` — Listar tipos de incidencias activos.
- `GET /api/mercados/tipos-incidencia/{id}` — Consultar detalle de tipo de incidencia.
- `GET /api/mercados/incidencias` — Búsqueda paginada con filtros multicriterio vía JPA Specifications.
- `POST /api/mercados/incidencias` — Registro oficial de incidencias.
- `GET /api/mercados/incidencias/{id}` — Detalle integral con actuaciones, evidencias e infracciones asociadas.
- `POST /api/mercados/incidencias/{id}/actuaciones` — Registro de diligencias y actuaciones de campo.
- `POST /api/mercados/incidencias/{id}/evidencias` — Registro probatorio de evidencias documentales y fotográficas.
- `PATCH /api/mercados/incidencias/{id}/asignar` — Asignación de servidor y unidad orgánica responsable.
- `PATCH /api/mercados/incidencias/{id}/resolver` — Registro de resolución de la contingencia.
- `PATCH /api/mercados/incidencias/{id}/cerrar` — Cierre y archivo administrativo.
- `PATCH /api/mercados/incidencias/{id}/descartar` — Descarte motivado.
- `PATCH /api/mercados/incidencias/{id}/reaperturar` — Reapertura fundamentada.
- `POST /api/mercados/incidencias/{id}/derivar` — Derivación formal a expediente de presunta infracción.
- `GET /api/mercados/tipificaciones` — Catálogo oficial de infracciones tipificadas.
- `GET /api/mercados/infracciones` — Búsqueda paginada de presuntas infracciones.
- `POST /api/mercados/infracciones` — Registro directo de presunta infracción.
- `GET /api/mercados/infracciones/{id}` — Ficha completa de presunta infracción.
- `PATCH /api/mercados/infracciones/{id}/evaluar` — Cambio de estado en la fase evaluadora.
- `POST /api/mercados/infracciones/{id}/descargo` — Incorporación formal de descargos y pruebas del administrado.
- `POST /api/mercados/infracciones/{id}/decision` — Emisión de pronunciamiento y derivación formal al PAS.

#### Vistas Web Thymeleaf (`/dashboard/mercados/**`)
- `/dashboard/mercados/incidencias/dashboard` — Panel de comando con métricas e indicadores en tiempo real.
- `/dashboard/mercados/incidencias` — Bandeja general de incidencias con filtros avanzados y paginación.
- `/dashboard/mercados/incidencias/nueva` — Formulario de registro con validaciones territoriales e institucionales.
- `/dashboard/mercados/incidencias/{id}` — Ficha técnica con bitácora cronológica y gestión de actuaciones.
- `/dashboard/mercados/incidencias/seguimiento` — Tablero operativo de control de incidencias activas.
- `/dashboard/mercados/incidencias/reportes` — Consola analítica de reportes gerenciales.
- `/dashboard/mercados/incidencias/reportes/excel` — Exportación binaria XLSX (Apache POI).
- `/dashboard/mercados/incidencias/reportes/pdf` — Reporte ejecutivo en PDF (OpenPDF).
- `/dashboard/mercados/infracciones` — Bandeja general de presuntas infracciones.
- `/dashboard/mercados/infracciones/nueva` — Formulario de detección y registro de presunta infracción.
- `/dashboard/mercados/infracciones/{id}` — Expediente preliminar con descargos y formulario de decisión.

---

### 7. Cobertura de Pruebas Automatizadas (29/29 - 100% Exitosas)
- **`MercadoIncidenciaServiceTest` (7 pruebas unitarias de servicio):**
  1. `test01_crearIncidencia_exitosa`: Registro y correlativo automático `INC-MER-2026-000001`.
  2. `test02_crearIncidencia_tipoRequierePuesto_sinPuesto_lanzaExcepcion`: Validación de consistencia de catálogo.
  3. `test03_crearIncidencia_inconsistenciaMercadoSector_lanzaExcepcion`: Validación de congruencia territorial.
  4. `test04_asignarResponsable`: Delegación a servidor municipal y cambio a `EN_ATENCION`.
  5. `test05_agregarActuacionYEvidencia`: Registro de bitácora y cálculo de hash probatorio.
  6. `test06_resolverYCerrarIncidencia`: Ciclo completo de atención y conformidad.
  7. `test07_reaperturarIncidencia`: Reapertura fundamentada con registro de actuaciones.
- **`MercadoInfraccionYReporteTest` (9 pruebas de presuntas infracciones y reportes):**
  8. `test01_registrarPresuntaInfraccion_exitosa`: Registro de presunta infracción con correlativo `INF-MER-2026-000001`.
  9. `test02_derivarIncidenciaAInfraccion`: Derivación motivada desde una incidencia operativa.
  10. `test03_iniciarEvaluacion`: Transición formal a fase de evaluación preliminar.
  11. `test04_registrarDescargo`: Derecho de defensa con registro de alegatos y documentos.
  12. `test05_decidirInfraccion_derivadaAProcedimiento`: Pronunciamiento derivando al PAS sin generar multa automática.
  13. `test06_decidirInfraccion_descartada`: Archivo motivado por hechos no acreditados.
  14. `test07_obtenerEstadisticas`: Consolidación analítica de indicadores de mercado.
  15. `test08_generarReporteExcel`: Generación binaria válida XLSX con cabeceras y estilos institucionales.
  16. `test09_generarReportePdf`: Generación binaria válida PDF con firma institucional OpenPDF.
- **`MercadoIncidenciaWebSecurityTest` (13 pruebas de integración web y seguridad):**
  17. `usuarioAnonimoEsRedirigidoALogin`: Redirección 302 ante accesos no autenticados.
  18. `ciudadanoSinPermisoRecibeForbiddenAlCrearIncidencia`: Rechazo 403 Forbidden a usuarios sin `MERCADOS_INCIDENCIA_CREAR`.
  19. `ciudadanoSinPermisoRecibeForbiddenAlCrearInfraccion`: Rechazo 403 Forbidden a usuarios sin `MERCADOS_INFRACCION_REGISTRAR`.
  20. `adminAccedeADashboardIncidencias`: Renderizado 200 OK del dashboard con métricas reales.
  21. `adminAccedeAListadoIncidencias`: Renderizado 200 OK de la bandeja con JPA Specifications.
  22. `adminAccedeAFormularioNuevaIncidencia`: Renderizado 200 OK del formulario institucional.
  23. `adminAccedeASeguimientoIncidencias`: Renderizado 200 OK del tablero operativo.
  24. `adminAccedeAReportesIncidencias`: Renderizado 200 OK de la consola de reportes.
  25. `adminDescargaReporteExcel`: Descarga binaria 200 OK con Content-Disposition XLSX.
  26. `adminDescargaReportePdf`: Descarga binaria 200 OK con Content-Disposition PDF.
  27. `adminAccedeAPresuntasInfracciones`: Renderizado 200 OK del listado de infracciones.
  28. `adminConsultaCatalogoTiposViaApi`: Acceso 200 OK con autenticación JWT al catálogo REST.
  29. `usuarioAnonimoRecibeUnauthorizedEnApi`: Respuesta 401 Unauthorized sin token JWT.

---

## 39. Módulo 2: Mercados Municipales — Submódulo de Renovaciones, Cambios de Titular y Transferencias Autorizadas (PROMPT 31 DE 150)

### 1. Marco Jurídico y Doctrinal
El presente submódulo implementa el régimen formal y reglamentario de la ocupación comercial en los mercados públicos de la Municipalidad Distrital de Quinua, bajo estricto apego a la normativa peruana:
- **Ley N° 27972 (Ley Orgánica de Municipalidades), Art. 83:** Los mercados municipales constituyen infraestructura y bienes de dominio público destinados al servicio público de abastecimiento local. Las autorizaciones de conducción y ocupación son **estrictamente temporales y no libremente transferibles**.
- **Texto Único Ordenado de la Ley N° 27444 (Ley del Procedimiento Administrativo General, D.S. N° 004-2019-JUS):** Regula los principios del debido procedimiento, verdad material, predictibilidad, derecho de contradicción, formalización documental y eficacia de los actos administrativos.
- **Ley N° 29733 (Ley de Protección de Datos Personales):** Protege la confidencialidad de la información personal de los comerciantes. En consultas públicas y códigos QR de verificación no se exponen DNIs, estados de deuda ni datos sensibles.

---

### 2. Principios y Distinciones Administrativas
1. **Renovación:** Prórroga o nueva vigencia autorizada de una asignación, contrato de ocupación o autorización municipal vigente, para el mismo titular empadronado que mantiene el cumplimiento de sus obligaciones. No altera la titularidad ni crea una persona distinta.
2. **Cambio de Titular:** Reconocimiento formal de un nuevo conductor sobre un puesto comercial, derivado de causales regladas y debidamente acreditadas (fallecimiento del titular original con declaratoria de herederos, incapacidad permanente sobreviniente justificada). Jamás se realiza editando el comerciante de una asignación histórica.
3. **Transferencia Autorizada:** Operación de cesión de posición jurídica de carácter estrictamente excepcional, permitida **únicamente cuando exista procedimiento institucional y ordenanza expresa vigente** (`permite_transferencia = true`). Por defecto, el sistema bloquea cualquier solicitud de transferencia no habilitada por acuerdo de Concejo o norma expresa.

---

### 3. Modelo de Datos y Migración Flyway (`V33__mercados_renovaciones_transferencias.sql`)
- **`seq_mercados_solicitud_gestion`:** Secuencia segura para correlativos `SOL-MER-YYYY-NNNNNN`.
- **`mercados_procedimientos_gestion`:** Catálogo de trámites configurables, base normativa, oficina competente, plazos y bandera `permite_transferencia`.
- **`mercados_solicitudes_gestion`:** Ficha integral de solicitud con referencias foráneas inmutables a mercado, puesto, titular actual, titular propuesto, instrumentos de origen e instrumentos resultantes.
- **`mercados_solicitudes_evaluaciones`:** Dictámenes técnico-legales emitidos por servidores municipales (FAVORABLE, DESFAVORABLE, OBSERVADA, REQUIERE_INFORMACION).
- **`mercados_solicitudes_observaciones`:** Registro formal de observaciones documentales y de requisitos.
- **`mercados_solicitudes_subsanaciones`:** Subsanaciones adjuntadas por el administrado con verificación de conformidad.
- **`mercados_historial_titularidad`:** Registro histórico inmutable de cambios de titular y renovaciones por cada puesto comercial.

---

### 4. Diagrama de Estados de la Solicitud de Gestión
```
[ BORRADOR ]
     │
     ▼
[ PRESENTADA ] ────────┐ (Detección de observaciones)
     │                 ▼
     │         [ OBSERVADA ]
     │                 │
     │                 ▼ (Subsanación de requisitos)
     │         [ PENDIENTE_SUBSANACION / EN_EVALUACION ]
     │                 │
     ▼                 ▼
[ EN_EVALUACION (Dictamen Técnico-Legal) ]
     ├──► [ RECHAZADA (Dictamen Desfavorable) ] ──► [ FINALIZADA ]
     │
     └──► [ APROBADA (Acto Resolutivo Favorable) ]
               │
               ▼
          [ PENDIENTE_FORMALIZACION ]
               │
               ▼ (Emisión de nuevo instrumento / extensión formal)
          [ FORMALIZADA ]
```

---

### 5. Reglas de Negocio Críticas Implementadas
1. **Inmutabilidad de Asignaciones Históricas:** La asignación de origen jamás sufre modificaciones en su `comerciante_id`. En cambios de titular o transferencias, la asignación previa se marca como `FINALIZADA` con fecha efectiva de cese, y se genera una **nueva asignación independiente** para el titular propuesto.
2. **Separación de Aprobación y Formalización:** La aprobación administrativa es un pronunciamiento declarativo de procedencia; la titularidad efectiva y la ocupación en sistema solo cambian cuando se formaliza mediante resolución, adenda o acta suscrita.
3. **Idempotencia y Prevención de Doble Ocupación:** Bloqueos a nivel transaccional y validación de unicidad que impiden registrar dos titulares simultáneos en un mismo puesto físico.
4. **No Traslado Automático de Obligaciones:** Las deudas u obligaciones comerciales devengadas por el titular saliente no se trasladan automáticamente al titular entrante, preservando el principio de causalidad y personalidad de las obligaciones.
5. **Verificación Pública QR con Anonimización:** Consulta pública bajo `/verificar/gestion-ocupacion/{token}` que acredita autenticidad del acto municipal sin exponer datos personales del comerciante.

---

### 6. Permisos RBAC Implementados
1. `MERCADOS_RENOVACION_VER` — Visualizar bandejas y expedientes de renovación.
2. `MERCADOS_RENOVACION_CREAR` — Registrar y presentar solicitudes de renovación.
3. `MERCADOS_RENOVACION_EVALUAR` — Calificar requisitos y emitir informe técnico de renovación.
4. `MERCADOS_RENOVACION_APROBAR` — Emitir acto administrativo de aprobación o rechazo de renovación.
5. `MERCADOS_CAMBIO_TITULAR_VER` — Consultar trámites de cambio de titular.
6. `MERCADOS_CAMBIO_TITULAR_CREAR` — Registrar solicitudes de cambio de titular por causa legítima.
7. `MERCADOS_CAMBIO_TITULAR_EVALUAR` — Dictaminar procedencia jurídica de cambios de titular.
8. `MERCADOS_CAMBIO_TITULAR_APROBAR` — Decidir formalmente sobre solicitudes de cambio de titular.
9. `MERCADOS_TRANSFERENCIA_VER` — Consultar bandejas de transferencias autorizadas.
10. `MERCADOS_TRANSFERENCIA_CREAR` — Solicitar transferencias excepcionales habilitadas.
11. `MERCADOS_TRANSFERENCIA_EVALUAR` — Calificar viabilidad de transferencias bajo normas expresas.
12. `MERCADOS_TRANSFERENCIA_APROBAR` — Pronunciarse sobre transferencias excepcionales autorizadas.
13. `MERCADOS_GESTION_OCUPACION_FORMALIZAR` — Formalizar decisiones y actualizar el registro de ocupación.
14. `MERCADOS_GESTION_OCUPACION_HISTORIAL_VER` — Consultar trazabilidad inmutable de titulares por puesto.
15. `MERCADOS_GESTION_OCUPACION_EXPORTAR` — Descargar consolidados y reportes en Excel y PDF.

---

### 7. Catálogo de Endpoints Implementados

#### API REST (`/api/mercados/**`)
- `GET /api/mercados/procedimientos-gestion` — Catálogo de procedimientos institucionales activos.
- `GET /api/mercados/renovaciones` — Búsqueda paginada y filtrada de solicitudes de renovación.
- `POST /api/mercados/renovaciones` — Registro de solicitud de renovación.
- `GET /api/mercados/cambios-titular` — Búsqueda paginada de solicitudes de cambio de titular.
- `POST /api/mercados/cambios-titular` — Registro de trámite de cambio de titular.
- `GET /api/mercados/transferencias` — Búsqueda paginada de transferencias autorizadas.
- `POST /api/mercados/transferencias` — Registro de trámite de transferencia excepcional.
- `GET /api/mercados/gestiones-ocupacion/{id}` — Detalle integral con observaciones, subsanaciones y evaluaciones.
- `POST /api/mercados/gestiones-ocupacion/{id}/presentar` — Presentación formal de solicitud en borrador.
- `POST /api/mercados/gestiones-ocupacion/{id}/observar` — Registro formal de observaciones de requisitos.
- `POST /api/mercados/gestiones-ocupacion/{id}/subsanar` — Subsanación y anexo de documentos de descargo.
- `POST /api/mercados/gestiones-ocupacion/{id}/evaluar` — Emisión de informe técnico-legal calificador.
- `POST /api/mercados/gestiones-ocupacion/{id}/aprobar` — Aprobación motivada por autoridad competente.
- `POST /api/mercados/gestiones-ocupacion/{id}/rechazar` — Desestimación motivada con notificación.
- `POST /api/mercados/gestiones-ocupacion/{id}/formalizar` — Formalización efectiva y actualización de ocupación.
- `GET /api/mercados/puestos/{puestoId}/historial-titularidad` — Línea de tiempo histórica del puesto.
- `GET /api/mercados/gestiones-ocupacion/estadisticas` — Métricas analíticas de gestión de ocupaciones.

#### Vistas Web Thymeleaf (`/dashboard/mercados/**` y `/verificar/**`)
- `/dashboard/mercados/gestiones-ocupacion/dashboard` — Panel de comando con estadísticas, gráficos e indicadores clave.
- `/dashboard/mercados/renovaciones` — Bandeja general de solicitudes de renovación de ocupación.
- `/dashboard/mercados/renovaciones/nueva` — Formulario de solicitud de renovación con autocompletado institucional.
- `/dashboard/mercados/cambios-titular` — Bandeja de solicitudes de cambio de titularidad.
- `/dashboard/mercados/cambios-titular/nuevo` — Formulario de trámite sucesorio o de reemplazo justificado.
- `/dashboard/mercados/transferencias` — Bandeja de transferencias de puestos de mercado.
- `/dashboard/mercados/transferencias/nueva` — Formulario de transferencias excepcionales reguladas.
- `/dashboard/mercados/gestiones-ocupacion/{id}` — Ficha técnica con pestañas de evaluación, bitácora y modales operativos.
- `/dashboard/mercados/gestiones-ocupacion/reportes/excel` — Exportación de reportes XLSX vía Apache POI.
- `/dashboard/mercados/gestiones-ocupacion/reportes/pdf` — Reporte ejecutivo en PDF vía LibrePDF OpenPDF.
- `/verificar/gestion-ocupacion/{token}` — Portal público de autenticidad y trazabilidad con código QR.

---

### 8. Cobertura de Pruebas Automatizadas (14/14 - 100% Exitosas)
- **`MercadoRenovacionServiceTest` (3 pruebas de ciclo de vida de renovaciones):**
  1. `testCrearRenovacionExitosamente`: Generación de correlativo `SOL-MER-YYYY-NNNNNN` y validación de datos.
  2. `testRechazarRenovacionInvalida`: Detección de incompatibilidades cronológicas y asignaciones inexistentes.
  3. `testFlujoCompletoRenovacion`: Flujo integral Presentada -> Observada -> Subsanada -> Evaluada -> Aprobada -> Formalizada con extensión de vigencia.
- **`MercadoCambioTitularYTransferenciaTest` (5 pruebas de cambio de titular y transferencias):**
  4. `testRechazarCambioTitularMismaPersona`: Regla de negocio que exige titular propuesto diferente al titular actual.
  5. `testRechazarTransferenciaNoPermitida`: Bloqueo automático cuando el procedimiento institucional prohíbe transferencias (`permite_transferencia = false`).
  6. `testAdmitirTransferenciaHabilitada`: Admisión de trámites cuando existe ordenanza u habilitación expresa (`permite_transferencia = true`).
  7. `testFormalizacionCambioTitularPreservaInmutabilidadYRegistraHistorial`: Cierre de asignación anterior, creación de nueva asignación para el cesionario y registro inmutable en `HistorialTitularidadPuesto`.
  8. `testImpedirDobleFormalizacion`: Idempotencia y control de concurrencia ante múltiples ejecuciones sobre la misma solicitud.
- **`MercadoGestionOcupacionWebSecurityTest` (6 pruebas de integración web y seguridad):**
  9. `testVerificacionPublicaQrSinAutenticacion`: Verificación anónima 200 OK en `/verificar/gestion-ocupacion/{token}` con anonimización de datos.
  10. `testBandejaRenovacionesAutorizada`: Acceso 200 OK con permiso `MERCADOS_RENOVACION_VER`.
  11. `testBandejaRenovacionesDenegada`: Bloqueo 403 Forbidden a usuarios sin atribución.
  12. `testDashboardConsolidado`: Renderizado 200 OK del panel de comando de gestiones de ocupación.
  13. `testDescargaReporteExcel`: Emisión binaria de hoja de cálculo XLSX vía Apache POI.
  14. `testDescargaReportePdf`: Emisión binaria de documento PDF vía LibrePDF OpenPDF.

---

## 40. Módulo 2: Mercados Municipales — Submódulo de Planos Interactivos de Mercados y Puestos Comerciales (PROMPT 32 DE 150)

### 1. Marco Teórico y Principio de Separación Gráfica-Administrativa
El presente submódulo implementa un sistema gráfico interactivo basado en SVG modular de alto rendimiento para representar visualmente la distribución espacial de los mercados municipales, sus sectores, pabellones, pasillos y puestos comerciales, bajo las siguientes premisas esenciales:
- **La Fuente de Verdad es PostgreSQL:** La información jurídica, comercial, titularidad y estado ocupacional reside exclusivamente en las entidades normalizadas de la base de datos (`PuestoComercial`, `AsignacionPuesto`, etc.). El plano SVG es una capa de representación visual sincronizada y no un almacén de datos independiente.
- **Independencia Operativa:** Mover, redimensionar o rotar un elemento gráfico en pantalla **no altera bajo ninguna circunstancia** la asignación administrativa, la merced conductiva ni los derechos del comerciante.
- **Coordenadas Gráficas vs. Topografía Oficial:** El lienzo gráfico maneja unidades de diseño (`anchoBase`, `altoBase` en píxeles de referencia SVG) orientadas a la usabilidad y consulta interactiva, sin sustituir levantamientos topográficos certificados ni planos catastrales oficiales.

---

### 2. Modelo de Datos y Migración Flyway (`V34__mercados_planos_interactivos.sql`)
- **`mercados_planos`:** Entidad principal del plano por mercado. Campos: `id` (UUID), `mercado_id` (FK inmutable), `codigo` (código institucional único), `nombre`, `descripcion`, `ancho_base` (por defecto 1200px), `alto_base` (por defecto 800px), `unidad_referencia` (`PIXELES`), `estado` (`ACTIVO`, `INACTIVO`), `version_actual_id` (FK diferida a la versión publicada activa).
- **`mercados_planos_versiones`:** Control de revisiones inmutables. Campos: `id` (UUID), `plano_id` (FK), `numero_version` (entero secuencial), `estado` (`BORRADOR`, `EN_REVISION`, `PUBLICADA`, `ARCHIVADA`), `descripcion_cambios`, `usuario_creador_id`, `usuario_publicador_id`, `fecha_creacion`, `fecha_publicacion`, `hash_contenido` (SHA-256 para integridad criptográfica), `imagen_referencia_url`, `imagen_referencia_opacidad`, `imagen_referencia_escala`, `version` (control de concurrencia optimista).
- **`mercados_planos_elementos`:** Elementos visuales del lienzo. Campos: `id` (UUID), `version_plano_id` (FK), `tipo_elemento` (`MERCADO`, `SECTOR`, `PABELLON`, `ZONA`, `PUESTO`, `PASILLO`, `ACCESO`, `SALIDA`, `ESCALERA`, `AREA_COMUN`, `ETIQUETA`, `OTRO`), `referencia_entidad_id` (UUID referencial a puesto/sector/pabellón), `codigo_visual`, `etiqueta`, `coordenada_x`, `coordenada_y`, `ancho`, `alto`, `rotacion`, `orden_capa`, `estilo_configurado` (JSON con estilos SVG).
- **`mercados_planos_auditoria`:** Registro pormenorizado de eventos de auditoría (creación, edición de borrador, publicación, restauración, archivado, importación de imagen de referencia).

---

### 3. Ciclo de Vida y Reglas de Versionado Inmutable
```
[ NUEVO PLANO ] ──► [ VERSIÓN 1: BORRADOR ]
                           │
                           ├──► [ EN_REVISION ]
                           │          │
                           │          ▼
                           ▼    [ PUBLICADA ] (Activa para consulta pública e interna)
                     [ ARCHIVADA ]    │
                           ▲          ▼
                           └── (Al publicar nueva versión, la anterior se archiva)
                                      │
                                      ▼
             [ RESTAURAR COMO BORRADOR ] ──► [ VERSIÓN N+1: BORRADOR ]
```
1. **Unicidad de Versión Publicada:** Cada mercado cuenta con a lo sumo una única versión activa en estado `PUBLICADA`. Al publicar un nuevo borrador, el sistema transaccional archiva de forma atómica la versión previa.
2. **Inmutabilidad Histórica:** Las versiones `PUBLICADA` y `ARCHIVADA` son de solo lectura. Todo cambio requiere trabajar sobre un `BORRADOR`.
3. **Restauración Segura:** La restauración de una versión previa jamás sobreescribe registros históricos; clona la estructura y elementos de la versión elegida como un nuevo `BORRADOR` con número correlativo incrementado.

---

### 4. Sincronización en Tiempo Real y Cálculo de Estados Visuales
El servicio `EstadoVisualPuestoService` evalúa en tiempo de consulta el estado dinámico de cada puesto comercial asociado:
- **`DISPONIBLE` (Verde `#10b981`):** Puesto operativo sin asignación activa ni reserva vigente.
- **`OCUPADO` (Azul `#3b82f6`):** Puesto con asignación formal vigente y conductor comercial acreditado.
- **`RESERVADO` (Amarillo `#f59e0b`):** Puesto con trámite administrativo de asignación en evaluación o reserva legal.
- **`INACTIVO` (Gris `#64748b`):** Puesto no habilitado temporalmente para el giro comercial.
- **`RESTRINGIDO` (Rojo `#ef4444`):** Puesto clausurado, en mantenimiento estructural o con medidas cautelares sanitarias.

**Seguridad y Privacidad:** Al consultar el detalle de un puesto desde el plano, si el usuario autenticado carece del permiso `MERCADOS_COMERCIANTE_VER`, los datos del conductor comercial (nombre, documento de identidad, teléfono) se anonimizan automáticamente con leyendas institucionales protegidas.

---

### 5. Editor y Visor Interactivo SVG
- **Visor Interactivo (`/dashboard/mercados/planos/{mercadoId}`):**
  - Renderizado SVG vectorial responsivo con soporte de transformaciones dinámicas.
  - Controles de zoom: Acercar (+20%), Alejar (-20%), Restablecer y Ajustar al contenedor (Fit to screen).
  - Desplazamiento panorámico (Pan): Soporte fluido de arrastre con ratón y eventos táctiles (touch gestures).
  - Búsqueda en tiempo real con resaltado visual del puesto y centrado automático en el lienzo.
  - Filtros combinados por estado visual (`DISPONIBLE`, `OCUPADO`, etc.), sector y pabellón.
  - Selección accesible por clic y navegación por teclado (tecla Enter / Espacio).
  - Panel lateral informativo y modal con pestañas de ficha técnica, ocupación, comerciante, contratos, incidencias e inspecciones.
- **Editor Visual (`/dashboard/mercados/planos/{mercadoId}/editar`):**
  - Lienzo interactivo con cuadrícula de ajuste milimétrico (Snap-to-Grid configurable: 10px, 20px, etc.).
  - Herramientas de dibujo: Rectángulos para puestos, zonas, pabellones, accesos, salidas y etiquetas de texto.
  - Manipulación directa: Arrastre libre con límites del lienzo, redimensionamiento numérico y rotación angular (0°, 90°, 180°, 270°).
  - Herramientas de alineación múltiple (izquierda, derecha, arriba, abajo, centro).
  - Gestión de capas por orden visual (`ordenCapa`).
  - Vinculación obligatoria con puestos reales existentes del mercado (impide asociar puestos de mercados externos).
  - Prevención de duplicados visuales para un mismo puesto dentro de la misma versión.
  - Carga y calibración de planos arquitectónicos de referencia (PNG/JPEG/WebP) con control de opacidad y escala.
  - Guardado transaccional de borradores con generación de hash criptográfico SHA-256 y control de concurrencia optimista.

---

### 6. Permisos RBAC Implementados
1. `MERCADOS_PLANO_VER` — Visualizar planos interactivos y consultar puestos comerciales.
2. `MERCADOS_PLANO_CREAR` — Crear nuevos planos y configurar lienzos base de mercados.
3. `MERCADOS_PLANO_EDITAR` — Modificar geometrías, dimensiones y posiciones en borradores de planos.
4. `MERCADOS_PLANO_GUARDAR` — Almacenar y actualizar versiones en estado borrador.
5. `MERCADOS_PLANO_REVISAR` — Enviar y calificar versiones técnicas en revisión.
6. `MERCADOS_PLANO_PUBLICAR` — Publicar oficialmente versiones aprobadas de planos de mercado.
7. `MERCADOS_PLANO_VERSIONES_VER` — Consultar el catálogo histórico de versiones de planos.
8. `MERCADOS_PLANO_RESTAURAR` — Restaurar versiones archivadas como nuevos borradores editables.
9. `MERCADOS_PLANO_REFERENCIA_GESTIONAR` — Cargar y calibrar imágenes arquitectónicas de referencia de fondo.
10. `MERCADOS_PLANO_AUDITORIA_VER` — Visualizar la bitácora de auditoría de planos y elementos gráficos.

---

### 7. Catálogo de Endpoints Implementados

#### API REST (`/api/mercados/**`)
- `GET /api/mercados/{mercadoId}/plano` — Consulta integral del plano publicado activo con elementos y estados en tiempo real.
- `POST /api/mercados/{mercadoId}/plano` — Creación de plano base para un mercado municipal.
- `PUT /api/mercados/{mercadoId}/plano` — Actualización de metadatos y dimensiones de lienzo del plano.
- `GET /api/mercados/{mercadoId}/plano/versiones` — Listado cronológico de versiones del plano.
- `GET /api/mercados/{mercadoId}/plano/versiones/{versionId}` — Detalle de una versión con sus elementos gráficos.
- `POST /api/mercados/{mercadoId}/plano/borradores` — Creación de un nuevo borrador de versión.
- `PUT /api/mercados/{mercadoId}/plano/borradores/{versionId}` — Guardado transaccional de elementos en borrador.
- `POST /api/mercados/{mercadoId}/plano/versiones/{versionId}/publicar` — Publicación oficial de versión y archivado de versión previa.
- `POST /api/mercados/{mercadoId}/plano/versiones/{versionId}/restaurar` — Clonación de versión histórica a nuevo borrador.
- `POST /api/mercados/{mercadoId}/plano/versiones/{versionId}/imagen-referencia` — Configuración de imagen de referencia arquitectónica.
- `GET /api/mercados/{mercadoId}/plano/puestos` — Consulta de puestos disponibles y vinculables para el editor.
- `GET /api/mercados/{mercadoId}/plano/estadisticas` — Métricas en vivo de ocupación y estados de puestos en el plano.
- `GET /api/mercados/puestos/{puestoId}/informacion-plano` — Ficha técnica integral de puesto para modal y panel lateral.

#### Vistas Web Thymeleaf (`/dashboard/mercados/planos/**`)
- `/dashboard/mercados/planos` — Catálogo general de planos por mercado municipal con tarjetas ejecutivas.
- `/dashboard/mercados/planos/crear` — Asistente de configuración de nuevo plano base.
- `/dashboard/mercados/planos/{mercadoId}` — Visor interactivo SVG con herramientas de zoom, pan, filtros y modales.
- `/dashboard/mercados/planos/{mercadoId}/editar` — Editor visual SVG avanzado con cuadrícula, herramientas de dibujo y capas.
- `/dashboard/mercados/planos/{mercadoId}/versiones` — Registro histórico de versiones, bitácora de auditoría y restauración.

---

### 8. Cobertura de Pruebas Automatizadas (13/13 - 100% Exitosas)
- **`MercadoPlanoServiceTest` (7 pruebas de lógica de negocio y reglas de dominio):**
  1. `testCrearPlanoConVersionBorradorInicial`: Creación de plano base y generación automática de la versión 1 en estado `BORRADOR`.
  2. `testRechazarPlanoConCodigoDuplicado`: Control de unicidad de código institucional de plano.
  3. `testGuardarBorradorConElementos`: Persistencia transaccional de elementos SVG, generación de hash SHA-256 y control de concurrencia.
  4. `testRechazarPuestoDeOtroMercado`: Aislamiento institucional que prohíbe asociar puestos de un mercado ajeno.
  5. `testRechazarPuestoDuplicadoEnMismaVersion`: Detección y rechazo de duplicados de puestos en el lienzo.
  6. `testPublicarVersionYArchivarAnterior`: Publicación atómica, paso a `PUBLICADA` de la versión seleccionada y paso a `ARCHIVADA` de la versión previa.
  7. `testRestaurarVersionComoNuevoBorrador`: Restauración que preserva la inmutabilidad histórica y clona elementos en una versión borrador `N+1`.
- **`MercadoPlanoWebSecurityTest` (6 pruebas de integración web y seguridad RBAC):**
  8. `testListadoPlanosSinAutenticacionRedirigeALogin`: Bloqueo 302 Redirect al login para usuarios anónimos.
  9. `testVisorPlanoConPermisoVisualizar`: Acceso 200 OK al visor interactivo con permiso `MERCADOS_PLANO_VER`.
  10. `testEditorPlanoDenegadoSinPermiso`: Bloqueo 403 Forbidden al editor visual para usuarios sin permiso `MERCADOS_PLANO_EDITAR`.
  11. `testEditorPlanoPermitidoConRolAdmin`: Acceso 200 OK al editor visual para usuarios con atribución.
  12. `testConsultarInformacionPuestoConAnonimizacion`: Protección de privacidad en `/api/mercados/puestos/{id}/informacion-plano` enmascarando comerciante si no tiene `MERCADOS_COMERCIANTE_VER`.
  13. `testGuardarBorradorDenegadoSinCsrf`: Protección contra falsificación de peticiones en sitios cruzados (CSRF).

---

## 41. Módulo 2: Portal Web de Consulta y Autogestión para Comerciantes (Prompt 33 de 150)

### 1. Descripción Funcional
El **Portal Web de Consulta y Autogestión para Comerciantes** (`/portal/comerciante/**` y `/api/portal/comerciante/**`) es el subsistema ciudadano del Módulo de Mercados Municipales de la Municipalidad Distrital de Quinua. Brinda a los comerciantes empadronados una plataforma digital moderna, segura y accesible para la autogestión de su información comercial, puestos asignados, contratos, licencias/autorizaciones, obligaciones tributarias y no tributarias, solicitudes de renovación de ocupación, reporte de incidencias y descarga de documentos oficiales con firma digital y verificación mediante código QR.

### 2. Principios de Seguridad y Aislamiento Institucional (Anti-IDOR)
- **Cero Confianza en Entradas de Cliente:** Ningún endpoint web ni API REST confía en `comercianteId` proveniente de parámetros HTTP, query strings o payloads JSON.
- **Resolución Estricta en Backend (`ComerciantePortalContextService`):**
  1. La identidad del usuario se obtiene exclusivamente del `SecurityContextHolder` autenticado (sesión web o JWT bearer).
  2. Se valida contra la tabla `mercados_vinculos_usuario_comerciante` que exista un vínculo activo (`ACTIVO`) y no vencido entre el usuario ciudadano y el comerciante.
  3. Soporte de **Auto-vinculación Transparente:** Si es titular como persona natural y su DNI coincide con el del comerciante, el sistema auto-crea el vínculo titular.
  4. Soporte **Multi-comerciante:** En caso de ciudadanos que administren más de un puesto o representación comercial, el portal implementa un selector de contexto persistido en preferencias y sesión.
- **Validación Anti-IDOR Cruzada:** Todo acceso a puestos, contratos, autorizaciones, obligaciones, comprobantes de pago, inspecciones e incidencias valida rigurosamente la titularidad o asignación del comerciante activo. Todo intento de acceso a recursos de terceros es bloqueado con código HTTP 403 Forbidden y registrado en `mercados_portal_auditoria`.

### 3. Modelo de Datos y Migración Flyway (`V35__portal_comerciante.sql`)
1. **`mercados_vinculos_usuario_comerciante`**: Registro formal de vínculos de acceso y facultades entre usuarios del sistema y comerciantes (Titular, Representante Legal, Apoderado, Administrador Delegado, Familiar Autorizado).
2. **`mercados_comerciante_preferencias`**: Preferencias del comerciante (canal preferido de notificación, contexto activo, alertas por email/SMS/WhatsApp).
3. **`mercados_portal_auditoria`**: Bitácora de seguridad y trazabilidad exclusiva de accesos, descargas documentales, cambios de datos de contacto y bloqueos anti-IDOR en el portal.
4. **Catálogo de 11 Permisos Granulares RBAC:**
   - `PORTAL_COMERCIANTE_ACCEDER`: Ingreso al portal de comerciantes.
   - `PORTAL_COMERCIANTE_PERFIL_VER`: Consulta de ficha y datos de empadronamiento.
   - `PORTAL_COMERCIANTE_CONTACTO_EDITAR`: Actualización de datos de contacto con auditoría.
   - `PORTAL_COMERCIANTE_PUESTOS_VER`: Consulta de puestos comerciales asignados y plano interactivo.
   - `PORTAL_COMERCIANTE_CONTRATOS_VER`: Consulta de contratos de ocupación y contratos históricos.
   - `PORTAL_COMERCIANTE_AUTORIZACIONES_VER`: Consulta de licencias y autorizaciones comerciales.
   - `PORTAL_COMERCIANTE_FINANZAS_VER`: Consulta de estado de cuenta, obligaciones y pagos.
   - `PORTAL_COMERCIANTE_SOLICITUDES_VER`: Seguimiento de trámites y solicitudes de gestión.
   - `PORTAL_COMERCIANTE_SOLICITUDES_CREAR`: Presentación de solicitudes de renovación de ocupación.
   - `PORTAL_COMERCIANTE_DOCUMENTOS_DESCARGAR`: Descarga de contratos, autorizaciones y recibos oficiales.
   - `PORTAL_COMERCIANTE_INCIDENCIAS_CREAR`: Reporte ciudadano de incidencias operativas y sanitarias.

### 4. Vistas Web Ciudadanas Implementadas (`/portal/comerciante/**`)
- `/portal/comerciante/dashboard`: Tablero principal con métricas en vivo, puestos activos, deuda exigible, alertas y accesos directos.
- `/portal/comerciante/perfil`: Ficha de datos empadronados, enmascaramiento de identidad (DNI/RUC) y formulario de actualización de contacto.
- `/portal/comerciante/puestos`: Catálogo interactivo de puestos asignados con sector, dimensiones y canon.
- `/portal/comerciante/puestos/{puestoId}`: Ficha técnica detallada del puesto, ubicación espacial y condiciones de asignación.
- `/portal/comerciante/asignaciones`: Registro histórico y vigente de resoluciones de asignación de puestos.
- `/portal/comerciante/contratos`: Listado de contratos de ocupación comercial y vigencias.
- `/portal/comerciante/contratos/{contratoId}`: Detalle de contrato y descarga oficial en PDF con código seguro.
- `/portal/comerciante/autorizaciones`: Listado de licencias y permisos comerciales con códigos QR de verificación.
- `/portal/comerciante/obligaciones`: Catálogo de tasas, tarifas de uso y obligaciones pendientes, vencidas o pagadas.
- `/portal/comerciante/pagos`: Registro histórico de pagos recibidos, medios de pago y recibos de caja.
- `/portal/comerciante/estado-cuenta`: Estado de cuenta consolidado con cálculo de deuda exigible y descarga en PDF.
- `/portal/comerciante/solicitudes`: Listado y trazabilidad de expedientes y solicitudes de gestión de ocupación.
- `/portal/comerciante/solicitudes/renovacion`: Asistente de presentación de solicitudes de renovación de asignación.
- `/portal/comerciante/inspecciones`: Histórico de inspecciones sanitarias y de seguridad notificadas, con observaciones y estado.
- `/portal/comerciante/incidencias`: Listado de incidencias operativas reportadas por el comerciante.
- `/portal/comerciante/incidencias/nueva`: Formulario de reporte de incidencias en puestos o áreas comunes del mercado.
- `/portal/comerciante/notificaciones`: Centro de notificaciones administrativas y recordatorios de vencimiento.
- `/portal/comerciante/documentos`: Repositorio documental unificado de resoluciones, contratos, autorizaciones y constancias.
- `/portal/comerciante/plano`: Visor espacial integrado del mercado para consultar la ubicación física de sus puestos.
- `/portal/comerciante/ayuda`: Guía de trámites, derechos, deberes del comerciante y preguntas frecuentes de la Municipalidad de Quinua.

### 5. Catálogo de Endpoints Implementados

#### API REST (`/api/portal/comerciante/**`)
- `GET /api/portal/comerciante/contextos` — Listado de comerciantes a los que el usuario tiene acceso vinculado.
- `POST /api/portal/comerciante/seleccionar-contexto/{comercianteId}` — Cambio de contexto activo del comerciante.
- `GET /api/portal/comerciante/dashboard` — Resumen ejecutivo de puestos, contratos, deuda y avisos.
- `GET /api/portal/comerciante/perfil` — Consulta de perfil empadronado con datos de documento anonimizados.
- `PUT /api/portal/comerciante/contacto` — Actualización transaccional de teléfono, email y dirección con bitácora.
- `GET /api/portal/comerciante/puestos` — Listado de puestos asignados al comerciante.
- `GET /api/portal/comerciante/puestos/{puestoId}` — Ficha del puesto con validación anti-IDOR.
- `GET /api/portal/comerciante/asignaciones` — Listado paginado de asignaciones del comerciante.
- `GET /api/portal/comerciante/contratos` — Listado de contratos de ocupación del comerciante.
- `GET /api/portal/comerciante/contratos/{contratoId}` — Ficha de contrato con validación anti-IDOR.
- `GET /api/portal/comerciante/contratos/{contratoId}/descargar` — Descarga de PDF oficial firmado de contrato.
- `GET /api/portal/comerciante/autorizaciones` — Listado de autorizaciones y licencias comerciales.
- `GET /api/portal/comerciante/autorizaciones/{autorizacionId}` — Detalle de autorización y código QR.
- `GET /api/portal/comerciante/autorizaciones/{autorizacionId}/descargar` — Descarga de PDF oficial de autorización.
- `GET /api/portal/comerciante/obligaciones` — Consulta paginada y filtrable de obligaciones tributarias/no tributarias.
- `GET /api/portal/comerciante/pagos` — Listado de pagos y amortizaciones confirmadas.
- `GET /api/portal/comerciante/pagos/{pagoId}/constancia` — Descarga de constancia oficial de pago de caja en PDF.
- `GET /api/portal/comerciante/estado-cuenta` — Estado de cuenta consolidado del comerciante.
- `GET /api/portal/comerciante/estado-cuenta/descargar` — Generación y descarga de estado de cuenta en PDF.
- `GET /api/portal/comerciante/solicitudes` — Historial de solicitudes de gestión presentadas.
- `POST /api/portal/comerciante/solicitudes/renovacion` — Registro de solicitud de renovación de ocupación.
- `GET /api/portal/comerciante/inspecciones` — Listado de inspecciones notificadas al comerciante.
- `GET /api/portal/comerciante/incidencias` — Listado de incidencias operativas registradas por el comerciante.
- `POST /api/portal/comerciante/incidencias` — Reporte ciudadano de incidencia operativa.
- `GET /api/portal/comerciante/documentos` — Catálogo unificado de documentos oficiales autorizados para descarga.

### 6. Cobertura de Pruebas Automatizadas (23/23 - 100% Exitosas)
- **`MercadoPortalComercianteServiceTest` (14 pruebas de lógica y servicios de negocio):**
  1. `testResolucionContextoComercianteActivo`: Resolución y auto-vinculación de comerciante activo para el usuario en sesión.
  2. `testListadoYCambioDeContextoComerciante`: Conmutación entre múltiples comerciantes vinculados a un mismo usuario.
  3. `testConsultaPerfilComercianteConEnmascaramiento`: Enmascaramiento de seguridad de DNI/RUC conforme a Ley de Protección de Datos Personales.
  4. `testActualizacionContactoComercialYAuditoria`: Modificación de correo, teléfono y dirección con auditoría inmutable.
  5. `testConsultaDashboardComerciante`: Resumen de métricas, puestos, recaudación y obligaciones.
  6. `testListadoPuestosYDetallePropio`: Consulta de puestos asignados y ficha técnica del puesto.
  7. `testRechazoIdorAccesoPuestoAjeno`: Rechazo estricto (403 Forbidden) al intentar acceder a un puesto comercial ajeno.
  8. `testListadoContratosYDescargaPdf`: Visualización de contratos y emisión del archivo PDF correspondiente.
  9. `testRechazoIdorContratoAjeno`: Rechazo estricto (403 Forbidden) al intentar consultar un contrato de otro comerciante.
  10. `testListadoAutorizacionesConQr`: Consulta de autorizaciones comerciales con validación de URL de código QR.
  11. `testEstadoCuentaYGeneracionPdf`: Consolidación del estado de cuenta, deuda exigible y generación del PDF informativo.
  12. `testDescargaConstanciaPagoYRechazoAjeno`: Descarga de comprobante oficial de caja y rechazo de recibos de terceros.
  13. `testPresentacionSolicitudRenovacion`: Flujo formal de solicitud de renovación de asignación de puesto.
  14. `testRechazoRenovacionAsignacionAjena`: Validación de pertenencia que impide renovar puestos de otros comerciantes.
  15. `testReporteIncidenciaYRechazoPuestoAjeno`: Reporte ciudadano de anomalías y bloqueo si se asocia a un puesto no asignado.
  16. `testRechazoUsuarioSinVinculo`: Denegación de acceso cuando un usuario no posee ningún comerciante vinculado.
- **`MercadoPortalComercianteWebSecurityTest` (9 pruebas de seguridad web, RBAC y control de acceso):**
  17. `testRechazoUsuarioAnonimo`: Retorno de HTTP 401 Unauthorized para peticiones no autenticadas.
  18. `testAccesoDashboardApiConJwt`: Acceso 200 OK a la API del portal autenticado mediante JWT y rol `ROLE_COMERCIANTE`.
  19. `testConsultaPerfilApi`: Obtención de ficha personal en formato JSON con documento anonimizado.
  20. `testRechazoIdorPuestoAjenoApi`: Bloqueo HTTP 403 Forbidden al intentar consultar vía API un puesto de otro comerciante.
  21. `testRechazoIdorContratoAjenoApi`: Bloqueo HTTP 403 Forbidden al solicitar un contrato no perteneciente al usuario autenticado.
  22. `testRechazoIdorObligacionAjenaApi`: Bloqueo HTTP 403 Forbidden al intentar inspeccionar una obligación ajena.
  23. `testPresentacionRenovacionApi`: Registro transaccional exitoso de solicitud de renovación por endpoint REST.
  24. `testAccesoVistasWebConSesionAutenticada`: Renderizado 200 OK de vistas Thymeleaf con sesión web autenticada.
  25. `testActualizacionContactoWebRequiereCsrf`: Verificación de protección obligatoria CSRF en peticiones POST web.

---

## 42. Módulo de Mercados Municipales: Centro de Reportes, Estadísticas y Documentos PDF/Excel (Prompt 34)

### 1. Descripción del Submódulo
El **Centro de Reportes, Estadísticas y Documentos de Mercados Municipales** provee una plataforma integral de inteligencia de negocios y analítica operativa para la toma de decisiones gerenciales y el cumplimiento de la rendición de cuentas de la Municipalidad Distrital de Quinua.

El submódulo opera bajo el **Principio de Datos Reales**, obteniendo agregaciones y proyecciones directamente de PostgreSQL mediante consultas optimizadas que evitan problemas de N+1 y conteos duplicados.

### 2. Principales Capacidades y Fórmulas
1. **Dashboard Ejecutivo (`/dashboard/mercados/reportes/ejecutivo`):**
   - Consolidación transversal de infraestructura comercial, legalidad de puestos, cobranzas y fiscalización.
   - Comparación de desempeño entre períodos (mes actual vs anterior, año actual vs anterior, o rango personalizado con zona horaria `America/Lima`).
2. **Indicador y Tasa de Ocupación:**
   $$\text{Porcentaje de Ocupación} = \left(\frac{\text{Puestos Ocupados}}{\text{Puestos Habilitados}}\right) \times 100$$
   - *Denominador considerado:* Puestos físicos con estado diferente de `NO_ASIGNABLE`.
   - Si no existen puestos habilitados, el sistema reporta `"Sin datos suficientes"` de forma segura sin excepciones por división entre cero.
3. **Padrón de Comerciantes y Asignaciones:**
   - Conteo de comerciantes distintos activos y puestos asignados (evitando multiplicar comerciantes que poseen más de un puesto comercial).
4. **Finanzas y Recaudación Neta:**
   $$\text{Recaudación Neta} = \text{Pagos Confirmados} - \text{Reversos/Anulaciones}$$
   - Atribución exacta de recaudación a conceptos cobrados mediante las aplicaciones de pago, sin duplicar ingresos por pagos consolidados.
   - Antigüedad de saldos agrupada en rangos: *No Vencidos*, *1 a 30 días*, *31 a 60 días*, y *Más de 60 días*.
5. **Inspecciones e Incidencias:**
   - Métricas de actas con hallazgos y medidas correctivas pendientes sin multiplicar inspecciones.
   - Cálculo de tiempos promedio de atención y resolución en horas.
6. **Vencimientos y Renovaciones:**
   - Monitoreo preventivo de contratos, autorizaciones y asignaciones en plazos próximos a expirar (menores a 30 días).

### 3. Motor de Exportación y Seguridad
- **Exportación a Excel (`.xlsx`):** Implementada con **Apache POI**. Aplica sanitización de inyección de fórmulas (`CSV/Formula Injection`) prefijando con comilla simple (`'`) cualquier celda de texto que comience con `=, +, -, @`. Incluye congelamiento de paneles, formateo monetario `S/ #,##0.00` y estilos ejecutivos.
- **Exportación a PDF institucional:** Generada con **OpenPDF (LibrePDF)**. Incorpora membrete oficial de la Municipalidad Distrital de Quinua, metadatos, tabla formateada con paginación dinámica `Página X de Y`, totales y sello de control.
- **Historial de Exportaciones:** Registro inmutable en tabla `mercados_historial_exportaciones` para trazabilidad y auditoría de descargas.

### 4. Endpoints y Vistas
- `GET /dashboard/mercados/reportes` — Centro general de reportes.
- `GET /dashboard/mercados/reportes/ejecutivo` — Dashboard consolidado con KPIs y tendencias.
- `GET /dashboard/mercados/reportes/ocupacion` — Reporte y desglose de ocupación por mercado, sector y pabellón.
- `GET /dashboard/mercados/reportes/puestos` — Padrón detallado de puestos comerciales con filtros.
- `GET /dashboard/mercados/reportes/comerciantes` — Padrón oficial de comerciantes activos.
- `GET /dashboard/mercados/reportes/contratos` — Reporte de contratos de arrendamiento.
- `GET /dashboard/mercados/reportes/autorizaciones` — Reporte de autorizaciones comerciales municipales.
- `GET /dashboard/mercados/reportes/vencimientos` — Instrumentos comerciales vencidos y por vencer.
- `GET /dashboard/mercados/reportes/recaudacion` — Reporte de ingresos y amortizaciones de caja.
- `GET /dashboard/mercados/reportes/deudas` — Reporte de obligaciones tributarias/no tributarias pendientes y mora.
- `GET /dashboard/mercados/reportes/inspecciones` — Fiscalización y cumplimiento de actas.
- `GET /dashboard/mercados/reportes/incidencias` — Reporte y tiempos de resolución de anomalías.
- `GET /dashboard/mercados/reportes/renovaciones` — Solicitudes de prórroga y cambio de titular.
- `GET /dashboard/mercados/reportes/exportaciones` — Historial de descargas realizadas.
- `GET /api/mercados/reportes/dashboard` — Datos consolidados en JSON para dashboards interactivos.
- `POST /api/mercados/reportes/exportar/excel` — Generación binaria y descarga de archivo XLSX.
- `POST /api/mercados/reportes/exportar/pdf` — Generación binaria y descarga de archivo PDF.

### 5. Cobertura de Pruebas Automatizadas (14/14 - 100% Exitosas)
- **`MercadoReportesServiceTest` (7 pruebas de agregación, cálculo y sanitización):**
  1. `test01_indicadores_dashboard_calculo_real`: Verificación de cálculo unificado de KPIs de mercados, puestos y finanzas.
  2. `test02_indicador_ocupacion_porcentajes_y_denominador_cero`: Cálculo exacto de tasa de ocupación y manejo sin excepción cuando denominador es 0.
  3. `test03_recaudacion_neta_y_antiguedad_deudas`: Cálculo de recaudación neta deduciendo reversos y clasificación de antigüedad de mora en 4 rangos.
  4. `test04_inspecciones_e_incidencias_sin_duplicados`: Conteo fidedigno de actas con hallazgos e incidencias sin duplicar registros.
  5. `test05_exportacion_excel_poi_valido_y_sanitizacion`: Generación de `.xlsx` con Apache POI y verificación de sanitización de fórmulas maliciosas (`=SUM(...)` $\to$ `'=SUM(...)`).
  6. `test06_exportacion_pdf_valido_con_paginacion`: Generación de documento PDF binario válido con encabezado institucional.
  7. `test07_historial_exportaciones_registro`: Registro en base de datos del historial de exportación y auditoría de descarga.
- **`MercadoReportesWebSecurityTest` (7 pruebas de seguridad web, RBAC y renderizado):**
  8. `test01_acceso_anonimo_dashboard_api_rechazado`: Bloqueo HTTP 401 Unauthorized para peticiones no autenticadas.
  9. `test02_admin_dashboard_api_ok`: Acceso autorizado HTTP 200 OK para administradores con privilegios completos.
  10. `test03_operador_sin_permiso_finanzas_rechazado_403`: Bloqueo estricto HTTP 403 Forbidden a reportes financieros para usuarios sin autoridad `MERCADOS_REPORTES_FINANZAS`.
  11. `test04_ciudadano_sin_permiso_reportes_rechazado_403`: Denegación HTTP 403 a usuarios con rol `ROLE_COMERCIANTE`.
  12. `test05_exportar_excel_ok`: Emisión y descarga binaria exitosa de Excel (`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`).
  13. `test06_exportar_pdf_ok`: Emisión y descarga binaria exitosa de PDF (`application/pdf`).
  14. `test07_vistas_web_render_ok`: Renderizado exitoso de las 13 pantallas web Thymeleaf del submódulo de reportes.

---

## 43. Módulo 2: Mercados Municipales — Integración Final, Seguridad, Optimización y Pruebas E2E (Prompt 35)

### 1. Resumen de Integración del Módulo 2 (Prompts 21 al 34)
El Módulo 2: **Mercados Municipales** de la Municipalidad Distrital de Quinua ha alcanzado su cierre formal, consolidando e integrando de manera exhaustiva las 14 áreas funcionales desarrolladas:
1. **Prompt 21:** Arquitectura base, esquema relacional en PostgreSQL y migraciones Flyway (V21–V36).
2. **Prompt 22:** Administración de mercados municipales y configuraciones operativas.
3. **Prompt 23:** Zonificación física en sectores, pabellones, pasadizos y tipos de giro comercial.
4. **Prompt 24:** Catálogo y ciclo de vida de puestos comerciales (disponibilidad, ocupación, mantenimiento).
5. **Prompt 25:** Padrón oficial de comerciantes (personas naturales y jurídicas) con vinculación DNI/RUC.
6. **Prompt 26:** Proceso formal de adjudicación y asignación de puestos comerciales.
7. **Prompt 27:** Instrumentos legales de arrendamiento, contratos y autorizaciones comerciales con QR.
8. **Prompt 28:** Gestión financiera, obligaciones tributarias/no tributarias, sesiones de caja y conciliación exacta.
9. **Prompt 29:** Inspecciones sanitarias, técnicas y fiscalización preventiva con actas numeradas.
10. **Prompt 30:** Gestión integral de incidencias operativas, actuaciones y resolución sin sanciones arbitrarias.
11. **Prompt 31:** Renovaciones de asignación, cesiones, cambios de titular y traspasos reglamentados.
12. **Prompt 32:** Plano interactivo vectorial SVG con renderizado dinámico y vinculación bidireccional.
13. **Prompt 33:** Portal del Comerciante con autenticación segura, prevención IDOR y autoservicio de documentos.
14. **Prompt 34:** Centro de reportes analíticos, estadísticas con datos reales y motor de exportación en PDF y Excel seguro.

### 2. Consolidación de Navegación y Vistas Administrativas
Se actualizó el menú lateral institucional en [`admin-layout.html`](file:///c:/Users/ASUS/Desktop/quinua/SIGM-QUINUA/src/main/resources/templates/admin/admin-layout.html), garantizando accesos directos, breadcrumbs y control por permisos a todas las secciones:
- `/dashboard/mercados` (Dashboard General)
- `/dashboard/mercados/listar` (Mercados)
- `/dashboard/mercados/sectores` y `/dashboard/mercados/pabellones` (Zonificación)
- `/dashboard/mercados/puestos` (Catálogo de Puestos)
- `/dashboard/mercados/comerciantes` (Padrón de Comerciantes)
- `/dashboard/mercados/asignaciones` (Adjudicaciones)
- `/dashboard/mercados/contratos` y `/dashboard/mercados/autorizaciones` (Contratos y Permisos)
- `/dashboard/mercados/finanzas` (Finanzas, Obligaciones y Caja)
- `/dashboard/mercados/inspecciones` (Fiscalización e Inspecciones)
- `/dashboard/mercados/incidencias` (Incidencias y Reclamos)
- `/dashboard/mercados/gestiones-ocupacion/dashboard` (Renovaciones y Traspasos)
- `/dashboard/mercados/plano` (Plano Interactivo SVG)
- `/dashboard/mercados/reportes` (Centro de Reportes y Estadísticas)

### 3. Suite Maestra de Pruebas de Integración E2E (`MercadoModulo2E2EIntegracionTest`)
Se implementó y ejecutó exitosamente la suite de pruebas integrales de punta a punta, validando los 7 escenarios críticos del módulo con un **100% de éxito (7/7)**:
- `test01_infraestructura_y_adjudicacion_completa`: Flujo de creación de Mercado $\to$ Sector $\to$ Pabellón $\to$ Puesto $\to$ Registro de Comerciante $\to$ Adjudicación de puesto con cambio atómico de estado a `OCUPADO`.
- `test02_conciliacion_financiera_y_amortizacion`: Emisión de obligación por merced conductiva (S/ 150.00), apertura de sesión de caja, recepción de pago en efectivo, amortización contable exacta (`saldoPendiente` = 0.00) y transición a estado `PAGADA`.
- `test03_fiscalizacion_e_inspeccion`: Programación de fiscalización ordinaria asignada a inspector municipal, registro de hallazgos sanitarios y finalización de acta.
- `test04_incidencias_operativas_y_resolucion`: Registro de contingencia de infraestructura, asignación a responsable operativo, registro de actuaciones técnicas y cierre formal con motivo de resolución.
- `test05_portal_comerciante_aislamiento_seguro`: Autenticación con `ROLE_COMERCIANTE`, resolución segura de contexto mediante vínculo titular verificado, prevención estricta de fuga de datos de terceros y anonimización de documentos personales.
- `test06_inteligencia_negocios_y_ocupacion_real`: Consulta de indicadores analíticos reales calculados desde PostgreSQL, validación de tasa porcentual de ocupación y agregaciones del dashboard ejecutivo.
- `test07_seguridad_exportacion_excel_sanitizacion`: Generación binaria de archivo XLSX válido mediante Apache POI con aplicación de sanitización contra inyección de fórmulas CSV.

### 4. Resultados de Verificación y Compilación
- **Migraciones Flyway:** 36 migraciones aplicadas y validadas exitosamente contra PostgreSQL (`sigm_quinua`), sin discrepancias de checksums.
- **Pruebas de Integración E2E:** 7/7 pruebas superadas sin fallos ni errores.
- **Empaquetado JAR:** Construcción exitosa del ejecutable de producción `target/sigm-quinua-0.0.1-SNAPSHOT.jar` mediante `./mvnw.cmd package -DskipTests`.

---

## 44. Módulo 3: Seguridad Ciudadana y Serenazgo — Arquitectura Inicial y Base de Datos (Prompt 36)

### 1. Resumen de Arquitectura y Misión
El Módulo 3: **Seguridad Ciudadana y Serenazgo** del SIGM-QUINUA implementa la infraestructura operativa, cartografía preventiva, gestión de personal y central de incidencias de la Municipalidad Distrital de Quinua. 

El módulo se rige estrictamente por la **Ley del Servicio de Serenazgo Municipal (Ley N° 31297)** y el **Principio de Competencia**, distinguiendo las funciones preventivas y comunitarias del serenazgo distrital respecto a las competencias exclusivas de la Policía Nacional del Perú (investigación criminal y orden judicial) y el Ministerio Público.

```text
pe.gob.municipalidad.sigm.modules.serenazgo/
├── controller/
│   ├── SerenazgoRestController.java             # Endpoints REST (/api/serenazgo/**)
│   └── SerenazgoWebController.java              # Vistas Web (/dashboard/serenazgo)
│
├── service/
│   ├── CatalogoSeguridadService.java            # Catálogos técnicos de seguridad y cargos
│   ├── SectorPatrullajeService.java             # Gestión territorial de sectores y zonas
│   ├── DashboardSeguridadService.java           # Métricas e indicadores reales desde PostgreSQL
│   └── impl/                                    # Implementaciones transaccionales Spring
│
├── entity/
│   ├── CargoOperativoSerenazgo.java             # serenazgo_cargos_operativos
│   ├── PersonalSerenazgo.java                   # serenazgo_personal (reutiliza Persona y Usuario)
│   ├── UnidadMovilSerenazgo.java                # serenazgo_unidades (camionetas, motos)
│   ├── EquipamientoSerenazgo.java               # serenazgo_equipamiento (radios, chalecos)
│   ├── HistorialEquipamientoSerenazgo.java      # serenazgo_equipamiento_historial
│   ├── SectorPatrullaje.java                    # serenazgo_sectores
│   ├── ZonaPatrullaje.java                      # serenazgo_zonas
│   ├── PuntoReferenciaSeguridad.java            # serenazgo_puntos_referencia (coordenadas GPS)
│   ├── TurnoSerenazgo.java                      # serenazgo_turnos (mañana, tarde, noche)
│   ├── ProgramacionServicioSerenazgo.java       # serenazgo_programacion_servicios
│   ├── TipoIncidenciaSeguridad.java             # serenazgo_tipos_incidencia
│   ├── IncidenciaSeguridadCiudadana.java        # serenazgo_incidencias
│   ├── AlertaCiudadanaSeguridad.java            # serenazgo_alertas_ciudadanas (portal vecinal)
│   ├── IntervencionSerenazgo.java               # serenazgo_intervenciones
│   ├── ParticipanteIntervencionSerenazgo.java   # serenazgo_intervenciones_participantes
│   ├── PatrullajeSerenazgo.java                 # serenazgo_patrullajes
│   ├── ParticipantePatrullajeSerenazgo.java     # serenazgo_patrullajes_participantes
│   ├── PuntoRecorridoPatrullaje.java            # serenazgo_patrullajes_puntos_recorrido
│   ├── EvidenciaSeguridadCiudadana.java         # serenazgo_evidencias (vinculado a documentos)
│   └── SupervisionSerenazgo.java                # serenazgo_supervisiones
│
├── repository/                                  # 20 repositorios Spring Data JPA
├── enums/                                       # 20 enumeraciones de dominio operativo
└── dto/                                         # DTOs de transferencia desacoplados de JPA
```

### 2. Base de Datos Relacional y Migración Flyway V37
Se ejecutó la migración `V37__seguridad_ciudadana_core.sql` que introduce:
- **20 Tablas Relacionales:** Con claves primarias UUID (`gen_random_uuid()`), claves foráneas `ON DELETE RESTRICT` o `CASCADE` justificadas, índices B-Tree para optimización de búsqueda territorial y temporal, y restricciones `CHECK` para integridad numérica de coordenadas geográficas (`latitud` $\in [-90, 90]$, `longitud` $\in [-180, 180]$).
- **9 Secuencias Correlativas:** Para generación segura y secuencial de códigos sin colisiones de concurrencia (`seq_serenazgo_personal`, `seq_serenazgo_unidad`, `seq_serenazgo_sector`, `seq_serenazgo_incidencia`, etc.).
- **Catálogos Normativos Poblados:** 5 cargos operativos base, 8 tipos de incidencia municipal y 3 turnos de servicio de guardia rotativa.
- **Seguridad RBAC:** 12 permisos institucionales asignados a `ROLE_ADMIN`, `ROLE_SUPERADMIN` y `ROLE_SERENO`.

### 3. Endpoints Implementados
- `GET /api/serenazgo/catalogos` — Catálogos completos de cargos, turnos, prioridades, estados y tipos de unidad.
- `GET /api/serenazgo/catalogos/tipos-incidencia` — Catálogo activo de incidencias tipificadas.
- `GET /api/serenazgo/catalogos/prioridades` — Prioridades operativas (`BAJA`, `MEDIA`, `ALTA`, `CRITICA`).
- `GET /api/serenazgo/sectores` — Listado de sectores y cuadrantes activos.
- `GET /api/serenazgo/sectores/{id}/zonas` — Zonas operativas pertenecientes a un sector específico.
- `GET /api/serenazgo/resumen` — Resumen numérico en tiempo real calculado desde PostgreSQL.
- `GET /dashboard/serenazgo` — Interfaz Thymeleaf de control y visualización operativa.

### 4. Cobertura de Pruebas Automatizadas (11/11 - 100% Exitosas)
- **`SerenazgoCoreEntidadesTest` (5 pruebas de persistencia e integridad):**
  1. `test01_catalogos_tecnicos_flyway`: Verificación de catálogos poblados automáticamente por Flyway V37.
  2. `test02_persistencia_personal_serenazgo`: Creación de sereno vinculado a `Persona`, validación de secuencias y unicidad de código.
  3. `test03_persistencia_unidad_y_equipamiento`: Registro de camioneta operativa, kilometraje y asignación de radio transceptor.
  4. `test04_territorio_sectores_y_zonas`: Configuración de sector urbano y agregación de zonas de patrullaje.
  5. `test05_ciclo_operativo_incidencia_y_patrullaje`: Flujo encadenado de registro de incidencia $\to$ intervención en lugar $\to$ patrullaje preventivo $\to$ supervisión de servicio.
- **`SerenazgoCoreIntegrationTest` (6 pruebas de seguridad web, RBAC y vistas):**
  6. `test01_acceso_anonimo_rechazado`: Rechazo estricto `HTTP 401 Unauthorized` a peticiones sin token JWT.
  7. `test02_acceso_admin_catalogos`: Consulta autorizada de catálogos con token JWT y rol administrador (`HTTP 200 OK`).
  8. `test03_listar_sectores_api`: Acceso autorizado a sectores para efectivos con rol `ROLE_SERENO`.
  9. `test04_resumen_dashboard_api`: Consulta agregada de indicadores para tableros de control.
  10. `test05_vista_dashboard_thymeleaf`: Renderizado de vista web Thymeleaf con cookie de sesión segura.
  11. `test06_usuario_sin_permiso_rechazado`: Bloqueo `HTTP 403 Forbidden` a usuarios con rol `ROLE_CIUDADANO`.

---

## 45. Módulo 3: Seguridad Ciudadana y Serenazgo — Gestión Integral de Personal, Cargos y Documentación (Prompt 37)

### 1. Resumen Funcional
Implementación del subsistema de **Gestión Integral de Personal de Serenazgo**, cubriendo la administración del cuerpo operativo de seguridad ciudadana municipal de conformidad con la Ley N° 31297 (Ley del Servicio de Serenazgo Municipal) y normativas de carrera administrativa municipal:
- **Padrón de Efectivos:** Vinculación estricta con la entidad ciudadana `Persona` (Prompt 1) y cuentas de usuario `Usuario` (Prompt 2).
- **Legajo y Documentación:** Registro de contratos, capacitaciones técnicas, certificados médicos/psicológicos y licencias de conducir.
- **Historial de Cargos Operativos:** Ascensos, rotaciones, reasignaciones de servicio y cambios de estado administrativo (`ACTIVO`, `SUSPENDIDO`, `VACACIONES`, `LICENCIA`, `CESADO`).
- **Control de Secuencias:** Generación correlativa atómica de códigos institucionales `SER-{AÑO}-{CORRELATIVO:04d}` respaldados por la secuencia `seq_serenazgo_personal`.

### 2. Base de Datos y Migración Flyway V38
- Migración `V38__serenazgo_personal.sql` consolidando tablas relacionales de personal, historial de cargos, capacitaciones y legajos documentales.
- Permisos RBAC sembrados: `SERENAZGO_PERSONAL_VER`, `SERENAZGO_PERSONAL_CREAR`, `SERENAZGO_PERSONAL_EDITAR`, `SERENAZGO_PERSONAL_CAMBIAR_ESTADO`, `SERENAZGO_PERSONAL_LEGAJO`, `SERENAZGO_PERSONAL_EXPORTAR`.

### 3. Vistas y Controladores
- **Controlador Web:** `PersonalSerenazgoWebController` (`/dashboard/serenazgo/personal/**`).
- **Controlador REST:** `PersonalSerenazgoRestController` (`/api/serenazgo/personal/**`).
- **Vistas Thymeleaf:** Listado con filtros y paginación, formulario de registro, edición de legajo y ficha técnica detallada.

---

## 46. Módulo 3: Seguridad Ciudadana y Serenazgo — Gestión de Unidades Móviles, Flota Vehicular y Equipamiento Operativo (Prompt 38)

### 1. Resumen de Arquitectura y Misión
El subsistema de **Gestión de Unidades Móviles y Equipamiento del Serenazgo** provee la infraestructura de administración, supervisión técnica, control de disponibilidad y trazabilidad física para todos los recursos móviles y herramientas de seguridad ciudadana de la Municipalidad Distrital de Quinua:
- **Flota Vehicular y Unidades Móviles:** Camionetas, motocicletas, bicicletas y vehículos de apoyo municipal con control de código interno, placa reglamentaria oficial, marca, modelo, número de serie de motor/chasis, código patrimonial y estado operativo (`DISPONIBLE`, `ASIGNADA`, `EN_MANTENIMIENTO`, `FUERA_DE_SERVICIO`, `INACTIVA`, `RETIRADA`).
- **Equipamiento y Bienes Operativos:** Radios transceptores de comunicación, chalecos antibalas/tácticos, linternas de alta potencia, cascos de protección, cámaras corporales (bodycams) y botiquines. Diferenciación entre bienes individualizados con número de serie y materiales fungibles controlados por cantidad.
- **Asignaciones Operativas y Devoluciones:** Entrega formal de vehículos y equipos a serenos en servicio activo, control de concurrencia y prevención estricta de asignaciones dobles simultáneas. Devolución formal con verificación de kilometraje/estado físico e inspección de daños.
- **Mantenimiento Preventivo y Correctivo:** Programación, ejecución y cierre de mantenimientos mecánicos y técnicos, con registro de taller autorizado, costos e informes técnicos. Transición automática a estado `EN_MANTENIMIENTO` bloqueando nuevas asignaciones mientras dure el servicio.
- **Incidencias Técnicas y Averías:** Reporte oportuno de fallas mecánicas, siniestros viales, pérdidas o daños de equipamiento con nivel de gravedad (`LEVE`, `MODERADA`, `GRAVE`, `CRITICA`), medidas inmediatas de contingencia y seguimiento administrativo.

### 2. Modelo Relacional y Migración Flyway V39 (`V39__serenazgo_unidades_equipamiento.sql`)
- **Secuencias Correlativas:**
  - `seq_serenazgo_equipamiento` $\to$ Códigos de equipamiento `EQP-{YYYY}-{0000}`.
  - `seq_serenazgo_mantenimiento` $\to$ Códigos de mantenimiento `MNT-{YYYY}-{00000}`.
  - `seq_serenazgo_incidencia_tecnica` $\to$ Códigos de incidencia `INC-{YYYY}-{00000}`.
- **Tablas Relacionales:**
  1. `serenazgo_unidades`: Actualizada con campos de número de serie, versión optimista (`version`) y nuevos estados operativos.
  2. `serenazgo_equipamiento`: Actualizada con campos de marca, modelo, número de serie, control por cantidad (`esControlPorCantidad`, `cantidadStock`, `cantidadDisponible`), versión y restricciones únicas.
  3. `serenazgo_unidades_historial_estados`: Bitácora inmutable de transiciones de estado de vehículos con estado anterior, nuevo, motivo justificado, documento de referencia y usuario operador.
  4. `serenazgo_unidades_asignaciones`: Registro formal de asignaciones temporales con personal responsable, fecha/hora inicio, fecha estimada/real de retorno, kilometraje de salida/entrada, nivel de combustible y estado de asignación (`ACTIVA`, `DEVUELTA`, `CANCELADA`).
  5. `serenazgo_equipamiento_asignaciones`: Registro de asignaciones de equipamiento individual o por lote cuantitativo con sereno receptor, motivo y estado de conservación.
  6. `serenazgo_mantenimientos`: Registro unificado de mantenimientos para vehículos y equipos mediante discriminador `tipo_recurso`, tipo (`PREVENTIVO`, `CORRECTIVO`, `INSPECCION_TECNICA`), fechas, costos y proveedor técnico.
  7. `serenazgo_incidencias_tecnicas`: Registro formal de anomalías técnicas, averías y daños con nivel de gravedad, estado de atención (`REPORTADA`, `EN_EVALUACION`, `EN_REPARACION`, `RESUELTA`, `DESESTIMADA`) y medidas adoptadas.
- **Catálogo de 11 Permisos Granulares RBAC:**
  - `SERENAZGO_UNIDADES_VER`: Consulta de catálogo de flota vehicular y disponibilidad.
  - `SERENAZGO_UNIDADES_CREAR`: Alta de nuevas unidades móviles en inventario municipal.
  - `SERENAZGO_UNIDADES_EDITAR`: Modificación de ficha técnica y especificaciones de vehículos.
  - `SERENAZGO_UNIDADES_CAMBIAR_ESTADO`: Transición administrativa de estados operativos.
  - `SERENAZGO_UNIDADES_ASIGNAR`: Asignación y recepción de vehículos en servicio de patrullaje.
  - `SERENAZGO_UNIDADES_MANTENIMIENTO`: Registro y liquidación de mantenimientos vehiculares.
  - `SERENAZGO_EQUIPAMIENTO_VER`: Consulta de inventario de equipamiento y disponibilidad.
  - `SERENAZGO_EQUIPAMIENTO_CREAR`: Alta de equipos operativos y herramientas de seguridad.
  - `SERENAZGO_EQUIPAMIENTO_EDITAR`: Actualización de datos técnicos de equipamiento.
  - `SERENAZGO_EQUIPAMIENTO_ASIGNAR`: Entrega y devolución de herramientas y material operativo.
  - `SERENAZGO_EQUIPAMIENTO_EXPORTAR`: Exportación de reportes ejecutivos en PDF y Excel institucional.

### 3. Servicios de Negocio Implementados
- **`UnidadesMovilesService` / `UnidadesMovilesServiceImpl`:**
  - `registrarUnidad`: Generación automática de código `UNID-{YYYY}-{NNNN}`, validación de placas normalizadas peruanas y unicidad de número de serie/código patrimonial.
  - `actualizarUnidad`: Edición protegida de datos técnicos con control de concurrencia optimista (`@Version`).
  - `cambiarEstado`: Transición reglamentada validando que unidades con asignación activa no puedan pasar a inactividad sin devolución previa, persistiendo bitácora inmutable en `serenazgo_unidades_historial_estados`.
  - `asignarUnidad`: Verificación de personal activo, validación de que el sereno no posea otra unidad asignada simultáneamente, verificación de que el vehículo esté `DISPONIBLE` y transición atómica a `ASIGNADA`.
  - `devolverUnidad`: Cierre formal de asignación con registro de kilometraje final, inspección técnica de recepción y liberación de la unidad a estado `DISPONIBLE`.
  - `generarFichaTecnicaPdf`: Generación de informe técnico en PDF A4 mediante OpenPDF con sello institucional y trazabilidad operativa.
- **`EquipamientoSerenazgoService` / `EquipamientoSerenazgoServiceImpl`:**
  - `registrarEquipamiento`: Generación correlativa `EQP-{YYYY}-{NNNN}`, control de bienes individualizados vs controlados por cantidad.
  - `actualizarEquipamiento`: Actualización de ficha y control de stock operativo.
  - `asignarEquipamiento`: Validación de stock disponible o estado operativo `BUENO`/`REGULAR`, descuento automático de stock y registro de asignación formal.
  - `devolverEquipamiento`: Devolución parcial o total con reintegro al stock disponible y evaluación de daños.
- **`RecursosSeguridadService` / `RecursosSeguridadServiceImpl`:**
  - `obtenerDashboardRecursos`: Cálculo agregado en tiempo real directamente desde PostgreSQL (unidades totales, disponibles, asignadas, en mantenimiento, equipos asignados, incidencias abiertas).
  - `programarMantenimiento`: Registro de orden de servicio con transición temporal de estado operativo y asignación de taller.
  - `finalizarMantenimiento`: Liquidación del mantenimiento, registro de costo real y restitución de disponibilidad operativa.
  - `reportarIncidencia`: Apertura de ticket técnico con tipificación, gravedad y notificación preventiva.
  - `exportarInventarioExcel`: Exportación en formato `.xlsx` con Apache POI conteniendo dos pestañas ejecutivas (*Unidades Móviles* y *Equipamiento Operativo*) con cabeceras estilizadas, autoajuste de columnas y sanitización estricta contra inyección de fórmulas CSV.

### 4. Vistas Web Thymeleaf y API REST
- **Vistas Administrativas (`/dashboard/serenazgo/**`):**
  - `/dashboard/serenazgo/unidades`: Listado interactivo con tarjetas de indicadores, tabla de flota, filtros por tipo, placa, oficina y estado, paginación y acciones rápidas.
  - `/dashboard/serenazgo/unidades/nueva`: Formulario de registro con validaciones frontend y backend.
  - `/dashboard/serenazgo/unidades/{id}/editar`: Formulario de edición con control de versión.
  - `/dashboard/serenazgo/unidades/{id}`: Ficha técnica detallada organizada en 6 pestañas (*Información General*, *Asignaciones*, *Mantenimientos*, *Incidencias*, *Documentos*, *Historial de Estados*) con 5 modales interactivos (*Asignar Unidad*, *Devolver Unidad*, *Cambiar Estado*, *Programar Mantenimiento*, *Reportar Incidencia*).
  - `/dashboard/serenazgo/equipamiento`: Listado de equipamiento con filtros por categoría, número de serie, condición y disponibilidad.
  - `/dashboard/serenazgo/equipamiento/nuevo`: Formulario de alta para bienes individualizados y controlados por cantidad.
  - `/dashboard/serenazgo/equipamiento/{id}/editar`: Formulario de edición de equipamiento.
  - `/dashboard/serenazgo/equipamiento/{id}`: Ficha detallada con historial de entregas, devoluciones, mantenimientos e incidencias.
- **Controladores Implementados:**
  - `UnidadesMovilesWebController` (`/dashboard/serenazgo/unidades/**`).
  - `EquipamientoWebController` (`/dashboard/serenazgo/equipamiento/**`).
  - `RecursosSeguridadRestController` (`/api/serenazgo/recursos/**`): Endpoints REST para métricas de dashboard, asignaciones, devoluciones, mantenimientos, incidencias y exportación de inventarios.

### 5. Cobertura de Pruebas Automatizadas (19/19 - 100% Exitosas)
- **`UnidadesMovilesServiceTest` (9 pruebas de lógica de negocio y transiciones):**
  1. `test01_registrarUnidadExitosamente`: Generación de código correlativo, normalización de placas y persistencia.
  2. `test02_rechazarRegistroPorPlacaDuplicada`: Control de unicidad de placa vehicular.
  3. `test03_rechazarRegistroPorCodigoPatrimonialDuplicado`: Control de unicidad de código patrimonial.
  4. `test04_cambiarEstadoConHistorialInmutable`: Registro de bitácora histórica y cambio de estado.
  5. `test05_rechazarCambioEstadoSiUnidadEstaAsignada`: Regla de negocio que impide retirar o pasar a mantenimiento un vehículo en servicio activo sin previo retorno.
  6. `test06_asignarUnidadExitosamente`: Asignación temporal a sereno activo y cambio atómico a `ASIGNADA`.
  7. `test07_rechazarAsignacionSiUnidadNoDisponible`: Prevención de asignación simultánea de unidades no disponibles.
  8. `test08_devolverUnidadExitosamente`: Devolución formal con verificación de kilometraje y cambio a `DISPONIBLE`.
  9. `test09_generarFichaTecnicaPdf`: Generación exitosa de documento binario PDF con cabecera municipal.
- **`EquipamientoSerenazgoServiceTest` (5 pruebas de equipos y control por cantidad):**
  10. `test01_registrarEquipamientoIndividualizado`: Registro de bien con número de serie único.
  11. `test02_registrarEquipamientoPorCantidad`: Alta de insumos por lote con control de existencias.
  12. `test03_asignarEquipamientoIndividual`: Entrega de radio transceptor y actualización de estado.
  13. `test04_asignarEquipamientoPorCantidadYDescontarStock`: Descuento atómico de existencias disponibles.
  14. `test05_devolverEquipamientoYRestituirStock`: Devolución parcial/total y restitución de existencias.
- **`RecursosSeguridadWebSecurityTest` (5 pruebas de integración web, RBAC y CSRF):**
  15. `test01_accesoAnonimoRechazado`: Bloqueo HTTP 401 Unauthorized a endpoints REST sin token JWT.
  16. `test02_listarUnidadesWebSinAutenticacionRedirigeLogin`: Redirección 302 al login para accesos anónimos a vistas web.
  17. `test03_listarUnidadesConPermisoVer`: Acceso 200 OK a vistas administrativas con permiso `SERENAZGO_UNIDADES_VER`.
  18. `test04_dashboardMetricasApiConRolSereno`: Consulta autorizada de KPIs desde `/api/serenazgo/recursos/dashboard`.
  19. `test05_exportarExcelInventario`: Generación y descarga binaria autorizada de libro XLSX con permiso `SERENAZGO_EQUIPAMIENTO_EXPORTAR`.

---

## 47. Módulo 3: Seguridad Ciudadana y Serenazgo — Registro y Administración de Sectores y Zonas de Patrullaje (Prompt 39)

### 1. Resumen de Arquitectura y Organización Territorial
El subsistema de **Registro y Administración de Sectores y Zonas de Patrullaje** implementa la infraestructura de organización, supervisión administrativa y delimitación geográfica autorizada para el despliegue del Serenazgo de la Municipalidad Distrital de Quinua:
- **Diferenciación Conceptual Estricta:**
  - **Sector de Patrullaje:** Unidad territorial operativa macro utilizada para organizar, planificar y supervisar la cobertura del servicio municipal de serenazgo. Asociado formalmente a una oficina institucional responsable (p. ej. *Subgerencia de Seguridad Ciudadana y Serenazgo*).
  - **Zona de Patrullaje:** Subdivisión operativa interna de un sector para patrullajes específicos a pie, en motocicleta o vehículo menor. Cada zona pertenece obligatoriamente a un sector válido.
  - **Aclaración Jurídica y Territorial:** Las delimitaciones y polígonos son de índole estrictamente operativa-administrativa para la prestación del servicio de seguridad ciudadana y no constituyen límites distritales o político-administrativos oficiales ni definitivos del distrito de Quinua.
- **Códigos Internos Unívocos:**
  - Sectores: `SEC-{YYYY}-{0000}` respaldados por la secuencia PostgreSQL `seq_serenazgo_sector`.
  - Zonas: `ZON-{YYYY}-{0000}` respaldados por la secuencia PostgreSQL `seq_serenazgo_zona`.
- **Estados Operativos y Transiciones:**
  - Estados: `BORRADOR`, `ACTIVO`, `INACTIVO`, `ARCHIVADO`.
  - Regla de Cascada de Inactivación: Si un sector transiciona a `INACTIVO` o `ARCHIVADO`, las zonas activas dependientes se desactivan automáticamente para impedir que existan zonas operativamente activas dentro de sectores inactivos.
- **Trazabilidad Inmutable:**
  - Historial de estados (`serenazgo_territorio_historial_estados`): usuario, fecha, motivo, entidad afectada (`SECTOR`/`ZONA`).
  - Historial de reasignaciones (`serenazgo_zonas_historial_sector`): trazabilidad completa cuando una zona se transfiere de un sector a otro, con usuario, fecha, motivo y referencia administrativa.
  - Historial de delimitaciones (`serenazgo_territorio_historial_delimitaciones`): versionado secuencial de geometrías (`v1`, `v2`, etc.) con usuario, motivo, fecha y respaldo GeoJSON.

### 2. Soporte Geográfico, Cartografía y Leaflet.js
- **Estándar GeoJSON (RFC 7946):** Geometrías almacenadas en formato GeoJSON nativo en WGS84 (`EPSG:4326`), compatibles con PostgreSQL estándar sin depender de extensiones binarias externas privativas.
- **Validador de Integridad Espacial (`GeoJsonValidatorService`):**
  - Validación sintáctica y semántica de tipos autorizados (`Polygon`, `MultiPolygon`, `Feature`, `FeatureCollection`).
  - Verificación de cierre de anillos poligonales (primer punto igual al último punto).
  - Control de cardinalidad mínima (mínimo 4 vértices para polígonos cerrados).
  - Verificación estricta de coordenadas WGS84: Longitud [-180, 180], Latitud [-90, 90] en orden `[longitud, latitud]`.
  - Cálculo automático de bounding box (`bbox: [minLon, minLat, maxLon, maxLat]`).
  - Prevención de geometrías vacías o polígonos degenerados.
- **Política de No Invención:** El sistema admite sectores y zonas sin geometría inicial. Se rechaza la generación de polígonos o coordenadas ficticias del distrito de Quinua.
- **Visualizador Cartográfico Interactivo:**
  - Integrado con Leaflet.js sobre el centro referencial de Quinua (`[-13.045, -74.140]`).
  - Capas temáticas diferenciadas: Sectores (polígonos azules translúcidos con bordes destacados) y Zonas (polígonos esmeralda translúcidos).
  - Popups informativos con código, nombre, tipo, estado y oficina responsable.
  - Mensaje claro de estado vacío cuando no existen geometrías autorizadas registradas.
  - Formulario modal de importación de archivos GeoJSON válidos con validación inmediata en backend.

### 3. Base de Datos y Migración Flyway V40 (`V40__serenazgo_sectores_zonas_geografia.sql`)
- **Restricciones de Integridad Actualizadas:**
  - Modificación de checks en `serenazgo_sectores` y `serenazgo_zonas` para admitir `BORRADOR`, `ACTIVO`, `INACTIVO`, `ARCHIVADO`.
  - Agregación de columnas de vigencia (`fecha_inicio_vigencia`, `fecha_fin_vigencia`), observaciones, campo GeoJSON de texto (`geometria_geojson`), flag `tiene_delimitacion` y prioridad operativa para zonas (`prioridad_operativa`).
- **Tablas de Historial Creadas:**
  1. `serenazgo_territorio_historial_delimitaciones`: Auditoría y versionado histórico de polígonos y delimitaciones.
  2. `serenazgo_zonas_historial_sector`: Trazabilidad inmutable de reasignación de zonas entre sectores.
  3. `serenazgo_territorio_historial_estados`: Bitácora inmutable de cambios de estado administrativo.
- **Catálogo de 9 Permisos Granulares RBAC:**
  - `SERENAZGO_SECTORES_VER`: Consulta de sectores y cuadrantes de patrullaje.
  - `SERENAZGO_SECTORES_CREAR`: Alta de sectores territoriales.
  - `SERENAZGO_SECTORES_EDITAR`: Modificación de sectores existentes.
  - `SERENAZGO_SECTORES_CAMBIAR_ESTADO`: Transición de estados operativos de sectores.
  - `SERENAZGO_ZONAS_VER`: Consulta de zonas de patrullaje.
  - `SERENAZGO_ZONAS_CREAR`: Registro de zonas dentro de sectores.
  - `SERENAZGO_ZONAS_EDITAR`: Actualización de datos y parámetros de zonas.
  - `SERENAZGO_ZONAS_REASIGNAR`: Transferencia de una zona hacia otro sector autorizado.
  - `SERENAZGO_GEOGRAFIA_EDITAR`: Edición, dibujo y actualización de delimitaciones.
  - `SERENAZGO_GEOGRAFIA_IMPORTAR`: Carga de archivos GeoJSON oficiales al repositorio territorial.
  - `SERENAZGO_GEOGRAFIA_EXPORTAR`: Exportación de capas espaciales autorizadas en GeoJSON, PDF y Excel.

### 4. Servicios de Dominio y Negocio
- **`SectorPatrullajeService` / `SectorPatrullajeServiceImpl`:**
  - `registrarSector`: Generación correlativa `SEC-{YYYY}-{0000}`, validación de fechas de vigencia y persistencia.
  - `actualizarSector`: Actualización controlada con bloqueo optimista (`@Version`).
  - `cambiarEstado`: Validación de permisos, registro de motivo en historial inmutable y desactivación en cascada de zonas hijas si el sector pasa a inactivo/archivado.
  - `actualizarGeometria`: Validación de GeoJSON mediante `GeoJsonValidatorService`, asignación de versión incremental e inserción en historial.
  - `generarFichaSectorPdf`: Ficha técnica de sector en formato PDF con OpenPDF con sello municipal y listado de zonas adscritas.
  - Compatibilidad preservada con firmas previas (`listarSectoresActivos()`, `obtenerSectorPorId()`).
- **`ZonaPatrullajeService` / `ZonaPatrullajeServiceImpl`:**
  - `registrarZona`: Generación correlativa `ZON-{YYYY}-{0000}`, vinculación obligatoria a sector existente y validación de prioridad.
  - `actualizarZona`: Edición protegida de metadatos de zona.
  - `reasignarSector`: Validación de sector destino, verificación de dependencias operativas, actualización atómica y registro en `serenazgo_zonas_historial_sector`.
  - `cambiarEstado`: Prevención de activación de zonas en sectores que se encuentren inactivos.
  - `actualizarGeometria`: Validación y versionado geográfico de la zona.
- **`TerritorioSeguridadService` / `TerritorioSeguridadServiceImpl`:**
  - `obtenerDashboardTerritorio`: KPIs agregados calculados directamente en PostgreSQL (total sectores, sectores activos, total zonas, zonas activas, delimitaciones disponibles, zonas pendientes de delimitación).
  - `obtenerGeoJsonCapas`: Construcción de `FeatureCollection` GeoJSON RFC 7946 con propiedades tipificadas (`codigo`, `nombre`, `tipo`, `estado`, `oficina`).
  - `importarGeoJson`: Parseo y carga atómica de delimitaciones para sectores o zonas autorizados.
  - `exportarExcelTerritorio`: Libro `.xlsx` mediante Apache POI con dos hojas de trabajo (*Sectores de Patrullaje* y *Zonas de Patrullaje*), filtros automáticos y sanitización contra inyección CSV.

### 5. Vistas Web Thymeleaf y API REST
- **Vistas Administrativas (`/dashboard/serenazgo/sectores` y `/dashboard/serenazgo/zonas`):**
  - `index.html`: Centro de control territorial con 3 pestañas principales (*Sectores*, *Zonas*, *Mapa territorial*), 6 tarjetas de indicadores reales y modales interactivos para cambio de estado, reasignación e importación GeoJSON.
  - `sector-form.html` y `sector-detalle.html`: Formulario de alta/edición y ficha técnica integral con 4 pestañas (*Información General*, *Zonas Adscritas*, *Delimitación y Mapa*, *Historial y Trazabilidad*).
  - `zona-form.html` y `zona-detalle.html`: Formulario de alta/edición y ficha técnica integral de zona con visualizador geográfico y trazabilidad de reasignaciones.
- **Controladores Implementados:**
  - `SectoresZonasWebController` (`/dashboard/serenazgo/sectores/**`, `/dashboard/serenazgo/zonas/**`).
  - `TerritorioRestController` (`/api/serenazgo/territorio/**`): Endpoints para métricas agregadas, FeatureCollection GeoJSON de capas, importación, reasignación, cambios de estado y descargas en PDF y Excel.

### 6. Cobertura de Pruebas Automatizadas (18/18 - 100% Exitosas)
- **`SectorPatrullajeServiceTest` (7 pruebas unitarias de dominio y cascada):**
  1. `test01_registrarSectorExitosamente`: Generación correlativa, validación de vigencias y persistencia.
  2. `test02_rechazarRegistroCodigoDuplicado`: Control de unicidad de código de sector.
  3. `test03_rechazarFechasVigenciaInvalidas`: Validación de inicio no posterior al fin.
  4. `test04_cambiarEstadoAInactivoDesactivaZonasEnCascada`: Desactivación automática de zonas dependientes.
  5. `test05_actualizarGeometriaGeoJsonValida`: Versionado y registro en historial de delimitaciones.
  6. `test06_actualizarGeometriaGeoJsonInvalidaLanzaExcepcion`: Rechazo estricto a GeoJSON sin cierre de anillo.
  7. `test07_generarFichaSectorPdf`: Generación de documento PDF OpenPDF con estructura municipal.
- **`ZonaPatrullajeServiceTest` (5 pruebas unitarias de zonas y reasignación):**
  8. `test01_registrarZonaExitosamente`: Registro de zona adscrita a sector válido y generación `ZON-{YYYY}-{0000}`.
  9. `test02_rechazarRegistroSiSectorInexistente`: Control de integridad referencial de sector.
  10. `test03_reasignarSectorExitosamente`: Transferencia de sector y bitácora en `serenazgo_zonas_historial_sector`.
  11. `test04_rechazarActivacionDeZonaEnSectorInactivo`: Prevención de zonas activas en sectores inactivos.
  12. `test05_rechazarGeometriaZonaInvalida`: Control de validación espacial de polígono.
- **`TerritorioSeguridadWebSecurityTest` (6 pruebas de integración web, RBAC y CSRF):**
  13. `test01_accesoAnonimoRechazadoApi`: Bloqueo `HTTP 401 Unauthorized` a endpoints REST sin token JWT.
  14. `test02_listarSectoresWebSinAutenticacionRedirigeLogin`: Redirección `302` a login para accesos anónimos.
  15. `test03_listarSectoresWebConPermisoVer`: Acceso `200 OK` a `/dashboard/serenazgo/sectores` con permiso `SERENAZGO_SECTORES_VER`.
  16. `test04_capasGeoJsonApiConRolSereno`: Consulta autorizada de capas espaciales GeoJSON con token JWT.
  17. `test05_exportarExcelTerritorio`: Descarga autorizada de libro XLSX con permiso `SERENAZGO_GEOGRAFIA_EXPORTAR`.
  18. `test06_cambiarEstadoSectorSinPermisoRechazado`: Bloqueo `403 Forbidden` a usuarios no autorizados.

---

## 48. MÓDULO 3 — SEGURIDAD CIUDADANA Y SERENAZGO: PROGRAMACIÓN DE TURNOS Y SERVICIOS DEL PERSONAL (PROMPT 40)

Implementado en el marco del **Prompt 40 de 150** para dotar a la Municipalidad Distrital de Quinua de un sistema integral de **Programación de Turnos y Servicios del Personal de Serenazgo**, correspondiente al Módulo 3: Seguridad Ciudadana y Serenazgo.

Permite a los supervisores y jefes de oficina organizar los horarios operativos, configurar turnos de guardia, programar servicios de patrullaje preventivo y vigilancia municipal, asignar serenos con funciones operativas específicas, reservar unidades móviles y equipamiento institucional, detectar conflictos de horario en tiempo real y emitir hojas de servicio y reportes ejecutivos.

```mermaid
graph TD
    T[Configuración de Turnos<br/>Diurno, Nocturno, Mixto, Especial<br/>Cruce de Medianoche 22:00-06:00] --> PRG[Programación de Servicio<br/>PRG-YYYY-00000<br/>BORRADOR]
    SEC[Sectores y Zonas<br/>Prompt 39] --> PRG
    PER[Personal Serenazgo<br/>Prompt 37] -->|Validación Disponibilidad| PRG
    UNI[Unidades Móviles<br/>Prompt 38] -->|Reserva Operativa| PRG
    EQ[Equipamiento<br/>Prompt 38] -->|Reserva Patrimonial| PRG
    
    PRG -->|Detección Conflictos: inicioA < finB && inicioB < finA| VAL{¿Sin Conflictos?}
    VAL -->|Conflicto Detectado| ERR[Alerta de Solapamiento Horario<br/>Impedir Asignación]
    VAL -->|Validación Exitosa| CONF[CONFIRMADA / PROGRAMADA]
    
    CONF -->|Inicio Operativo| CURSO[EN_CURSO]
    CURSO -->|Término de Servicio| FIN[FINALIZADA]
    CONF -->|Mantenimiento/Emergencia| REP[REPROGRAMADA<br/>Motivo Obligatorio + Historial]
    CONF -->|Suspensión Justificada| CANC[CANCELADA<br/>Liberación Inmediata de Personal y Recursos]
    
    CONF --> PDF[Hoja de Servicio PDF<br/>OpenPDF + Membrete Municipal]
    CONF --> XLS[Calendario Operativo Excel<br/>Apache POI XLSX]
```

### 1. Diferencia Conceptual entre Turno y Programación de Servicio
- **Turno (`TurnoSerenazgo`):** Franja horaria de referencia o disponibilidad operativa configurada de manera genérica para el servicio (ej. Diurno 07:00-15:00, Tarde 15:00-23:00, Amanecida 22:00-06:00). Soporta turnos que cruzan la medianoche calculando duraciones reales positivas sin artefactos temporales (`(1440 - t1) + t2`).
- **Programación de Servicio (`ProgramacionServicioSerenazgo`):** Despliegue operativo concreto asignado a una fecha, sector de patrullaje, zona facultativa, equipo de serenos (con roles de conductor, patrullero, operador de radio, etc.), vehículos asignados y equipos de comunicación/seguridad reservados.

### 2. Estructura de Base de Datos y Migración Flyway `V41`
- **Secuencias Correlativas:**
  - `seq_serenazgo_turno` para generación correlativa de turnos (`TUR-{YYYY}-{0000}`).
  - `seq_serenazgo_programacion` para generación correlativa de servicios (`PRG-{YYYY}-{00000}`).
- **Modificación a `serenazgo_turnos`:**
  - Inclusión de columnas `tipo_turno`, `cruza_medianoche`, `color_hex`, `version` (bloqueo optimista `@Version`), `deleted_at`.
- **Modificación a `serenazgo_programacion_servicios`:**
  - Inclusión de columnas `tipo_servicio`, `oficina_responsable_id`, `version`, auditoría completa y eliminación lógica.
- **Nuevas Tablas Relacionales:**
  - `serenazgo_programacion_personal`: Asignación N:M de serenos con campos `funcion_operativa` (`CONDUCTOR`, `PATRULLERO`, `OPERADOR_RADIO`, `SUPERVISOR_TURNO`, `MONITOR_CAMARAS`, `OTRO`), bandera `es_responsable`, estado de asignación (`ASIGNADO`, `LIBERADO`, `CANCELADO`, `SUSTITUIDO`) y notas operativas.
  - `serenazgo_programacion_unidades`: Reserva de unidades móviles con estado de reserva (`RESERVADO`, `EN_USO`, `LIBERADO`, `CANCELADO`), control de kilometraje y justificación.
  - `serenazgo_programacion_equipamiento`: Reserva de equipamiento patrimonial (radios, chalecos, linternas) con control de cantidad solicitada y estado de reserva.
  - `serenazgo_programacion_historial`: Registro inmutable de eventos del ciclo de vida (`CREACION`, `CONFIRMACION`, `ASIGNACION_PERSONAL`, `CAMBIO_RECURSOS`, `REPROGRAMADO`, `CANCELADO`, `INICIO_SERVICIO`, `FIN_SERVICIO`) almacenando valores anteriores y nuevos en formato JSONB.
- **Permisos RBAC Incorporados:**
  - `SERENAZGO_TURNOS_VER`, `SERENAZGO_TURNOS_CREAR`, `SERENAZGO_TURNOS_EDITAR`.
  - `SERENAZGO_PROGRAMACION_VER`, `SERENAZGO_PROGRAMACION_CREAR`, `SERENAZGO_PROGRAMACION_EDITAR`, `SERENAZGO_PROGRAMACION_ASIGNAR`, `SERENAZGO_PROGRAMACION_CONFIRMAR`, `SERENAZGO_PROGRAMACION_REPROGRAMAR`, `SERENAZGO_PROGRAMACION_CANCELAR`, `SERENAZGO_PROGRAMACION_EXPORTAR`.

### 3. Regla de Detección de Conflictos Horarios en Intervalo Semiabierto
- Se aplica la regla formal de solapamiento en intervalos semiabiertos `[inicio, fin)`:
  $$\text{Conflicto} \iff \text{inicio}_A < \text{fin}_B \land \text{inicio}_B < \text{fin}_A$$
- Si dos servicios consecutivos comparten únicamente el instante de límite (ej. Servicio 1 finaliza a las 14:00 y Servicio 2 inicia a las 14:00), la evaluación es estrictamente `false` ($14:00 < 14:00$ es falso), permitiendo transiciones limpias de guardia sin falsos positivos de conflicto.
- La validación se ejecuta en servidor tanto para el personal de serenazgo, para las unidades móviles y para los lotes de equipamiento.

### 4. Ciclo de Vida, Reprogramación y Cancelación Justificada
- **Estados:** `BORRADOR` $\rightarrow$ `PROGRAMADA` $\rightarrow$ `CONFIRMADA` $\rightarrow$ `EN_CURSO` $\rightarrow$ `FINALIZADA`.
- **Reprogramación:** Modifica fechas, horarios, turnos o sectores tras revalidar la disponibilidad de todo el personal y recursos vinculados. Exige motivo sustentado y deja traza auditable inmutable en el historial.
- **Cancelación:** Exige motivo administrativo obligatorio; actualiza la programación a `CANCELADA` y automáticamente libera todas las asignaciones de personal (`CANCELADO`) y reservas de vehículos/equipos (`CANCELADO`), dejando los recursos inmediatamente disponibles para otros servicios.

### 5. Servicios de Negocio Implementados
- **`TurnoSerenazgoService` / `TurnoSerenazgoServiceImpl`:**
  - Registro de turnos con código automático correlativo, detección de cruce de medianoche, cálculo de duración en minutos y validación de nombres únicos.
  - Cambio de estado (`ACTIVO` / `INACTIVO`) y actualización controlada.
- **`ProgramacionServicioService` / `ProgramacionServicioServiceImpl`:**
  - `crearProgramacion` / `actualizarProgramacion`: Orquestación atómica con validación de conflictos horarios.
  - `confirmarProgramacion`: Verificación final de integridad y confirmación operativa.
  - `reprogramar`: Ajuste temporal con verificación de solapamientos y registro histórico.
  - `cancelar`: Cancelación atómica con liberación inmediata de recursos.
  - `cambiarEstado`: Transiciones autorizadas con registro de justificación.
  - `verificarConflictos`: Diagnóstico preventivo de disponibilidad horaria para personal, unidades y equipos.
  - `obtenerKpis`: Indicadores en tiempo real (total servicios, confirmados, en curso, finalizados, cancelados, personal asignado, vehículos reservados).
  - `obtenerEventosCalendario`: Transformación de programaciones a eventos interactivos para vistas diaria, semanal y mensual.
  - `generarHojaServicioPdf`: Emisión de orden de servicio formal en formato PDF con OpenPDF, membrete de Quinua, tabla de tripulantes y firma del responsable.
  - `exportarExcel`: Cuadro de rol de servicios operativos en formato XLSX con Apache POI.

### 6. Interfaz de Usuario Web Thymeleaf (`/dashboard/serenazgo/programacion/**`)
- **Centro de Control Operativo (`index.html`):**
  - 4 pestañas interactivas: *Calendario Operativo*, *Programaciones*, *Turnos Horarios*, *Consulta de Disponibilidad*.
  - 6 tarjetas de indicadores KPI en tiempo real.
  - Calendario interactivo con motor JavaScript nativo para visualización por mes, por semana y vista diaria de agenda.
  - Modal interactivo de verificación rápida de disponibilidad horaria.
- **Formulario Wizard de Programación (`programacion-form.html`):**
  - Secciones guiadas: *Datos Generales*, *Turno y Horario*, *Ubicación y Sector*, *Dotación de Personal*, *Unidades Móviles*, *Equipamiento*, *Observaciones*.
  - Filas dinámicas de personal con funciones operativas seleccionables y designación de responsable del servicio.
- **Ficha Integral de Servicio (`programacion-detalle.html`):**
  - 5 pestañas de detalle: *General*, *Personal Asignado*, *Recursos Reservados*, *Sector y Zona*, *Historial y Trazabilidad*.
  - Modales flotantes para Reprogramar Servicio y Cancelar Servicio con motivo obligatorio.
  - Botón de descarga de Hoja de Servicio en PDF.
- **Formulario de Turnos (`turno-form.html`):**
  - Registro y edición de turnos con previsualizador interactivo de duración en minutos y alerta visual cuando cruza la medianoche.

### 7. Endpoints de la API REST (`/api/serenazgo/programacion/**` y `/api/serenazgo/turnos/**`)
| Método | Endpoint | Permiso / Rol | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/serenazgo/programacion` | `SERENAZGO_PROGRAMACION_VER` | Listado paginado de programaciones con filtros |
| `GET` | `/api/serenazgo/programacion/kpis` | `SERENAZGO_PROGRAMACION_VER` | Indicadores operativos en tiempo real |
| `GET` | `/api/serenazgo/programacion/calendario` | `SERENAZGO_PROGRAMACION_VER` | Eventos de calendario para rango de fechas |
| `POST` | `/api/serenazgo/programacion/verificar-conflictos` | `SERENAZGO_PROGRAMACION_CREAR` | Diagnóstico de disponibilidad horaria |
| `POST` | `/api/serenazgo/programacion` | `SERENAZGO_PROGRAMACION_CREAR` | Creación de programación con asignaciones |
| `GET` | `/api/serenazgo/programacion/{id}` | `SERENAZGO_PROGRAMACION_VER` | Detalle integral de programación con recursos |
| `POST` | `/api/serenazgo/programacion/{id}/confirmar` | `SERENAZGO_PROGRAMACION_CONFIRMAR` | Confirmación formal de servicio |
| `POST` | `/api/serenazgo/programacion/{id}/reprogramar` | `SERENAZGO_PROGRAMACION_REPROGRAMAR` | Reprogramación horaria con motivo |
| `POST` | `/api/serenazgo/programacion/{id}/cancelar` | `SERENAZGO_PROGRAMACION_CANCELAR` | Cancelación de servicio y liberación de recursos |
| `GET` | `/api/serenazgo/programacion/{id}/hoja-servicio` | `SERENAZGO_PROGRAMACION_EXPORTAR` | Descarga de hoja de servicio en PDF |
| `GET` | `/api/serenazgo/programacion/exportar-excel` | `SERENAZGO_PROGRAMACION_EXPORTAR` | Exportación de cuadrante de guardias en Excel |
| `GET` | `/api/serenazgo/turnos` | `SERENAZGO_TURNOS_VER` | Listado de turnos configurados |
| `POST` | `/api/serenazgo/turnos` | `SERENAZGO_TURNOS_CREAR` | Creación de turno horario con cálculo de duración |

---

*SIGM-QUINUA — Construido para servir al pueblo de Quinua con eficiencia, seguridad y transparencia.*
