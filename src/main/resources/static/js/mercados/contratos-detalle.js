// SIGM-QUINUA: Manejo de acciones y modales en detalle de contrato de ocupación
function abrirModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('hidden');
}

function cerrarModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.add('hidden');
}

async function enviarRevision() {
    if (!confirm('¿Confirma enviar este contrato a revisión legal y administrativa?')) return;
    const contratoId = document.getElementById('contratoId').value;
    await ejecutarPeticion(`/api/mercados/contratos/${contratoId}/revision`, 'POST', {});
}

async function ejecutarEstado(endpoint, inputId, modalId) {
    const contratoId = document.getElementById('contratoId').value;
    const motivoVal = document.getElementById(inputId)?.value || '';

    const payload = {
        motivo: motivoVal,
        documentoReferencia: motivoVal
    };

    cerrarModal(modalId);
    await ejecutarPeticion(`/api/mercados/contratos/${contratoId}${endpoint}`, 'POST', payload);
}

async function ejecutarPeticion(url, method, payload) {
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
            throw new Error(err.message || 'Error al ejecutar la acción sobre el contrato.');
        }

        window.location.reload();
    } catch (e) {
        alert(e.message);
    }
}
