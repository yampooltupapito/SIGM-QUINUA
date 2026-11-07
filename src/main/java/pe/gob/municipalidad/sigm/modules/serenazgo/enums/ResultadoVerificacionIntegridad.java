package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;

@Getter
public enum ResultadoVerificacionIntegridad {
    COINCIDENTE("Integridad Coincidente (SHA-256 Verificado)", "El hash calculado coincide exactamente con la huella digital registrada", "check-circle", "text-emerald-400 bg-emerald-500/10 border-emerald-500/20"),
    NO_COINCIDENTE("Integridad No Coincidente (Posible Alteración)", "El hash calculado difiere del registrado en la base de datos", "alert-triangle", "text-rose-400 bg-rose-500/10 border-rose-500/20"),
    ARCHIVO_NO_DISPONIBLE("Archivo Físico No Disponible", "El archivo no se encuentra en el repositorio de almacenamiento privado", "file-x", "text-amber-400 bg-amber-500/10 border-amber-500/20"),
    ERROR_LECTURA("Error de Lectura / Acceso", "Ocurrió un error de E/S o permisos al acceder al archivo", "shield-alert", "text-rose-400 bg-rose-500/10 border-rose-500/20"),
    NO_VERIFICADO("Verificación Pendiente", "No se ha ejecutado una comprobación posterior al registro inicial", "help-circle", "text-slate-400 bg-slate-500/10 border-slate-500/20");

    private final String descripcion;
    private final String detalle;
    private final String icono;
    private final String badgeClase;

    ResultadoVerificacionIntegridad(String descripcion, String detalle, String icono, String badgeClase) {
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.icono = icono;
        this.badgeClase = badgeClase;
    }
}
