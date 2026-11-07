/**
 * SIGM-QUINUA - Mesa de Partes Virtual
 * Módulo de Carga y Gestión de Documentos (solicitud-documentos.js)
 */
(function(window) {
    'use strict';

    const SolicitudDocumentos = {
        documentosAdjuntos: [],

        renderizarLista: function(containerId, onEliminarCallback) {
            const container = document.getElementById(containerId);
            if (!container) return;

            if (this.documentosAdjuntos.length === 0) {
                container.innerHTML = `
                    <div class="py-8 text-center text-slate-400 border-2 border-dashed border-slate-200 rounded-xl bg-slate-50/50">
                        <i data-lucide="file-x" class="w-8 h-8 mx-auto mb-2 text-slate-300"></i>
                        <p class="text-xs">No ha adjuntado ningún documento aún.</p>
                        <p class="text-[11px] text-slate-400 mt-0.5">Seleccione los archivos requeridos según el tipo de trámite.</p>
                    </div>
                `;
                if (window.lucide) window.lucide.createIcons();
                return;
            }

            let html = `
                <div class="overflow-hidden border border-slate-200 rounded-xl bg-white shadow-sm">
                    <table class="min-w-full divide-y divide-slate-200 text-xs">
                        <thead class="bg-slate-50 text-slate-600 font-semibold">
                            <tr>
                                <th class="px-3 py-2.5 text-left">Archivo</th>
                                <th class="px-3 py-2.5 text-left">Tipo</th>
                                <th class="px-3 py-2.5 text-left">Tamaño</th>
                                <th class="px-3 py-2.5 text-center">Acción</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
            `;

            this.documentosAdjuntos.forEach(doc => {
                const kb = doc.tamanoBytes ? Math.round(doc.tamanoBytes / 1024) : 0;
                const tamanoStr = kb > 1024 ? (kb / 1024).toFixed(1) + ' MB' : kb + ' KB';
                const docUuid = doc.documentoUuid || doc.uuid;

                html += `
                    <tr class="hover:bg-slate-50 transition-colors">
                        <td class="px-3 py-2.5 font-medium text-slate-800 flex items-center gap-2">
                            <i data-lucide="file-check" class="w-4 h-4 text-emerald-600 flex-shrink-0"></i>
                            <span class="truncate max-w-xs" title="${doc.nombreOriginal || doc.nombreArchivo}">
                                ${doc.nombreOriginal || doc.nombreArchivo}
                            </span>
                        </td>
                        <td class="px-3 py-2.5 text-slate-500">
                            <span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-50 text-blue-700 border border-blue-200">
                                ${doc.tipoRelacion || 'ANEXO'}
                            </span>
                        </td>
                        <td class="px-3 py-2.5 text-slate-500 font-mono text-[11px]">${tamanoStr}</td>
                        <td class="px-3 py-2.5 text-center">
                            <button type="button" data-doc-uuid="${docUuid}" class="btn-eliminar-doc text-red-500 hover:text-red-700 p-1 rounded hover:bg-red-50 transition-colors" title="Eliminar archivo">
                                <i data-lucide="trash-2" class="w-4 h-4"></i>
                            </button>
                        </td>
                    </tr>
                `;
            });

            html += `
                        </tbody>
                    </table>
                </div>
            `;

            container.innerHTML = html;
            if (window.lucide) window.lucide.createIcons();

            container.querySelectorAll('.btn-eliminar-doc').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const uuid = btn.getAttribute('data-doc-uuid');
                    if (onEliminarCallback) onEliminarCallback(uuid);
                });
            });
        },

        subirArchivo: async function(expedienteUuid, file, tipoRelacion) {
            const formData = new FormData();
            formData.append('archivo', file);
            formData.append('tipoRelacion', tipoRelacion || 'ANEXO');

            const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

            const headers = {};
            if (token) headers[header] = token;

            const response = await fetch(`/api/mesa-partes/expedientes/${expedienteUuid}/documentos/subir`, {
                method: 'POST',
                headers: headers,
                body: formData
            });

            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.message || 'Error al subir el archivo adjunto.');
            }

            return await response.json();
        },

        eliminarArchivo: async function(expedienteUuid, documentoUuid) {
            const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

            const headers = {};
            if (token) headers[header] = token;

            const response = await fetch(`/api/mesa-partes/expedientes/${expedienteUuid}/documentos/archivo/${documentoUuid}`, {
                method: 'DELETE',
                headers: headers
            });

            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.message || 'Error al eliminar el archivo.');
            }

            return await response.json();
        }
    };

    window.SolicitudDocumentos = SolicitudDocumentos;
})(window);
