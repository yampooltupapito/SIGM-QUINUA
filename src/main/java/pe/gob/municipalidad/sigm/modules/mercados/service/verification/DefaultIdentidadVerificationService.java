package pe.gob.municipalidad.sigm.modules.mercados.service.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementación predeterminada del servicio de verificación de identidad y RUC.
 * Aplica validaciones matemáticas y sintácticas estrictas para DNI y RUC de Perú.
 * Delega en proveedores oficiales (RENIEC / SUNAT) solo cuando estén debidamente configurados.
 */
@Slf4j
@Service
public class DefaultIdentidadVerificationService implements IdentidadVerificationService {

    private static final Pattern PATTERN_DNI = Pattern.compile("^[0-9]{8}$");
    private static final Pattern PATTERN_RUC = Pattern.compile("^(10|15|16|17|20)[0-9]{9}$");
    private static final int[] RUC_FACTORES = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private final Optional<ReniecVerificationProvider> reniecProvider;
    private final Optional<SunatVerificationProvider> sunatProvider;

    @Autowired
    public DefaultIdentidadVerificationService(
            @Autowired(required = false) ReniecVerificationProvider reniecProvider,
            @Autowired(required = false) SunatVerificationProvider sunatProvider) {
        this.reniecProvider = Optional.ofNullable(reniecProvider);
        this.sunatProvider = Optional.ofNullable(sunatProvider);
    }

    @Override
    public boolean validarFormatoDni(String dni) {
        if (dni == null) {
            return false;
        }
        String clean = dni.trim();
        return PATTERN_DNI.matcher(clean).matches();
    }

    @Override
    public boolean validarFormatoRuc(String ruc) {
        if (ruc == null) {
            return false;
        }
        String clean = ruc.trim();
        if (!PATTERN_RUC.matcher(clean).matches()) {
            return false;
        }

        // Algoritmo Módulo 11 oficial SUNAT
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

    @Override
    public VerificationResult verificarDni(String dni) {
        if (!validarFormatoDni(dni)) {
            return VerificationResult.formatoInvalido("LOCAL", dni, "El DNI debe contener exactamente 8 dígitos numéricos.");
        }

        if (reniecProvider.isPresent() && reniecProvider.get().estaConfigurado()) {
            try {
                return reniecProvider.get().verificarDni(dni);
            } catch (Exception e) {
                log.warn("Error consultando proveedor oficial RENIEC: {}", e.getMessage());
                return VerificationResult.formatoValido("LOCAL", dni, "DNI con formato válido. Pendiente de verificación por servicio externo.");
            }
        }

        return VerificationResult.formatoValido("LOCAL", dni, "Formato de DNI válido (Validación sintáctica institucional).");
    }

    @Override
    public VerificationResult verificarRuc(String ruc) {
        if (!validarFormatoRuc(ruc)) {
            return VerificationResult.formatoInvalido("LOCAL", ruc, "El RUC debe tener 11 dígitos, prefijo válido (10/15/16/17/20) y cumplir con el dígito verificador.");
        }

        if (sunatProvider.isPresent() && sunatProvider.get().estaConfigurado()) {
            try {
                return sunatProvider.get().verificarRuc(ruc);
            } catch (Exception e) {
                log.warn("Error consultando proveedor oficial SUNAT: {}", e.getMessage());
                return VerificationResult.formatoValido("LOCAL", ruc, "RUC con formato válido. Pendiente de verificación por servicio externo.");
            }
        }

        return VerificationResult.formatoValido("LOCAL", ruc, "Formato y dígito verificador de RUC válido conforme a algoritmo SUNAT.");
    }
}
