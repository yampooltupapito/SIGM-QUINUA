/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para detalle de Puesto Comercial (Pestañas, Modal de Reubicación)
 */
document.addEventListener('DOMContentLoaded', () => {
    initFormularioReubicacion();
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

function cambiarPestana(idDestino) {
    const pestanas = ['pestana-info', 'pestana-estados', 'pestana-ubicacion', 'pestana-documentos'];
    pestanas.forEach(p => {
        const el = document.getElementById(p);
        const btn = document.getElementById(`btn-${p}`);
        if (el && btn) {
            if (p === idDestino) {
                el.classList.remove('hidden');
                btn.classList.add('border-blue-500', 'text-blue-400');
                btn.classList.remove('border-transparent', 'text-slate-400');
            } else {
                el.classList.add('hidden');
                btn.classList.remove('border-blue-500', 'text-blue-400');
                btn.classList.add('border-transparent', 'text-slate-400');
            }
        }
    });
}

function abrirModalUbicacion() {
    const modal = document.getElementById('modalReubicarPuesto');
    if (modal) modal.classList.remove('hidden');
}

function cerrarModalUbicacion() {
    const modal = document.getElementById('modalReubicarPuesto');
    if (modal) modal.classList.add('hidden');
}

function initFormularioReubicacion() {
    const form = document.getElementById('formReubicarPuesto');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const puestoId = document.getElementById('reubicarPuestoId').value;
        const mercadoId = document.getElementById('reubicarMercadoId').value;
        const nuevoSectorId = document.getElementById('nuevoSectorId')?.value || null;
        const nuevoPabellonId = document.getElementById('nuevoPabellonId')?.value || null;
        const nuevaUbicacionRef = document.getElementById('nuevaUbicacionRef')?.value.trim() || null;
        const motivo = document.getElementById('motivoReubicacion')?.value.trim();

        const { token, header } = getCsrfData();
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) headers[header] = token;

        try {
            const resp = await fetch(`/api/mercados/${mercadoId}/puestos/${puestoId}/ubicacion`, {
                method: 'PATCH',
                headers: headers,
                body: JSON.stringify({
                    nuevoSectorId: nuevoSectorId,
                    nuevoPabellonId: nuevoPabellonId,
                    nuevaUbicacionReferencia: nuevaUbicacionRef,
                    motivo: motivo
                })
            });

            if (!resp.ok) {
                const err = await resp.json().catch(() => ({}));
                throw new Error(err.message || err.error || `Error ${resp.status}`);
            }

            window.location.reload();
        } catch (err) {
            alert(err.message || 'Error al reubicar el puesto.');
        }
    });
}
