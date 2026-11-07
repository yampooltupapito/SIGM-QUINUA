package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoPredio {
    URBANO("Predio Urbano", "Inmuebles en zona urbana del distrito de Quinua"),
    RURAL("Predio Rústico / Rural", "Predios agrícolas, pastizales o eriazos en el ámbito distrital");

    private final String descripcion;
    private final String detalle;
}
