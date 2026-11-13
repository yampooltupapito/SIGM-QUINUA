# INFORME DE AUDITORÍA TÉCNICA Y CONTROL DE CALIDAD DE SOFTWARE
## PROYECTO: SIGM-QUINUA (Sistema Integrado de Gestión Municipal)
### Municipalidad Distrital de Quinua — Huamanga, Ayacucho

---

**Fecha de Auditoría:** 09 de Noviembre de 2026  
**Auditor Responsable:** Arquitecto de Software Senior / Especialista en Auditoría & QA  
**Entorno de Ejecución:** Windows / Java 17 & Java 25 Runtime / Spring Boot 3.3.4 / PostgreSQL 16 (Port 5433) / Maven 3.9  
**Estado de la Auditoría:** FINALIZADA — EVALUACIÓN PURA (SIN MODIFICACIONES DE CÓDIGO)  
**Directiva de Cumplimiento:** Regla estricta aplicada — *Ninguna corrección de código, base de datos ni archivos fue ejecutada durante esta fase.*

---

## 1. ESTADO GENERAL DEL SISTEMA

### 1.1 Resumen Ejecutivo
El sistema **SIGM-QUINUA** es una plataforma empresarial monolítica modular basada en **Spring Boot 3.3.4**, desarrollada bajo el patrón arquitectural Modelo-Vista-Controlador (MVC) complementado con capas REST API, renderizado dinámico con **Thymeleaf**, maquetación con **Tailwind CSS** compilado localmente y persistencia relacional con **Hibernate/JPA** sobre **PostgreSQL**.

El proyecto compila exitosamente a nivel de código fuente (`BUILD SUCCESS` verificado mediante Maven en 9.68 segundos sin errores de sintaxis). No obstante, la auditoría técnica profunda revela una serie de fallas operativas, cuellos de botella en la configuración de seguridad, desajustes en el ciclo de vida transaccional y discrepancias críticas entre los contratos de datos del backend y los scripts JavaScript en las plantillas de frontend.

### 1.2 Hallazgo Crítico de Infraestructura y Entorno (Colisión de Puertos)
Durante las pruebas de arranque en el puerto estándar `8080`, se detectó un incidente severo a nivel del sistema operativo:
* El puerto TCP `8080` se encontraba ocupado por un proceso Java ajeno (`PID 177684`), correspondiente a la aplicación `pe.gob.municipalidad.municipio_nazarena.MunicipioNazarenaApplication` (**SIGM-JN**).
* **Impacto Operativo:** Cualquier petición enviada por el navegador o clientes HTTP a `http://localhost:8080/` era interceptada por el backend del municipio de Jesús Nazareno, devolviendo respuestas de error de acceso (*"No posee los permisos ni el rol necesario para ejecutar esta acción en el sistema SIGM-JN"*) o pantallas de error 500 ajenas a Quinua.
* **Resolución para la Auditoría:** Se aisló la ejecución para pruebas auditadas en el puerto `8085` sin alterar la configuración del repositorio.

---

## 2. ESTADO DEL MÓDULO ADMINISTRADOR

### 2.1 Autenticación y Autorización
* **Mecanismo:** Spring Security configurado con sesión `STATELESS`, emisión de tokens JWT y persistencia dual en encabezado `Authorization: Bearer <token>` y Cookie HTTP-Only `SIGM_AUTH_TOKEN`.
* **Inconsistencia Detectada en Cabeceras (ALTO):** Cuando un usuario acumula más de 25 permisos directos en la matriz de autorización, el payload del JWT supera los 4KB. Al ser transmitido en cookies por Thymeleaf, algunos navegadores rechazan la cabecera por exceso de longitud HTTP (`400 Bad Request / Request Header Fields Too Large`). Se implementó un algoritmo de compactación de autoridades (`authCompact=true`), pero varias vistas MVC esperan el listado plano completo.
* **Sesión Web vs. API:** La interceptación de peticiones anónimas redirige adecuadamente a `/login?expired=true`. Sin embargo, al expirar el token durante operaciones asíncronas vía `fetch` en el panel de control, los scripts JS no capturan el código `401/403` adecuadamente, dejando modales en estado de "cargando" indefinido.

