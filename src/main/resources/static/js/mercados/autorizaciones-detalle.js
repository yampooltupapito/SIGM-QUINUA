// SIGM-QUINUA: Manejo de acciones y modales en detalle de autorización comercial
function abrirModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('hidden');
}

function cerrarModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.add('hidden');
}

async function enviarEvaluacion() {
    if (!confirm('¿Confirma presentar esta solicitud de autorización a evaluación técnica?')) return;
    const autorizacionId = document.getElementById('autorizacionId').value;
    await ejecutarPeticionAut(`/api/mercados/autorizaciones/${autorizacionId}/evaluar`, 'POST', {});
}

async function ejecutarEstadoAut(endpoint, inputId, modalId) {
    const autorizacionId = document.getElementById('autorizacionId').value;
    const motivoVal = document.getElementById(inputId)?.value || '';

    const payload = {
        motivo: motivoVal,
        documentoReferencia: motivoVal
    };

    cerrarModal(modalId);
    await ejecutarPeticionAut(`/api/mercados/autorizaciones/${autorizacionId}${endpoint}`, 'POST', payload);
}

async function ejecutarPeticionAut(url, method, payload) {
    try {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const headers = { 'Content-Type': 'application/json' };
        if (token && header) headers[header] = token;

        const res = await fetch(url, {
            method: method,
            headers: headers,
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Error al ejecutar la acción sobre la autorización.');
        }

        window.location.reload();
    } catch (e) {
        alert(e.message);
    }
}
