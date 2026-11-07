package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

/**
 * Vinculación o personería de quien recibe materialmente el documento.
 */
@Getter
public enum TipoVinculoReceptor {
    TITULAR_CONTRIBUYENTE("Titular Contribuyente", "Persona natural obligada tributariamente"),
    REPRESENTANTE_LEGAL("Representante Legal", "Apoderado o gerente acreditado en el expediente"),
    FAMILIAR_CONVIVIENTE("Familiar / Conviviente", "Familiar mayor de edad con domicilio en el mismo predio"),
    EMPLEADO_TRABAJADOR("Empleado / Trabajador", "Dependiente laboral en el establecimiento o domicilio comercial"),
    VECINO_RESPONSABLE("Vecino", "Vecino colindante que suscribe acta de negativa o testigo"),
    CONSERJE_PORTERO("Conserje / Vigilante", "Encargado de recepción o vigilancia de inmueble/condominio"),
    OTRO("Otro", "Tercero identificado en el acta de notificación");

    private final String etiqueta;
    private final String descripcion;

    TipoVinculoReceptor(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
}

