package pe.gob.municipalidad.sigm.web.advice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test-api")
    static class TestController {

        record TestDto(@NotBlank(message = "El nombre no puede estar vacío") String nombre) {}

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Predio municipal con código 999 no existe.");
        }

        @GetMapping("/duplicate")
        public void throwDuplicate() {
            throw new DuplicateResourceException("El expediente con número EXP-2026-001 ya está registrado.");
        }

        @GetMapping("/conflict")
        public void throwConflict() {
            throw new ConflictException("La licencia comercial no puede cancelarse en su estado actual.");
        }

        @GetMapping("/invalid-request")
        public void throwInvalidRequest() {
            throw new InvalidRequestException("La fecha de inicio no puede ser posterior a la fecha final.");
        }

        @GetMapping("/business-rule")
        public void throwBusinessRule() {
            throw new BusinessException("BUSINESS_001", "El administrado posee deudas coactivas pendientes.");
        }

        @GetMapping("/unexpected")
        public void throwUnexpected() {
            throw new RuntimeException("Error crítico interno de base de datos SQL connection timeout password=secret123");
        }

        @PostMapping("/validate")
        public String validateBody(@Valid @RequestBody TestDto dto) {
            return "OK: " + dto.nombre();
        }
    }

    @BeforeEach
    void setUp() {
        TraceContext.setTraceId("trace-global-test-1");
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException debe retornar 404 con estructura de error y traceId")
    void handleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test-api/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Predio municipal con código 999 no existe."))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("DuplicateResourceException debe retornar 409 Conflict")
    void handleDuplicateResourceException() throws Exception {
        mockMvc.perform(get("/test-api/duplicate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("El expediente con número EXP-2026-001 ya está registrado."));
    }

    @Test
    @DisplayName("ConflictException debe retornar 409 Conflict")
    void handleConflictException() throws Exception {
        mockMvc.perform(get("/test-api/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("La licencia comercial no puede cancelarse en su estado actual."));
    }

    @Test
    @DisplayName("InvalidRequestException debe retornar 400 Bad Request")
    void handleInvalidRequestException() throws Exception {
        mockMvc.perform(get("/test-api/invalid-request").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("La fecha de inicio no puede ser posterior a la fecha final."));
    }

    @Test
    @DisplayName("BusinessException debe retornar 422 Unprocessable Entity con código de negocio")
    void handleBusinessException() throws Exception {
        mockMvc.perform(get("/test-api/business-rule").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_001"))
                .andExpect(jsonPath("$.message").value("El administrado posee deudas coactivas pendientes."));
    }

    @Test
    @DisplayName("Validación fallida en DTO debe retornar 400 con mapa de errores por campo")
    void handleValidationException() throws Exception {
        mockMvc.perform(post("/test-api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Los datos enviados no son válidos"))
                .andExpect(jsonPath("$.errors.nombre").value("El nombre no puede estar vacío"));
    }

    @Test
    @DisplayName("JSON mal formado debe retornar 400 con mensaje seguro")
    void handleMalformedJson() throws Exception {
        mockMvc.perform(post("/test-api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nombre: invalid-json-syntax")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("formato JSON válido")));
    }

    @Test
    @DisplayName("Error inesperado en servidor debe responder 500 y no filtrar stack traces ni datos confidenciales")
    void handleUnexpectedExceptionProtectsSecrets() throws Exception {
        mockMvc.perform(get("/test-api/unexpected").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Ocurrió un error inesperado")))
                .andExpect(jsonPath("$.message").value(not(containsString("password=secret123"))))
                .andExpect(jsonPath("$.message").value(not(containsString("SQL"))))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}
