package pe.gob.municipalidad.sigm.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rutas públicas (/ping, /verify, /css/**) deben responder sin autenticación")
    void publicRoutesShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/verify"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/css/tailwind.min.css"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rutas protegidas no públicas deben requerir autenticación (401 Unauthorized)")
    void protectedRoutesShouldDenyAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }
}
