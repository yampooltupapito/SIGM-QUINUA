package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum EstadoDocumentoSeguridad {
    REGISTRADO("Registrado", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    EN_REVISION("En Revisión", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    VALIDADO_ADMINISTRATIVAMENTE("Validado Administrativamente", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    OBSERVADO("Observado", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    RESTRINGIDO("Restringido", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    ARCHIVADO("Archivado", "bg-slate-500/10 text-slate-400 border-slate-500/20");

    private final String descripcion;
    private final String badgeClase;

    EstadoDocumentoSeguridad(String descripcion, String badgeClase) {
        this.descripcion = descripcion;
        this.badgeClase = badgeClase;
    }
}
