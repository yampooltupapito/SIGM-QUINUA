// static/js/mercados/asignaciones-detalle.js

const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
const asignacionId = window.location.pathname.split('/').pop();

function cambiarPestana(tabId) {
    document.querySelectorAll('.tab-pane').forEach(el => el.classList.add('hidden'));
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('border-blue-500', 'text-white');
        btn.classList.add('border-transparent', 'text-slate-400');
    });

    const activeTab = document.getElementById(tabId);
    if (activeTab) activeTab.classList.remove('hidden');

    const activeBtn = document.getElementById('btn' + tabId.charAt(0).toUpperCase() + tabId.slice(1));
    if (activeBtn) {
        activeBtn.classList.remove('border-transparent', 'text-slate-400');
        activeBtn.classList.add('border-blue-500', 'text-white');
    }
}

function abrirModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) {
        m.classList.remove('hidden');
        m.classList.add('flex');
    }
}

function cerrarModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) {
        m.classList.add('hidden');
        m.classList.remove('flex');
    }
}

function enviarAccion(url, method = 'POST', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    const options = { method, headers };
    if (body) {
        options.body = JSON.stringify(body);
    }

    return fetch(url, options)
        .then(async res => {
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Error al ejecutar la acción.');
            }
            return res.json();
        });
}

function presentarSolicitud() {
    if (!confirm('¿Confirma la presentación formal de esta solicitud para inicio de evaluación técnica?')) return;
    enviarAccion(`/api/mercados/asignaciones/${asignacionId}/presentar`)
        .then(() => {
            alert('Solicitud presentada exitosamente.');
            window.location.reload();
        })
        .catch(err => alert(err.message));
}

function activarOcupacion() {
    if (!confirm('¿Confirma la activación de la ocupación efectiva del puesto por parte del comerciante adjudicatario? El puesto pasará al estado OCUPADO.')) return;
    enviarAccion(`/api/mercados/asignaciones/${asignacionId}/activar`)
        .then(() => {
            alert('Ocupación activada con éxito. El puesto ahora está VIGENTE y OCUPADO.');
            window.location.reload();
        })
        .catch(err => alert(err.message));
}

function reanudarAsignacion() {
    const motivo = prompt('Ingrese el motivo de la reanudación de la asignación comercial:');
    if (motivo === null) return;

    enviarAccion(`/api/mercados/asignaciones/${asignacionId}/reanudar?motivo=${encodeURIComponent(motivo)}`)
        .then(() => {
            alert('Asignación reanudada exitosamente.');
            window.location.reload();
        })
        .catch(err => alert(err.message));
}

document.addEventListener('DOMContentLoaded', function () {

    // Toggle de sección de devolución en modal finalización
    const chkDev = document.getElementById('chkDevolucionInmediata');
    const extraDev = document.getElementById('seccionDevolucionExtra');
    if (chkDev && extraDev) {
        chkDev.addEventListener('change', function () {
            if (this.checked) {
                extraDev.classList.remove('hidden');
            } else {
                extraDev.classList.add('hidden');
            }
        });
    }

    // Modal Evaluación
    const formEval = document.getElementById('formEvaluacion');
    if (formEval) {
        formEval.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                resultado: document.getElementById('evalResultado').value,
                motivo: document.getElementById('evalMotivo').value,
                observaciones: document.getElementById('evalObservaciones').value
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/evaluar`, 'POST', body)
                .then(() => {
                    alert('Evaluación técnica registrada exitosamente.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }

    // Modal Observación
    const formObs = document.getElementById('formObservacion');
    if (formObs) {
        formObs.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                detalleObservacion: document.getElementById('obsDetalle').value
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/observar`, 'POST', body)
                .then(() => {
                    alert('Observación administrativa formulada exitosamente.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }

    // Modal Aprobación
    const formAprob = document.getElementById('formAprobacion');
    if (formAprob) {
        formAprob.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                sustentoAprobacion: document.getElementById('aprobSustento').value,
                fechaInicioVigencia: document.getElementById('aprobFechaInicio').value || null,
                fechaFinVigencia: document.getElementById('aprobFechaFin').value || null,
                activarInmediatamente: document.getElementById('activarInmediatamente').checked
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/aprobar`, 'POST', body)
                .then(() => {
                    alert('Asignación aprobada exitosamente.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }

    // Modal Rechazo
    const formRech = document.getElementById('formRechazo');
    if (formRech) {
        formRech.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                motivoRechazo: document.getElementById('rechazoMotivo').value
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/rechazar`, 'POST', body)
                .then(() => {
                    alert('Solicitud denegada y rechazada.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }

    // Modal Suspensión
    const formSusp = document.getElementById('formSuspension');
    if (formSusp) {
        formSusp.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                motivoSuspension: document.getElementById('suspensionMotivo').value,
                mantieneReservaPuesto: document.getElementById('mantieneReservaPuesto').checked
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/suspender`, 'POST', body)
                .then(() => {
                    alert('Asignación suspendida cautelarmente.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }

    // Modal Finalización
    const formFin = document.getElementById('formFinalizacion');
    if (formFin) {
        formFin.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                motivoFinalizacion: document.getElementById('finMotivo').value,
                fechaEfectivaCierre: document.getElementById('finFecha').value || null,
                registrarDevolucionInmediata: document.getElementById('chkDevolucionInmediata').checked,
                estadoFisicoPuesto: document.getElementById('devEstadoFisicoFin')?.value || 'BUENO'
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/finalizar`, 'POST', body)
                .then(() => {
                    alert('Asignación finalizada.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }

    // Modal Devolución
    const formDev = document.getElementById('formDevolucion');
    if (formDev) {
        formDev.addEventListener('submit', function (e) {
            e.preventDefault();
            const body = {
                estadoFisicoPuesto: document.getElementById('devEstadoFisico').value,
                observaciones: document.getElementById('devObservaciones').value
            };
            enviarAccion(`/api/mercados/asignaciones/${asignacionId}/devolucion`, 'POST', body)
                .then(() => {
                    alert('Devolución física registrada exitosamente. Puesto liberado.');
                    window.location.reload();
                })
                .catch(err => alert(err.message));
        });
    }
});
