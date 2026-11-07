package pe.gob.municipalidad.sigm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LombokTestModel {

    private Long id;
    private String nombre;

    public void logDemo() {
        log.info("Lombok Slf4j funcionando correctamente para: {}", nombre);
    }
}
