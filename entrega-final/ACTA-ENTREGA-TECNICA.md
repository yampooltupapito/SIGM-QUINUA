# ACTA DE ENTREGA Y CONFORMIDAD TÉCNICA — SIGM-QUINUA

**Entidad:** Municipalidad Distrital de Quinua, Huamanga, Ayacucho, Perú  
**Proyecto:** Sistema Integral de Gestión Municipal (**SIGM-QUINUA**)  
**Fecha de Entrega:** 30 de Octubre de 2026  
**Lugar:** Palacio Municipal de la Villa Histórica de Quinua  

---

## 1. COMPARECIENTES
Por medio de la presente acta se deja constancia de la entrega formal de la versión candidata a producción del software institucional **SIGM-QUINUA**, desarrollado para la modernización de los servicios de la entidad edil.

* **Por el Equipo de Desarrollo / Ingeniería de Software:**  
  Equipo Consultor e Ingenieros de Sistemas Responsables.
* **Por la Municipalidad Distrital de Quinua:**  
  Responsable de Tecnologías de la Información / Gerente Municipal.

---

## 2. OBJETO DE LA ENTREGA
Hacer entrega formal de los siguientes entregables verificables:
1. **Código Fuente Completo:** Repositorio en Java 17 LTS, Spring Boot 4.1.1, PostgreSQL 17, Flyway V1 a V67, Thymeleaf y Tailwind CSS.
2. **Artefacto Ejecutable de Producción:** Archivo `sigm-quinua-0.0.1-SNAPSHOT.jar` (103.6 MB) con hash SHA-256 verificado (`3F3A30CEC5FF7BB15E8ED72812222C09D9DD710350C95D3274A961E55EB922A6`).
3. **Base de Datos y Migraciones:** 67 scripts SQL de migración Flyway aplicados sobre la base de datos `sigm_quinua`.
4. **Documentación Integral (38 archivos):** Especificaciones de arquitectura, diccionario de datos, diagramas C4/UML/BPMN/DER, manuales de usuario y memoria académica universitaria.
5. **Reporte de Calidad:** Certificación de 1,255 pruebas automatizadas ejecutadas y aprobadas (100% de éxito).

---

## 3. ALCANCE ENTREGADO (LOS CUATRO SERVICIOS MUNICIPALES)
Se certifica que la plataforma unifica exclusivamente cuatro servicios institucionales plenamente operativos:
* **Servicio 1:** Núcleo del Sistema y Mesa de Partes Virtual.
* **Servicio 2:** Mercados Municipales.
* **Servicio 3:** Seguridad Ciudadana y Serenazgo.
* **Servicio 4:** Recaudación Tributaria Municipal.

---

## 4. CONDICIONES PREVIAS AL DESPLIEGUE FINAL EN PRODUCCIÓN
Las partes acuerdan que el encendido formal en servidores de producción requiere el cumplimiento de las siguientes condiciones operativas:
1. Asignación formal de contraseñas definitivas para el usuario de base de datos y la clave de firma de tokens (`JWT_SECRET`).
2. Instalación de un Sistema de Alimentación Ininterrumpida (UPS) en el rack del servidor municipal.
3. Habilitación de certificado digital SSL/TLS para el dominio oficial `sigm.muniquinua.gob.pe`.
4. Capacitación presencial al personal municipal de ventanilla de caja y serenazgo.

En señal de conformidad con el estado técnico del sistema y la documentación recibida, se suscribe la presente acta técnica.

```text
__________________________________             __________________________________
Por el Equipo de Ingeniería                     Por la Municipalidad Distrital de Quinua
Responsable Técnico de Liberación               Subgerencia de TI / Gerencia Municipal
```

