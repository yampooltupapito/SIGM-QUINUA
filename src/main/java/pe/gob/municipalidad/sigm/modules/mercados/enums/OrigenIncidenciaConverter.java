package pe.gob.municipalidad.sigm.modules.mercados.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convertidor JPA resiliente para OrigenIncidencia.
 * Garantiza que cadenas inesperadas o en desuso en BD no interrumpan la carga de entidades.
 */
@Slf4j
@Converter(autoApply = true)
public class OrigenIncidenciaConverter implements AttributeConverter<OrigenIncidencia, String> {

    @Override
    public String convertToDatabaseColumn(OrigenIncidencia attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public OrigenIncidencia convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return OrigenIncidencia.OTRO;
        }
        try {
            return OrigenIncidencia.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Origen de incidencia desconocido '{}' en BD, mapeando a OTRO", dbData);
            return OrigenIncidencia.OTRO;
        }
    }
}
