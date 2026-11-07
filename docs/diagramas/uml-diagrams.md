# DIAGRAMAS UML — MODELADO ORIENTADO A OBJETOS SIGM-QUINUA

## 1. DIAGRAMA DE CASOS DE USO GENERAL

```mermaid
flowchart LR
    subgraph Actores ["Actores"]
        Admin((Administrador))
        Operador((Operador Mesa Partes))
        Cajero((Cajero Municipal))
        Sereno((Personal Serenazgo))
        Ciudadano((Ciudadano))
    end

    subgraph CasosUso ["Casos de Uso Principales"]
        UC01["UC01: Iniciar Sesión / Recuperar Acceso"]
        UC02["UC02: Consultar Dashboard Institucional"]
        UC03["UC03: Radicar Expediente con Código QR"]
        UC04["UC04: Derivar y Atender Expediente"]
        UC05["UC05: Asignar Puesto Comercial"]
        UC06["UC06: Registrar Incidencia y Despachar Móvil"]
        UC07["UC07: Aperturar / Cerrar Caja Municipal"]
        UC08["UC08: Cobrar Tributo y Emitir Recibo"]
        UC09["UC09: Consultar Trámite Ciudadano"]
    end

    Admin --> UC01
    Admin --> UC02
    Admin --> UC05

    Operador --> UC01
    Operador --> UC03
    Operador --> UC04

    Cajero --> UC01
    Cajero --> UC07
    Cajero --> UC08

    Sereno --> UC01
    Sereno --> UC06

    Ciudadano --> UC09
```

---

## 2. DIAGRAMA DE CLASES DEL DOMINIO PRINCIPAL

```mermaid
classDiagram
    class Expediente {
        +UUID id
        +String codigoSeguimiento
        +LocalDate fechaPresentacion
        +EstadoExpediente estado
        +String asunto
        +Persona administrado
        +Oficina oficinaActual
    }

    class PuestoComercial {
        +UUID id
        +String codigo
        +String numero
        +BigDecimal areaMetrosCuadrados
        +EstadoPuesto estado
        +MercadoMunicipal mercado
        +SectorMercado sector
    }

    class AsignacionPuesto {
        +UUID id
        +String codigoAsignacion
        +LocalDate fechaInicio
        +LocalDate fechaFin
        +EstadoAsignacion estado
        +PuestoComercial puesto
        +Comerciante comerciante
    }

    class IncidenciaSeguridadCiudadana {
        +UUID id
        +String codigoIncidencia
        +OffsetDateTime fechaHora
        +TipoIncidencia tipo
        +SeveridadIncidencia severidad
        +EstadoIncidencia estado
        +SectorPatrullaje sector
    }

    class PagoTributario {
        +UUID id
        +String numeroRecibo
        +LocalDate fechaPago
        +BigDecimal montoTotal
        +EstadoPago estado
        +Contribuyente contribuyente
        +CajaMunicipal caja
    }

    PuestoComercial "1" <-- "0..*" AsignacionPuesto : adjudicado_en
    Expediente "1" --> "0..*" DerivacionExpediente : contiene
```

---

## 3. DIAGRAMA DE SECUENCIA: REGISTRO DE PAGO EN CAJA

```mermaid
sequenceDiagram
    autonumber
    actor Cajero as Cajero Municipal
    participant Web as CajaWebController
    participant Service as CajaService
    participant BD as PagoRepository (JPA)

    Cajero->>Web: Envía formulario de cobro (ID Obligación, Monto, Medio de Pago)
    Web->>Service: registrarPago(PagoRequest)
    Service->>Service: Validar que la caja esté ABIERTA
    Service->>Service: Validar que la deuda no esté cancelada
    Service->>BD: Guardar PagoTributario (Genera número de recibo)
    Service->>BD: Actualizar saldo de la obligación a CANCELADO
    Service->>Service: Generar PDF con huella SHA-256 y Código QR
    Service-->>Web: Retorna PagoResponse con ID y URL de recibo
    Web-->>Cajero: Muestra pantalla de éxito y descarga de recibo oficial
```

