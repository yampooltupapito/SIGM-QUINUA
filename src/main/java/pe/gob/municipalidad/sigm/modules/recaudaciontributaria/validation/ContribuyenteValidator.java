package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ContribuyenteCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ContribuyenteUpdateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.DuplicateCheckResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVerificacionDocumento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validador especializado para el subsistema de contribuyentes tributarios municipales.
 * Implementa validaciones matemáticas oficiales para DNI y RUC (Módulo 11),
 * detección de duplicados estrictos y coincidencias fonéticas/nominales de advertencia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContribuyenteValidator {

    private static final Pattern PATTERN_DNI = Pattern.compile("^[0-9]{8}$");
    private static final Pattern PATTERN_RUC = Pattern.compile("^(10|15|16|17|20)[0-9]{9}$");
    private static final Pattern PATTERN_EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PATTERN_PHONE = Pattern.compile("^[0-9+ -]{7,20}$");
    private static final int[] RUC_FACTORES = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private final ContribuyenteTributarioRepository contribuyenteRepository;

    /**
     * Valida sintácticamente un DNI peruano de 8 dígitos numéricos.
     */
    public boolean validarFormatoDni(String dni) {
        if (dni == null) return false;
        return PATTERN_DNI.matcher(dni.trim()).matches();
    }

    /**
     * Valida la estructura y el dígito verificador oficial de SUNAT para RUC (Módulo 11).
     */
    public boolean validarFormatoRuc(String ruc) {
        if (ruc == null) return false;
        String clean = ruc.trim();
        if (!PATTERN_RUC.matcher(clean).matches()) {
            return false;
        }

        int suma = 0;
        for (int i = 0; i < 10; i++) {
            int digito = Character.getNumericValue(clean.charAt(i));
            suma += digito * RUC_FACTORES[i];
        }

        int residuo = suma % 11;
        int complemento = 11 - residuo;
        int digitoVerificadorEsperado;

        if (complemento == 10) {
            digitoVerificadorEsperado = 0;
        } else if (complemento == 11) {
            digitoVerificadorEsperado = 1;
        } else {
            digitoVerificadorEsperado = complemento;
        }

        int digitoReal = Character.getNumericValue(clean.charAt(10));
        return digitoReal == digitoVerificadorEsperado;
    }

    /**
     * Valida la solicitud de creación de un contribuyente según sea Persona Natural o Jurídica.
     */
    public EstadoVerificacionDocumento validarCreacion(ContribuyenteCreateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de registro no puede ser nula");
        }

        String tipoDoc = request.getTipoDocumento() != null ? request.getTipoDocumento().trim().toUpperCase() : "";
        String numDoc = request.getNumeroDocumento() != null ? request.getNumeroDocumento().trim() : "";

        if (tipoDoc.isEmpty() || numDoc.isEmpty()) {
            throw new BusinessException("El tipo y número de documento son obligatorios.");
        }

        // 1. Verificación de duplicado estricto
        if (contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumento(tipoDoc, numDoc)) {
            throw new DuplicateResourceException("Ya existe un contribuyente registrado con " + tipoDoc + " N° " + numDoc);
        }

        // 2. Validación según tipo de documento
        EstadoVerificacionDocumento estadoVerif = EstadoVerificacionDocumento.NO_VERIFICADO;

        if ("DNI".equalsIgnoreCase(tipoDoc)) {
            if (!validarFormatoDni(numDoc)) {
                throw new BusinessException("El DNI '" + numDoc + "' es inválido. Debe contener exactamente 8 dígitos numéricos.");
            }
            estadoVerif = EstadoVerificacionDocumento.VALIDADO_FORMATO;
        } else if ("RUC".equalsIgnoreCase(tipoDoc)) {
            if (!validarFormatoRuc(numDoc)) {
                throw new BusinessException("El RUC '" + numDoc + "' es inválido según el algoritmo oficial de verificación (Módulo 11 SUNAT).");
            }
            estadoVerif = EstadoVerificacionDocumento.VALIDADO_FORMATO;
        }

        // 3. Reglas según Tipo de Contribuyente
        if (request.getTipoContribuyente() == TipoContribuyente.PERSONA_NATURAL) {
            if (request.getNombres() == null || request.getNombres().trim().isEmpty() ||
                request.getPrimerApellido() == null || request.getPrimerApellido().trim().isEmpty()) {
                throw new BusinessException("Para persona natural, los nombres y el primer apellido son obligatorios.");
            }
        } else if (request.getTipoContribuyente() == TipoContribuyente.PERSONA_JURIDICA) {
            if (request.getNombreRazonSocial() == null || request.getNombreRazonSocial().trim().isEmpty()) {
                throw new BusinessException("Para persona jurídica, la Razón Social es obligatoria.");
            }
            if (!"RUC".equalsIgnoreCase(tipoDoc)) {
                throw new BusinessException("Para persona jurídica el tipo de documento debe ser RUC.");
            }
        }

        // 4. Validaciones opcionales de contacto
        if (request.getCorreo() != null && !request.getCorreo().trim().isEmpty()) {
            if (!PATTERN_EMAIL.matcher(request.getCorreo().trim()).matches()) {
                throw new BusinessException("El correo electrónico ingresado no tiene un formato válido.");
            }
        }

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (!PATTERN_PHONE.matcher(request.getTelefono().trim()).matches()) {
                throw new BusinessException("El número de teléfono ingresado contiene caracteres inválidos.");
            }
        }

        return estadoVerif;
    }

    /**
     * Valida la actualización de datos de un contribuyente existente.
     */
    public void validarActualizacion(UUID id, ContribuyenteUpdateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de actualización no puede ser nula");
        }

        String tipoDoc = request.getTipoDocumento() != null ? request.getTipoDocumento().trim().toUpperCase() : "";
        String numDoc = request.getNumeroDocumento() != null ? request.getNumeroDocumento().trim() : "";

        if (contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(tipoDoc, numDoc, id)) {
            throw new DuplicateResourceException("El documento " + tipoDoc + " N° " + numDoc + " ya está asignado a otro contribuyente.");
        }

        if ("DNI".equalsIgnoreCase(tipoDoc) && !validarFormatoDni(numDoc)) {
            throw new BusinessException("El DNI ingresado no cumple con el formato de 8 dígitos.");
        } else if ("RUC".equalsIgnoreCase(tipoDoc) && !validarFormatoRuc(numDoc)) {
            throw new BusinessException("El RUC ingresado no cumple con el algoritmo oficial de SUNAT.");
        }

        if (request.getCorreo() != null && !request.getCorreo().trim().isEmpty()) {
            if (!PATTERN_EMAIL.matcher(request.getCorreo().trim()).matches()) {
                throw new BusinessException("El correo electrónico ingresado no tiene un formato válido.");
            }
        }
    }

    /**
     * Detección preventiva de duplicados por documento o por coincidencia nominal.
     */
    public DuplicateCheckResponse verificarDuplicados(String tipoDocumento, String numeroDocumento, String nombreRazonSocial, UUID excludeId) {
        boolean duplicadoExacto = false;
        String mensajeExacto = null;

        if (tipoDocumento != null && numeroDocumento != null) {
            String tDoc = tipoDocumento.trim().toUpperCase();
            String nDoc = numeroDocumento.trim();
            if (excludeId == null) {
                duplicadoExacto = contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumento(tDoc, nDoc);
            } else {
                duplicadoExacto = contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(tDoc, nDoc, excludeId);
            }
            if (duplicadoExacto) {
                mensajeExacto = "Ya existe un contribuyente registrado con " + tDoc + " " + nDoc;
            }
        }

        List<ContribuyenteTributario> potenciales = List.of();
        if (nombreRazonSocial != null && nombreRazonSocial.trim().length() >= 4) {
            potenciales = contribuyenteRepository.buscarPotencialesDuplicadosNombre(nombreRazonSocial.trim(), excludeId);
        }

        return DuplicateCheckResponse.builder()
                .existeDuplicadoExacto(duplicadoExacto)
                .mensajeDuplicadoExacto(mensajeExacto)
                .coincidenciasPotenciales(potenciales.stream()
                        .map(c -> c.getCodigoContribuyente() + " - " + c.getNombreRazonSocial() + " (" + c.getTipoDocumento() + " " + c.getNumeroDocumentoEnmascarado() + ")")
                        .toList())
                .build();
    }
}
