# 05. MODELO DE BASE DE DATOS Y PERSISTENCIA — SIGM-QUINUA

## 1. ESPECIFICACIÓN DEL MOTOR DE BASE DE DATOS
* **Sistema Gestor de Base de Datos (SGBD):** PostgreSQL 17 (versión 17.10 oficial).
* **Nombre de la Base de Datos:** `sigm_quinua`.
* **Puerto Predeterminado:** `5433` (configurable vía variable de entorno `DB_PORT`).
* **Codificación y Cotejamiento:** `UTF8` / `es_PE.UTF-8` (garantiza soporte nativo para caracteres en castellano y quechua ayacuchano).
* **Zona Horaria del Motor:** `America/Lima` (UTC-5).
* **Gestión de Migraciones:** Flyway Community Edition (67 migraciones secuenciales aplicadas desde `V1__...` hasta `V67__...`).

---

## 2. TABLAS PRINCIPALES POR DOMINIO

### A. Dominio Institucional y Seguridad (Núcleo)
* `usuarios`: Credenciales de acceso, correo institucional, estado y bloqueo.
* `roles`: Roles del sistema (`ROLE_ADMIN`, `ROLE_MESA_PARTES`, `ROLE_MERCADOS`, `ROLE_SERENO`, `ROLE_CAJERO`, etc.).
* `permisos`: Permisos granulares asociados a módulos y acciones.
* `usuarios_roles`: Tabla puente muchos-a-muchos entre usuarios y roles.
* `roles_permisos`: Tabla puente muchos-a-muchos entre roles y permisos.
* `oficinas`: Unidades orgánicas de la Municipalidad Distrital de Quinua.
* `personas`: Padrón base de personas naturales con DNI y datos de contacto.
* `personas_juridicas`: Padrón base de empresas y asociaciones con RUC.
* `auditoria_administrativa`: Registro inmutable de transacciones críticas.

### B. Dominio de Mesa de Partes Virtual
* `mesa_partes_expedientes`: Maestro de expedientes radicados con código anual `EXP-YYYY-NNNNNN`.
* `mesa_partes_requisitos`: Requisitos adjuntos a cada expediente (archivos PDF).
* `mesa_partes_derivaciones`: Historial de pases y derivaciones entre oficinas.
* `mesa_partes_observaciones`: Notificaciones de observaciones con plazo de subsanación.
* `mesa_partes_actuaciones`: Resoluciones, informes técnicos y proveídos emitidos.

### C. Dominio de Mercados Municipales
* `mercados_municipales`: Instalaciones de mercados (Mercado Central, Artesanal).
* `mercados_sectores`: Sectores dentro del mercado (carnes, verduras, artesanía, comidas).
* `mercados_pabellones`: Pabellones o pasajes.
* `mercados_puestos`: Puestos individuales con número, área en $m^2$ y estado (`DISPONIBLE`, `OCUPADO`, `MANTENIMIENTO`).
* `mercados_comerciantes`: Padrón de comerciantes empadronados.
* `mercados_asignaciones`: Contratos y actas de adjudicación de puestos.
* `mercados_historial_ocupacion`: Trazabilidad inmutable de altas, bajas y transferencias.
* `mercados_cobros_diarios`: Registro de cobro diario de sisa por boleta.

### D. Dominio de Seguridad Ciudadana y Serenazgo
* `serenazgo_personal`: Efectivos de serenazgo activos.
* `serenazgo_turnos`: Programación de turnos (Mañana, Tarde, Noche).
* `serenazgo_sectores`: Zonas y cuadrantes territoriales de patrullaje en Quinua.
* `serenazgo_unidades_moviles`: Camionetas, motocicletas y cuatrimotos.
* `serenazgo_incidencias`: Registro de intervenciones con georreferenciación, tipificación y severidad.
* `serenazgo_intervenciones`: Acciones ejecutadas y apoyo policial coordinado (PNP).

### E. Dominio de Recaudación Tributaria Municipal
* `tributaria_contribuyentes`: Contribuyentes registrados ante rentas.
* `tributaria_predios`: Declaraciones juradas de predios urbanos y rurales.
* `tributaria_obligaciones`: Cuotas emitidas de Impuesto Predial y Arbitrios.
* `tributaria_pagos`: Asientos de recaudación con desglose de insoluto, reajuste e interés.
* `tributaria_cajas`: Sesiones de caja diaria con saldo inicial, ingresos y arqueo.
* `tributaria_fraccionamientos`: Convenios aprobados con cronograma de pagos.

---

## 3. POLÍTICA DE INTEGRIDAD Y REGLAS ACID

1. **Claves Primarias y Unicidad:**
   * UUID v4 en entidades transaccionales expuestas (`expedientes`, `documentos`, `incidencias`) para mitigar vulnerabilidades de enumeración.
   * Índices únicos (`UNIQUE`) en códigos institucionales de expediente, códigos de asignación, DNI y RUC.
2. **Integridad Referencial (`ON DELETE RESTRICT`):**
   * Ningún registro padre con transacciones vinculadas (un contribuyente con pagos o un puesto con contrato histórico) puede ser eliminado físicamente.
3. **Control de Concurrencia Optimista (`@Version`):**
   * En tablas de asignación de puestos y pagos tributarios para evitar condiciones de carrera en operaciones concurrentes.

