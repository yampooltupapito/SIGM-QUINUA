# MANUAL DE OPERACIÓN DE MESA DE PARTES (`ROLE_MESA_PARTES`) — SIGM-QUINUA

## 1. PROPÓSITO DEL ROL
El operador de Mesa de Partes es la puerta de entrada formal de las solicitudes de los ciudadanos de Quinua. Se encarga de la radicación, verificación de requisitos TUPA, foliación digital, emisión de constancias con código QR y derivación a las áreas técnicas.

---

## 2. PROCEDIMIENTOS PASO A PASO

### A. Registro y Radicación de Expediente
1. Ingrese a **Mesa de Partes** → **Nuevo Expediente** (`/mesa-partes/expedientes/nuevo`).
2. Seleccione el Tipo de Trámite del catálogo TUPA o trámite general.
3. Ingrese los datos del administrado: DNI/RUC, nombres completos, domicilio procesal en Quinua, teléfono y correo electrónico.
4. Escriba el asunto concreto de la solicitud y el número de folios físicos que componen el documento.
5. Adjunte el archivo digital escaneado en formato PDF (máximo 15 MB). El sistema calculará automáticamente la huella SHA-256 del documento.
6. Haga clic en **Registrar y Radicar**.
7. El sistema generará el código único correlativo (e.g. `EXP-2026-000142`) y mostrará el botón de impresión de constancia.

### B. Emisión de Constancia de Recepción con QR
1. En la pantalla de confirmación, haga clic en **Descargar Constancia Oficial (PDF)**.
2. La constancia incluye:
   * Escudo de la Municipalidad Distrital de Quinua.
   * Número de expediente, fecha y hora exacta con zona horaria de Perú (`America/Lima`).
   * Código de seguridad alfanumérico para seguimiento.
   * Código QR que permite al ciudadano escanear con su teléfono móvil y cotejar la autenticidad directamente en `/verificar-documento`.

### C. Derivación a Oficinas Municipales
1. En la ficha del expediente, ubique la sección **Derivaciones**.
2. Seleccione la Oficina de Destino (e.g. Subgerencia de Desarrollo Urbano, Gerencia de Rentas).
3. Ingrese el proveído de derivación y las indicaciones de atención.
4. Haga clic en **Derivar Expediente**. El expediente pasará al estado `EN_TRAMITE` y se reflejará en la bandeja de entrada del jefe de dicha oficina.

