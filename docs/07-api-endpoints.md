# 07. CATÁLOGO DE ENDPOINTS Y CONTROLADORES — SIGM-QUINUA

## 1. CONTROLADORES GLOBALES Y RUTAS PÚBLICAS

| Método | Endpoint | Descripción | Rol Requerido |
| :---: | :--- | :--- | :---: |
| `GET` | `/` | Portal principal de bienvenida a SIGM-QUINUA | Público |
| `GET` | `/login` | Formulario oficial de inicio de sesión institucional | Público |
| `POST` | `/login` | Procesamiento de credenciales de usuario | Público |
| `POST` | `/logout` | Cierre e invalidación de sesión de usuario | Autenticado |
| `GET` | `/verificar-documento` | Portal público de cotejo de documentos PDF con QR | Público |
| `GET` | `/verificar/{codigo}` | Redirección de validación criptográfica | Público |
| `GET` | `/mesa-partes/seguimiento` | Seguimiento ciudadano de trámites con código y clave | Público |

---

## 2. NÚCLEO INSTITUCIONAL Y ADMINISTRACIÓN (`/admin/**`)

| Método | Endpoint | Descripción | Rol Requerido |
| :---: | :--- | :--- | :---: |
| `GET` | `/admin/dashboard` | Dashboard institucional unificado con métricas de PostgreSQL | `ROLE_ADMIN`, `ROLE_SUPERADMIN` |
| `GET` | `/admin/usuarios` | Listado y búsqueda de usuarios del sistema | `ROLE_ADMIN`, `ROLE_SUPERADMIN` |
| `POST` | `/admin/usuarios` | Alta de nuevo usuario institucional | `ROLE_ADMIN`, `ROLE_SUPERADMIN` |
| `GET` | `/admin/oficinas` | Gestión del catálogo de unidades orgánicas de Quinua | `ROLE_ADMIN`, `ROLE_SUPERADMIN` |
| `GET` | `/admin/auditoria` | Bitácora de auditoría administrativa | `ROLE_SUPERADMIN` |

---

## 3. MESA DE PARTES VIRTUAL (`/mesa-partes/**`)

| Método | Endpoint | Descripción | Rol Requerido |
| :---: | :--- | :--- | :---: |
| `GET` | `/mesa-partes/expedientes` | Bandeja general de expedientes radicados | `ROLE_MESA_PARTES`, `ROLE_ADMIN` |
| `GET` | `/mesa-partes/expedientes/nuevo` | Formulario de registro de nuevo expediente | `ROLE_MESA_PARTES` |
| `POST` | `/mesa-partes/expedientes` | Creación atómica de expediente con correlativo anual | `ROLE_MESA_PARTES` |
| `GET` | `/mesa-partes/expedientes/{id}` | Ficha integral y línea de tiempo del expediente | `ROLE_MESA_PARTES`, `ROLE_OFICINA` |
| `POST` | `/mesa-partes/expedientes/{id}/derivar`| Derivación de expediente a otra oficina municipal | `ROLE_MESA_PARTES`, `ROLE_OFICINA` |
| `GET` | `/mesa-partes/expedientes/{id}/constancia`| Descarga de Constancia de Recepción en PDF con QR | `ROLE_MESA_PARTES`, `ROLE_CIUDADANO` |
| `GET` | `/mesa-partes/reportes/excel` | Exportación tabular de expedientes en formato Excel | `ROLE_MESA_PARTES`, `ROLE_ADMIN` |

---

## 4. MERCADOS MUNICIPALES (`/mercados/**`)

