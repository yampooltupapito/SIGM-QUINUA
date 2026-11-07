package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridicaRepresentante;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepresentanteRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteJuridicoCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteNaturalCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActividadComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoRegistroInterno;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoRepresentacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ActividadComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio especializado en el alta y empadronamiento de comerciantes (Naturales y Jurídicos).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComercianteRegistroService {

    private final ComercianteRepository comercianteRepository;
    private final PersonaRepository personaRepository;
    private final PersonaJuridicaRepository personaJuridicaRepository;
    private final PersonaJuridicaRepresentanteRepository representanteRepository;
    private final ActividadComercialRepository actividadRepository;
    private final ComercianteValidator validator;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional
    public Comerciante registrarComercianteNatural(ComercianteNaturalCreateRequest request) {
        validator.validarRegistroNatural(request);

        String dni = request.getNumeroDocumento().trim();
        Persona persona = personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", dni)
                .orElseGet(() -> {
                    Persona nueva = Persona.builder()
                            .tipoDocumento("DNI")
                            .numeroDocumento(dni)
                            .nombres(request.getNombres().trim())
                            .apellidoPaterno(request.getApellidoPaterno().trim())
                            .apellidoMaterno(request.getApellidoMaterno() != null ? request.getApellidoMaterno().trim() : null)
                            .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                            .correo(request.getCorreo() != null ? request.getCorreo().trim() : null)
                            .direccion(request.getDireccion() != null ? request.getDireccion().trim() : null)
                            .estado("ACTIVO")
                            .build();
                    return personaRepository.save(nueva);
                });

        // Actualizar datos de contacto si ya existía la persona
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            persona.setTelefono(request.getTelefono().trim());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            persona.setCorreo(request.getCorreo().trim());
        }
        if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
            persona.setDireccion(request.getDireccion().trim());
        }
        personaRepository.save(persona);

        String codigo = (request.getCodigoComerciante() != null && !request.getCodigoComerciante().trim().isEmpty())
                ? request.getCodigoComerciante().trim().toUpperCase()
                : generarSiguienteCodigoComerciante();

        Set<ActividadComercial> actividades = new HashSet<>();
        if (request.getActividadIds() != null && !request.getActividadIds().isEmpty()) {
            actividades.addAll(actividadRepository.findAllById(request.getActividadIds()));
        }

        EstadoComerciante estado = request.getEstado() != null ? request.getEstado() : EstadoComerciante.ACTIVO;

        Comerciante comerciante = Comerciante.builder()
                .tipoComerciante(TipoComerciante.PERSONA_NATURAL)
                .persona(persona)
                .codigoComerciante(codigo)
                .fechaRegistro(LocalDate.now())
                .estado(estado)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .actividades(actividades)
                .build();

        Comerciante guardado = comercianteRepository.save(comerciante);
        log.info("Comerciante Persona Natural empadronado: {} (DNI {}) con código {}",
                persona.getNombreCompleto(), dni, guardado.getCodigoComerciante());

        auditoriaService.registrar("REGISTRAR_COMERCIANTE_NATURAL", "mercados_comerciantes", guardado.getId().toString(), "EXITO",
                "Alta de comerciante natural código " + guardado.getCodigoComerciante() + " para DNI " + dni);

        return guardado;
    }

    @Transactional
    public Comerciante registrarComercianteJuridico(ComercianteJuridicoCreateRequest request) {
        validator.validarRegistroJuridico(request);

        String ruc = request.getRuc().trim();
        PersonaJuridica pj = personaJuridicaRepository.findByRuc(ruc)
                .orElseGet(() -> {
                    PersonaJuridica nueva = PersonaJuridica.builder()
                            .ruc(ruc)
                            .razonSocial(request.getRazonSocial().trim())
                            .nombreComercial(request.getNombreComercial() != null ? request.getNombreComercial().trim() : null)
                            .domicilioFiscal(request.getDomicilioFiscal() != null ? request.getDomicilioFiscal().trim() : null)
                            .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                            .correo(request.getCorreo() != null ? request.getCorreo().trim() : null)
                            .estadoRegistroInterno(EstadoRegistroInterno.PENDIENTE_VERIFICACION)
                            .build();
                    return personaJuridicaRepository.save(nueva);
                });

        // Actualizar datos comerciales
        if (request.getNombreComercial() != null && !request.getNombreComercial().isBlank()) {
            pj.setNombreComercial(request.getNombreComercial().trim());
        }
        if (request.getDomicilioFiscal() != null && !request.getDomicilioFiscal().isBlank()) {
            pj.setDomicilioFiscal(request.getDomicilioFiscal().trim());
        }
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            pj.setTelefono(request.getTelefono().trim());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            pj.setCorreo(request.getCorreo().trim());
        }
        personaJuridicaRepository.save(pj);

        // Representante legal si fue suministrado
        if (request.getRepresentanteDni() != null && !request.getRepresentanteDni().trim().isEmpty()) {
            String dniRep = request.getRepresentanteDni().trim();
            Persona repPersona = personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", dniRep)
                    .orElseGet(() -> {
                        Persona p = Persona.builder()
                                .tipoDocumento("DNI")
                                .numeroDocumento(dniRep)
                                .nombres(request.getRepresentanteNombres() != null ? request.getRepresentanteNombres().trim() : "Por Actualizar")
                                .apellidoPaterno(request.getRepresentanteApellidoPaterno() != null ? request.getRepresentanteApellidoPaterno().trim() : "Por Actualizar")
                                .apellidoMaterno(request.getRepresentanteApellidoMaterno() != null ? request.getRepresentanteApellidoMaterno().trim() : null)
                                .estado("ACTIVO")
                                .build();
                        return personaRepository.save(p);
                    });

            if (!representanteRepository.existsByPersonaJuridicaIdAndPersonaId(pj.getId(), repPersona.getId())) {
                PersonaJuridicaRepresentante rep = PersonaJuridicaRepresentante.builder()
                        .personaJuridica(pj)
                        .persona(repPersona)
                        .tipoRepresentacion(request.getRepresentanteTipo() != null ? request.getRepresentanteTipo() : TipoRepresentacion.REPRESENTANTE_LEGAL)
                        .fechaInicio(request.getRepresentanteFechaInicio() != null ? request.getRepresentanteFechaInicio() : LocalDate.now())
                        .estado("ACTIVO")
                        .build();
                representanteRepository.save(rep);
            }
        }

        String codigo = (request.getCodigoComerciante() != null && !request.getCodigoComerciante().trim().isEmpty())
                ? request.getCodigoComerciante().trim().toUpperCase()
                : generarSiguienteCodigoComerciante();

        Set<ActividadComercial> actividades = new HashSet<>();
        if (request.getActividadIds() != null && !request.getActividadIds().isEmpty()) {
            actividades.addAll(actividadRepository.findAllById(request.getActividadIds()));
        }

        EstadoComerciante estado = request.getEstado() != null ? request.getEstado() : EstadoComerciante.ACTIVO;

        Comerciante comerciante = Comerciante.builder()
                .tipoComerciante(TipoComerciante.PERSONA_JURIDICA)
                .personaJuridica(pj)
                .codigoComerciante(codigo)
                .fechaRegistro(LocalDate.now())
                .estado(estado)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .actividades(actividades)
                .build();

        Comerciante guardado = comercianteRepository.save(comerciante);
        log.info("Comerciante Persona Jurídica empadronado: {} (RUC {}) con código {}",
                pj.getRazonSocial(), ruc, guardado.getCodigoComerciante());

        auditoriaService.registrar("REGISTRAR_COMERCIANTE_JURIDICO", "mercados_comerciantes", guardado.getId().toString(), "EXITO",
                "Alta de comerciante jurídico código " + guardado.getCodigoComerciante() + " para RUC " + ruc);

        return guardado;
    }

    public String generarSiguienteCodigoComerciante() {
        Long seq = comercianteRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        return String.format("COM-%d-%06d", anio, seq);
    }
}
