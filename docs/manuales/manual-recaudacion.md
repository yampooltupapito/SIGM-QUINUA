# MANUAL DE RECAUDACIÓN TRIBUTARIA Y CAJA (`ROLE_CAJERO`, `ROLE_RENTAS`) — SIGM-QUINUA

## 1. PROPÓSITO DEL ROL
Gestionar la cuenta corriente de los contribuyentes de Quinua, determinar obligaciones de Impuesto Predial y Arbitrios Municipales, liquidar intereses moratorios, celebrar convenios de fraccionamiento y realizar el cobro diario y arqueo en ventanilla de caja municipal.

---

## 2. PROCEDIMIENTOS PASO A PASO

### A. Apertura de Caja Diaria (Obligatoria para Cobrar)
1. Al iniciar la jornada laboral, el cajero accede a **Caja Municipal** → **Apertura de Caja** (`/tributaria/caja/apertura`).
2. Digite el monto de efectivo base recibido para dar vuelto (e.g. S/ 100.00).
3. Haga clic en **Aperturar Caja**.
4. La sesión de caja quedará en estado `ABIERTA` vinculada al usuario cajero autenticado.

### B. Cobro de Tributos y Emisión de Recibo Oficial
1. Ingrese a **Caja** → **Ventanilla de Cobro** (`/tributaria/caja/cobro`).
2. Busque al contribuyente por DNI o código tributario.
3. El sistema listará las cuotas pendientes de Impuesto Predial y Arbitrios, calculando automáticamente el reajuste y la Tasa de Interés Moratorio (TIM) devengada a la fecha actual.
4. Seleccione las cuotas que el contribuyente desea cancelar (total o pago a cuenta).
5. Seleccione el medio de pago (Efectivo o Transferencia bancaria verificada).
6. Haga clic en **Procesar Cobro**.
7. El sistema registrará el pago atómicamente y generará el **Recibo de Pago Tributario en PDF** con código QR oficial. Entregue el comprobante al contribuyente.

### C. Convenios de Fraccionamiento Tributario
1. Para contribuyentes con deudas acumuladas de años anteriores, acceda a **Rentas** → **Fraccionamientos** (`/tributaria/fraccionamientos`).
2. Seleccione las deudas a incluir en el convenio.
3. Defina el número de cuotas mensuales (hasta 24 cuotas según reglamento distrital) y la cuota inicial mínima del 20%.
4. Genere la simulación financiera y apruebe el convenio con el número de resolución de alcaldía correspondiente.
5. Imprima el Convenio de Fraccionamiento oficial con el cronograma detallado de vencimientos.

### D. Arqueo y Cierre Diario de Caja
1. Al finalizar la atención al público, diríjase a **Caja** → **Cierre de Caja** (`/tributaria/caja/cierre`).
2. El sistema mostrará el total recaudado según el sistema informático desglosado por concepto.
3. Ingrese el monto de efectivo físico recontado en ventanilla.
4. Si existe diferencia, registre la justificación en observaciones.
5. Haga clic en **Cerrar y Cuadrar Caja**.
6. Imprima el **Acta de Arqueo y Cuadre de Caja** para entrega a la Subgerencia de Tesorería.

