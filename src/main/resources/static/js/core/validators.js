/**
 * ============================================================================
 * SIGM-QUINUA — VALIDACIONES ACCESIBLES DE FORMULARIOS (WCAG 2.2 AA)
 * Municipalidad Distrital de Quinua, Huamanga, Ayacucho, Perú
 * ============================================================================
 */
(function () {
    "use strict";

    function marcarErrorCampo(input, mensaje) {
        if (!input) return;
        input.classList.add("is-invalid");
        input.classList.remove("is-valid");
        input.setAttribute("aria-invalid", "true");

        const group = input.closest(".sigm-form-group") || input.parentElement;
        if (!group) return;

        let errEl = group.querySelector(".sigm-field-error[data-dynamic='true']");
        if (!errEl) {
            errEl = document.createElement("p");
            errEl.className = "sigm-field-error mt-1";
            errEl.setAttribute("data-dynamic", "true");
            errEl.setAttribute("role", "alert");
            group.appendChild(errEl);
        }
        errEl.textContent = mensaje;
    }

    function limpiarErrorCampo(input) {
        if (!input) return;
        input.classList.remove("is-invalid");
        input.removeAttribute("aria-invalid");

        const group = input.closest(".sigm-form-group") || input.parentElement;
        if (!group) return;

        const errEl = group.querySelector(".sigm-field-error[data-dynamic='true']");
        if (errEl) errEl.remove();
    }

    function validarControl(input) {
        if (!input || input.disabled || input.type === "hidden") return true;
        const val = (input.value || "").trim();

        if (input.hasAttribute("required") && !val) {
            marcarErrorCampo(input, "Este campo es obligatorio.");
            return false;
        }

        const docRule = input.getAttribute("data-validate-doc");
        if (val && docRule === "DNI" && !/^\d{8}$/.test(val)) {
            marcarErrorCampo(input, "El DNI debe contener exactamente 8 dígitos numéricos.");
            return false;
        }
        if (val && docRule === "RUC" && !/^(10|20)\d{9}$/.test(val)) {
            marcarErrorCampo(input, "El RUC debe contener 11 dígitos válidos (iniciando en 10 o 20).");
            return false;
        }

        if (val && input.type === "email" && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) {
            marcarErrorCampo(input, "Ingrese un correo electrónico institucional o personal válido.");
            return false;
        }

        limpiarErrorCampo(input);
        if (val) input.classList.add("is-valid");
        return true;
    }

    function validarFormulario(form) {
        if (!form) return true;
        let esValido = true;
        let primerInvalido = null;

        const campos = form.querySelectorAll("input, select, textarea");
        campos.forEach(function (campo) {
            if (!validarControl(campo)) {
                esValido = false;
                if (!primerInvalido) primerInvalido = campo;
            }
        });

        if (!esValido && primerInvalido) {
            primerInvalido.focus();
        }
        return esValido;
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("form[data-sigm-validate='true']").forEach(function (form) {
            form.querySelectorAll("input, select, textarea").forEach(function (campo) {
                campo.addEventListener("blur", function () {
                    validarControl(campo);
                });
                campo.addEventListener("input", function () {
                    if (campo.classList.contains("is-invalid")) {
                        validarControl(campo);
                    }
                });
            });

            form.addEventListener("submit", function (e) {
                if (!validarFormulario(form)) {
                    e.preventDefault();
                    e.stopPropagation();
                }
            });
        });
    });

    window.SigmValidators = {
        validarFormulario: validarFormulario,
        validarControl: validarControl,
        marcarErrorCampo: marcarErrorCampo,
        limpiarErrorCampo: limpiarErrorCampo
    };
})();