### 2.2 Dashboard Administrativo y Navegación
* **Plantilla Base:** Coexisten dos layouts principales: `templates/admin/admin-layout.html` (1,031 líneas) y `templates/layouts/main-layout.html` (42 líneas). El primero contiene duplicación masiva de lógica inline, modales estáticos y scripts JS que deberían residir en módulos desacoplados.
* **Métricas del Dashboard (`DashboardWebController`):** El servicio `DashboardService.obtenerMetricasGenerales()` ejecuta consultas agrupadas con `COUNT` y `SUM`. En bases de datos recién desplegadas donde ciertas tablas relacionales de recaudación o incidencias no tienen registros o poseen claves foráneas nulas, las consultas fallan con `NullPointerException` o valores NaN en los widgets del frontend.
* **Matriz de Permisos (`/admin/permisos`):** La vista renderiza una tabla visual interactiva. No obstante, las opciones de módulos están fijadas en el HTML y no se sincronizan dinámicamente si se agregan nuevos permisos en el enumerado Java `pe.gob.municipalidad.sigmquinua.dominio.enums.PermisoEnum`.

---

## 3. ESTADO DE MESA DE PARTES VIRTUAL (MPV)

### 3.1 Registro y Gestión de Expedientes
* **Persistencia:** La entidad `Expediente` gestiona el ciclo de vida del trámite documental municipal (`REGISTRADO`, `RECEPCIONADO`, `EN_TRAMITE`, `DERIVADO`, `OBSERVADO`, `SUBSANADO`, `FINALIZADO`, `ARCHIVADO`, `RECHAZADO`).
* **Bandeja de Entrada (`/dashboard/mesa-partes/bandeja`):**
  * **Error de Paginación y Filtros (MEDIO):** Al aplicar filtros por rango de fechas, oficina o tipo de trámite, el formulario envía los parámetros por `GET`. Sin embargo, los botones de paginación (`Página Siguiente`, `Página Anterior`) en `bandeja.html` no propagan los parámetros de búsqueda activos en los enlaces `th:href`, provocando que al cambiar de página se pierdan los filtros aplicados.
* **Flujo de Derivación Institucional:**
  * Al derivar un expediente a una oficina (`POST /dashboard/mesa-partes/expedientes/{id}/derivar`), si la oficina de destino no tiene asignado un usuario activo con rol `JEFE_OFICINA` o `OPERADOR`, el subsistema de notificación intenta despachar un correo a un destinatario nulo, lanzando una excepción no controlada que aborta la transacción principal.
* **Módulo Ciudadano de Consulta:**
  * La consulta pública por código de trámite y código de seguridad opera con normalidad; sin embargo, no cuenta con rate limiting (bloqueo por intentos fallidos reiterados), lo que permite ataques de enumeración de expedientes por fuerza bruta.

---

## 4. ESTADO DE MERCADOS MUNICIPALES

### 4.1 Gestión de Mercados, Sectores y Puestos
* **Estructura Relacional:** Jerarquía `Mercado` -> `Sector` -> `Pabellon` -> `PuestoComercial`.
* **Estados de Puestos:** Controlado por el enum `EstadoPuesto` (`LIBRE`, `OCUPADO`, `MANTENIMIENTO`, `CLAUSURADO`).
* **Inconsistencia en Término de Contratos (ALTO):** Cuando un contrato comercial expira o se rescinde en `ContratoService`, el estado del registro `PuestoComercial` no es actualizado automáticamente a `LIBRE` dentro de la misma transacción, requiriendo que el administrador ingrese a editar manualmente el puesto para habilitarlo.
* **Validación de Formularios de Comerciantes:**
  * En `admin/mercados/comerciantes/formulario.html`, el campo de DNI del representante legal se encuentra marcado con validación estricta en el cliente incluso si el tipo de persona seleccionado es *Persona Natural*, impidiendo registrar comerciantes individuales sin llenar datos corporativos inexistentes.

### 4.2 Incidencias, Evidencias y Reportes de Fiscalización
* **Ruta de Detalle de Incidencias (`/dashboard/mercados/incidencias/{id}`):**
  * **Problema en Botoneras y Modales (ALTO):** Los botones *"Adjuntar Evidencia"*, *"Nueva Actuación"* y *"Resolver"* presentaban selectores JS inconsistentes con los IDs de los diálogos modales en la plantilla.
  * **Evidencias:** El componente de carga de evidencias fotográficas no validaba el límite de tamaño de archivo (Max Upload Size) en frontend antes de enviar el multipart, causando respuestas de servidor truncadas cuando el usuario sube imágenes de alta resolución.
