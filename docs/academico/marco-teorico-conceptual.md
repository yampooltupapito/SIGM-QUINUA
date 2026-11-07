# MARCO TEÓRICO Y CONCEPTUAL — SIGM-QUINUA

## 1. MARCO TEÓRICO

### A. Gobierno Electrónico y Modernización de la Gestión Pública
El gobierno electrónico (*e-Government*) comprende el uso de las tecnologías de la información por parte de las instituciones públicas para mejorar la prestación de servicios, fomentar la transparencia y optimizar los procesos internos. En el Perú, la Política Nacional de Transformación Digital (Decreto de Urgencia N° 006-2020) y la Ley del Procedimiento Administrativo General (Ley N° 27444) establecen la obligatoriedad de implementar medios digitales para la recepción y tramitación de expedientes, asegurando la presunción de veracidad y la simplificación administrativa.

### B. Patrón Arquitectónico de Monolito Modular
Frente al paradigma de los microservicios —cuyo despliegue suele acarrear complejidad operacional de red, latencias y altos requerimientos de hardware—, el **Monolito Modular** propone encapsular los límites de dominio (*bounded contexts*) dentro de una única base de código y un solo proceso de ejecución. Cada módulo cuenta con límites de visibilidad estrictos y encapsulamiento de datos, interactuando mediante interfaces bien definidas en memoria y compartiendo transaccionalidad ACID en una base de datos relacional.

### C. Seguridad y Control de Acceso Basado en Roles (RBAC)
El estándar ANSI/INCITS 359-2004 formaliza el modelo RBAC, donde los privilegios no se asignan directamente a los usuarios sino a roles abstractos, y estos roles a su vez agrupan permisos sobre entidades del sistema. Esto permite una auditoría clara y reduce los vectores de escalamiento de privilegios vertical y horizontal.

---

## 2. MARCO CONCEPTUAL (FUNDAMENTOS TECNOLÓGICOS)

* **Java 17 LTS:** Versión de soporte extendido con mejoras en rendimiento, registros (*records*), tipos de datos sellados (*sealed classes*) y gestión optimizada de memoria en el recolector de basura ZGC/G1.
* **Spring Boot 4 / Spring Framework:** Contenedor de inversión de control (IoC) e inyección de dependencias (DI) que provee soporte integral para transacciones declarativas (`@Transactional`), seguridad web y persistencia.
* **PostgreSQL 17:** SGBD relacional avanzado con soporte ACID estricto, tipos JSONB nativos, optimizador de consultas avanzado y funciones criptográficas.
* **Thymeleaf:** Motor de plantillas Java de procesamiento en servidor (*server-side rendering*) que previene vulnerabilidades XSS mediante escape automático de caracteres en el DOM.

