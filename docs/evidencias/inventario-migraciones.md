# INVENTARIO DE MIGRACIONES FLYWAY (V1 A V67) — SIGM-QUINUA

Ubicación: `src/main/resources/db/migration/`  
Motor Destino: PostgreSQL 17  
Esquema: `public`  
Tabla de Control de Versiones: `flyway_schema_history`

| Versión | Nombre del Script SQL | Propósito y Contenido Principal |
| :---: | :--- | :--- |
| **V1** | `V1__init_schema.sql` | Esquema inicial: tablas base del núcleo institucional y tipos ENUM. |
| **V2** | `V2__seed_data.sql` | Datos de semilla institucional: Municipalidad Distrital de Quinua, ubigeo `050108`. |
| **V3** | `V3__personas_oficinas.sql` | Estructura de personas naturales, jurídicas y catálogo de oficinas municipales. |
| **V4** | `V4__roles_permisos.sql` | Semilla de roles iniciales y permisos RBAC por módulo. |
| **V5** | `V5__mesa_partes_core.sql` | Tablas de expedientes, estados, correlativos y requisitos de mesa de partes. |
| **V6** | `V6__mercados_core.sql` | Infraestructura de mercados, sectores, pabellones y puestos comerciales. |
| **V7** | `V7__serenazgo_core.sql` | Personal de serenazgo, cuadrantes territoriales y registro de incidencias. |
| **V8** | `V8__tributaria_core.sql` | Contribuyentes, predios, cuotas de impuesto predial y arbitrios. |
| **V9-V15** | `V9__...` a `V15__...` | Módulos de caja municipal, liquidación de TIM y fraccionamientos tributarios. |
| **V16-V30**| `V16__...` a `V30__...` | Trazabilidad documental, actuaciones, inspecciones sanitarias en mercados y turnos. |
| **V31-V50**| `V31__...` a `V50__...` | Índices de rendimiento, secuencias seguras y compatibilidad de firmas SHA-256. |
| **V51-V65**| `V51__...` a `V65__...` | Unificación de catálogos, tablas de auditoría y enmascaramiento de datos personales. |
| **V66** | `V66__optimizacion_consolidacion_servicios_roles_permisos.sql` | Consolidación oficial de los 4 servicios y reasignación de 151 permisos a Mercados. |
| **V67** | `V67__optimizaciones_indices_rendimiento_finales.sql` | Índices compuestos B-Tree y optimizaciones de consultas agregadas en PostgreSQL 17. |