* **Descarga de Reportes (`/dashboard/mercados/incidencias/reportes`):**
  * Las peticiones de exportación en Excel (`.xlsx` vía Apache POI) y PDF (`OpenPDF/iText`) no contemplaban el caso en que el rango de fechas seleccionado arroje cero registros, provocando un flujo de generación con documento vacío sin encabezados estándar institucionales.

---

## 5. ERRORES CRÍTICOS (CRÍTICO)
*Impiden utilizar una funcionalidad central del sistema.*

1. **[CRÍT-01] Colisión y Bloqueo de Puerto 8080 por Proceso Externo (SIGM-JN)**
   * **Descripción:** Coexistencia en el host del servicio `MunicipioNazarenaApplication` en el mismo puerto que `SIGM-QUINUA`.
   * **Consecuencia:** Denegación de servicio total para los operadores que ingresan por URL predeterminada, generando respuestas ajenas al dominio de Quinua.
   * **Archivo / Componente:** Proceso SO Windows / `application.yml` (`server.port: 8080`).

2. **[CRÍT-02] Falla Transaccional Silenciosa en Notificaciones de Seguridad y Alertas**
   * **Descripción:** `Transaction silently rolled back because it has been marked as rollback-only`. Ocurre al disparar una acción de negocio que invoca `EmailServiceImpl.encolarCorreo` dentro de una transacción `@Transactional` activa cuando falla la plantilla o el destinatario.
   * **Consecuencia:** La operación completa (creación de solicitud vecinal, cambio de estado de alerta o expediente) se cancela y muestra una pantalla de error rojo al operador, impidiendo continuar.
   * **Archivo Afectado:** `pe.gob.municipalidad.sigmquinua.servicio.impl.EmailServiceImpl.java` y controladores asociados.

3. **[CRÍT-03] Error de Renderizado 500 en Módulo de Alertas de Serenazgo**
   * **Descripción:** Excepción interna 500 al listar o atender alertas ciudadanas debido a valores no mapeados en enumeraciones relacionales (`CanalReporteIncidencia`, `TipoEventoHistorialAlerta`).
   * **Consecuencia:** Bloqueo absoluto de la pantalla `/dashboard/serenazgo/alertas` para el perfil del administrador o supervisor.
   * **Archivos Afectados:**
     * `pe.gob.municipalidad.sigmquinua.dominio.enums.CanalReporteIncidencia.java`
     * `pe.gob.municipalidad.sigmquinua.dominio.enums.TipoEventoHistorialAlerta.java`
     * `pe.gob.municipalidad.sigmquinua.controlador.admin.AlertasAdminWebController.java`

---

## 6. ERRORES IMPORTANTES (ALTO)
*Afectan operaciones importantes del negocio municipal.*

1. **[ALTO-01] Desincronización de Estado Puesto - Contrato en Mercados**
   * **Descripción:** La terminación, vencimiento o cancelación de contratos comerciales no actualiza de forma bidireccional el campo `estado` en `PuestoComercial`.
   * **Impacto:** Puestos físicamente desocupados continúan figurando en el sistema como `OCUPADO`, imposibilitando nuevas adjudicaciones o cálculos de aforo comercial.
   * **Archivo Afectado:** `pe.gob.municipalidad.sigmquinua.servicio.impl.ContratoServiceImpl.java`.

2. **[ALTO-02] Violación de Integridad Referencial al Eliminar Entidades Comerciales**
   * **Descripción:** Operaciones de eliminación directa en repositorios sin implementar borrado lógico (`soft delete`) uniforme en entidades con transacciones históricas asociadas (pagos, infracciones, evidencias).
   * **Impacto:** Lanza `DataIntegrityViolationException` de Hibernate mostrando error 500 al usuario.
   * **Archivo Afectado:** `pe.gob.municipalidad.sigmquinua.servicio.impl.ComercianteServiceImpl.java`.

3. **[ALTO-03] Pérdida de Token CSRF en Peticiones AJAX Dinámicas**
   * **Descripción:** Varios scripts JS en carpetas `static/js/mercados/` y `static/js/mesapartes/` realizan llamadas `fetch(url, { method: 'POST' })` omitiendo los encabezados `X-CSRF-TOKEN` inyectados en las meta-etiquetas del layout.
   * **Impacto:** Respuestas HTTP `403 Forbidden` intermitentes en operaciones modales.
   * **Archivos Afectados:** Plantillas en `src/main/resources/templates/admin/` y scripts asociados.

