/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para interacción del Árbol Jerárquico de Estructura de Mercados
 */
document.addEventListener('DOMContentLoaded', () => {
    initArbolJerarquico();
    initFiltroArbol();
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

function initArbolJerarquico() {
    // Manejo de colapsar / expandir nodos individuales
    document.querySelectorAll('.toggle-nodo').forEach(header => {
        header.addEventListener('click', () => {
            const targetId = header.getAttribute('data-target-id');
            const targetContent = document.getElementById(targetId);
            const chevron = header.querySelector('.nodo-chevron');

            if (targetContent) {
                const isHidden = targetContent.classList.contains('hidden');
                if (isHidden) {
                    targetContent.classList.remove('hidden');
                    if (chevron) chevron.style.transform = 'rotate(0deg)';
                } else {
                    targetContent.classList.add('hidden');
                    if (chevron) chevron.style.transform = 'rotate(-90deg)';
                }
            }
        });
    });

    // Botón Expandir Todo
    const btnExpandir = document.getElementById('btnExpandirTodo');
    if (btnExpandir) {
        btnExpandir.addEventListener('click', () => {
            document.querySelectorAll('[id^="contenido-sector-"]').forEach(content => {
                content.classList.remove('hidden');
            });
            document.querySelectorAll('.nodo-chevron').forEach(chevron => {
                chevron.style.transform = 'rotate(0deg)';
            });
        });
    }

    // Botón Colapsar Todo
    const btnColapsar = document.getElementById('btnColapsarTodo');
    if (btnColapsar) {
        btnColapsar.addEventListener('click', () => {
            document.querySelectorAll('[id^="contenido-sector-"]').forEach(content => {
                content.classList.add('hidden');
            });
            document.querySelectorAll('.nodo-chevron').forEach(chevron => {
                chevron.style.transform = 'rotate(-90deg)';
            });
        });
    }
}

function initFiltroArbol() {
    const inputFiltro = document.getElementById('filtroArbol');
    if (!inputFiltro) return;

    inputFiltro.addEventListener('input', () => {
        const query = inputFiltro.value.trim().toLowerCase();
        const nodos = document.querySelectorAll('.nodo-arbol');

        nodos.forEach(nodo => {
            const dataSearch = (nodo.getAttribute('data-search') || '').toLowerCase();
            const textContent = nodo.textContent.toLowerCase();

            if (!query || dataSearch.includes(query) || textContent.includes(query)) {
                nodo.classList.remove('hidden');
                // Si coincide y hay búsqueda activa, expandir el nodo para mostrar el resultado
                if (query) {
                    const targetContent = nodo.querySelector('[id^="contenido-sector-"]');
                    const chevron = nodo.querySelector('.nodo-chevron');
                    if (targetContent) targetContent.classList.remove('hidden');
                    if (chevron) chevron.style.transform = 'rotate(0deg)';
                }
            } else {
                nodo.classList.add('hidden');
            }
        });
    });
}
