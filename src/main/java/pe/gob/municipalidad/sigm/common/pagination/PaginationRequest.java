package pe.gob.municipalidad.sigm.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pe.gob.municipalidad.sigm.common.constants.PaginationConstants;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;

import java.util.Set;

/**
 * Objeto de transferencia para parámetros de paginación y ordenamiento seguro en SIGM-QUINUA.
 * Valida límites y restringe el ordenamiento únicamente a campos expresamente autorizados por cada módulo,
 * previniendo inyecciones de propiedades o accesos a columnas sensibles.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    @Builder.Default
    private int page = PaginationConstants.DEFAULT_PAGE_NUMBER;

    @Builder.Default
    private int size = PaginationConstants.DEFAULT_PAGE_SIZE;

    @Builder.Default
    private String sort = PaginationConstants.DEFAULT_SORT_FIELD;

    @Builder.Default
    private String direction = PaginationConstants.DEFAULT_SORT_DIRECTION;

    /**
     * Construye un Pageable verificando que los límites y el campo de ordenamiento sean válidos y seguros.
     *
     * @param allowedSortFields Conjunto de nombres de propiedades permitidas para ordenar.
     * @return Pageable configurado de Spring Data.
     * @throws InvalidRequestException si la página, tamaño o campo de ordenamiento no cumplen las reglas.
     */
    public Pageable toPageable(Set<String> allowedSortFields) {
        if (page < 0) {
            throw new InvalidRequestException("El número de página no puede ser menor a 0.");
        }

        if (size < PaginationConstants.MIN_PAGE_SIZE || size > PaginationConstants.MAX_PAGE_SIZE) {
            throw new InvalidRequestException(String.format(
                    "El tamaño de página debe estar comprendido entre %d y %d registros.",
                    PaginationConstants.MIN_PAGE_SIZE, PaginationConstants.MAX_PAGE_SIZE));
        }

        if (sort == null || sort.trim().isEmpty()) {
            sort = PaginationConstants.DEFAULT_SORT_FIELD;
        }

        String normalizedSort = sort.trim();
        if (allowedSortFields != null && !allowedSortFields.isEmpty()) {
            boolean isAllowed = allowedSortFields.stream()
                    .anyMatch(field -> field.equalsIgnoreCase(normalizedSort));
            if (!isAllowed) {
                throw new InvalidRequestException(String.format(
                        "El campo de ordenamiento '%s' no está permitido. Campos válidos: %s",
                        normalizedSort, allowedSortFields));
            }
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = (direction != null && direction.trim().equalsIgnoreCase("ASC"))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
        } catch (Exception e) {
            sortDirection = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(sortDirection, normalizedSort));
    }
}
