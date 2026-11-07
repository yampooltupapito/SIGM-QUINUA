package pe.gob.municipalidad.sigm.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /verify debe renderizar la vista 'pages/verification' con los atributos del modelo")
    void verifyPageShouldRenderProperly() throws Exception {
        mockMvc.perform(get("/verify"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/verification"))
                .andExpect(model().attributeExists("appName", "appFullName", "municipality", "modules"))
                .andExpect(model().attribute("modules", hasSize(10)))
                .andExpect(content().string(containsString("SIGM-QUINUA")))
                .andExpect(content().string(containsString("Municipalidad Distrital de Quinua")));
    }
}
