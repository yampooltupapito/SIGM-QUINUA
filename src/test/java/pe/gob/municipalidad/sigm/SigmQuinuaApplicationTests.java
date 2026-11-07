package pe.gob.municipalidad.sigm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class SigmQuinuaApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void lombokAnnotationsWorkCorrectly() {
        LombokTestModel model = LombokTestModel.builder()
                .id(1L)
                .nombre("Municipalidad Distrital de Quinua")
                .build();

        org.junit.jupiter.api.Assertions.assertEquals(1L, model.getId());
        org.junit.jupiter.api.Assertions.assertEquals("Municipalidad Distrital de Quinua", model.getNombre());
        model.setNombre("Quinua Histórica");
        org.junit.jupiter.api.Assertions.assertEquals("Quinua Histórica", model.getNombre());
        model.logDemo();
    }

}
