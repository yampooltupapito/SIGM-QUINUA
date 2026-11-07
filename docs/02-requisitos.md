# 02. ESPECIFICACIÓN DE REQUISITOS DE SOFTWARE — SIGM-QUINUA

## 1. ACTORES DEL SISTEMA

| Actor | Descripción | Rol en el Sistema |
| :--- | :--- | :--- |
| **Superadministrador** | Responsable de TI y gobernanza general | `ROLE_SUPERADMIN` |
| **Administrador Institucional** | Gerente municipal o administrador del sistema | `ROLE_ADMIN` |
| **Operador de Mesa de Partes** | Servidor municipal receptor y derivador | `ROLE_MESA_PARTES` |
| **Jefe / Asistente de Oficina** | Funcionario de área orgánica de destino | `ROLE_OFICINA` |
| **Administrador de Mercados** | Responsable de la subgerencia de comercialización | `ROLE_MERCADOS` |
| **Supervisor de Serenazgo** | Jefe de la unidad de serenazgo y patrullaje | `ROLE_SERENAZGO_SUPERVISOR` |
| **Sereno Operativo** | Personal de campo que atiende incidencias | `ROLE_SERENO` |
| **Cajero Municipal** | Servidor autorizado para cobrar y cuadrar caja | `ROLE_CAJERO` |
| **Especialista Tributario** | Liquidador de rentas y fiscalizador | `ROLE_RENTAS` |
| **Ciudadano Administrado** | Vecino que radica trámites o reporta alertas | `ROLE_CIUDADANO` |
| **Comerciante Adjudicatario** | Titular de puesto comercial en mercado | `ROLE_COMERCIANTE` |
| **Contribuyente Distrital** | Sujeto pasivo de obligaciones tributarias | `ROLE_CONTRIBUYENTE` |

---

## 2. REQUISITOS FUNCIONALES (RF) IMPLEMENTADOS

### A. Núcleo Institucional y Seguridad
* **RF-01:** Autenticación de usuarios mediante credenciales encriptadas con algoritmo BCrypt (factor de costo 12).
* **RF-02:** Gestión de usuarios (alta, modificación, bloqueo preventivo tras 5 intentos fallidos, y reseteo seguro).
* **RF-03:** Asignación granular de roles y permisos basados en la matriz RBAC institucional.
* **RF-04:** Registro de oficinas y unidades orgánicas del Organigrama Municipal de Quinua.
* **RF-05:** Auditoría automática de operaciones críticas (creador, fecha de creación, modificador y fecha de actualización).
* **RF-06:** Dashboard institucional con métricas consolidadas en tiempo real procedentes de PostgreSQL 17.

### B. Mesa de Partes Virtual y Trámite Documentario
* **RF-07:** Registro de expedientes con generación atómica de correlativo anual único (`EXP-YYYY-XXXXXX`).
* **RF-08:** Adjunto de documentos sustentatorios en formato PDF con validación de tipo MIME y cálculo de hash SHA-256.
* **RF-09:** Emisión de Constancia de Recepción Digital con código QR de cotejo público y marca temporal.
* **RF-10:** Derivación secuencial de expedientes entre oficinas municipales con plazo límite de atención.
* **RF-11:** Registro de observaciones con notificación al administrado y flujo de subsanación.
* **RF-12:** Portal de seguimiento ciudadano de trámites sin requerir inicio de sesión mediante código de expediente y clave de seguridad.

### C. Mercados Municipales
* **RF-13:** Catálogo de mercados municipales, pabellones, sectores y puestos comerciales clasificados por giro comercial.
* **RF-14:** Padrón de comerciantes individuales (DNI) y personas jurídicas (RUC).
* **RF-15:** Asignación formal de puestos con validación de unicidad (prohibición de doble adjudicación activa).
* **RF-16:** Generación de contratos y actas de posesión con fecha de inicio, vencimiento y canon establecido.
* **RF-17:** Trazabilidad inmutable de cambios de estado y movimientos en el puesto comercial (`HistorialOcupacion`).
* **RF-18:** Cobro diario de tasa de sisa y emisión de liquidación mensual de merced conductiva.

### D. Seguridad Ciudadana y Serenazgo
* **RF-19:** Mapeo de cuadrantes de seguridad y sectores del distrito de Quinua.
* **RF-20:** Programación de turnos del cuerpo de serenazgo y asignación de personal a unidades móviles/motos.
* **RF-21:** Registro y tipificación de incidencias ciudadanas (alteración del orden público, robo, violencia familiar, emergencias médicas).
* **RF-22:** Enmascaramiento de datos personales de denunciantes, menores y víctimas conforme a la Ley N° 29733.
* **RF-23:** Despacho de unidades móviles y emisión de partes de ocurrencia en formato oficial PDF.
* **RF-24:** Generación de estadísticas operativas y mapa térmico de puntos críticos de intervención.

### E. Recaudación Tributaria Municipal
* **RF-25:** Registro de contribuyentes domiciliados y predios clasificados por zona y características físicas.
* **RF-26:** Determinación y emisión de obligaciones anuales de Impuesto Predial y Arbitrios Municipales.
* **RF-27:** Gestión de cuenta corriente del contribuyente con liquidación de intereses conforme a la TIM vigente.
* **RF-28:** Generación de convenios de fraccionamiento tributario en cuotas con simulación financiera previa.
* **RF-29:** Apertura diaria de caja municipal, cobro en ventanilla, emisión de recibo oficial con QR y arqueo de caja con cuadre de efectivo.
* **RF-30:** Anulación justificada de pagos con reversión contable automática y auditoría obligatoria.

---

## 3. REQUISITOS NO FUNCIONALES (RNF)

* **RNF-01 (Rendimiento):** Tiempo medio de respuesta inferior a 800 ms para transacciones web habituales bajo carga estándar.
* **RNF-02 (Disponibilidad y Concurrencia):** Operación concurrente garantizada mediante pool de conexiones HikariCP configurado en Spring Boot.
* **RNF-03 (Seguridad Criptográfica):** Almacenamiento de contraseñas con BCrypt, firmas digitales SHA-256 para constancias y protección contra OWASP Top 10 (CSRF, XSS, SQL Injection).
* **RNF-04 (Compatibilidad e Interfaz):** Interfaz web responsiva con Tailwind CSS adaptada a pantallas de escritorio (1920x1080), laptops (1366x768) y dispositivos móviles (375x667).
* **RNF-05 (Integridad de Datos):** Transacciones relacionales ACID gestionadas por Hibernate en PostgreSQL 17 con aislamiento `READ_COMMITTED`.
* **RNF-06 (Portabilidad):** Despliegue empaquetado en un único archivo ejecutable `.jar` multiplataforma (Windows Server o Linux Ubuntu Server) que requiere exclusivamente OpenJDK 17.

