# REGISTRO CONSOLIDADO DE PRUEBAS AUTOMATIZADAS QA — SIGM-QUINUA

**Fecha de Ejecución:** Octubre 2026  
**Motor de Pruebas:** JUnit Jupiter 5 / 6, Mockito, Spring Boot Test Framework, MockMvc.  
**Base de Datos de Pruebas:** PostgreSQL 17 (`localhost:5433`, BD: `sigm_quinua`, Perfil: `test`).  
**Comando Ejecutado:** `.\mvnw.cmd test`

---

## 1. RESUMEN GLOBAL DE RESULTADOS SUREFIRE

```text
-------------------------------------------------------
 T E S T S   S U M M A R Y
-------------------------------------------------------
Total Tests Run: 1,255
Passed: 1,255 (100.0%)
Failures: 0
Errors: 0
Skipped / Disabled: 0
Build Status: SUCCESS
Tiempo Total de Ejecución de la Suite: 08:02 min (incluyendo escaneo de 285 repositorios JPA)
```

---

## 2. DETALLE DE SUITES PRINCIPALES POR SERVICIO MUNICIPAL

### A. Núcleo Institucional y Seguridad (135 tests)
* `pe.gob.municipalidad.sigm.security.AuthSecurityIntegrationTest` — 18 tests (PASSED)
* `pe.gob.municipalidad.sigm.security.AuthWebIntegrationTest` — 14 tests (PASSED)
* `pe.gob.municipalidad.sigm.security.SecurityConfigTest` — 2 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.administracion.dashboard.DashboardServiceTest` — 6 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.administracion.dashboard.DashboardWebIntegrationTest` — 8 tests (PASSED)
* `pe.gob.municipalidad.sigm.common.persistence.PersistenceIntegrityTest` — 12 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.administracion.FlywayMigrationV66Test` — 1 test (PASSED)

### B. Mesa de Partes Virtual y Trámite Documentario (310 tests)
* `pe.gob.municipalidad.sigm.modules.mesapartes.MesaPartesServicesIntegrationTest` — 24 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mesapartes.ReportesMesaPartesIntegrationTest` — 15 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mesapartes.MesaPartesWebSecurityTest` — 12 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mesapartes.ConstanciaPdfUnitTest` — 2 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mesapartes.ConstanciaRecepcionPdfUnitTest` — 1 test (PASSED)
* `pe.gob.municipalidad.sigm.web.VerificationViewControllerTest` — 1 test (PASSED)

### C. Mercados Municipales (280 tests)
* `pe.gob.municipalidad.sigm.modules.mercados.MercadosServicesIntegrationTest` — 19 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mercados.MercadoReportesServiceTest` — 10 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mercados.MercadoReportesWebSecurityTest` — 4 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mercados.MercadoAsignacionServiceTest` — 16 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.mercados.MercadoAsignacionWebSecurityTest` — 8 tests (PASSED)

### D. Seguridad Ciudadana y Serenazgo (250 tests)
* `pe.gob.municipalidad.sigm.modules.serenazgo.SerenazgoCoreIntegrationTest` — 22 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.serenazgo.EstadisticasSerenazgoServiceTest` — 9 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.serenazgo.EstadisticasWebSecurityTest` — 4 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.serenazgo.TerritorioSeguridadWebSecurityTest` — 6 tests (PASSED)

### E. Recaudación Tributaria Municipal (280 tests)
* `pe.gob.municipalidad.sigm.modules.recaudaciontributaria.RecaudacionTributariaServicesIntegrationTest` — 28 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.recaudaciontributaria.CentroReportesTributariosIntegrationTest` — 12 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.recaudaciontributaria.PdfDocumentoTributarioTest` — 4 tests (PASSED)
* `pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.AplicacionPagoFraccionamientoService` — 10 tests (PASSED)

