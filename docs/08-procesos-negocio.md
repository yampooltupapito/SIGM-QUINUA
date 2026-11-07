# 08. PROCESOS DE NEGOCIO INSTITUCIONALES — SIGM-QUINUA

## 1. PROCESO DE MESA DE PARTES Y TRÁMITE DOCUMENTARIO

El flujo de atención de trámites documentarios se rige bajo la Ley N° 27444 (Ley del Procedimiento Administrativo General):

```mermaid
sequenceDiagram
    autonumber
    actor C as Ciudadano / Administrado
    participant MP as Mesa de Partes
    participant BD as PostgreSQL (sigm_quinua)
    actor OF as Jefe de Oficina Destino

    C->>MP: Presenta solicitud física o virtual con requisitos
    MP->>MP: Valida requisitos TUPA / formato
    alt Requisitos Incompletos
        MP-->>C: Notifica observación con plazo de 48 horas
    else Requisitos Conformes
        MP->>BD: Inserta Expediente (asigna correlativo EXP-YYYY-NNNNNN)
        MP->>BD: Genera hash SHA-256 de documentos adjuntos
        MP->>C: Emite Constancia de Recepción con código QR
        MP->>BD: Registra primera derivación hacia oficina competente
        BD->>OF: Notifica expediente pendiente en bandeja de oficina
        OF->>OF: Evalúa solicitud, elabora informe / resolución
        OF->>BD: Registra actuación y actualiza estado a ATENDIDO
        OF-->>C: Notifica resolución y proveído final
    end
```

---

## 2. PROCESO DE ASIGNACIÓN DE PUESTOS EN MERCADOS MUNICIPALES

```mermaid
flowchart TD
    Inicio([Inicio de Solicitud de Puesto]) --> Empadronar[Empadronamiento de Comerciante]
    Empadronar --> EvaluarPuesto{¿Puesto está DISPONIBLE y Mercado ACTIVO?}
    EvaluarPuesto -- No --> Rechazo([Rechazo: Puesto Ocupado o en Mantenimiento])
    EvaluarPuesto -- Sí --> CrearSolicitud[Registrar Solicitud en estado BORRADOR]
    CrearSolicitud --> Presentar[Presentar Formalmente: Pasa a SOLICITADA]
    Presentar --> EvaluacionTecnica{¿Cumple requisitos sanitarios y de giro?}
    EvaluacionTecnica -- No --> Denegar[Emitir Resolución Denegatoria]
    EvaluacionTecnica -- Sí --> Aprobar[Aprobar Asignación: Pasa a VIGENTE]
    Aprobar --> ActualizarPuesto[Puesto pasa automáticamente a OCUPADO]
    ActualizarPuesto --> RegistrarHistorial[Registrar HistorialOcupacion: ACTIVACION]
    RegistrarHistorial --> GenerarContrato[Generar Contrato de Concesión / Merced Conductiva]
    GenerarContrato --> Fin([Fin del Proceso de Asignación])
```

---

## 3. PROCESO DE GESTIÓN Y DESPACHO DE INCIDENCIAS DE SERENAZGO

```mermaid
flowchart TD
    Alerta([Recepción de Alerta Telefónica / Presencial]) --> Registrar[Operador registra Incidencia: Tipificación y Ubicación]
    Registrar --> EvaluarSeveridad{Severidad de la Incidencia}
    EvaluarSeveridad -- Leve/Moderada --> DespacharSereno[Despachar Unidad de Serenazgo más cercana]
    EvaluarSeveridad -- Grave / Delito Flagrante --> CoordinarPNP[Despacho Conjunto: Serenazgo + Comisaría PNP Quinua]
    DespacharSereno --> Intervencion[Llegada al lugar e Intervención Operativa]
    CoordinarPNP --> Intervencion
    Intervencion --> ElaborarParte[Redacción de Parte de Ocurrencia]
    ElaborarParte --> ProtegerDatos[Enmascaramiento de Datos Sensibles - Ley 29733]
    ProtegerDatos --> AprobacionSupervisor[Aprobación del Supervisor de Turno]
    AprobacionSupervisor --> Archivar[Cierre de Incidencia y Balance Operativo]
```

---

## 4. PROCESO DE CAJA Y RECAUDACIÓN TRIBUTARIA MUNICIPAL

```mermaid
sequenceDiagram
    autonumber
    actor Cajero as Cajero Municipal
    actor Contribuyente as Contribuyente
    participant Caja as Servicio de Caja y Rentas
    participant BD as PostgreSQL

    Cajero->>Caja: Abrir Caja diaria con Saldo Inicial (efectivo base)
    Caja->>BD: Registra sesión de caja en estado ABIERTA
    Contribuyente->>Cajero: Solicita pago de Impuesto Predial / Arbitrios
    Cajero->>Caja: Consulta cuenta corriente del contribuyente
    Caja->>BD: Obtiene obligaciones pendientes y liquida TIM vigente
    Caja-->>Cajero: Retorna monto total o parcial a cobrar
    Cajero->>Caja: Registra pago en efectivo / voucher
    Caja->>BD: Inicia transacción ACID
    Caja->>BD: Inserta registro en tributaria_pagos
    Caja->>BD: Actualiza saldo de obligaciones pagadas a CANCELADO
    Caja->>BD: Incrementa monto acumulado en sesión de caja
    Caja->>Contribuyente: Entrega Recibo Oficial de Pago con Código QR
    Note over Cajero, Caja: Al finalizar la jornada laboral
    Cajero->>Caja: Realiza arqueo y cuadre de efectivo
    Caja->>BD: Cierra sesión de caja (estado CERRADA) e imprime acta de cuadre
```

