package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionAsignarRecursosRequest {

    private UUID unidadMovilId;

    private UUID responsableOperativoId;

    @Builder.Default
    private List<UUID> personalIds = new ArrayList<>();

    @Builder.Default
    private List<FuncionOperativaPersonal> funciones = new ArrayList<>();

    @Builder.Default
    private List<UUID> equipamientoIds = new ArrayList<>();

    private CanalComunicacionDespacho canalComunicacion;

    @Size(max = 2000, message = "Las instrucciones no pueden superar los 2000 caracteres")
    private String instruccionesDespacho;

    @Builder.Default
    private Boolean despachar = false;
}
