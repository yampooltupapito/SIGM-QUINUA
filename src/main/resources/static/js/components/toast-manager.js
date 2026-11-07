/**
 * ============================================================================
 * SIGM-QUINUA — GESTOR DE NOTIFICACIONES FLOTANTES (TOAST) SEGURO CONTRA XSS
 * ============================================================================
 */
(function () {
    "use strict";

    // Si window.mostrarToast ya está registrado por admin-ui.js, exponemos alias en SigmToast
    window.SigmToast = {
        show: function (mensaje, tipo) {
            if (typeof window.mostrarToast === "function") {
                window.mostrarToast(mensaje, tipo || "info");
            }
        },
        success: function (mensaje) { this.show(mensaje, "success"); },
        warning: function (mensaje) { this.show(mensaje, "warning"); },
        error: function (mensaje) { this.show(mensaje, "error"); },
        info: function (mensaje) { this.show(mensaje, "info"); }
    };
})();

