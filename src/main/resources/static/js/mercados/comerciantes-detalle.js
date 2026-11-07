/**
 * comerciantes-detalle.js — Manejo de pestañas para la vista detallada de comerciante.
 */

function cambiarPestana(tabId) {
    // Ocultar todos los contenidos
    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(c => c.classList.add('hidden'));

    // Desactivar todos los botones
    const buttons = document.querySelectorAll('.tab-btn');
    buttons.forEach(b => {
        b.classList.remove('border-blue-500', 'text-blue-400');
        b.classList.add('border-transparent', 'text-slate-400');
    });

    // Activar pestaña seleccionada
    const targetContent = document.getElementById(tabId);
    if (targetContent) {
        targetContent.classList.remove('hidden');
    }

    const btnId = 'btn' + tabId.charAt(0).toUpperCase() + tabId.slice(1);
    const targetBtn = document.getElementById(btnId);
    if (targetBtn) {
        targetBtn.classList.remove('border-transparent', 'text-slate-400');
        targetBtn.classList.add('border-blue-500', 'text-blue-400');
    }
}
