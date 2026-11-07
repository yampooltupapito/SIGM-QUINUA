# PROCESOS DE NEGOCIO BPMN — SIGM-QUINUA

Modelado de los procesos institucionales conforme al estándar BPMN 2.0 (Business Process Model and Notation).

---

## 1. PROCESO BPMN: RADICACIÓN Y DERIVACIÓN EN MESA DE PARTES

```mermaid
flowchart TD
    Inicio([● Inicio: Solicitud Recibida]) --> T1[Tarea: Validar Requisitos TUPA y Formatos]
    T1 --> G1{¿Requisitos Completos?}
    
    G1 -- No --> T2[Tarea: Registrar Observación y Notificar al Ciudadano]
    T2 --> EventoEspera[(⏱ Espera: Plazo de Subsanación 48 hrs)]
    EventoEspera --> G2{¿Subsanó a Tiempo?}
    G2 -- No --> FinRechazo([■ Fin: Trámite Declarado en Abandono])
    G2 -- Sí --> T1

    G1 -- Sí --> T3[Tarea: Generar Correlativo EXP-YYYY-NNNNNN]
    T3 --> T4[Tarea: Emitir Constancia Digital con Código QR y SHA-256]
    T4 --> T5[Tarea: Derivar a Oficina Competente]
    T5 --> T6[Tarea: Evaluación Técnica y Emisión de Proveído/Resolución]
    T6 --> FinExito([■ Fin: Expediente Atendido y Notificado])
```

---

## 2. PROCESO BPMN: APERTURA, COBRO Y ARQUEO DIARIO DE CAJA

```mermaid
flowchart TD
    InicioCaja([● Inicio de Jornada]) --> T_Apertura[Tarea: Registrar Apertura de Caja con Monto Inicial]
    T_Apertura --> EsperaCobros[Ciclo de Atención: Cobro a Contribuyentes]
    
    EsperaCobros --> T_Consulta[Tarea: Consultar Estado de Deuda y TIM]
    T_Consulta --> T_Cobro[Tarea: Registrar Pago y Emitir Recibo Oficial]
    T_Cobro --> G_Continuar{¿Continúa Jornada Laboral?}
    
    G_Continuar -- Sí --> EsperaCobros
    G_Continuar -- No --> T_Arqueo[Tarea: Conteo Físico de Efectivo y Cuadre de Sistema]
    
    T_Arqueo --> G_Cuadre{¿Efectivo coincide con Sistema?}
    G_Cuadre -- Diferencia --> T_Obs[Tarea: Registrar Acta de Observación y Descuadre]
    G_Cuadre -- Conforme --> T_Cierre[Tarea: Cerrar Sesión de Caja e Imprimir Resumen Diario]
    T_Obs --> T_Cierre
    T_Cierre --> FinCaja([■ Fin de Sesión Diaria])
```

