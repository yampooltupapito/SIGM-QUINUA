# DIAGRAMAS C4 — MODELO DE ARQUITECTURA DE SOFTWARE SIGM-QUINUA

El modelo C4 permite describir la arquitectura de SIGM-QUINUA en diferentes niveles de abstracción: Contexto del Sistema (Nivel 1), Contenedores (Nivel 2) y Componentes (Nivel 3).

---

## 1. NIVEL 1: DIAGRAMA DE CONTEXTO DEL SISTEMA

Muestra cómo SIGM-QUINUA se relaciona con sus usuarios humanos (ciudadanos y servidores municipales) y con entidades externas reales.

```mermaid
flowchart TD
    subgraph Usuarios ["Usuarios del Sistema"]
        Ciudadano["Ciudadano / Administrado<br/>(Habitante de Quinua)"]
        Comerciante["Comerciante Adjudicatario<br/>(Titular de Puesto de Mercado)"]
        Contribuyente["Contribuyente Distrital<br/>(Sujeto de Obligaciones Tributarias)"]
        PersonalMunicipal["Personal Municipal<br/>(Cajeros, Operadores, Serenos, Administradores)"]
    end

    subgraph Sistema ["Frontera del Sistema Institucional"]
        SIGM["SIGM-QUINUA<br/><b>Sistema Integral de Gestión Municipal</b><br/>Gestiona trámites, mercados, serenazgo y rentas"]
    end

    subgraph Externos ["Sistemas y Autoridades Externas"]
        PNP["Comisaría PNP Quinua<br/>(Apoyo en intervenciones de seguridad)"]
        ReniecSunat["Bases de Datos de Identificación<br/>(Validación de estructura de DNI y RUC)"]
    end

    Ciudadano -->|"Presenta trámites y consulta expedientes con QR"| SIGM
    Comerciante -->|"Consulta récord de pagos y canon de puesto"| SIGM
    Contribuyente -->|"Consulta predios, cuenta corriente y arbitrios"| SIGM
    PersonalMunicipal -->|"Gestiona expedientes, recauda en caja y despacha serenos"| SIGM

    SIGM -.->|"Coordina parte de ocurrencia policial"| PNP
    SIGM -.->|"Verifica sintaxis y algoritmos de control de DNI/RUC"| ReniecSunat
```

---

## 2. NIVEL 2: DIAGRAMA DE CONTENEDORES

Muestra las unidades ejecutables y de almacenamiento que componen la solución.

```mermaid
flowchart TD
    UsuarioFinal["Usuario / Servidor Municipal"]

    subgraph FronteraApp ["Contenedores de SIGM-QUINUA"]
        Nginx["Reverse Proxy Nginx<br/><i>[HTTPS :443]</i><br/>Terminación SSL y enrutamiento"]
        SpringBoot["Aplicación Spring Boot (Fat JAR)<br/><i>[Java 17 / Spring Boot 4.1.1]</i><br/>Monolito modular con MVC, seguridad y servicios"]
        Postgres["Base de Datos Relacional<br/><i>[PostgreSQL 17 :5433]</i><br/>Almacena tablas relacionales, índices y secuencias"]
        Storage["Almacenamiento Local de Archivos<br/><i>[Sistema de Archivos del Servidor]</i><br/>Guarda PDFs de expedientes y sustentos"]
    end

    UsuarioFinal -->|"HTTPS / Peticiones Web"| Nginx
    Nginx -->|"Proxy reverso HTTP :8080"| SpringBoot
    SpringBoot -->|"JDBC / HikariCP Pool (Puerto 5433)"| Postgres
    SpringBoot -->|"Lectura / Escritura de PDFs con hash SHA-256"| Storage
```

---

## 3. NIVEL 3: DIAGRAMA DE COMPONENTES DEL MONOLITO MODULAR

Muestra los principales componentes Java dentro de la aplicación Spring Boot.

```mermaid
flowchart TD
    subgraph SpringBootApp ["Contenedor de la Aplicación Spring Boot"]
        SecurityComp["Security Filter Chain<br/><i>Spring Security 6 + BCrypt</i>"]
        
        subgraph Modulos ["Módulos de los 4 Servicios"]
            AdminComp["Módulo Núcleo y Administración<br/><i>DashboardService, UsuarioService</i>"]
            MesaComp["Módulo Mesa de Partes<br/><i>ExpedienteService, ConstanciaPdfService</i>"]
            MercadosComp["Módulo Mercados Municipales<br/><i>PuestoService, AsignacionService, SisaService</i>"]
            SerenazgoComp["Módulo Serenazgo y Seguridad<br/><i>IncidenciaService, DespachoService, TurnoService</i>"]
            TributarioComp["Módulo Recaudación Tributaria<br/><i>PredialService, ArbitriosService, CajaService</i>"]
        end

        subgraph Infraestructura ["Servicios de Infraestructura y Transversales"]
            AuditService["Servicio de Auditoría Administrativa"]
            DocStorage["DocumentStorageService (IO Seguro)"]
            PdfEngine["Motor Generador PDF (OpenPDF / QR ZXing)"]
            ExcelEngine["Motor Exportador Excel (Apache POI)"]
        end
    end

    SecurityComp --> Modulos
    AdminComp --> AuditService
    MesaComp --> DocStorage
    MesaComp --> PdfEngine
    MercadosComp --> ExcelEngine
    SerenazgoComp --> PdfEngine
    TributarioComp --> PdfEngine
    TributarioComp --> ExcelEngine
```

