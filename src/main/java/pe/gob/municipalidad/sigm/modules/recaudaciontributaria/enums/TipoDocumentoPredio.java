package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de documentos y títulos de sustento registrados para el predio.
 */
@Getter
@RequiredArgsConstructor
public enum TipoDocumentoPredio {
    TITULO_PROPIEDAD("Título de Propiedad Registral (SUNARP)"),
    CONTRATO_COMPRAVENTA("Escritura Pública / Contrato de Compraventa"),
    CONSTANCIA_POSESION("Constancia de Posesión Municipal / Comunal"),
    PLANO_CATASTRAL("Plano Catastral / Memoria Descriptiva"),
    DECLARACION_JURADA("Declaración Jurada de Autoavalúo (HR/PU)"),
    CERTIFICADO_JURISDICCION("Certificado de Jurisdicción"),
    FOTOGRAFIA("Fotografía de Fachada / Inspección"),
    OTRO("Otro Documento Sustentatorio");

    private final String descripcion;

    public String getNombre() {
        return descripcion;
    }
}
