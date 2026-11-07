# 17. GLOSARIO INSTITUCIONAL Y TÉRMINOS TÉCNICOS — SIGM-QUINUA

## 1. TÉRMINOS INSTITUCIONALES Y MUNICIPALES (LEGISLACIÓN PERUANA)

* **Arbitrios Municipales:** Tasas que se pagan por la prestación o mantenimiento de servicios públicos individuales como Limpieza Pública, Parques y Jardines, y Serenazgo (Decreto Supremo N° 156-2004-EF - TUO de la Ley de Tributación Municipal).
* **Impuesto Predial:** Tributo anual que grava el valor de los predios urbanos y rústicos en base a su autovalúo (aranceles y valores unitarios oficiales de edificación).
* **Tasa de Interés Moratorio (TIM):** Interés legal aplicable a las deudas tributarias impagas, fijado periódicamente por la SUNAT y adoptado por la municipalidad.
* **TUPA (Texto Único de Procedimientos Administrativos):** Documento de gestión que compendia y sistematiza los procedimientos administrativos tramitados ante la Municipalidad de Quinua.
* **Mesa de Partes:** Unidad orgánica responsable de recibir, foliar, registrar y derivar los documentos y expedientes presentados por los administrados.
* **Merced Conductiva:** Pago o canon periódico que efectúa el comerciante adjudicatario por el uso y usufructo temporal de un puesto comercial en el mercado municipal.
* **Sisa Diaria:** Cobro por concepto de derecho de piso o comercio ambulatorio/ferial en espacios públicos autorizados.
* **Serenazgo:** Servicio municipal de seguridad ciudadana preventiva y disuasiva prestado en coordinación con la Policía Nacional del Perú (Ley N° 31297).
* **Parte de Ocurrencia:** Documento administrativo oficial emitido por serenazgo que detalla los hechos, intervenciones, personas implicadas y evidencias de una incidencia.
* **LPAG (Ley N° 27444):** Ley del Procedimiento Administrativo General, marco regulatorio de los plazos, requisitos y garantías de los trámites ante la administración pública.

---

## 2. TÉRMINOS TÉCNICOS DE SOFTWARE Y ARQUITECTURA

* **ACID (Atomicidad, Consistencia, Aislamiento, Durabilidad):** Conjunto de propiedades que garantizan que las transacciones de base de datos se procesen de manera confiable.
* **BCrypt:** Función de hash criptográfico de contraseñas adaptativa basada en el cifrado Blowfish, resistente a ataques de fuerza bruta.
* **C4 Model:** Modelo de diagramación para arquitectura de software compuesto por 4 niveles: Contexto, Contenedores, Componentes y Código.
* **CSRF (Cross-Site Request Forgery):** Vulnerabilidad web en la que comandos no autorizados son transmitidos desde un usuario en el que el sitio confía. Mitigado en SIGM-QUINUA mediante tokens sincronizados.
* **Flyway:** Herramienta de migración y control de versiones de bases de datos relacionales mediante scripts SQL secuenciales versionados (`V1__...`, `V2__...`).
* **HikariCP:** Pool de conexiones JDBC de alto rendimiento utilizado por Spring Boot para gestionar conexiones eficientes a PostgreSQL.
* **IDOR (Insecure Direct Object References):** Vulnerabilidad donde una aplicación expone referencias a objetos internos de implementación sin validar permisos de propiedad.
* **JPA (Jakarta Persistence API):** Especificación estándar de Java para la gestión de datos relacionales en aplicaciones empresariales.
* **Monolito Modular:** Patrón arquitectónico donde toda la aplicación se ejecuta en un solo proceso pero su código está estructurado en módulos de dominio desacoplados.
* **RBAC (Role-Based Access Control):** Mecanismo de restricción de accesos al sistema según los roles institucionales asignados a cada usuario.
* **Thymeleaf:** Motor de plantillas Java del lado del servidor para renderizar HTML5 seguro e interactivo.

