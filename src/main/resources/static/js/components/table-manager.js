/**
 * ============================================================================
 * SIGM-QUINUA — GESTOR DE TABLAS DE DATOS, FILTROS Y BÚSQUEDA RÁPIDA
 * ============================================================================
 */
(function () {
    "use strict";

    function limpiarFiltrosFormulario(formId) {
        const form = document.getElementById(formId);
        if (!form) return;
        form.querySelectorAll("input:not([type='hidden']), select").forEach(function (el) {
            if (el.type === "checkbox" || el.type === "radio") {
                el.checked = false;
            } else {
                el.value = "";
            }
        });
        form.submit();
    }

    function filtrarTablaLocal(inputEl, tableId) {
        const table = document.getElementById(tableId);
        if (!inputEl || !table) return;
        const term = (inputEl.value || "").trim().toLowerCase();
        const rows = table.querySelectorAll("tbody tr[data-table-row]");
        let visibles = 0;

        rows.forEach(function (row) {
            const text = (row.textContent || "").toLowerCase();
            const match = !term || text.includes(term);
            row.classList.toggle("hidden", !match);
            if (match) visibles++;
        });

        const emptyRow = table.querySelector("tbody tr[data-empty-filter-row]");
        if (emptyRow) {
            emptyRow.classList.toggle("hidden", visibles > 0);
        }
    }

    window.SigmTable = {
        limpiarFiltros: limpiarFiltrosFormulario,
        filtrarLocal: filtrarTablaLocal
    };
})();

