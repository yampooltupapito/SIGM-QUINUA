// SIGM-QUINUA: Control de pestañas y emisión de alertas de vencimiento
function mostrarPestana(tabId) {
    document.getElementById('tab-proximos').classList.add('hidden');
    document.getElementById('tab-vencidos').classList.add('hidden');

    document.getElementById('btn-tab-proximos').classList.remove('bg-blue-600', 'text-white', 'font-bold');
    document.getElementById('btn-tab-proximos').classList.add('text-slate-400');

    document.getElementById('btn-tab-vencidos').classList.remove('bg-blue-600', 'text-white', 'font-bold');
    document.getElementById('btn-tab-vencidos').classList.add('text-slate-400');

    document.getElementById(tabId).classList.remove('hidden');
    const activeBtn = tabId === 'tab-proximos' ? document.getElementById('btn-tab-proximos') : document.getElementById('btn-tab-vencidos');
    activeBtn.classList.remove('text-slate-400');
    activeBtn.classList.add('bg-blue-600', 'text-white', 'font-bold');
}

async function emitirAlertasVencimiento() {
    if (!confirm('¿Desea escanear y enviar alertas internas de vencimiento al Centro de Notificaciones?')) return;

    try {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const headers = { 'Content-Type': 'application/json' };
        if (token && header) headers[header] = token;

        const res = await fetch('/api/mercados/documentos/alertas/emitir', {
            method: 'POST',
            headers: headers
        });

        if (!res.ok) throw new Error('Error al emitir alertas de vencimiento.');

        const data = await res.json();
        alert(data.mensaje + ' Total enviadas: ' + data.totalEmitidas);
    } catch (e) {
        alert(e.message);
    }
}
