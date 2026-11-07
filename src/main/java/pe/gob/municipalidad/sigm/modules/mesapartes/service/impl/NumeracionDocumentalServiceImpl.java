package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CorrelativoDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CorrelativoDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.NumeracionDocumentalService;

import java.time.OffsetDateTime;

@Service
public class NumeracionDocumentalServiceImpl implements NumeracionDocumentalService {

    private static final Logger log = LoggerFactory.getLogger(NumeracionDocumentalServiceImpl.class);

    private final CorrelativoDocumentalRepository correlativoRepository;
    private final OficinaRepository oficinaRepository;

    public NumeracionDocumentalServiceImpl(CorrelativoDocumentalRepository correlativoRepository,
                                           OficinaRepository oficinaRepository) {
        this.correlativoRepository = correlativoRepository;
        this.oficinaRepository = oficinaRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public String generarNumeroDocumento(String tipoDocumento, Long oficinaId, Integer anio) {
        if (anio == null) {
            anio = OffsetDateTime.now().getYear();
        }

        Oficina oficina = null;
        String oficinaSiglas = "GEN";
        if (oficinaId != null) {
            oficina = oficinaRepository.findById(oficinaId).orElse(null);
            if (oficina != null && oficina.getSigla() != null && !oficina.getSigla().isBlank()) {
                oficinaSiglas = oficina.getSigla();
            } else if (oficina != null && oficina.getCodigo() != null) {
                oficinaSiglas = oficina.getCodigo();
            }
        }

        // Bloqueo pesimista para evitar colisiones concurrentes y garantizar monotonicidad
        CorrelativoDocumental correlativo = correlativoRepository.findForUpdate(tipoDocumento, oficinaId, anio)
                .orElseGet(() -> {
                    CorrelativoDocumental nuevo = CorrelativoDocumental.builder()
                            .tipoDocumento(tipoDocumento)
                            .oficina(oficinaRepository.findById(oficinaId).orElse(null))
                            .anio(OffsetDateTime.now().getYear())
                            .ultimoNumero(0L)
                            .formato("{TIPO}-{NUM}-{ANIO}-MDQ/{OFICINA}")
                            .build();
                    return correlativoRepository.saveAndFlush(nuevo);
                });

        long nuevoNumero = correlativo.getUltimoNumero() + 1;
        correlativo.setUltimoNumero(nuevoNumero);
        correlativoRepository.saveAndFlush(correlativo);

        String numeroFormateado = String.format("%04d", nuevoNumero);
        String resultado = correlativo.getFormato()
                .replace("{TIPO}", tipoDocumento)
                .replace("{NUM}", numeroFormateado)
                .replace("{ANIO}", String.valueOf(anio))
                .replace("{OFICINA}", oficinaSiglas);

        log.info("Correlativo generado: {} para tipo {} y oficina {}", resultado, tipoDocumento, oficinaSiglas);
        return resultado;
    }
}