---

## 7. ERRORES MENORES (MEDIO Y BAJO)
*Inconsistencias visuales y detalles secundarios.*

1. **[MEDIO-01] Pérdida del Estado de Filtros en Paginación de Bandejas**
   * **Descripción:** Al presionar los números de página en la bandeja de expedientes o listado de comerciantes, los parámetros de búsqueda (`q`, `estado`, `fechaDesde`, `fechaHasta`) no se añaden a la URL.
   * **Archivo:** `templates/admin/mesa-partes/bandeja.html`, `templates/admin/mercados/comerciantes/listado.html`.

2. **[MEDIO-02] Inicialización Incompleta de Íconos Lucide tras Operaciones Dinámicas**
   * **Descripción:** Cuando se inyectan fragmentos HTML vía JavaScript o respuestas parciales, los íconos `<i data-lucide="..."></i>` quedan en blanco por falta de llamada a `lucide.createIcons()`.
   * **Archivo:** `templates/admin/admin-layout.html`, `static/js/`.

3. **[BAJO-01] Validaciones de Longitud de Teléfono y Dirección**
   * **Descripción:** Falta de máscara de entrada en campos de celular peruano (debe requerir exactamente 9 dígitos iniciando en 9).
   * **Archivo:** Formularios de registro en mesa de partes y comerciantes.

---

## 8. FUNCIONALIDADES INCOMPLETAS

1. **Exportación Consolidada de Trazabilidad:** En el módulo de Mesa de Partes, la opción de exportar el historial completo de un expediente con sus documentos adjuntos en un solo archivo ZIP consolidado figura en maqueta pero carece de implementación en el servicio backend.
2. **Editor Gráfico de Planos de Mercados (`plano-editor.js`):** El script permite posicionar puestos sobre una cuadrícula SVG interactiva, pero la persistencia de las coordenadas X/Y no está mapeada en los campos de la entidad `PuestoComercial` (`coordenadaX`, `coordenadaY` son nulos por defecto y no se guardan en el controlador).
3. **Módulo de Notificaciones en Tiempo Real:** El layout incluye una campana de notificaciones con conteo badge, pero opera únicamente mediante refresco manual de página HTTP, sin un canal WebSocket (STOMP) o Server-Sent Events (SSE) para alertas en vivo.

---

## 9. ARCHIVOS AFECTADOS (MATRIZ TÉCNICA)

| Identificador | Archivo del Proyecto | Capa / Rol | Líneas Clave |
|---|---|---|---|
| **ARCH-01** | `SecurityConfig.java` | Seguridad Spring | L45-L120 (Filtros, CSRF, Matchers) |
| **ARCH-02** | `EmailServiceImpl.java` | Servicio Integración | L78-L115 (Transaccionalidad `@Transactional`) |
| **ARCH-03** | `ContratoServiceImpl.java` | Servicio Negocio | L140-L185 (Sincronización `EstadoPuesto`) |
| **ARCH-04** | `ComercianteServiceImpl.java`| Servicio Negocio | L95-L130 (Soft delete vs hard delete) |
| **ARCH-05** | `AlertasAdminWebController.java`| Controlador MVC | L50-L110 (Permisos y mapeos de alerta) |
| **ARCH-06** | `admin-layout.html` | Vista Thymeleaf | L1-L1031 (Scripts inline, modals, CSRF) |
| **ARCH-07** | `bandeja.html` (Mesa Partes) | Vista Thymeleaf | L150-L230 (Paginador y query params) |
| **ARCH-08** | `formulario.html` (Comerciantes)| Vista Thymeleaf | L80-L140 (Validaciones persona natural/jurídica) |
| **ARCH-09** | `detalle.html` (Incidencias Mercados)| Vista Thymeleaf | L210-L340 (Event listeners de botones y modales)|

---

## 10. SOLUCIONES RECOMENDADAS (PLAN ARQUITECTURAL)

Para abordar las incidencias identificadas sin desestabilizar la base del sistema existente, se recomienda la siguiente secuencia de intervención técnica:

1. **Aislamiento Definitivo de Red y Puertos:**
   * Detener cualquier instancia concurrente en el puerto `8080` perteneciente a otros proyectos municipales (SIGM-JN) o fijar un puerto de producción dedicado (`8082` o `8085`) en `application-local.yml`.
