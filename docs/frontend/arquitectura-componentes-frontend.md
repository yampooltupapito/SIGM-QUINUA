# DOCUMENTACIÓN DE ARQUITECTURA Y COMPONENTES FRONTEND — SIGM-QUINUA

**Sistema Integral de Gestión Municipal de la Municipalidad Distrital de Quinua, Huamanga, Ayacucho, Perú**

---

## 1. Arquitectura Frontend

El frontend de **SIGM-QUINUA** está diseñado bajo una arquitectura **Monolítica Modular Orientada a Componentes SSR (Server-Side Rendering)** utilizando:
- **Motor de Plantillas:** Thymeleaf + Ultraq Layout Dialect + Spring Security Extras.
- **Sistema de Estilos:** Tailwind CSS compilado localmente (`tailwind.min.css`) + Sistema de Diseño Institucional Centralizado (`app.css`).
- **Interactividad Cliente:** JavaScript ES6+ nativo modular (`core/admin-ui.js`, módulos específicos de dominio), Chart.js local (`chart.min.js`) y Lucide Icons local (`lucide.min.js`).
- **Cero Dependencias CDN Externas:** Todos los recursos estáticos se sirven localmente desde el empaquetado Spring Boot para garantizar disponibilidad offline/intranet municipal y cumplir con políticas estrictas de Content Security Policy (CSP).

---

## 2. Estructura de Carpetas

```text
src/main/resources/
├── static/
│   ├── css/
│   │   ├── tailwind.min.css          # Utilidades Tailwind compiladas
│   │   ├── app.css                   # Tokens CSS, variables de tema, sidebar, scrollbars y accesibilidad
│   │   └── auth.css                  # Estilos específicos de autenticación
│   ├── images/
│   │   ├── logo-quinua.jpg           # Escudo y logotipo oficial de la Municipalidad Distrital de Quinua
│   │   ├── fondo-obelisco-quinua.jpg # Fondo panorámico del Santuario Histórico de la Pampa de Ayacucho
│   │   ├── obelisco-quinua.jpg       # Fotografía institucional del Obelisco
│   │   ├── quinua-pueblo-encanto.jpg # Reconocimiento oficial MINCETUR Pueblos con Encanto
│   │   ├── plaza-quinua-colonial.jpg # Plaza Principal y arquitectura colonial
│   │   └── quinua-panoramica.jpg     # Vista aérea del distrito de Quinua
│   └── js/
│       ├── core/
│       │   └── admin-ui.js           # Lógica transversal: Sidebar, Topbar Search, Modales, Fetch CSRF, Toast seguro
│       ├── mercados/                 # 25 módulos JS del Servicio 2 (Mercados Municipales)
│       ├── mesapartes/               # 7 módulos JS del Servicio 1 (Mesa de Partes Virtual)
│       ├── auth.js                   # Validaciones e interactividad de login
│       ├── chart.min.js              # Librería local de gráficos estadísticos
│       └── lucide.min.js             # Librería local de iconografía vectorial
│
└── templates/
    ├── admin/
    │   ├── admin-layout.html         # Layout maestro administrativo (modularizado)
    │   ├── dashboard.html            # Dashboard ejecutivo de los 4 servicios municipales
    │   ├── mesa-partes/              # Vistas administrativas del Servicio 1
    │   ├── mercados/                 # Vistas y subcomponentes del Servicio 2
    │   ├── serenazgo/                # Vistas operativas y geográficas del Servicio 3
    │   └── tributaria/               # Vistas recaudatorias y catastrales del Servicio 4
    ├── fragments/
    │   ├── navigation/
    │   │   ├── topbar.html           # Barra superior con buscador global Alt+/ y notificaciones
    │   │   └── sidebar.html          # Menú lateral colapsable con control RBAC de los 4 servicios
    │   ├── components/
    │   │   └── ui-components.html    # Componentes reutilizables (pageHeader, kpiCard, statusBadge, emptyState, alertBanner)
    │   ├── header.html               # Cabecera de verificación técnica
    │   ├── sidebar.html              # Sidebar resumido de verificación técnica
    │   └── footer.html               # Pie de página institucional
    ├── portal/
    │   ├── portal-layout.html        # Layout maestro del Portal Público Ciudadano
    │   ├── index.html                # Portada pública con catálogo de los 4 servicios y galería patrimonial
    │   ├── comerciante/
    │   │   └── layout.html           # Layout del Portal del Comerciante
    │   ├── seguridad/                # Vistas ciudadanas de alertas de seguridad
    │   └── tributario/
    │       └── layout-tributario.html # Layout del Portal Tributario Ciudadano
    ├── auth/                         # Vistas de inicio y recuperación de sesión
    └── errors/                       # Vistas de errores HTTP (400, 403, 404, 500)
```

---

## 3. Layouts del Sistema

