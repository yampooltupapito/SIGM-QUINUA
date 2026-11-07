package pe.gob.municipalidad.sigm.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.gob.municipalidad.sigm.common.pagination.PagedResponse;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    @DisplayName("ApiResponse.ok debe retornar HTTP 200 con éxito, mensaje, timestamp y traceId")
    void apiResponseOkShouldReturn200WithMetadata() {
        TraceContext.setTraceId("trace-test-123");
        try {
            ResponseEntity<ApiResponse<String>> response = ApiResponse.ok("Operación exitosa", "Datos de prueba");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Operación exitosa", response.getBody().getMessage());
            assertEquals("Datos de prueba", response.getBody().getData());
            assertEquals("trace-test-123", response.getBody().getTraceId());
            assertNotNull(response.getBody().getTimestamp());
        } finally {
            TraceContext.clear();
        }
    }

    @Test
    @DisplayName("ApiResponse.created debe retornar HTTP 201 Created con cuerpo")
    void apiResponseCreatedShouldReturn201WithBody() {
        ResponseEntity<ApiResponse<Integer>> response = ApiResponse.created("Recurso registrado", 101);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Recurso registrado", response.getBody().getMessage());
        assertEquals(101, response.getBody().getData());
    }

    @Test
    @DisplayName("ApiResponse.noContent debe retornar HTTP 204 sin cuerpo")
    void apiResponseNoContentShouldReturn204WithoutBody() {
        ResponseEntity<Void> response = ApiResponse.noContent();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertFalse(response.hasBody());
    }

    @Test
    @DisplayName("ApiResponse.paginated debe envolver PagedResponse con HTTP 200")
    void apiResponsePaginatedShouldWrapPagedResponse() {
        PagedResponse<String> paged = PagedResponse.<String>builder()
                .content(List.of("Elemento 1", "Elemento 2"))
                .page(0)
                .size(10)
                .totalElements(2)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        ResponseEntity<ApiResponse<PagedResponse<String>>> response = ApiResponse.paginated(paged);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().getContent().size());
        assertEquals(1, response.getBody().getData().getTotalPages());
    }
}
