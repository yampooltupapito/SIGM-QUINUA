/**
 * SIGM-QUINUA - Mesa de Partes Virtual
 * Módulo de Validaciones en Cliente (solicitud-validacion.js)
 */
(function(window) {
    'use strict';

    const SolicitudValidacion = {

        validarTexto: function(valor, min, max, obligatorio, nombreCampo) {
            if (obligatorio && (!valor || valor.trim() === '')) {
                return `El campo "${nombreCampo}" es obligatorio.`;
            }
            if (valor && min && valor.trim().length < min) {
                return `El campo "${nombreCampo}" debe tener al menos ${min} caracteres.`;
            }
            if (valor && max && valor.trim().length > max) {
                return `El campo "${nombreCampo}" no puede superar los ${max} caracteres.`;
            }
            return null;
        },

        validarNumero: function(valor, obligatorio, nombreCampo) {
            if (obligatorio && (!valor || valor.trim() === '')) {
                return `El campo "${nombreCampo}" es obligatorio.`;
            }
            if (valor && valor.trim() !== '') {
                const num = Number(valor.trim());
                if (isNaN(num)) {
                    return `El campo "${nombreCampo}" debe ser un valor numérico válido.`;
                }
            }
            return null;
        },

        validarFecha: function(valor, obligatorio, nombreCampo) {
            if (obligatorio && (!valor || valor.trim() === '')) {
                return `El campo "${nombreCampo}" es obligatorio.`;
            }
            if (valor && valor.trim() !== '') {
                const regex = /^\d{4}-\d{2}-\d{2}$/;
                if (!regex.test(valor.trim())) {
                    return `El campo "${nombreCampo}" debe tener formato AAAA-MM-DD.`;
                }
            }
            return null;
        },

        validarArchivo: function(file, formatosPermitidos, maxMb) {
            if (!file) {
                return 'No se ha seleccionado ningún archivo.';
            }

            const maxBytes = (maxMb || 15) * 1024 * 1024;
            if (file.size > maxBytes) {
                return `El archivo "${file.name}" supera el tamaño máximo permitido de ${maxMb || 15} MB.`;
            }

            if (formatosPermitidos && formatosPermitidos.length > 0) {
                const extension = '.' + file.name.split('.').pop().toLowerCase();
                const mime = file.type ? file.type.toLowerCase() : '';

                // Lista de formatos admitidos (mimes o extensiones)
                const admitidos = Array.isArray(formatosPermitidos) 
                    ? formatosPermitidos 
                    : formatosPermitidos.split(',').map(s => s.trim().toLowerCase());

                const coincideExt = admitidos.some(f => f.startsWith('.') && f === extension);
                const coincideMime = admitidos.some(f => !f.startsWith('.') && mime.includes(f.replace('application/', '').replace('image/', '')));

                if (!coincideExt && !coincideMime && !admitidos.includes(extension) && !admitidos.includes(mime)) {
                    // Verificación flexible de tipos comunes
                    const esPdf = extension === '.pdf' && admitidos.some(a => a.includes('pdf'));
                    const esImagen = (extension === '.jpg' || extension === '.jpeg' || extension === '.png') && admitidos.some(a => a.includes('image') || a.includes('jpeg') || a.includes('png') || a.includes('jpg'));
                    if (!esPdf && !esImagen) {
                        return `El formato del archivo "${file.name}" (${extension}) no está admitido. Formatos permitidos: ${admitidos.join(', ')}`;
                    }
                }
            }

            return null;
        },

        limpiarErrores: function(container) {
            const errores = container.querySelectorAll('.campo-error-msg');
            errores.forEach(el => el.remove());
            const inputs = container.querySelectorAll('.border-red-500');
            inputs.forEach(el => {
                el.classList.remove('border-red-500', 'bg-red-50');
                el.classList.add('border-slate-300');
            });
        },

        mostrarError: function(inputElement, mensaje) {
            if (!inputElement) return;
            inputElement.classList.remove('border-slate-300');
            inputElement.classList.add('border-red-500', 'bg-red-50');

            let err = inputElement.parentElement.querySelector('.campo-error-msg');
            if (!err) {
                err = document.createElement('p');
                err.className = 'campo-error-msg text-xs text-red-600 mt-1 font-medium flex items-center gap-1';
                inputElement.parentElement.appendChild(err);
            }
            err.innerHTML = `<svg class="w-3.5 h-3.5 inline-block flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg> <span>${mensaje}</span>`;
        }
    };

    window.SolicitudValidacion = SolicitudValidacion;
})(window);
