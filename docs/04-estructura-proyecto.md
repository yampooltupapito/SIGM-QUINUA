# 04. ESTRUCTURA DEL PROYECTO Y PAQUETES — SIGM-QUINUA

## 1. ÁRBOL PRINCIPAL DE DIRECTORIOS

```text
SIGM-QUINUA/
├── .mvn/                              # Wrapper de Maven
├── docs/                              # Documentación técnica, arquitectónica y operativa
│   ├── diagramas/                     # Diagramas C4, UML, BPMN y DER
│   ├── manuales/                      # Manuales de usuario por rol
│   ├── academico/                     # Documentación de sustentación universitaria
│   └── evidencias/                    # Reportes de pruebas QA y migraciones
├── src/
│   ├── main/
│   │   ├── java/pe/gob/municipalidad/sigm/
│   │   │   ├── SigmQuinuaApplication.java
│   │   │   ├── common/                # Clases compartidas y utilitarias
│   │   │   ├── config/                # Configuraciones Spring Boot y beans
│   │   │   ├── infrastructure/        # Implementaciones de infraestructura
│   │   │   ├── security/              # Seguridad, filtros y autenticación
│   │   │   ├── web/                   # Controladores web globales y manejo de excepciones
│   │   │   └── modules/               # Módulos de los 4 servicios autorizados
│   │   │       ├── administracion/    # Núcleo institucional, usuarios, oficinas, dashboard
│   │   │       ├── mesapartes/        # Trámite documentario y mesa de partes virtual
│   │   │       ├── mercados/          # Puestos, comerciantes, contratos y sisa
│   │   │       ├── serenazgo/         # Turnos, patrullajes, incidencias y supervisión
│   │   │       └── recaudaciontributaria/ # Impuesto predial, arbitrios, caja y fraccionamiento
│   │   └── resources/
│   │       ├── db/migration/          # Migraciones SQL de Flyway (V1__ a V67__)
│   │       ├── static/                # Archivos CSS compilados, JavaScript y recursos
│   │       │   ├── css/               # Tailwind CSS institucional
│   │       │   ├── js/                # Scripts JavaScript nativos por módulo
│   │       │   └── img/               # Escudo y logos de la Municipalidad de Quinua
│   │       ├── templates/             # Plantillas Thymeleaf organizadas por módulo y rol
│   │       └── application.properties # Configuración central de propiedades
│   └── test/
│       ├── java/pe/gob/municipalidad/sigm/ # 165 clases de prueba automatizadas (1,255 tests)
│       └── resources/
│           └── application-test.properties # Configuración del perfil de pruebas
├── pom.xml                            # Descriptor de dependencias Maven
├── mvnw                               # Script ejecutable de Maven para Linux/macOS
└── mvnw.cmd                           # Script ejecutable de Maven para Windows
```

---

## 2. DETALLE DE PAQUETES POR MÓDULO

### A. Paquete Común (`common`)
* `common.audit`: Contiene `AuditableEntity` y `SecurityAuditorAware` para registrar auditoría automática JPA.
* `common.constants`: Valores constantes institucionales, estados y expresiones regulares oficiales (validación de DNI peruano de 8 dígitos y RUC de 11 dígitos).
* `common.document`: Abstracción de persistencia de archivos, entidades `Documento` y repositorio documental con almacenamiento seguro en disco.
* `common.exception`: Jerarquía de excepciones de negocio (`BusinessException`, `ResourceNotFoundException`, `ConflictException`, `InvalidRequestException`).
* `common.response`: Estructura unificada de respuestas API (`ApiResponse<T>` y `ErrorResponse`).
* `common.util`: Funciones de formateo de moneda en Soles (S/), fechas en zona horaria peruana y neutralización de entradas.

### B. Módulo de Administración y Núcleo Institucional (`modules.administracion`)
* `controller`: Controladores de gestión de usuarios, roles, permisos y oficinas.
* `dashboard`: Servicios y controladores del panel de control institucional con agregaciones dinámicas.
* `entity`: Entidades `Usuario`, `Rol`, `Permiso`, `Oficina`, `AuditoriaAdministrativa`.
* `repository`: Repositorios Spring Data JPA para el acceso a las tablas de gobernanza institucional.
* `service`: Lógica de autenticación, alta de servidores públicos y registro de auditoría.

### C. Módulo de Mesa de Partes Virtual (`modules.mesapartes`)
* `controller` / `web`: Vistas de recepción física, bandeja de entrada digital, derivación y seguimiento ciudadano.
* `dto`: Objetos de transferencia para registro de expedientes, solicitudes y observaciones.
* `entity`: `Expediente`, `RequisitoExpediente`, `DerivacionExpediente`, `ObservacionExpediente`.
* `service`: Generación de correlativo anual `EXP-YYYY-NNNNNN`, derivaciones y emisión de constancias PDF.

### D. Módulo de Mercados Municipales (`modules.mercados`)
* `controller` / `web`: Vistas de gestión de mercados, puestos comerciales, comerciantes y recaudación de sisa.
* `entity`: `MercadoMunicipal`, `SectorMercado`, `PabellonMercado`, `PuestoComercial`, `Comerciante`, `AsignacionPuesto`, `HistorialOcupacion`.
* `repository`: Repositorios con ordenamiento determinista para historial de movimientos.
* `service`: Reglas de exclusividad de asignación, emisión de contratos y registro de inspecciones higiénico-sanitarias.

### E. Módulo de Seguridad Ciudadana y Serenazgo (`modules.serenazgo`)
* `controller` / `web`: Gestión de partes de serenazgo, alertas ciudadanas, mapas y patrullajes.
* `entity`: `IncidenciaSeguridadCiudadana`, `PersonalSerenazgo`, `TurnoSerenazgo`, `SectorPatrullaje`, `UnidadMovilSerenazgo`.
* `service`: Asignación de vehículos, despacho de auxilio, enmascaramiento de identidad de víctimas y estadísticas.

### F. Módulo de Recaudación Tributaria Municipal (`modules.recaudaciontributaria`)
* `controller` / `web`: Cuentas corrientes, liquidación de arbitrios, convenios de fraccionamiento y caja municipal.
* `entity`: `Contribuyente`, `PredioTributario`, `ObligacionTributaria`, `PagoTributario`, `CajaMunicipal`, `ConvenioFraccionamiento`.
* `service`: Determinación tributaria, cálculo de TIM, control de saldos y emisión de recibos PDF con código QR.

