package pe.gob.municipalidad.sigm.common.constants;

/**
 * Constantes y límites de paginación para consultas de listas en SIGM-QUINUA.
 */
public final class PaginationConstants {

    private PaginationConstants() {
        // Utility class
    }

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;

    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
}
