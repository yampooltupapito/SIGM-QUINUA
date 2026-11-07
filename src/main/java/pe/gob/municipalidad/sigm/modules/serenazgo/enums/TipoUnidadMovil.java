package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoUnidadMovil {
    CAMIONETA("Camioneta Operativa"),
    MOTOCICLETA("Motocicleta de Patrullaje"),
    BICICLETA("Bicicleta de Proximidad"),
    VEHICULO_APOYO("Vehículo de Apoyo Logístico"),
    OTRO("Otro Vehículo Habilitado");

    private final String descripcion;

    TipoUnidadMovil(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
