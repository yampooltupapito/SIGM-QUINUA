/**
 * ============================================================================
 * SIGM-QUINUA — MÓDULO CORE DE INTERFAZ ADMINISTRATIVA Y COMPONENTES UI
 * Municipalidad Distrital de Quinua, Huamanga, Ayacucho, Perú
 * ============================================================================
 * Responsabilidades:
 * 1. Inicialización segura de iconografía Lucide.
 * 2. Gestión accesible del Sidebar colapsable (Desktop y Mobile) con persistencia.
 * 3. Control de menús desplegables (Notificaciones y Perfil) con atributos ARIA.
 * 4. Buscador global interactivo de módulos y servicios (Atajo Alt + /).
 * 5. Gestión accesible de Modales (foco inicial, retorno de foco, tecla Escape).
 * 6. Cliente HTTP con protección CSRF automática y control de errores.
 * 7. Sistema de notificaciones Toast seguro contra XSS (nodos DOM puros).
 * 8. Prevención global de doble envío en formularios transaccionales.
 * ============================================================================
 */

(function () {
    "use strict";

    let ultimoElementoEnfocado = null;

    // Catálogo de rutas de acceso rápido para el buscador global (4 Servicios Oficiales + Núcleo)
    const CATALOGO_NAVEGACION_RAPIDA = [
        { titulo: "Panel Principal (Dashboard)", categoria: "Núcleo", url: "/admin/dashboard", icono: "layout-dashboard" },
        { titulo: "Usuarios y Cuentas", categoria: "Administración", url: "/admin/usuarios", icono: "users" },
        { titulo: "Roles Institucionales", categoria: "Administración", url: "/admin/roles", icono: "shield-alert" },
        { titulo: "Matriz de Permisos", categoria: "Administración", url: "/admin/permisos", icono: "grid" },
        { titulo: "Oficinas Municipales", categoria: "Administración", url: "/admin/oficinas", icono: "building" },
        { titulo: "Auditoría Integral", categoria: "Administración", url: "/admin/auditoria", icono: "history" },
        { titulo: "Gestión Documental", categoria: "Núcleo", url: "/dashboard/documentos", icono: "folder-archive" },
        { titulo: "Bandeja de Mesa de Partes", categoria: "1. Mesa de Partes", url: "/dashboard/mesa-partes/bandeja", icono: "inbox" },
        { titulo: "Derivaciones de Expedientes", categoria: "1. Mesa de Partes", url: "/dashboard/mesa-partes/derivaciones", icono: "send" },
        { titulo: "Atención de Expedientes", categoria: "1. Mesa de Partes", url: "/dashboard/mesa-partes/atencion", icono: "file-check" },
        { titulo: "Reportes de Mesa de Partes", categoria: "1. Mesa de Partes", url: "/dashboard/mesa-partes/reportes", icono: "bar-chart-2" },
        { titulo: "Configuración TUPA y Trámites", categoria: "1. Mesa de Partes", url: "/dashboard/mesa-partes/configuracion", icono: "settings" },
        { titulo: "Dashboard de Mercados", categoria: "2. Mercados Municipales", url: "/dashboard/mercados", icono: "shopping-bag" },
        { titulo: "Padrón de Comerciantes", categoria: "2. Mercados Municipales", url: "/dashboard/mercados/comerciantes", icono: "users" },
        { titulo: "Asignaciones y Ocupación", categoria: "2. Mercados Municipales", url: "/dashboard/mercados/asignaciones", icono: "key" },
        { titulo: "Contratos de Ocupación", categoria: "2. Mercados Municipales", url: "/dashboard/mercados/contratos", icono: "file-text" },
        { titulo: "Finanzas y Cobranzas de Mercados", categoria: "2. Mercados Municipales", url: "/dashboard/mercados/finanzas", icono: "dollar-sign" },
        { titulo: "Planos Interactivos de Mercados", categoria: "2. Mercados Municipales", url: "/dashboard/mercados/planos", icono: "map" },
        { titulo: "Panel de Seguridad y Serenazgo", categoria: "3. Seguridad Ciudadana", url: "/dashboard/serenazgo", icono: "shield" },
        { titulo: "Incidencias de Seguridad", categoria: "3. Seguridad Ciudadana", url: "/dashboard/serenazgo/incidencias", icono: "alert-triangle" },
        { titulo: "Alertas Ciudadanas", categoria: "3. Seguridad Ciudadana", url: "/dashboard/serenazgo/alertas", icono: "bell-ring" },
        { titulo: "Personal de Serenazgo", categoria: "3. Seguridad Ciudadana", url: "/dashboard/serenazgo/personal", icono: "user-check" },
        { titulo: "Unidades Móviles y Patrullaje", categoria: "3. Seguridad Ciudadana", url: "/dashboard/serenazgo/patrullajes", icono: "truck" },
        { titulo: "Mapa Geográfico de Serenazgo", categoria: "3. Seguridad Ciudadana", url: "/dashboard/serenazgo/mapa", icono: "map-pin" },
        { titulo: "Panel de Recaudación Tributaria", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria", icono: "receipt" },
        { titulo: "Padrón de Contribuyentes", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/contribuyentes", icono: "users" },
        { titulo: "Predios Urbanos y Rurales", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/predios", icono: "home" },
        { titulo: "Impuesto Predial", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/impuesto-predial", icono: "landmark" },
        { titulo: "Arbitrios Municipales", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/arbitrios", icono: "trees" },
        { titulo: "Obligaciones y Cuentas Corrientes", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/obligaciones", icono: "file-spreadsheet" },
        { titulo: "Caja Municipal y Pagos", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/cajas", icono: "credit-card" },
        { titulo: "Gestión de Deudas", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/deudas", icono: "wallet" },
        { titulo: "Fraccionamientos y Convenios", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/fraccionamientos", icono: "calendar" },
        { titulo: "Reportes Tributarios", categoria: "4. Recaudación Tributaria", url: "/dashboard/tributaria/reportes", icono: "pie-chart" }
    ];

    /**
     * Inicializa los iconos Lucide en el contenedor indicado o documento,
     * evitando re-renderizar iconos SVG ya procesados o entrar en bucles.
     */
    let isRenderingIcons = false;
    function initLucideIcons() {
        if (isRenderingIcons) return;
        if (!window.lucide || typeof window.lucide.createIcons !== "function") return;
        try {
            isRenderingIcons = true;
            // Desmarcar SVGs ya renderizados para que createIcons solo procese nodos nuevos
            document.querySelectorAll("svg[data-lucide]").forEach(function (svg) {
                svg.setAttribute("data-lucide-icon", svg.getAttribute("data-lucide"));
                svg.removeAttribute("data-lucide");
            });
            if (document.querySelector("[data-lucide]")) {
                window.lucide.createIcons();
                document.querySelectorAll("svg[data-lucide]").forEach(function (svg) {
                    svg.setAttribute("data-lucide-icon", svg.getAttribute("data-lucide"));
                    svg.removeAttribute("data-lucide");
                });
            }
        } finally {
            isRenderingIcons = false;
        }
    }

    /**
     * Configura la apertura, cierre y persistencia del Sidebar administrativo.
     */
    function initSidebar() {
        const btnToggle = document.getElementById("btnToggleSidebar");
        const sidebar = document.getElementById("mainSidebar");
        const backdrop = document.getElementById("sidebarBackdrop");

        if (!btnToggle || !sidebar) return;

        // Restaurar estado colapsado y posición de scroll en escritorio
        try {
            const savedCollapsed = localStorage.getItem("sigm_sidebar_collapsed");
            if (savedCollapsed === "true" && window.innerWidth >= 1024) {
                sidebar.classList.remove("sidebar-expanded");
                sidebar.classList.add("sidebar-collapsed");
                btnToggle.setAttribute("aria-expanded", "false");
            }
            const savedScroll = sessionStorage.getItem("sigm_sidebar_scroll");
            if (savedScroll !== null) {
                sidebar.scrollTop = parseInt(savedScroll, 10) || 0;
            }
            sidebar.addEventListener("scroll", function () {
                try {
                    sessionStorage.setItem("sigm_sidebar_scroll", String(sidebar.scrollTop));
                } catch (_) {}
            }, { passive: true });
        } catch (_) {
            // Ignorar si localStorage/sessionStorage está restringido
        }

        btnToggle.addEventListener("click", function () {
            const isMobile = window.innerWidth < 1024;
            if (isMobile) {
                const isHidden = sidebar.classList.contains("-translate-x-full");
                if (isHidden) {
                    sidebar.classList.remove("-translate-x-full");
                    if (backdrop) backdrop.classList.remove("hidden");
                    btnToggle.setAttribute("aria-expanded", "true");
                } else {
                    sidebar.classList.add("-translate-x-full");
                    if (backdrop) backdrop.classList.add("hidden");
                    btnToggle.setAttribute("aria-expanded", "false");
                }
            } else {
                const isExpanded = sidebar.classList.contains("sidebar-expanded");
                if (isExpanded) {
                    sidebar.classList.remove("sidebar-expanded");
                    sidebar.classList.add("sidebar-collapsed");
                    btnToggle.setAttribute("aria-expanded", "false");
                    try { localStorage.setItem("sigm_sidebar_collapsed", "true"); } catch (_) {}
                } else {
                    sidebar.classList.remove("sidebar-collapsed");
                    sidebar.classList.add("sidebar-expanded");
                    btnToggle.setAttribute("aria-expanded", "true");
                    try { localStorage.setItem("sigm_sidebar_collapsed", "false"); } catch (_) {}
                }
            }
        });

        if (backdrop) {
            backdrop.addEventListener("click", function () {
                sidebar.classList.add("-translate-x-full");
                backdrop.classList.add("hidden");
                btnToggle.setAttribute("aria-expanded", "false");
            });
        }
    }

    /**
     * Configura los dropdowns de Notificaciones y Perfil con soporte ARIA.
     */
    function initDropdowns() {
        const btnNotif = document.getElementById("btnNotificaciones");
        const dropNotif = document.getElementById("dropdownNotificaciones");
        const btnPerfil = document.getElementById("btnPerfil");
        const dropPerfil = document.getElementById("dropdownPerfil");

        function cerrarDropdown(btn, drop) {
            if (drop) drop.classList.add("hidden");
            if (btn) btn.setAttribute("aria-expanded", "false");
        }

        function alternarDropdown(btnActivo, dropActivo, btnOtro, dropOtro) {
            if (!btnActivo || !dropActivo) return;
            const estabaOculto = dropActivo.classList.contains("hidden");
            cerrarDropdown(btnOtro, dropOtro);
            if (estabaOculto) {
                dropActivo.classList.remove("hidden");
                btnActivo.setAttribute("aria-expanded", "true");
            } else {
                cerrarDropdown(btnActivo, dropActivo);
            }
        }

        if (btnNotif && dropNotif) {
            btnNotif.addEventListener("click", function (e) {
                e.stopPropagation();
                alternarDropdown(btnNotif, dropNotif, btnPerfil, dropPerfil);
            });
        }

        if (btnPerfil && dropPerfil) {
            btnPerfil.addEventListener("click", function (e) {
                e.stopPropagation();
                alternarDropdown(btnPerfil, dropPerfil, btnNotif, dropNotif);
            });
        }

        document.addEventListener("click", function (e) {
            if (dropNotif && !dropNotif.contains(e.target) && e.target !== btnNotif) {
                cerrarDropdown(btnNotif, dropNotif);
            }
            if (dropPerfil && !dropPerfil.contains(e.target) && e.target !== btnPerfil) {
                cerrarDropdown(btnPerfil, dropPerfil);
            }
        });
    }

    /**
     * Inicializa el buscador contextual de módulos en el Topbar (#inputGlobalSearch).
     */
    function initGlobalSearch() {
        const searchInput = document.getElementById("inputGlobalSearch");
        if (!searchInput) return;

        const parentWrapper = searchInput.parentElement;
        if (!parentWrapper) return;

        let resultsBox = document.getElementById("globalSearchResults");
        if (!resultsBox) {
            resultsBox = document.createElement("div");
            resultsBox.id = "globalSearchResults";
            resultsBox.setAttribute("role", "listbox");
            resultsBox.className = "hidden absolute left-0 right-0 mt-1.5 bg-card border border-theme rounded-xl shadow-2xl max-h-80 overflow-y-auto z-50 divide-y divide-slate-700/40";
            parentWrapper.appendChild(resultsBox);
        }

        function renderResults(term) {
            resultsBox.textContent = "";
            const cleanTerm = term.trim().toLowerCase();
            if (!cleanTerm) {
                resultsBox.classList.add("hidden");
                return;
            }

            const sidebarLinks = Array.from(document.querySelectorAll("#mainSidebar a.nav-item[href]"));
            const allowedPaths = new Set(sidebarLinks.map(function (a) { return a.getAttribute("href"); }));

            const matches = CATALOGO_NAVEGACION_RAPIDA.filter(function (item) {
                const isAllowed = allowedPaths.size === 0 || allowedPaths.has(item.url) ||
                    Array.from(allowedPaths).some(function (p) { return p && item.url.startsWith(p); });
                return isAllowed && (item.titulo.toLowerCase().includes(cleanTerm) ||
                       item.categoria.toLowerCase().includes(cleanTerm));
            }).slice(0, 8);

            if (matches.length === 0) {
                const empty = document.createElement("div");
                empty.className = "px-4 py-3 text-xs text-slate-400 text-center";
                empty.textContent = "No se encontraron módulos para \"" + term.trim() + "\"";
                resultsBox.appendChild(empty);
                resultsBox.classList.remove("hidden");
                return;
            }

            matches.forEach(function (item) {
                const link = document.createElement("a");
                link.href = item.url;
                link.setAttribute("role", "option");
                link.className = "flex items-center justify-between px-3.5 py-2.5 hover:bg-slate-800/80 transition-colors text-xs";

                const left = document.createElement("div");
                left.className = "flex items-center space-x-2.5 min-w-0";

                const icon = document.createElement("i");
                icon.setAttribute("data-lucide", item.icono);
                icon.className = "w-4 h-4 text-blue-400 flex-shrink-0";

                const titleSpan = document.createElement("span");
                titleSpan.className = "text-slate-200 font-medium truncate";
                titleSpan.textContent = item.titulo;

                left.appendChild(icon);
                left.appendChild(titleSpan);

                const badge = document.createElement("span");
                badge.className = "text-[10px] px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700 flex-shrink-0 ml-2";
                badge.textContent = item.categoria;

                link.appendChild(left);
                link.appendChild(badge);
                resultsBox.appendChild(link);
            });

            resultsBox.classList.remove("hidden");
            initLucideIcons();
        }

        searchInput.addEventListener("input", function () {
            renderResults(searchInput.value);
        });

        searchInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                const firstOption = resultsBox.querySelector("a[role='option']");
                if (firstOption) {
                    e.preventDefault();
                    window.location.href = firstOption.href;
                }
            } else if (e.key === "Escape") {
                resultsBox.classList.add("hidden");
                searchInput.blur();
            }
        });

        document.addEventListener("click", function (e) {
            if (!parentWrapper.contains(e.target)) {
                resultsBox.classList.add("hidden");
            }
        });
    }

    /**
     * Configura atajos globales de teclado (Alt + / y Escape).
     */
    function initKeyboardShortcuts() {
        document.addEventListener("keydown", function (e) {
            if (e.altKey && (e.key === "/" || e.code === "Slash")) {
                e.preventDefault();
                const searchInput = document.getElementById("inputGlobalSearch");
                if (searchInput) searchInput.focus();
            }

            if (e.key === "Escape" || e.code === "Escape") {
                document.querySelectorAll(".modal-container:not(.hidden)").forEach(function (m) {
                    cerrarModal(m.id);
                });

                const dropN = document.getElementById("dropdownNotificaciones");
                const btnN = document.getElementById("btnNotificaciones");
                const dropP = document.getElementById("dropdownPerfil");
                const btnP = document.getElementById("btnPerfil");
                const searchRes = document.getElementById("globalSearchResults");

                if (dropN) dropN.classList.add("hidden");
                if (btnN) btnN.setAttribute("aria-expanded", "false");
                if (dropP) dropP.classList.add("hidden");
                if (btnP) btnP.setAttribute("aria-expanded", "false");
                if (searchRes) searchRes.classList.add("hidden");
            }
        });
    }

    /**
     * Previene el doble envío accidental en formularios POST.
     */
    function initFormProtection() {
        document.addEventListener("submit", function (e) {
            const form = e.target;
            if (!form || form.tagName !== "FORM") return;
            const method = (form.getAttribute("method") || "get").toLowerCase();
            if (method !== "post") return;

            if (form.dataset.submitting === "true") {
                e.preventDefault();
                return;
            }

            form.dataset.submitting = "true";
            const submitButtons = form.querySelectorAll('button[type="submit"], input[type="submit"]');
            submitButtons.forEach(function (btn) {
                btn.classList.add("is-submitting");
            });

            // Liberar bloqueo tras 8 segundos por si hubo descarga de archivo o validación cancelada
            setTimeout(function () {
                form.dataset.submitting = "false";
                submitButtons.forEach(function (btn) {
                    btn.classList.remove("is-submitting");
                });
            }, 8000);
        });
    }

    /**
     * Abre un modal por su ID de forma accesible.
     */
    function abrirModal(id) {
        const modal = document.getElementById(id);
        if (!modal) return;

        ultimoElementoEnfocado = document.activeElement;
        modal.classList.remove("hidden");
        modal.classList.add("flex");
        modal.setAttribute("aria-hidden", "false");

        const firstControl = modal.querySelector("input:not([type='hidden']), select, textarea, button:not([onclick*='cerrarModal'])");
        if (firstControl) {
            setTimeout(function () { firstControl.focus(); }, 20);
        }
    }

    /**
     * Cierra un modal por su ID y devuelve el foco al elemento disparador.
     */
    function cerrarModal(id) {
        const modal = document.getElementById(id);
        if (!modal) return;

        modal.classList.add("hidden");
        modal.classList.remove("flex");
        modal.setAttribute("aria-hidden", "true");

        if (ultimoElementoEnfocado && typeof ultimoElementoEnfocado.focus === "function") {
            ultimoElementoEnfocado.focus();
            ultimoElementoEnfocado = null;
        }
    }

    /**
     * Obtiene el token y cabecera CSRF desde las etiquetas meta del documento.
     */
    function getCsrfData() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        return {
            token: tokenMeta ? tokenMeta.getAttribute("content") : "",
            header: headerMeta ? headerMeta.getAttribute("content") : "X-CSRF-TOKEN"
        };
    }

    /**
     * Extrae mensajes detallados de validación o negocio desde la respuesta JSON del backend.
     */
    function extraerMensajeError(data, status) {
        if (!data) {
            return "Error al procesar la solicitud (HTTP " + status + ")";
        }
        const detalles = [];
        const sources = [data.errors, data.validationErrors, data.fieldErrors, data.details];
        sources.forEach(function (src) {
            if (!src) return;
            if (Array.isArray(src)) {
                src.forEach(function (item) {
                    if (typeof item === "string") detalles.push(item);
                    else if (item && item.defaultMessage) detalles.push(item.defaultMessage);
                    else if (item && item.message) detalles.push(item.message);
                });
            } else if (typeof src === "object") {
                Object.keys(src).forEach(function (key) {
                    const val = src[key];
                    if (typeof val === "string") detalles.push(val);
                    else if (Array.isArray(val)) detalles.push(val.join(", "));
                });
            }
        });

        if (detalles.length > 0) {
            return detalles.join(" | ");
        }
        if (data.message) {
            return data.message;
        }
        return "Error al procesar la solicitud (HTTP " + status + ")";
    }

    /**
     * Muestra u oculta un banner de alerta de error dentro del modal actualmente abierto.
     */
    function mostrarErrorEnModalActivo(mensaje) {
        const modalActivo = document.querySelector(".modal-container:not(.hidden)");
        if (!modalActivo) return;
        const formOrBody = modalActivo.querySelector("form") || modalActivo.querySelector(".bg-card");
        if (!formOrBody) return;

        let alertBox = modalActivo.querySelector(".modal-inline-error");
        if (!mensaje) {
            if (alertBox) alertBox.remove();
            return;
        }

        if (!alertBox) {
            alertBox = document.createElement("div");
            alertBox.className = "modal-inline-error p-3 mb-3 rounded-lg bg-rose-950/90 border border-rose-500/80 text-rose-200 text-xs flex items-start space-x-2.5 shadow-md";
            formOrBody.insertBefore(alertBox, formOrBody.firstChild);
        }
        alertBox.innerHTML = "";
        const icon = document.createElement("i");
        icon.setAttribute("data-lucide", "alert-circle");
        icon.className = "w-4 h-4 text-rose-400 flex-shrink-0 mt-0.5";
        const span = document.createElement("span");
        span.className = "flex-1 font-medium leading-relaxed";
        span.textContent = mensaje;
        alertBox.appendChild(icon);
        alertBox.appendChild(span);
        initLucideIcons();
    }

    /**
     * Cliente Fetch con inyección automática de CSRF y manejo estructurado de errores.
     */
    async function fetchApi(url, options) {
        const opts = options || {};
        const csrf = getCsrfData();
        const headers = opts.headers || {};

        if (csrf.token && !headers[csrf.header]) {
            headers[csrf.header] = csrf.token;
        }
        if (!headers["Content-Type"] && !(opts.body instanceof FormData)) {
            headers["Content-Type"] = "application/json";
        }
        opts.headers = headers;

        mostrarErrorEnModalActivo(null);

        try {
            const response = await fetch(url, opts);
            const contentType = response.headers.get("content-type") || "";
            const data = contentType.includes("application/json") ? await response.json() : null;

            if (!response.ok) {
                const errorMsg = extraerMensajeError(data, response.status);
                mostrarToast(errorMsg, "error");
                mostrarErrorEnModalActivo(errorMsg);
                const apiErr = new Error(errorMsg);
                apiErr._toastShown = true;
                throw apiErr;
            }
            return data;
        } catch (err) {
            if (err && !err._toastShown && err.message) {
                mostrarToast(err.message, "error");
                mostrarErrorEnModalActivo(err.message);
            }
            throw err;
        }
    }

    /**
     * Muestra una notificación Toast flotante siempre visible sobre cualquier modal.
     */
    function mostrarToast(mensaje, tipo) {
        const toastType = tipo || "success";
        let container = document.getElementById("toastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "toastContainer";
            container.className = "fixed top-20 right-6 flex flex-col space-y-2.5 max-w-md pointer-events-none";
            document.body.appendChild(container);
        } else if (container.parentElement !== document.body) {
            document.body.appendChild(container);
        }
        container.style.zIndex = "99999";

        const toast = document.createElement("div");
        toast.setAttribute("role", "alert");
        toast.setAttribute("aria-live", "assertive");

        const colorClasses = toastType === "success"
            ? "bg-slate-950/95 border-emerald-500 border-l-4 text-emerald-100 shadow-emerald-950/50"
            : (toastType === "warning"
                ? "bg-slate-950/95 border-amber-500 border-l-4 text-amber-100 shadow-amber-950/50"
                : (toastType === "info"
                    ? "bg-slate-950/95 border-blue-500 border-l-4 text-blue-100 shadow-blue-950/50"
                    : "bg-slate-950/95 border-rose-500 border-l-4 text-rose-100 shadow-rose-950/60"));

        toast.className = "pointer-events-auto flex items-start space-x-3 px-4 py-3.5 rounded-xl border shadow-2xl text-xs font-semibold transition-all duration-300 transform translate-y-2 opacity-0 " + colorClasses;

        const iconName = toastType === "success"
            ? "check-circle"
            : (toastType === "warning"
                ? "alert-triangle"
                : (toastType === "info" ? "info" : "alert-circle"));
        const iconColor = toastType === "success"
            ? "text-emerald-400"
            : (toastType === "warning"
                ? "text-amber-400"
                : (toastType === "info" ? "text-blue-400" : "text-rose-400"));

        const iconEl = document.createElement("i");
        iconEl.setAttribute("data-lucide", iconName);
        iconEl.className = "w-4 h-4 flex-shrink-0 mt-0.5 " + iconColor;

        const textEl = document.createElement("span");
        textEl.className = "flex-1 leading-relaxed";
        textEl.textContent = String(mensaje || "");

        const closeBtn = document.createElement("button");
        closeBtn.type = "button";
        closeBtn.className = "text-slate-400 hover:text-white ml-2 font-bold text-sm leading-none";
        closeBtn.textContent = "×";
        closeBtn.onclick = function () { toast.remove(); };

        toast.appendChild(iconEl);
        toast.appendChild(textEl);
        toast.appendChild(closeBtn);
        container.appendChild(toast);

        initLucideIcons();

        setTimeout(function () {
            toast.classList.remove("translate-y-2", "opacity-0");
        }, 10);

        const duration = toastType === "error" ? 6500 : 4200;
        setTimeout(function () {
            toast.classList.add("opacity-0");
            setTimeout(function () { toast.remove(); }, 300);
        }, duration);
    }

    /**
     * Marca una notificación individual como leída.
     */
    async function marcarNotificacionLeida(id) {
        try {
            await fetchApi("/api/notificaciones/" + encodeURIComponent(id) + "/leer", { method: "PUT" });
            mostrarToast("Notificación actualizada", "success");
            setTimeout(function () { window.location.reload(); }, 400);
        } catch (e) {
            console.error("Error al marcar notificación:", e);
        }
    }

    /**
     * Marca todas las notificaciones del usuario como leídas.
     */
    async function marcarTodasNotificacionesLeidas() {
        try {
            await fetchApi("/api/notificaciones/marcar-todas", { method: "PUT" });
            mostrarToast("Todas las notificaciones marcadas como leídas", "success");
            const badge = document.getElementById("badgeNotificaciones");
            if (badge) badge.classList.add("hidden");
            setTimeout(function () { window.location.reload(); }, 400);
        } catch (e) {
            console.error("Error al marcar todas las notificaciones:", e);
        }
    }

    /**
     * Abre o cierra el submenú desplegable (acordeón) de cualquiera de los 4 Servicios Municipales
     * en el sidebar izquierdo y rota el ícono chevron-down.
     */
    function toggleSidebarServiceMenu(submenuId, btnTrigger) {
        const panel = document.getElementById(submenuId);
        if (!panel) return;
        const isHidden = panel.classList.contains("hidden");
        if (isHidden) {
            panel.classList.remove("hidden");
            if (btnTrigger) {
                btnTrigger.setAttribute("aria-expanded", "true");
                const chevron = btnTrigger.querySelector("svg, [data-lucide='chevron-down']");
                if (chevron) chevron.classList.add("rotate-180");
            }
        } else {
            panel.classList.add("hidden");
            if (btnTrigger) {
                btnTrigger.setAttribute("aria-expanded", "false");
                const chevron = btnTrigger.querySelector("svg, [data-lucide='chevron-down']");
                if (chevron) chevron.classList.remove("rotate-180");
            }
        }
    }

    /**
     * Abre o cierra la lista desplegable personalizada con iconos vectoriales Lucide
     * dentro de cada servicio municipal.
     */
    function toggleSidebarCustomDropdown(menuId, btnTrigger) {
        const menu = document.getElementById(menuId);
        if (!menu) return;
        const isHidden = menu.classList.contains("hidden");

        // Cerrar cualquier otra lista desplegable abierta en el sidebar
        document.querySelectorAll(".sidebar-custom-dropdown-menu").forEach(function (other) {
            if (other.id !== menuId) {
                other.classList.add("hidden");
            }
        });
        document.querySelectorAll(".sidebar-dropdown-chevron").forEach(function (chev) {
            chev.classList.remove("rotate-180");
        });

        if (isHidden) {
            menu.classList.remove("hidden");
            if (btnTrigger) {
                const chevron = btnTrigger.querySelector(".sidebar-dropdown-chevron, svg:last-child");
                if (chevron) chevron.classList.add("rotate-180");
            }
            initLucideIcons();
        } else {
            menu.classList.add("hidden");
        }
    }

    /**
     * Resalta automáticamente el enlace activo dentro del submenú de Servicios Municipales
     * y sincroniza el botón desplegable personalizado con el icono vectorial Lucide activo.
     */
    function initSidebarServiceMenus() {
        const currentPath = window.location.pathname;
        const currentFull = currentPath + (window.location.search || "");

        // 1. Sincronizar listas desplegables personalizadas (.sidebar-custom-dropdown) con iconos vectoriales
        document.querySelectorAll(".sidebar-custom-dropdown").forEach(function (dropdownContainer) {
            let matchedOption = null;
            dropdownContainer.querySelectorAll(".sidebar-dropdown-option").forEach(function (opt) {
                const href = opt.getAttribute("href");
                if (!href) return;
                if (href === currentFull) {
                    matchedOption = opt;
                } else if (!matchedOption && href === currentPath) {
                    matchedOption = opt;
                }
            });

            if (matchedOption) {
                matchedOption.classList.add("bg-blue-600/20", "text-blue-300", "font-semibold");
                const labelEl = dropdownContainer.querySelector(".sidebar-dropdown-active-label");
                const optionTextEl = matchedOption.querySelector("span");
                if (labelEl && optionTextEl) {
                    labelEl.textContent = optionTextEl.textContent.trim();
                }
                const iconTarget = dropdownContainer.querySelector(".sidebar-dropdown-active-icon");
                const optionSvg = matchedOption.querySelector("svg, i[data-lucide]");
                if (iconTarget && optionSvg) {
                    const clonedIcon = optionSvg.cloneNode(true);
                    clonedIcon.classList.add("sidebar-dropdown-active-icon");
                    iconTarget.replaceWith(clonedIcon);
                }
            }
        });

        // Cerrar listas desplegables personalizadas al hacer clic fuera
        document.addEventListener("click", function (ev) {
            if (!ev.target.closest(".sidebar-custom-dropdown")) {
                document.querySelectorAll(".sidebar-custom-dropdown-menu").forEach(function (menu) {
                    menu.classList.add("hidden");
                });
                document.querySelectorAll(".sidebar-dropdown-chevron").forEach(function (chev) {
                    chev.classList.remove("rotate-180");
                });
            }
        });

        // 2. Resaltar el enlace activo (.sidebar-sublink) en los grupos del menú
        let bestMatch = null;
        document.querySelectorAll(".sidebar-sublink").forEach(function (link) {
            const href = link.getAttribute("href");
            if (!href) return;
            if (href === currentFull || href === currentPath) {
                bestMatch = link;
            }
        });

        if (bestMatch) {
            bestMatch.classList.remove("text-slate-300", "hover:bg-slate-800/70");
            bestMatch.classList.add("bg-blue-600/20", "text-blue-300", "font-semibold", "border", "border-blue-500/30");
            const parentSubmenu = bestMatch.closest(".sidebar-submenu");
            if (parentSubmenu && parentSubmenu.classList.contains("hidden")) {
                parentSubmenu.classList.remove("hidden");
            }
        }
    }

    // Exponer API pública global para compatibilidad con manejadores en vistas Thymeleaf
    window.abrirModal = abrirModal;
    window.cerrarModal = cerrarModal;
    window.getCsrfData = getCsrfData;
    window.fetchApi = fetchApi;
    window.mostrarToast = mostrarToast;
    window.marcarNotificacionLeida = marcarNotificacionLeida;
    window.marcarTodasNotificacionesLeidas = marcarTodasNotificacionesLeidas;
    window.toggleSidebarServiceMenu = toggleSidebarServiceMenu;
    window.toggleSidebarCustomDropdown = toggleSidebarCustomDropdown;

    // Inicialización al cargar el DOM
    document.addEventListener("DOMContentLoaded", function () {
        initLucideIcons();
        initSidebar();
        initSidebarServiceMenus();
        initDropdowns();
        initGlobalSearch();
        initKeyboardShortcuts();
        initFormProtection();
    });
})();

