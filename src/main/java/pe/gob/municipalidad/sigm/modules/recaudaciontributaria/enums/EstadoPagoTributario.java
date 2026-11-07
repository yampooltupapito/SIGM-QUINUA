package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoPagoTributario {
    BORRADOR("Borrador", "bg-slate-500/10 text-slate-400 border-slate-500/20"),
    PENDIENTE_VERIFICACION("Pendiente de Verificación Bancaria", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    CONFIRMADO("Confirmado", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    REGISTRADO("Registrado en Ventanilla", "bg-blue-500/10 text-blue-400 border-blue-500/20"),
    PROCESADO("Procesado y Aplicado", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    RECHAZADO("Rechazado", "bg-orange-500/10 text-orange-400 border-orange-500/20"),
    ANULADO("Anulado", "bg-rose-500/10 text-rose-400 border-rose-500/20"),
    REVERTIDO("Revertido Financieramente", "bg-purple-500/10 text-purple-400 border-purple-500/20");

    private final String descripcion;
    private final String badgeClass;
}