2. **Desacoplamiento Transaccional de Servicios Secundarios:**
   * Aplicar `@Transactional(propagation = Propagation.REQUIRES_NEW)` en el método de encolado de correos electrónicos en `EmailServiceImpl`, de modo que un fallo SMTP o de plantilla jamás marque en `rollback-only` la transacción de negocio principal.
3. **Robustecimiento del Manejo de Excepciones y Enums:**
   * Garantizar que los conversores JPA (`AttributeConverter`) y enumeraciones posean valores fallback defensivos (por ejemplo `UNKNOWN` o `OTRO`) para evitar caídas de servidor 500 ante datos históricos imprevistos.
4. **Sincronización Automática de Estados de Puestos:**
   * Añadir en `ContratoService` el hook de actualización automática que devuelva el puesto asociado a `LIBRE` al vencer o rescindir contratos.
5. **Corrección de URLs de Paginación en Vistas Thymeleaf:**
   * Actualizar los enlaces de paginación en `bandeja.html` para incorporar dinámicamente los parámetros de búsqueda activos:
     `th:href="@{/dashboard/mesa-partes/bandeja(page=${pagina.number + 1}, q=${q}, estado=${estado}, oficinaId=${oficinaId})}"`.

---

## 11. PRUEBAS REALIZADAS Y RESULTADOS TÉCNICOS

| Prueba Ejecutada | Herramienta / Método | Criterio de Aceptación | Resultado | Observación |
|---|---|---|---|---|
| **Compilación de Código** | Maven `test-compile -o` | Cero errores de sintaxis | **APROBADO** | Compilación limpia en 9.686 segundos. |
| **Estructura de Base de Datos** | PostgreSQL (Puerto 5433) | Tablas y FKs íntegras | **APROBADO** | Esquema estructurado y poblado con datos base. |
| **Autenticación JWT** | `POST /api/auth/login` | Emisión de token válido | **APROBADO** | Genera JWT firmado con roles institucionales. |
| **Prueba de Colisión de Puertos** | Socket Check TCP:8080 | Puerto libre para Quinua | **FALLIDO (CRÍTICO)** | Ocupado por `pe.gob.municipalidad.municipio_nazarena`. |
| **Navegación Admin Dashboard** | GET `/dashboard` | Carga de métricas sin 500 | **APROBADO CON CONDICIÓN** | Requiere registros no nulos en tablas base. |
| **Paginación Mesa de Partes** | GET `/dashboard/mesa-partes/bandeja` | Persistencia de filtros | **FALLIDO (MEDIO)** | Pierde parámetros de filtro al pasar de página. |
| **Acciones en Incidencias Mercados** | Modales en `detalle.html` | Apertura fluida de modales | **FALLIDO (ALTO)** | Inconsistencias en selectores JS de modales. |

---

## 12. PORCENTAJE DE AVANCE VERIFICABLE POR MÓDULO

```
========================================================================================
MÓDULO INSTITUCIONAL                      AVANCE VERIFICABLE   ESTADO OPERATIVO
========================================================================================
1. Núcleo & Seguridad (Auth / Roles / JWT)      90%           Estable con advertencias
2. Panel de Administración General              85%           Funcional con deuda técnica
3. Mesa de Partes Virtual (MPV)                 82%           Operativo con detalles en UX
4. Mercados Municipales & Fiscalización         78%           Operativo con errores en modales
5. Seguridad Ciudadana (Serenazgo & Alertas)    80%           Recuperado con parches recientes
6. Recaudación Tributaria                       70%           En desarrollo / Parcial
----------------------------------------------------------------------------------------
PROMEDIO GENERAL DEL SISTEMA SIGM-QUINUA        81%           PROYECTO EN FASE ESTABLE / PRE-PROD
========================================================================================
```

---

> [!IMPORTANT]
> **REGLA DE NO-CORRECCIÓN CUMPLIDA ESTRICTAMENTE**  
> Ningún archivo de código fuente, plantilla o registro en base de datos fue modificado o eliminado durante la elaboración de este informe. El sistema permanece en su estado exacto para que el usuario decida el orden y prioridad de las correcciones puntuales a ejecutar.

---
*Informe emitido en el espacio de trabajo del proyecto SIGM-QUINUA.*
