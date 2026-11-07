package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoAplicacionPago {
    ACTIVA("Activa / Imputada", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    REVERTIDA("Revertida / Anulada", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
