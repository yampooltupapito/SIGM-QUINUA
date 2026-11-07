# MATRIZ DE TRAZABILIDAD ACADÉMICA Y TÉCNICA — SIGM-QUINUA

Relación directa y verificable entre los objetivos específicos del proyecto, los requisitos de software, las clases de código fuente y las pruebas automatizadas asociadas:

| Objetivo Específico | Requisitos Asociados | Clases Principales de Código Fuente | Suites de Pruebas de Verificación | Resultado QA |
| :--- | :--- | :--- | :--- | :---: |
| **OE-01: Mesa de Partes Virtual y Núcleo** | RF-01 a RF-12 | `ExpedienteService`<br/>`ConstanciaPdfServiceImpl`<br/>`DashboardServiceImpl` | `MesaPartesServicesIntegrationTest`<br/>`ReportesMesaPartesIntegrationTest`<br/>`DashboardWebIntegrationTest` | **APROBADO** (100%) |
| **OE-02: Mercados Municipales** | RF-13 a RF-18 | `PuestoComercialService`<br/>`AsignacionPuestoService`<br/>`HistorialOcupacionService` | `MercadosServicesIntegrationTest`<br/>`MercadoReportesServiceTest`<br/>`MercadoAsignacionWebSecurityTest` | **APROBADO** (100%) |
| **OE-03: Seguridad y Serenazgo** | RF-19 a RF-24 | `IncidenciaSeguridadService`<br/>`TurnoSerenazgoService`<br/>`ReportesEstadisticosSerenazgo` | `SerenazgoCoreIntegrationTest`<br/>`EstadisticasSerenazgoServiceTest`<br/>`EstadisticasWebSecurityTest` | **APROBADO** (100%) |
| **OE-04: Recaudación Tributaria** | RF-25 a RF-30 | `ContribuyenteService`<br/>`CajaMunicipalService`<br/>`PdfDocumentoTributarioService` | `RecaudacionTributariaServicesIntegrationTest`<br/>`CentroReportesTributariosIntegrationTest`<br/>`PdfDocumentoTributarioTest` | **APROBADO** (100%) |
| **OE-05: Seguridad y Calidad** | RNF-01 a RNF-06 | `SecurityConfig`<br/>`JwtTokenProvider`<br/>`GlobalExceptionHandler` | `AuthSecurityIntegrationTest`<br/>`PersistenceIntegrityTest`<br/>`FlywayMigrationV66Test` | **APROBADO** (100%) |

