package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CorrelativoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CorrelativoExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionNumeracionService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfiguracionNumeracionServiceImpl implements ConfiguracionNumeracionService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionNumeracionServiceImpl.class);

    private final CorrelativoExpedienteRepository correlativoRepository;

    public ConfiguracionNumeracionServiceImpl(CorrelativoExpedienteRepository correlativoRepository) {
        this.correlativoRepository = correlativoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NumeracionResponse> listarCorrelativos() {
        return correlativoRepository.findAll()
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NumeracionResponse obtenerCorrelativoActual(int anio) {
        CorrelativoExpediente c = correlativoRepository.findByPrefijoAndAnio("MPV", anio)
                .orElse(CorrelativoExpediente.builder()
                        .prefijo("MPV")
                        .anio(anio)
                        .ultimoNumero(0)
                        .build());
        return mapearResponse(c);
    }

    private NumeracionResponse mapearResponse(CorrelativoExpediente c) {
        long ult = c.getUltimoNumero() != null ? c.getUltimoNumero() : 0L;
        String siguienteFormato = String.format("%s-%d-%06d",
                c.getPrefijo() != null ? c.getPrefijo() : "MPV",
                c.getAnio() != null ? c.getAnio() : LocalDate.now().getYear(),
                ult + 1
        );

        return NumeracionResponse.builder()
                .id(c.getId())
                .anio(c.getAnio())
                .ultimoNumero(ult)
                .formatoSugerido(siguienteFormato)
                .prefijo(c.getPrefijo())
                .sufijo("MDQ")
                .entidad("Mesa de Partes Virtual")
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
