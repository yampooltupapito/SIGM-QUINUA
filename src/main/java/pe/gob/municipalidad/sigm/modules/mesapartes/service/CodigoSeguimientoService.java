package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CodigoSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;

public interface CodigoSeguimientoService {

    CodigoSeguimiento generarOObtenerCodigo(Expediente expediente);

    CodigoSeguimiento validarCodigoSeguimiento(String codigo, String ipOrigen);

    void revocarCodigo(Long expedienteId, String motivo, String username);

    void rotarCodigo(Long expedienteId, String username);
}
