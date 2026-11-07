package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado del flujo de seguimiento administrativo de una deuda tributaria.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoSeguimientoDeuda {
    EN_SEGUIMIENTO("En Seguimiento Activo", "bg-sky-500/10 text-sky-400 border-sky-500/20"),
    ASIGNADO("Asignado a Responsable", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    COMPROMISO_PENDIENTE("Con Compromiso de Pago Pendiente", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    CONCLUIDO("Seguimiento Concluido", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    DERIVADO("Derivado a Otra Área", "bg-indigo-500/10 text-indigo-400 border-indigo-500/20"),
    OBSERVADO("Observado / En Espera", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String descripcion;
    private final String badgeClass;
}
