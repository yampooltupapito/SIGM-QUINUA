package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MedioPagoTributario {
    EFECTIVO_CAJA("Efectivo en Ventanilla / Caja Municipal"),
    TRANSFERENCIA_BANCARIA("Transferencia Interbancaria CCI"),
    DEPOSITO_CUENTA("Depósito en Cuenta Corriente Municipal"),
    TARJETA_DEBITO("Tarjeta de Débito (POS)"),
    TARJETA_CREDITO("Tarjeta de Crédito (POS)"),
    BILLETERA_DIGITAL("Billetera Digital (Yape / Plin Institucional)"),
    CHEQUE("Cheque de Gerencia Certificado"),
    OTRO("Otro Medio Autorizado");

    private final String descripcion;
}
