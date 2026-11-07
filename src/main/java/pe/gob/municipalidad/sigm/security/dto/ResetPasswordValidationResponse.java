package pe.gob.municipalidad.sigm.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordValidationResponse {
    private boolean valid;
    private String username;
    private String message;
}
