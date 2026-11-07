package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteContactoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteContextoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComerciantePerfilResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AuditoriaPortalComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.VinculoUsuarioComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVinculoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AuditoriaPortalComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.VinculoUsuarioComercianteRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PortalComerciantePerfilService {

    private final ComerciantePortalContextService contextService;
    private final ComercianteRepository comercianteRepository;
    private final VinculoUsuarioComercianteRepository vinculoRepository;
    private final AuditoriaPortalComercianteRepository auditoriaRepository;

    public PortalComerciantePerfilService(
            ComerciantePortalContextService contextService,
            ComercianteRepository comercianteRepository,
            VinculoUsuarioComercianteRepository vinculoRepository,
            AuditoriaPortalComercianteRepository auditoriaRepository) {
        this.contextService = contextService;
        this.comercianteRepository = comercianteRepository;
        this.vinculoRepository = vinculoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional(readOnly = true)
    public PortalComerciantePerfilResponse obtenerPerfil() {
        Usuario usuario = contextService.getUsuarioAutenticado();
        Comerciante c = contextService.getComercianteActivo();

        Optional<VinculoUsuarioComerciante> vinculoOpt = vinculoRepository
                .findByUsuarioIdAndComercianteId(usuario.getId(), c.getId());

        TipoVinculoComerciante vinculoTipo = vinculoOpt.map(VinculoUsuarioComerciante::getTipoVinculo)
                .orElse(TipoVinculoComerciante.TITULAR);

        String facultades = vinculoOpt.map(VinculoUsuarioComerciante::getFacultadesRepresentacion).orElse("Titular Registrado");

        String docEnmascarado = enmascararDocumento(c);
        String telefono = c.getTelefonoContacto();
        String email = c.getCorreoContacto();
        String direccion = c.getDireccionContacto();

        List<PortalComercianteContextoResponse> otros = contextService.listarContextosDisponibles();

        return PortalComerciantePerfilResponse.builder()
                .comercianteId(c.getId())
                .codigoComerciante(c.getCodigoComerciante())
                .nombreCompletoORazonSocial(c.getNombreOTitular())
                .tipoComerciante(c.getTipoComerciante())
                .documentoEnmascarado(docEnmascarado)
                .telefono(telefono)
                .correoElectronico(email)
                .direccion(direccion)
                .estadoRegistro(c.getEstado())
                .fechaRegistro(c.getFechaRegistro())
                .tipoVinculo(vinculoTipo)
                .facultadesRepresentacion(facultades)
                .otrosContextosDisponibles(otros)
                .build();
    }

    @Transactional
    public PortalComerciantePerfilResponse actualizarContacto(PortalComercianteContactoUpdateRequest request) {
        return actualizarContacto(request, "127.0.0.1");
    }

    @Transactional
    public PortalComerciantePerfilResponse actualizarContacto(PortalComercianteContactoUpdateRequest request, String ipAddress) {
        Usuario usuario = contextService.getUsuarioAutenticado();
        Comerciante c = contextService.getComercianteActivo();

        String telefonoAnterior = c.getTelefonoContacto();
        String correoAnterior = c.getCorreoContacto();

        if (c.isPersonaNatural() && c.getPersona() != null) {
            c.getPersona().setTelefono(request.getTelefono().trim());
            c.getPersona().setCorreo(request.getCorreoElectronico().trim().toLowerCase());
            if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
                c.getPersona().setDireccion(request.getDireccion().trim());
            }
        } else if (c.isPersonaJuridica() && c.getPersonaJuridica() != null) {
            c.getPersonaJuridica().setTelefono(request.getTelefono().trim());
            c.getPersonaJuridica().setCorreo(request.getCorreoElectronico().trim().toLowerCase());
            if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
                c.getPersonaJuridica().setDomicilioFiscal(request.getDireccion().trim());
            }
        }

        c = comercianteRepository.save(c);

        auditoriaRepository.save(AuditoriaPortalComerciante.builder()
                .comercianteId(c.getId())
                .usuarioId(usuario.getId())
                .username(usuario.getUsername())
                .tipoAccion("ACTUALIZAR_CONTACTO")
                .descripcion("Actualización de datos de contacto comercial desde el Portal Ciudadano")
                .detallesJson("{\"telefonoAnterior\":\"" + telefonoAnterior + "\",\"telefonoNuevo\":\"" + request.getTelefono() +
                        "\",\"correoAnterior\":\"" + correoAnterior + "\",\"correoNuevo\":\"" + request.getCorreoElectronico() + "\"}")
                .ipAddress(ipAddress)
                .build());

        return obtenerPerfil();
    }

    private String enmascararDocumento(Comerciante c) {
        if (c.isPersonaNatural() && c.getPersona() != null) {
            String doc = c.getPersona().getNumeroDocumento();
            if (doc != null && doc.length() >= 4) {
                return c.getPersona().getTipoDocumento() + ": *****" + doc.substring(doc.length() - 3);
            }
            return "DNI: ********";
        } else if (c.isPersonaJuridica() && c.getPersonaJuridica() != null) {
            String ruc = c.getPersonaJuridica().getRuc();
            if (ruc != null && ruc.length() >= 4) {
                return "RUC: 20******" + ruc.substring(ruc.length() - 3);
            }
            return "RUC: ***********";
        }
        return "DOCUMENTO PROTEGIDO";
    }
}
