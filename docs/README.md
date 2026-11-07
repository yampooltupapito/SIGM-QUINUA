# ÍNDICE GENERAL DE DOCUMENTACIÓN TÉCNICA E INSTITUCIONAL — SIGM-QUINUA

**Sistema Integral de Gestión Municipal de la Municipalidad Distrital de Quinua**  
*Provincia de Huamanga, Departamento de Ayacucho, República del Perú*  
**Versión del Sistema:** 1.0.0-PROD (Build 0.0.1-SNAPSHOT)  
**Versión Documental:** 2.0 (Revisión Final de Entrega — Prompt 79 de 80)  
**Fecha de Revisión:** Octubre 2026  
**Estado:** Sistema Implementado y Validado (100% de Pruebas Aprobadas)

---

## 1. PRESENTACIÓN Y ALCANCE DOCUMENTAL

El presente repositorio documental reúne la especificación arquitectónica, técnica, funcional, operativa, de seguridad, de calidad y académica de la plataforma **SIGM-QUINUA**. Toda la documentación aquí contenida refleja fidedignamente el código fuente desarrollado en Java 17, Spring Boot 4.1.1, PostgreSQL 17 y sus plantillas institucionales Thymeleaf.

El sistema concentra sus operaciones exclusivamente en **cuatro servicios municipales consolidados**:
1. **Núcleo del Sistema y Mesa de Partes Virtual:** Gestión institucional, seguridad, oficinas, usuarios, radicación digital y seguimiento de expedientes.
2. **Mercados Municipales:** Padrón de comerciantes, pabellones, puestos, contratos, cobro diario de sisa y merced conductiva.
3. **Seguridad Ciudadana y Serenazgo:** Planificación de turnos, patrullajes, cuadrantes, despacho de unidades móviles, reporte de incidencias y protección ciudadana.
4. **Recaudación Tributaria Municipal:** Padrón de contribuyentes, impuesto predial, arbitrios, cuenta corriente, convenios de fraccionamiento, caja y cobranza coactiva.

---

## 2. ESTRUCTURA Y NAVEGACIÓN DOCUMENTAL

### A. Especificaciones de Ingeniería y Arquitectura
* [01. Descripción General del Sistema](01-descripcion-general.md) — Objetivos, contexto de Quinua, alcance y tecnologías.
* [02. Especificación de Requisitos de Software](02-requisitos.md) — Requisitos funcionales (RF) y no funcionales (RNF).
* [03. Arquitectura del Sistema](03-arquitectura.md) — Monolito modular, patrones de capas, desacoplamiento y flujos.
* [04. Estructura del Proyecto y Paquetes](04-estructura-proyecto.md) — Árbol de código, convenciones y distribución de artefactos.
* [05. Modelo de Base de Datos y Persistencia](05-base-datos.md) — Esquema PostgreSQL 17, Flyway V1 a V67 y reglas ACID.
* [06. Seguridad, Autenticación y Autorización](06-seguridad.md) — Spring Security, RBAC, matriz de permisos, JWT y protección de datos.
* [07. Catálogo de Endpoints y APIs](07-api-endpoints.md) — Rutas HTTP, controladores REST/MVC, parámetros y códigos de estado.
* [08. Procesos de Negocio Institucionales](08-procesos-negocio.md) — Flujos operativos municipales y lógica de tramitación.
* [09. Integración y Comunicación Modular](09-integraciones.md) — Enlaces entre recaudación, mercados, serenazgo y mesa de partes.

### B. Operación, Despliegue y Mantenimiento
* [10. Manual de Instalación Local y Requisitos](10-instalacion.md) — Paso a paso para entorno Windows 11 / Linux con JDK 17 y PostgreSQL.
* [11. Configuración de Entornos y Propiedades](11-configuracion.md) — Parámetros de `application.properties` y variables de entorno.
* [12. Guía de Despliegue en Servidores](12-despliegue.md) — Empaquetado JAR, servicios systemd/Windows Service, reverse proxy Nginx.
* [13. Informe de Aseguramiento de Calidad y Pruebas QA](13-pruebas-qa.md) — 1,255 pruebas automatizadas, JUnit 5, Mockito y MockMvc.
* [14. Manual de Mantenimiento Preventivo y Correctivo](14-mantenimiento.md) — Monitoreo, rotación de logs, actualización y soporte.
* [15. Estrategia de Respaldo y Recuperación ante Desastres](15-respaldo-recuperacion.md) — Backups de PostgreSQL (`pg_dump`), retención y restauración.
* [16. Gestión de Riesgos y Limitaciones Técnicas](16-riesgos-limitaciones.md) — Riesgos residuales, restricciones de hardware y mitigaciones.
* [17. Glosario Institucional y Términos Técnicos](17-glosario.md) — Definición de términos tributarios, notariales y computacionales.

---

### C. Diagramas Técnicos y Modelado Formal
* [Diagramas C4 (Contexto, Contenedores, Componentes)](diagramas/c4-model.md)
* [Diagramas UML (Casos de Uso, Clases, Secuencia, Actividad)](diagramas/uml-diagrams.md)
* [Modelado de Procesos BPMN](diagramas/bpmn-processes.md)
* [Diagrama Entidad-Relación (DER PostgreSQL)](diagramas/der-database.md)

---

### D. Manuales de Usuario por Rol
* [Manual del Superadministrador](manuales/manual-superadministrador.md)
* [Manual del Administrador de Sistema](manuales/manual-administrador.md)
* [Manual de Operación de Mesa de Partes](manuales/manual-mesa-partes.md)
* [Manual de Gestión de Mercados Municipales](manuales/manual-mercados.md)
* [Manual del Servicio de Serenazgo y Seguridad](manuales/manual-serenazgo.md)
* [Manual de Recaudación Tributaria y Caja](manuales/manual-recaudacion.md)
* [Manual del Portal Ciudadano](manuales/manual-ciudadano.md)
* [Manual del Portal del Comerciante](manuales/manual-comerciante.md)
* [Manual del Portal del Contribuyente](manuales/manual-contribuyente.md)

---

### E. Sección Académica Universitaria
* [Memoria Académica y Sustentación](academico/memoria-academica.md)
* [Marco Teórico y Conceptual](academico/marco-teorico-conceptual.md)
* [Metodología de Desarrollo Aplicada](academico/metodologia-desarrollo.md)
* [Resultados Demostrables y Discusión](academico/resultados-discusion.md)
* [Matriz de Trazabilidad Académica](academico/trazabilidad-academica.md)

---

### F. Evidencias de Verificación
* [Registro Consolidado de Pruebas Automatizadas](evidencias/registro-pruebas-qa.md)
* [Inventario de Migraciones Flyway Aplicadas](evidencias/inventario-migraciones.md)

