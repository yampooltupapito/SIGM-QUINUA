# MANUAL DE GESTIÓN DE MERCADOS MUNICIPALES (`ROLE_MERCADOS`) — SIGM-QUINUA

## 1. PROPÓSITO DEL ROL
Administrar los mercados y ferias municipales del distrito de Quinua, regulando el padrón de comerciantes, la disponibilidad de puestos comerciales, las actas de adjudicación y el cobro de la tasa diaria de sisa y canon mensual.

---

## 2. PROCEDIMIENTOS PASO A PASO

### A. Empadronamiento de Comerciantes
1. Ingrese a **Mercados** → **Comerciantes** (`/mercados/comerciantes`).
2. Haga clic en **Registrar Comerciante**.
3. Ingrese el DNI o RUC. Complete los nombres, dirección en Quinua y giro comercial principal (artesanía de cerámica, verduras, carnes, abarrotes, gastronomía típica).
4. Guarde el registro para generar el código oficial de comerciante (`COM-YYYY-XXXXXX`).

### B. Asignación de Puesto Comercial
1. Ingrese a **Mercados** → **Puestos** (`/mercados/puestos`).
2. Verifique que el puesto se encuentre en estado `DISPONIBLE`.
3. Haga clic en **Asignar Puesto**.
4. Seleccione al comerciante empadronado.
5. Ingrese la fecha de inicio y fecha de vencimiento de la concesión (máximo 12 meses renovables).
6. Adjunte el documento de resolución o acta de posesión.
7. Haga clic en **Aprobar Asignación**.
8. El puesto cambiará automáticamente a estado `OCUPADO` y el sistema registrará de forma inmutable el movimiento en el `HistorialOcupacion` del puesto.

### C. Registro de Cobro de Sisa Diaria
1. Ingrese a **Mercados** → **Cobro de Sisa** (`/mercados/sisa/cobro`).
2. Busque el puesto o comerciante.
3. Ingrese el monto de la tasa diaria conforme al TUPA.
4. Confirme el cobro e imprima la boleta de sisa con numeración correlativa.

