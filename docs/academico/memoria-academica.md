# MEMORIA ACADÉMICA Y SUSTENTACIÓN DE PROYECTO DE INGENIERÍA — SIGM-QUINUA

**Título del Proyecto:**  
*Desarrollo e Implementación de un Sistema Integral de Gestión Municipal bajo Arquitectura de Monolito Modular para la Modernización del Gobierno Electrónico en la Municipalidad Distrital de Quinua, Huamanga, Ayacucho*

**Línea de Investigación:** Ingeniería de Software, Gobierno Digital y Sistemas de Información Empresariales.  
**Grado / Propósito Académico:** Proyecto Final de Ingeniería de Sistemas / Lenguajes de Programación.

---

## 1. RESUMEN EJECUTIVO (ABSTRACT)
El presente proyecto de ingeniería aborda la modernización administrativa y la transformación digital de la Municipalidad Distrital de Quinua mediante el diseño, desarrollo, aseguramiento de calidad y validación de una plataforma unificada denominada **SIGM-QUINUA**. Frente a la fragmentación de trámites en papel, la falta de control en los mercados municipales, la desconexión en el registro de incidencias de serenazgo y la evasión en la recaudación tributaria local, se implementó una solución monolítica modular en Java 17, Spring Boot 4.1.1 y PostgreSQL 17. La solución consolida cuatro servicios esenciales: Mesa de Partes Virtual, Mercados Municipales, Seguridad Ciudadana y Recaudación Tributaria. La calidad del software fue certificada mediante 1,255 pruebas automatizadas (100% aprobadas), garantizando transaccionalidad ACID, protección de datos personales (Ley N° 29733) y seguridad basada en roles (RBAC).

*Palabras clave:* Gobierno Electrónico, Monolito Modular, Spring Boot, PostgreSQL, Mesa de Partes, Recaudación Tributaria, Serenazgo, Quinua.

---

## 2. PLANTEAMIENTO DEL PROBLEMA
En las municipalidades distritales rurales e intermedias del Perú, como la Municipalidad Distrital de Quinua (Huamanga, Ayacucho), la gestión pública enfrenta limitaciones estructurales:
1. **Falta de Trazabilidad Documental:** Pérdida y retraso injustificado de expedientes ciudadanos tramitados en papel físico.
2. **Descontrol en Mercados y Comercio:** Ocupación informal de puestos, duplicidad de concesiones y pérdida de ingresos por cobro manual de sisa.
3. **Inseguridad y Falta de Evidencias:** Intervenciones de serenazgo registradas en cuadernos manuscritos sin tipificación estadística ni resguardo de la identidad de las víctimas.
4. **Baja Recaudación y Morosidad:** Dificultad para liquidar oportunamente el Impuesto Predial y Arbitrios por carecer de cuenta corriente automatizada.

---

## 3. OBJETIVOS DEL PROYECTO

### Objetivo General:
Desarrollar, auditar y validar una plataforma de software integral, segura y escalable que unifique los procesos críticos de gestión municipal en la Municipalidad Distrital de Quinua.

### Objetivos Específicos:
1. Diseñar e implementar el Núcleo Institucional y la Mesa de Partes Virtual con correlación atómica de expedientes y verificación ciudadana con código QR.
2. Sistematizar la administración de Mercados Municipales regulando la asignación única de puestos y la recaudación de sisa y merced conductiva.
3. Desarrollar el módulo de Seguridad Ciudadana y Serenazgo garantizando el despacho operativo de patrullas y la protección de datos conforme a la Ley N° 29733.
4. Automatizar la Recaudación Tributaria Municipal mediante la liquidación de obligaciones, convenios de fraccionamiento y control estricto de caja con arqueo diario.
5. Ejecutar una rigurosa auditoría de calidad mediante pruebas unitarias, de integración, seguridad y concurrencia sobre PostgreSQL 17.

