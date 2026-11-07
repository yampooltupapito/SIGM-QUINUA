package pe.gob.municipalidad.sigm.common.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationTest {

    private static final Set<String> ALLOWED_FIELDS = Set.of("id", "nombre", "fechaCreacion");

    @Test
    @DisplayName("Paginación con valores válidos debe generar Pageable correcto")
    void validPaginationRequestShouldProducePageable() {
        PaginationRequest request = PaginationRequest.builder()
                .page(1)
                .size(20)
                .sort("nombre")
                .direction("ASC")
                .build();

        Pageable pageable = request.toPageable(ALLOWED_FIELDS);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("nombre").isAscending());
    }

    @Test
    @DisplayName("Paginación con valores por defecto debe usar página 0 y tamaño 10")
    void defaultPaginationRequestShouldUseDefaults() {
        PaginationRequest request = new PaginationRequest();
        Pageable pageable = request.toPageable(ALLOWED_FIELDS);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    @DisplayName("Página negativa debe arrojar InvalidRequestException")
    void negativePageShouldThrowException() {
        PaginationRequest request = PaginationRequest.builder().page(-1).size(10).build();

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> request.toPageable(ALLOWED_FIELDS));
        assertTrue(ex.getMessage().contains("menor a 0"));
    }

    @Test
    @DisplayName("Tamaño menor a 1 o mayor a 100 debe arrojar InvalidRequestException")
    void invalidPageSizeShouldThrowException() {
        PaginationRequest sizeZero = PaginationRequest.builder().page(0).size(0).build();
        assertThrows(InvalidRequestException.class, () -> sizeZero.toPageable(ALLOWED_FIELDS));

        PaginationRequest sizeOver100 = PaginationRequest.builder().page(0).size(101).build();
        assertThrows(InvalidRequestException.class, () -> sizeOver100.toPageable(ALLOWED_FIELDS));
    }

    @Test
    @DisplayName("Campo de ordenamiento no autorizado debe arrojar InvalidRequestException")
    void unauthorizedSortFieldShouldThrowException() {
        PaginationRequest request = PaginationRequest.builder()
                .page(0)
                .size(10)
                .sort("password") // Intento de ordenar por campo sensible no autorizado
                .build();

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> request.toPageable(ALLOWED_FIELDS));
        assertTrue(ex.getMessage().contains("no está permitido"));
    }

    @Test
    @DisplayName("PagedResponse debe transformar Spring Data Page con metadatos completos")
    void pagedResponseFromSpringPage() {
        List<String> data = List.of("A", "B", "C");
        Page<String> springPage = new PageImpl<>(data, PageRequest.of(0, 3), 10);

        PagedResponse<String> response = PagedResponse.from(springPage);

        assertEquals(3, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(3, response.getSize());
        assertEquals(10, response.getTotalElements());
        assertEquals(4, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    @DisplayName("PagedResponse con función de mapeo debe transformar entidades a DTOs")
    void pagedResponseWithMapping() {
        List<Integer> numbers = List.of(1, 2, 3);
        Page<Integer> springPage = new PageImpl<>(numbers, PageRequest.of(0, 3), 3);

        PagedResponse<String> mappedResponse = PagedResponse.from(springPage, n -> "Item-" + n);

        assertEquals(List.of("Item-1", "Item-2", "Item-3"), mappedResponse.getContent());
    }
}
