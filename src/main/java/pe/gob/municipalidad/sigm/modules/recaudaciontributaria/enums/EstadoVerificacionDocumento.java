package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado de verificación de la identidad o documento del contribuyente.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoVerificacionDocumento {
    NO_VERIFICADO("No Verificado", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    VALIDADO_FORMATO("Validado por Formato Local", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    VERIFICADO_DOCUMENTALMENTE("Verificado Documentalmente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    VERIFICADO_EXTERNO("Verificado vía Servicio Oficial", "bg-teal-500/10 text-teal-400 border-teal-500/20");

    private final String descripcion;
    private final String badgeClass;
}
