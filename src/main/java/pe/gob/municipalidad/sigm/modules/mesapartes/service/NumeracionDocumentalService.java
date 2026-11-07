package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.common.model.Oficina;

public interface NumeracionDocumentalService {

    /**
     * Genera un número correlativo oficial de documento de forma atómica y concurrente.
     * Ejemplo: "RESOLUCION-0001-2026-MDQ/SG-GDUR" o "OFICIO-0015-2026-MDQ/ALC"
     */
    String generarNumeroDocumento(String tipoDocumento, Long oficinaId, Integer anio);
}
