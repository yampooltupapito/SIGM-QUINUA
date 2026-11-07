# MANUAL DEL PORTAL CIUDADANO — SIGM-QUINUA

## 1. ACCESO PÚBLICO Y SERVICIOS CIUDADANOS
El Portal Ciudadano de SIGM-QUINUA permite a los vecinos y administrados del distrito de Quinua realizar trámites y consultas de manera remota sin necesidad de acudir físicamente al Palacio Municipal.

---

## 2. PROCEDIMIENTOS CIUDADANOS

### A. Seguimiento de Trámites en Línea
1. Ingrese a: `http://localhost:8080/mesa-partes/seguimiento`.
2. Ingrese el **Código de Seguimiento** de su expediente (e.g. `EXP-2026-000142`) y su **Clave de Verificación** proporcionada en su constancia de recepción.
3. El sistema mostrará la línea de tiempo de su trámite:
   * Fecha de radicación.
   * Oficina donde se encuentra el expediente actualmente.
   * Estado del trámite (`REGISTRADO`, `EN_TRAMITE`, `OBSERVADO`, `ATENDIDO`).
   * Proveídos públicos y resoluciones emitidas.

### B. Cotejo de Documentos Oficiales mediante Código QR
1. Cualquier documento oficial emitido por la Municipalidad de Quinua (Constancias, Recibos, Partes) incluye un código QR en la parte inferior.
2. Escanee el código QR con la cámara de su teléfono celular o ingrese el código alfanumérico en `http://localhost:8080/verificar-documento`.
3. El portal de cotejo confirmará la autenticidad del documento, mostrando el nombre del titular enmascarado (para proteger su privacidad) y la fecha de expedición, garantizando que el documento no ha sido alterado.

