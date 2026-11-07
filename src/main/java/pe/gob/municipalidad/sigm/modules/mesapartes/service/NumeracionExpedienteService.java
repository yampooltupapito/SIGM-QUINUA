package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CorrelativoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CorrelativoExpedienteRepository;

import java.time.LocalDate;

/**
 * Servicio especializado para la generación atómica y concurrente de la numeración de expedientes.
 * Formato estándar: MPV-{AÑO}-{CORRELATIVO:06d} (ej: MPV-2026-000001).
 * Utiliza bloqueo pesimista en PostgreSQL para evitar saltos o duplicados concurrentes.
 */
@Service
public class NumeracionExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(NumeracionExpedienteService.class);
    public static final String PREFIJO_DEFAULT = "MPV";

    private final CorrelativoExpedienteRepository correlativoRepository;

    public NumeracionExpedienteService(CorrelativoExpedienteRepository correlativoRepository) {
        this.correlativoRepository = correlativoRepository;
    }

    /**
     * Genera el siguiente número correlativo para el año actual o especificado de manera atómica.
     *
     * @param anio Año de registro del expediente
     * @return Código formateado de expediente, ej. MPV-2026-000001
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generarSiguienteNumero(int anio) {
        return generarSiguienteNumero(PREFIJO_DEFAULT, anio);
    }

    /**
     * Genera el siguiente número correlativo para el año actual.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generarSiguienteNumeroActual() {
        return generarSiguienteNumero(PREFIJO_DEFAULT, LocalDate.now().getYear());
    }

    /**
     * Genera el siguiente número correlativo para un prefijo y año específicos.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generarSiguienteNumero(String prefijo, int anio) {
        String prefijoSeguro = (prefijo != null && !prefijo.isBlank()) ? prefijo.trim().toUpperCase() : PREFIJO_DEFAULT;

        try {
            correlativoRepository.asegurarExistenciaCorrelativo(prefijoSeguro, anio);
        } catch (Exception e) {
            log.debug("Asegurar existencia ya procesado: {}", e.getMessage());
        }

        CorrelativoExpediente correlativo = correlativoRepository.findForUpdate(prefijoSeguro, anio)
                .orElseGet(() -> {
                    log.info("Inicializando correlativo para prefijo '{}' y año {}", prefijoSeguro, anio);
                    CorrelativoExpediente nuevo = CorrelativoExpediente.builder()
                            .prefijo(prefijoSeguro)
                            .anio(anio)
                            .ultimoNumero(0)
                            .build();
                    return correlativoRepository.saveAndFlush(nuevo);
                });

        int siguiente = (correlativo.getUltimoNumero() != null ? correlativo.getUltimoNumero() : 0) + 1;
        correlativo.setUltimoNumero(siguiente);
        correlativoRepository.save(correlativo);

        String numeroGenerado = String.format("%s-%d-%06d", prefijoSeguro, anio, siguiente);
        log.debug("Número de expediente generado exitosamente: {}", numeroGenerado);
        return numeroGenerado;
    }
}
