package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoJsonFeatureCollectionResponse {

    @Builder.Default
    private String type = "FeatureCollection";

    @Builder.Default
    private List<GeoJsonFeature> features = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoJsonFeature {
        @Builder.Default
        private String type = "Feature";
        private String id;
        private Map<String, Object> properties;
        private Object geometry;
    }
}
