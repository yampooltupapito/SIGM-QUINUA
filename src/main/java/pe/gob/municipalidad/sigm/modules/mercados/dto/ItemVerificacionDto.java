package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVerificacionDto {

    private UUID id;
    private String codigo;
    private String categoria;
    private String descripcion;
    private int orden;
    private boolean obligatorio;
    private String referenciaNormativa;
    private String estado;
}
