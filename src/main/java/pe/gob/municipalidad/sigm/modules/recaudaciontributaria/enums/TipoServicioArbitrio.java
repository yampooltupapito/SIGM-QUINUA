package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum TipoServicioArbitrio {
    RECOLECCION_RESIDUOS("Recolección de Residuos Sólidos"),
    BARRIDO_CALLES("Barrido de Calles"),
    PARQUES_JARDINES("Parques y Jardines Públicos"),
    SERENAZGO("Seguridad Ciudadana y Serenazgo"),
    OTROS("Otros Servicios Públicos");

    private final String descripcion;

    TipoServicioArbitrio(String descripcion) {
        this.descripcion = descripcion;
    }
}
