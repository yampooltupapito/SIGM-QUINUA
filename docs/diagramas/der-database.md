# DIAGRAMA ENTIDAD-RELACIÓN (DER) — BASE DE DATOS POSTGRESQL 17 SIGM-QUINUA

Representación del modelo de datos relacional implementado en PostgreSQL (`sigm_quinua`), gestionado mediante Flyway V1 a V67.

```mermaid
erDiagram
    USUARIOS ||--o{ USUARIOS_ROLES : tiene
    ROLES ||--o{ USUARIOS_ROLES : asignado
    ROLES ||--o{ ROLES_PERMISOS : contiene
    PERMISOS ||--o{ ROLES_PERMISOS : asociado

    PERSONAS ||--o| USUARIOS : pertenece
    OFICINAS ||--o{ MESA_PARTES_EXPEDIENTES : deriva_a
    PERSONAS ||--o{ MESA_PARTES_EXPEDIENTES : solicita

    MESA_PARTES_EXPEDIENTES ||--o{ MESA_PARTES_DERIVACIONES : registra
    MESA_PARTES_EXPEDIENTES ||--o{ MESA_PARTES_REQUISITOS : adjunta

    MERCADOS_MUNICIPALES ||--o{ MERCADOS_SECTORES : divide_en
    MERCADOS_SECTORES ||--o{ MERCADOS_PUESTOS : agrupa
    PERSONAS ||--o{ MERCADOS_COMERCIANTES : empadronado
    MERCADOS_PUESTOS ||--o{ MERCADOS_ASIGNACIONES : adjudica
    MERCADOS_COMERCIANTES ||--o{ MERCADOS_ASIGNACIONES : titular
    MERCADOS_PUESTOS ||--o{ MERCADOS_HISTORIAL_OCUPACION : audita

    SERENAZGO_SECTORES ||--o{ SERENAZGO_INCIDENCIAS : ocurre_en
    SERENAZGO_PERSONAL ||--o{ SERENAZGO_TURNOS : asignado
    SERENAZGO_INCIDENCIAS ||--o{ SERENAZGO_INTERVENCIONES : genera

    PERSONAS ||--o{ TRIBUTARIA_CONTRIBUYENTES : registra
    TRIBUTARIA_CONTRIBUYENTES ||--o{ TRIBUTARIA_PREDIOS : declara
    TRIBUTARIA_CONTRIBUYENTES ||--o{ TRIBUTARIA_OBLIGACIONES : adeuda
    TRIBUTARIA_OBLIGACIONES ||--o{ TRIBUTARIA_PAGOS : cancela
    TRIBUTARIA_CAJAS ||--o{ TRIBUTARIA_PAGOS : recauda
    TRIBUTARIA_CONTRIBUYENTES ||--o{ TRIBUTARIA_FRACCIONAMIENTOS : conviene
```

---

## 1. DESCRIPCIÓN DE ENTIDADES CLAVE

* **`USUARIOS` / `ROLES` / `PERMISOS`:** Núcleo de autenticación y autorización RBAC. Claves primarias numéricas y UUIDs en usuarios.
* **`MESA_PARTES_EXPEDIENTES`:** Almacena el número único de expediente `EXP-YYYY-XXXXXX`, fecha, estado y código de seguridad para seguimiento público.
* **`MERCADOS_PUESTOS` / `MERCADOS_ASIGNACIONES`:** Registra las características físicas de los puestos y los contratos vigentes de los comerciantes con control de unicidad.
* **`SERENAZGO_INCIDENCIAS`:** Registra la tipificación, coordenadas georreferenciadas, estado y partes de ocurrencia en PDF.
* **`TRIBUTARIA_OBLIGACIONES` / `TRIBUTARIA_PAGOS`:** Controla la cuenta corriente, emisión de cuotas de predial y arbitrios, y los comprobantes oficiales emitidos en caja.