1. **`admin/admin-layout.html`**:
   - Layout principal del backoffice municipal.
   - Soporta simultáneamente herencia vía `layout:decorate="~{admin/admin-layout}"` y reemplazo parametrizado vía `th:replace="~{admin/admin-layout :: html(titulo=..., contenido=~{::#main-content})}"` o `content=~{::content}`.
   - Bloques disponibles: `page-css`, `content`, `scripts`, `page-scripts`.
2. **`portal/portal-layout.html`**:
   - Layout público institucional en paleta azul (`#1d4ed8`) y blanco (`#ffffff`), con navegación responsiva y sin desbordamientos.
3. **`portal/comerciante/layout.html`**:
   - Layout especializado para comerciantes empadronados con navegación lateral dedicada.
4. **`portal/tributario/layout-tributario.html`**:
   - Layout especializado para contribuyentes con selector de contexto multi-representación.

---

## 4. Fragmentos Thymeleaf Reutilizables

Ubicados en `templates/fragments/components/ui-components.html`:

### Encabezado de Página (`pageHeader`)
```html
<div th:replace="~{fragments/components/ui-components :: pageHeader('users', 'Gestión de Usuarios', 'Administración de cuentas y perfiles')}"></div>
```

### Tarjeta de Indicador KPI (`kpiCard`)
```html
<div th:replace="~{fragments/components/ui-components :: kpiCard('Contribuyentes Activos', ${totalActivos}, 'Padrón fiscal 2026', 'users', 'blue')}"></div>
```

### Insignia de Estado (`statusBadge`)
```html
<span th:replace="~{fragments/components/ui-components :: statusBadge(${expediente.estadoActual})}"></span>
```

### Estado Vacío (`emptyState`)
```html
<div th:replace="~{fragments/components/ui-components :: emptyState('inbox', 'Sin registros', 'No se encontraron resultados para los filtros aplicados.')}"></div>
```

### Banner de Alerta (`alertBanner`)
```html
<div th:replace="~{fragments/components/ui-components :: alertBanner('success', ${successMessage})}"></div>
```

---

## 5. Módulo JavaScript Core (`static/js/core/admin-ui.js`)

Centraliza la lógica de interfaz del panel administrativo sin contaminar las plantillas con bloques `<script>` repetidos:
- **`abrirModal(id)` / `cerrarModal(id)`**: Abre y cierra diálogos modales gestionando `aria-hidden`, asignando foco automático al primer campo interactivo y devolviendo el foco al botón de origen al cerrar o presionar `Escape`.
- **`fetchApi(url, options)`**: Cliente HTTP asíncrono que lee automáticamente las etiquetas `<meta name="_csrf">` y `<meta name="_csrf_header">`, adjunta la cabecera de seguridad y captura errores mostrando notificaciones comprensibles.
- **`mostrarToast(mensaje, tipo)`**: Renderiza notificaciones flotantes en `#toastContainer` utilizando `document.createElement` y `textContent` (prevención total contra XSS).
- **Buscador Global (`#inputGlobalSearch`, `Alt + /`)**: Filtra en tiempo real los 4 servicios municipales y sub-módulos con navegación por teclado (`Enter` y `Escape`).
- **Prevención de Doble Envío**: Intercepta el evento `submit` en formularios `POST` para bloquear clics repetidos mientras la petición está en curso.

---

## 6. Sistema de Diseño Institucional (`static/css/app.css`)

### Tokens del Panel Administrativo
| Token CSS | Valor Hex | Propósito |
| :--- | :--- | :--- |
| `--bg-main` | `#101827` | Fondo principal del área de trabajo |
| `--bg-sidebar` | `#1e293b` | Fondo del sidebar y topbar |
| `--bg-card` | `#202c3d` | Superficie de tarjetas, tablas y modales |
| `--border-theme` | `#334155` | Bordes de contenedores y divisores |
| `--color-primary` | `#1d4ed8` | Azul primario institucional |
| `--text-theme-light` | `#e2e8f0` | Texto principal de alto contraste |
| `--text-theme-muted` | `#94a3b8` | Texto secundario y etiquetas |

---

## 7. Accesibilidad (WCAG 2.2 AA) y Seguridad

1. **Foco Visible:** Regla global `:focus-visible` con contorno sólido de `2px` (`#3b82f6`) y `outline-offset: 2px`.
2. **Atributos ARIA Dinámicos:** Sincronización de `aria-expanded="true/false"` y `aria-controls` en el botón del sidebar (`#btnToggleSidebar`), menú de notificaciones (`#btnNotificaciones`), menú de perfil (`#btnPerfil`) y menú móvil del portal (`#btnMenuMovil`).
3. **Regiones Live:** `#toastContainer` declarado con `role="region" aria-live="polite"` y cada toast con `role="status"`.
4. **Protección XSS y CSRF:** Uso estricto de `th:text` en Thymeleaf, `textContent` en JavaScript y tokens CSRF en todos los formularios y llamadas `fetchApi`.
5. **Autorización Real:** La visibilidad en el sidebar mediante `sec:authorize` complementa la seguridad de `@PreAuthorize` y `SecurityFilterChain` en el backend.

