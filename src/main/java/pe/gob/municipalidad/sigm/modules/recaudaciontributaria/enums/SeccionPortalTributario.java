package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Secciones del Portal Tributario Ciudadano para auditoría y navegación segura.
 */
@Getter
@RequiredArgsConstructor
public enum SeccionPortalTributario {
    DASHBOARD("Panel Principal", "dashboard"),
    PERFIL("Perfil Tributario", "user"),
    VINCULACIONES("Gestión de Vinculaciones", "link-2"),
    PREDIOS("Mis Predios", "building-2"),
    OBLIGACIONES("Mis Obligaciones", "file-text"),
    IMPUESTO_PREDIAL("Impuesto Predial", "home"),
    ARBITRIOS("Arbitrios Municipales", "trees"),
    CUENTA_CORRIENTE("Mi Cuenta Corriente", "wallet"),
    PAGOS("Mis Pagos Confirmados", "credit-card"),
    RECIBOS("Mis Recibos y Comprobantes", "receipt"),
    DEUDAS("Mis Deudas", "alert-circle"),
    FRACCIONAMIENTOS("Mis Fraccionamientos", "calendar-check"),
    NOTIFICACIONES("Bandeja de Notificaciones", "inbox");

    private final String titulo;
    private final String icono;
}

