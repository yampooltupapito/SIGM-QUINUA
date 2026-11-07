/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para vista detallada de mercado, navegación por pestañas y asociación de documentos
 */
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initDocumentos();
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

function initTabs() {
    const tabLinks = document.querySelectorAll('.tab-link');
    const tabContents = document.querySelectorAll('.tab-content');

    tabLinks.forEach(link => {
        link.addEventListener('click', () => {
            const targetId = link.getAttribute('data-tab-target');

            // Desactivar todos los links
            tabLinks.forEach(l => {
                l.classList.remove('active', 'border-blue-500', 'text-white');
                l.classList.add('border-transparent', 'text-slate-400');
            });

            // Ocultar todos los contenidos
            tabContents.forEach(c => c.classList.add('hidden'));

            // Activar link actual
            link.classList.add('active', 'border-blue-500', 'text-white');
            link.classList.remove('border-transparent', 'text-slate-400');

            // Mostrar contenido correspondiente
            const targetContent = document.getElementById(targetId);
            if (targetContent) {
                targetContent.classList.remove('hidden');
            }

            if (window.lucide) window.lucide.createIcons();
        });
    });
}

function initDocumentos() {
    const btnAsociar = document.getElementById('btnAsociarDocumento');
    const formContainer = document.getElementById('formAsociarDocContainer');
    const btnCancelar = document.getElementById('btnCancelarDoc');
    const form = document.getElementById('formAsociarDoc');
    const tablaBody = document.getElementById('tablaDocsBody');

    if (!btnAsociar || !formContainer || !form) return;

    btnAsociar.addEventListener('click', () => {
        formContainer.classList.toggle('hidden');
        if (!formContainer.classList.contains('hidden')) {
            document.getElementById('docTitulo')?.focus();
        }
    });

    if (btnCancelar) {
        btnCancelar.addEventListener('click', () => {
            formContainer.classList.add('hidden');
            form.reset();
        });
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Extraer mercadoId desde la URL
        const pathParts = window.location.pathname.split('/');
        // Ejemplo /dashboard/mercados/{id}
        const mercadoId = pathParts[pathParts.length - 1];

        const tipo = document.getElementById('docTipo')?.value;
        const titulo = document.getElementById('docTitulo')?.value.trim();
        const docIdVal = document.getElementById('docDocumentoId')?.value.trim();
        const observaciones = document.getElementById('docObservaciones')?.value.trim();

        if (!titulo || !tipo) {
            alert('Por favor complete el título y tipo de documento.');
            return;
        }

        const payload = {
            tipo: tipo,
            titulo: titulo,
            documentoId: docIdVal || null,
            observaciones: observaciones || null
        };

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content ||
                          document.getElementById('csrf-token')?.value || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content ||
                           document.getElementById('csrf-header')?.value || 'X-CSRF-TOKEN';

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        const btnGuardar = document.getElementById('btnGuardarDoc');
        if (btnGuardar) btnGuardar.disabled = true;

        try {
            const res = await fetch(`/api/mercados/${mercadoId}/documentos`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok) {
                throw new Error(data.error || data.message || 'Error al asociar el documento.');
            }

            // Recargar para mostrar en la pestaña de documentos
            window.location.reload();

        } catch (err) {
            alert('Error: ' + err.message);
        } finally {
            if (btnGuardar) btnGuardar.disabled = false;
        }
    });
}
