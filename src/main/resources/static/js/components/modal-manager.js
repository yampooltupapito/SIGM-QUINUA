/**
 * ============================================================================
 * SIGM-QUINUA — GESTOR DE MODALES Y CONFIRMACIONES ACCESIBLES
 * ============================================================================
 */
(function () {
    "use strict";

    /**
     * Abre el modal de confirmación genérico poblando título, mensaje y acción.
     */
    function abrirModalConfirmacion(opciones) {
        const opts = opciones || {};
        const modalId = opts.modalId || "modalConfirmacionGlobal";
        const modal = document.getElementById(modalId);
        if (!modal) return;

        const tituloEl = modal.querySelector("[data-confirm-title]");
        const mensajeEl = modal.querySelector("[data-confirm-message]");
        const formEl = modal.querySelector("form[data-confirm-form]");
        const btnConfirmEl = modal.querySelector("[data-confirm-submit]");

        if (tituloEl && opts.titulo) tituloEl.textContent = opts.titulo;
        if (mensajeEl && opts.mensaje) mensajeEl.textContent = opts.mensaje;
        if (formEl && opts.actionUrl) formEl.setAttribute("action", opts.actionUrl);
        if (btnConfirmEl && opts.textoBoton) btnConfirmEl.textContent = opts.textoBoton;

        if (typeof window.abrirModal === "function") {
            window.abrirModal(modalId);
        } else {
            modal.classList.remove("hidden");
            modal.classList.add("flex");
            modal.setAttribute("aria-hidden", "false");
        }
    }

    window.abrirModalConfirmacion = abrirModalConfirmacion;
})();

