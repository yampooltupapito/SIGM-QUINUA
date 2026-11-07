# PAQUETE DE ENTREGA FINAL — SIGM-QUINUA

**Sistema Integral de Gestión Municipal de la Municipalidad Distrital de Quinua**  
*Provincia de Huamanga, Departamento de Ayacucho, República del Perú*  
**Versión Entregada:** 1.0.0-RC1 (Release Candidate 1 — Build 0.0.1-SNAPSHOT)  
**Fecha de Emisión:** Octubre 2026  
**Clasificación Técnica:** APTO PARA SOLICITAR AUTORIZACIÓN DE DESPLIEGUE (Categoría A)

---

## 1. CONTENIDO DEL PAQUETE DE ENTREGA

El presente paquete consolida los artefactos ejecutables, planes de ingeniería, guías de operación y el acta formal de entrega técnica para la transferencia institucional a la Municipalidad Distrital de Quinua:

1. [ACTA-ENTREGA-TECNICA.md](ACTA-ENTREGA-TECNICA.md) — Documento formal de transferencia técnica con firmas de conformidad y compromisos.
2. [METADATOS-ARTEFACTO.md](METADATOS-ARTEFACTO.md) — Especificación técnica del archivo JAR ejecutable, ruta física y huella criptográfica SHA-256.
3. [GUIA-DESPLIEGUE-PRODUCCION.md](GUIA-DESPLIEGUE-PRODUCCION.md) — Procedimiento paso a paso para la puesta en producción en servidor Linux / Windows con PostgreSQL 17 y Nginx HTTPS.
4. [LISTA-VERIFICACION-PRE-DESPLIEGUE.md](LISTA-VERIFICACION-PRE-DESPLIEGUE.md) — Lista de chequeo previa al encendido oficial del servicio municipal.
5. [PLAN-RESPALDO-Y-RECUPERACION.md](PLAN-RESPALDO-Y-RECUPERACION.md) — Cronograma y comandos para copias de seguridad de base de datos y repositorios de documentos PDF.
6. [PLAN-ROLLBACK.md](PLAN-ROLLBACK.md) — Procedimiento de contingencia y reversión ante incidentes críticos en producción.
7. [MATRIZ-RIESGOS-Y-LIMITACIONES.md](MATRIZ-RIESGOS-Y-LIMITACIONES.md) — Registro de riesgos residuales documentados y medidas de mitigación.

---

## 2. REPOSITORIO DE DOCUMENTACIÓN TÉCNICA Y MANUALES ASOCIADOS

Todos los manuales de usuario y especificaciones de arquitectura detalladas se encuentran disponibles en el directorio [`docs/`](../docs/):
* **Índice y Arquitectura:** [`docs/README.md`](../docs/README.md) | [`docs/03-arquitectura.md`](../docs/03-arquitectura.md)
* **Base de Datos y DER:** [`docs/05-base-datos.md`](../docs/05-base-datos.md) | [`docs/diagramas/der-database.md`](../docs/diagramas/der-database.md)
* **Seguridad y Endpoints:** [`docs/06-seguridad.md`](../docs/06-seguridad.md) | [`docs/07-api-endpoints.md`](../docs/07-api-endpoints.md)
* **Manuales Operativos:** [`docs/manuales/`](../docs/manuales/) (Superadmin, Administrador, Mesa de Partes, Mercados, Serenazgo, Recaudación, Portales Ciudadanos)
* **Evidencias QA:** [`docs/evidencias/registro-pruebas-qa.md`](../docs/evidencias/registro-pruebas-qa.md) (1,255 tests aprobados)
* **Sustentación Académica:** [`docs/academico/memoria-academica.md`](../docs/academico/memoria-academica.md)

