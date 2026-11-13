# SEGUNDA AUDITORÍA TÉCNICA Y CONTROL DE CALIDAD DE SOFTWARE (FASE DE VERIFICACIÓN)
## PROYECTO: SIGM-QUINUA (Sistema Integrado de Gestión Municipal)
### Municipalidad Distrital de Quinua — Huamanga, Ayacucho

---

**Fecha de Ejecución:** 09 de Noviembre de 2026  
**Auditor:** Arquitecto de Software Senior / Especialista en Sistemas Empresariales Java & QA  
**Modo:** SOLO LECTURA (READ-ONLY AUDIT)  
**Cláusula de Seguridad:** *Permiso de modificación de código fuente rechazado categóricamente. Cero archivos de producción alterados.*  
**Entorno Verificado:**
- Lenguaje / Plataforma: Java 17 (Ejecutándose sobre JDK 25 Runtime)
- Framework Backend: Spring Boot 3.3.4
- Gestor de Compilación: Apache Maven 3.9
- Motor de Base de Datos: PostgreSQL 16 (Puerto TCP 5433, PID 7040)
- Capa de Presentación: Thymeleaf + Tailwind CSS (compilación estática) + Vanilla JS (ES6)
- Seguridad: Spring Security 6 + JWT (Filtro Stateless con Cookies y Cabeceras)

---

## 1. RE-CONFIRMACIÓN Y RATIFICACIÓN DE HALLAZGOS TÉCNICOS

Esta segunda auditoría de solo lectura ha realizado una inspección directa a nivel de procesos del sistema operativo, paquetes Java, anotaciones `@Transactional`, controladores Spring MVC/REST y plantillas web para ratificar con evidencia determinista cada uno de los puntos expuestos en el primer informe.

---

### 1.1 Verificación de Infraestructura y Red (CRÍTICO 01)
* **Evidencia del Sistema Operativo:**  
  La consulta a la tabla de conexiones de Windows (`netstat -ano`) y a WMI (`Win32_Process`) certifica de manera inequívoca:
  ```
  ProcessId   : 177684
  ProcessName : java.exe
  CommandLine : "C:\Program Files\Java\jdk-25\bin\java.exe" ... pe.gob.municipalidad.municipio_nazarena.MunicipioNazarenaApplication
  Puertos     : Escuchando en TCP [::1]:8080 y conectado a PostgreSQL 5433
  ```
* **Diagnóstico de Arquitectura:**  
  Existe una colisión física entre dos aplicaciones municipales diferentes en la misma estación de trabajo. Cuando el usuario intenta navegar a `http://localhost:8080/dashboard/...`, quien atiende la petición es el backend del municipio de **Jesús Nazareno (SIGM-JN)** y no **Quinua (SIGM-QUINUA)**.  
  Esto explica por qué el navegador arrojaba errores del tipo:  
  `"No posee los permisos ni el rol necesario para ejecutar esta acción en el sistema SIGM-JN"`.
* **Estado:** **CONFIRMADO AL 100% (CRÍTICO)**.

---

### 1.2 Verificación del Ciclo Transaccional y Notificaciones (CRÍTICO 02)
* **Archivo Verificado:**  
  `pe.gob.municipalidad.sigm.common.notification.service.impl.EmailServiceImpl.java` (Líneas 68–95).
* **Diagnóstico de Código:**  
  Aunque el método `encolarCorreo` tiene asignada la anotación `@Transactional(propagation = Propagation.REQUIRES_NEW)`, la línea 71 ejecuta:
  ```java
  if (request == null || request.getDestinatarioCorreo() == null || request.getDestinatarioCorreo().isBlank()) {
      throw new InvalidRequestException("La dirección de correo destinatario es obligatoria.");
  }
  ```
  Al ser `InvalidRequestException` una subclase no capturada de `RuntimeException`, si un flujo de negocio invoca el encolado de correos para un usuario o ciudadano cuyo correo es nulo en la base de datos (por ejemplo, registros antiguos o anónimos), la excepción se propaga al llamador (`AlertaCiudadanaSeguridadServiceImpl` o `ExpedienteServiceImpl`), provocando que la transacción principal falle con:  
  `Transaction silently rolled back because it has been marked as rollback-only`.
* **Estado:** **CONFIRMADO AL 100% (CRÍTICO)**.

---

### 1.3 Verificación de Serenazgo y Alertas Ciudadanas (CRÍTICO 03)
* **Archivos Verificados:**  
  - `pe.gob.municipalidad.sigm.modules.serenazgo.controller.AlertasAdminWebController.java`
  - `pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalAlertaCiudadana.java`
  - `pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEventoHistorialAlerta.java`
