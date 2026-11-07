package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarioEventoResponse {

    private UUID id;
    private String codigo;
    private String title;
    private String start; // ISO-8601 string para FullCalendar / JS
    private String end;   // ISO-8601 string
    private String estado;
    private String estadoDesc;
    private String color;
    private String sectorNombre;
    private String zonaNombre;
    private String turnoNombre;
    private Integer personalCount;
    private Integer unidadesCount;
    private String responsableNombre;
}