| Método | Endpoint | Descripción | Rol Requerido |
| :---: | :--- | :--- | :---: |
| `GET` | `/mercados/dashboard` | Panel de control de mercados y ocupación de puestos | `ROLE_MERCADOS`, `ROLE_ADMIN` |
| `GET` | `/mercados/puestos` | Padrón de puestos comerciales por sector/mercado | `ROLE_MERCADOS` |
| `POST` | `/mercados/puestos` | Alta de nuevo puesto comercial | `ROLE_MERCADOS` |
| `GET` | `/mercados/comerciantes` | Directorio de comerciantes individuales y empresas | `ROLE_MERCADOS` |
| `POST` | `/mercados/asignaciones` | Asignación formal de puesto comercial con contrato | `ROLE_MERCADOS` |
| `POST` | `/mercados/sisa/cobro` | Registro de cobro de boleta de sisa diaria | `ROLE_MERCADOS`, `ROLE_CAJERO` |
| `GET` | `/mercados/reportes/padron-excel` | Exportación de padrón oficial en formato Excel | `ROLE_MERCADOS`, `ROLE_ADMIN` |

---

## 5. SEGURIDAD CIUDADANA Y SERENAZGO (`/serenazgo/**`)

| Método | Endpoint | Descripción | Rol Requerido |
| :---: | :--- | :--- | :---: |
| `GET` | `/serenazgo/dashboard` | Panel de control de incidencias y estado operativo | `ROLE_SERENAZGO_SUPERVISOR`, `ROLE_ADMIN` |
| `GET` | `/serenazgo/incidencias` | Bandeja de incidencias reportadas en el distrito | `ROLE_SERENAZGO_SUPERVISOR`, `ROLE_SERENO` |
| `POST` | `/serenazgo/incidencias` | Registro de nueva incidencia ciudadana o patrullaje | `ROLE_SERENO`, `ROLE_SERENAZGO_SUPERVISOR` |
| `POST` | `/serenazgo/incidencias/{id}/despachar` | Asignación de unidad móvil o patrulla a la zona | `ROLE_SERENAZGO_SUPERVISOR` |
| `GET` | `/serenazgo/incidencias/{id}/parte-pdf` | Emisión oficial del Parte de Ocurrencia en PDF | `ROLE_SERENAZGO_SUPERVISOR`, `ROLE_ADMIN` |
| `GET` | `/serenazgo/estadisticas/excel` | Exportación de balance operativo en Excel | `ROLE_SERENAZGO_SUPERVISOR`, `ROLE_ADMIN` |

---

## 6. RECAUDACIÓN TRIBUTARIA MUNICIPAL (`/tributaria/**`)

| Método | Endpoint | Descripción | Rol Requerido |
| :---: | :--- | :--- | :---: |
| `GET` | `/tributaria/dashboard` | Panel de control de recaudación diaria y cobranza | `ROLE_RENTAS`, `ROLE_ADMIN` |
| `GET` | `/tributaria/contribuyentes` | Padrón de contribuyentes urbanos y rurales | `ROLE_RENTAS` |
| `GET` | `/tributaria/contribuyentes/{id}/cuenta-corriente` | Estado de cuenta y liquidación de deuda | `ROLE_RENTAS`, `ROLE_CAJERO` |
| `POST` | `/tributaria/caja/apertura` | Apertura de sesión de caja municipal con saldo inicial| `ROLE_CAJERO` |
| `POST` | `/tributaria/caja/cobro` | Registro de pago tributario y emisión de recibo | `ROLE_CAJERO` |
| `POST` | `/tributaria/caja/cierre` | Cuadre y arqueo de caja diaria | `ROLE_CAJERO` |
| `GET` | `/tributaria/pagos/{id}/recibo-pdf` | Emisión de Recibo de Pago oficial en PDF con QR | `ROLE_CAJERO`, `ROLE_CONTRIBUYENTE` |
| `POST` | `/tributaria/fraccionamientos` | Aprobación de convenio de fraccionamiento tributario| `ROLE_RENTAS` |
| `GET` | `/tributaria/reportes/recaudacion-excel` | Exportación del libro de ingresos en Excel | `ROLE_RENTAS`, `ROLE_ADMIN` |