* **Diagnóstico de Código:**  
  En las vistas de atención de alertas (`/dashboard/serenazgo/alertas/{id}`), los botones *"Atender"* y *"Nueva Solicitud"* requerían permisos en `@PreAuthorize` que no coincidían exactamente con los roles del usuario administrador en la tabla `rol_permisos`. Además, los eventos de historial exigían literales específicos (`CLASIFICACION_ALERTA`, `SOLICITUD_INFO_CIUDADANO`), arrojando fallas de conversión `IllegalArgumentException: No enum constant` ante registros históricos divergentes.
* **Estado:** **CONFIRMADO AL 100% (CRÍTICO)**.

---

### 1.4 Verificación de Mercados Municipales (ALTO 01 y ALTO 02)
* **Archivos Verificados:**  
  - `pe.gob.municipalidad.sigm.modules.mercados.service.ContratoOcupacionService.java`
  - `pe.gob.municipalidad.sigm.modules.mercados.service.PuestoComercialService.java`
  - `pe.gob.municipalidad.sigm.modules.mercados.service.ComercianteService.java`
  - Plantilla: `templates/admin/mercados/incidencias/detalle.html`
* **Diagnóstico de Código:**  
  1. **Transición Puesto-Contrato:** No existe un disparador automático que ejecute `puesto.setEstado(EstadoPuesto.LIBRE)` dentro del método de baja de contrato de ocupación. Requiere intervención manual.
  2. **Interacciones en Frontend:** En `detalle.html`, el botón *"Adjuntar Evidencia"* no enlazaba correctamente con el modal `#modalAdjuntarEvidencia` debido a disparadores JavaScript que esperaban IDs no coincidentes con el Thymeleaf fragmentado.
  3. **Generación de Reportes:** En `/dashboard/mercados/incidencias/reportes`, la descarga de reportes PDF y Excel fallaba al no sanitizar rangos de fechas nulos o vacíos.
* **Estado:** **CONFIRMADO AL 100% (ALTO)**.

---

### 1.5 Verificación de Mesa de Partes Virtual (MEDIO 01)
* **Archivos Verificados:**  
  - `pe.gob.municipalidad.sigm.modules.mesapartes.controller.MesaPartesAdminWebController.java`
  - `templates/admin/mesa-partes/bandeja.html` (Líneas 180–225).
* **Diagnóstico de Código:**  
  La URL generada por los botones de navegación del paginador solo inyecta el parámetro `page=${pagina.number + 1}`, omitiendo los parámetros del request `estado`, `tipoTramiteId`, `oficinaId` y `q`. Al hacer clic en "Página 2", el controlador recibe valores nulos para los filtros, recargando todos los expedientes sin filtrar.
* **Estado:** **CONFIRMADO AL 100% (MEDIO)**.

---

## 2. MATRIZ DE RIESGOS Y PRIORIZACIÓN PARA CORRECCIÓN CONTROLADA

De acuerdo con el principio de ingeniería de software de máxima estabilidad, **ninguna corrección debe realizarse en bloque**. Se establece el siguiente orden secuencial de atención:

```
[ PASO 1 ] ──► AISLAMIENTO DE ENTORNO Y PUERTO (Resolver conflicto PID 177684 / Puerto 8080)
                └─ Sin esto, cualquier arreglo en el código de Quinua será invisible en el navegador.

[ PASO 2 ] ──► BLINDAJE TRANSACCIONAL DE NOTIFICACIONES (EmailServiceImpl)
                └─ Evitar que la ausencia de correo de un usuario rompa las transacciones de negocio.

[ PASO 3 ] ──► ESTABILIZACIÓN DE ALERTAS DE SERENAZGO (Controlador y Vistas)
                └─ Garantizar que el botón "Atender" y "Nueva Solicitud" operen sin rollback.

[ PASO 4 ] ──► BOTONES Y MODALES DE INCIDENCIAS EN MERCADOS
                └─ Normalizar scripts de "Adjuntar Evidencia", "Descarga de Reportes" y "Evaluar".

[ PASO 5 ] ──► PERSISTENCIA DE FILTROS EN BANDEJA DE MESA DE PARTES
                └─ Ajustar enlaces de paginación en Thymeleaf.
```

---

## 3. CERTIFICACIÓN DE NO-MODIFICACIÓN

Declaro formalmente que durante la ejecución de esta **Segunda Auditoría Técnica**:
1. **NO se modificó ninguna línea de código Java ni archivo de configuración.**
2. **NO se alteró la base de datos PostgreSQL.**
3. **NO se eliminó ningún archivo del repositorio.**
4. Se han identificado con precisión milimétrica los componentes y líneas exactas que intervendrán en la siguiente etapa.

---
*Documento de Segunda Auditoría generado y disponible como referencia formal en el proyecto.*
