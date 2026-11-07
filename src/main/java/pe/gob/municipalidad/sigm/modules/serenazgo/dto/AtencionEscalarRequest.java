package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EntidadEscalamiento;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionEscalarRequest {

    @NotNull(message = "La entidad destinataria es obligatoria")
    private EntidadEscalamiento entidadDestinataria;

    @NotBlank(message = "El motivo del escalamiento institucional es obligatorio")
    @Size(max = 2000, message = "El motivo no puede superar los 2000 caracteres")
    private String motivo;

    @Size(max = 50, message = "El canal no puede superar los 50 caracteres")
    @Builder.Default
    private String canalComunicacion = "TELEFONO_DIRECTO";

    @Size(max = 100, message = "La referencia no puede superar los 100 caracteres")
    private String referenciaComunicacion;

    @Size(max = 150, message = "El contacto no puede superar los 150 caracteres")
    private String contactoNombre;

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;

    public EntidadEscalamiento getEntidad() {
        return entidadDestinataria;
    }

    public void setEntidad(EntidadEscalamiento e) {
        this.entidadDestinataria = e;
    }

    public void setCanalComunicacion(pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho canal) {
        this.canalComunicacion = canal != null ? canal.name() : null;
    }

    public String getContactoReceptor() {
        return contactoNombre;
    }

    public void setContactoReceptor(String c) {
        this.contactoNombre = c;
    }

    public String getNumeroReferencia() {
        return referenciaComunicacion;
    }

    public void setNumeroReferencia(String n) {
        this.referenciaComunicacion = n;
    }
}
