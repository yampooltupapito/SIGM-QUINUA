package pe.gob.municipalidad.sigm.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "El nombre de usuario o correo electrónico es obligatorio.")
    @Size(min = 3, max = 150, message = "El identificador debe tener entre 3 y 150 caracteres.")
    private String identificador;
}
