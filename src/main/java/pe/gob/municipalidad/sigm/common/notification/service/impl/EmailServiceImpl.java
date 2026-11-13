package pe.gob.municipalidad.sigm.common.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionEnvio;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionIntento;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionPlantilla;
import pe.gob.municipalidad.sigm.common.notification.provider.MailProvider;
import pe.gob.municipalidad.sigm.common.notification.provider.SendResult;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionEnvioRepository;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionIntentoRepository;
import pe.gob.municipalidad.sigm.common.notification.repository.NotificacionPlantillaRepository;
import pe.gob.municipalidad.sigm.common.notification.service.EmailService;
import pe.gob.municipalidad.sigm.common.notification.template.NotificationTemplateEngine;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final NotificacionEnvioRepository envioRepository;
    private final NotificacionIntentoRepository intentoRepository;
    private final NotificacionPlantillaRepository plantillaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationTemplateEngine templateEngine;
    private final AuditoriaAdministrativaService auditoriaService;
    private final ApplicationContext applicationContext;
    private final String configuredProvider;

    public EmailServiceImpl(
            NotificacionEnvioRepository envioRepository,
            NotificacionIntentoRepository intentoRepository,
            NotificacionPlantillaRepository plantillaRepository,
            UsuarioRepository usuarioRepository,
            NotificationTemplateEngine templateEngine,
            AuditoriaAdministrativaService auditoriaService,
            ApplicationContext applicationContext,
            @Value("${sigm.mail.provider:mock}") String configuredProvider
    ) {
        this.envioRepository = envioRepository;
        this.intentoRepository = intentoRepository;
        this.plantillaRepository = plantillaRepository;
        this.usuarioRepository = usuarioRepository;
        this.templateEngine = templateEngine;
        this.auditoriaService = auditoriaService;
        this.applicationContext = applicationContext;
        this.configuredProvider = configuredProvider;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificacionEnvio encolarCorreo(NotificationDtos.EmailRequestDto request) {
        if (request == null || request.getDestinatarioCorreo() == null || request.getDestinatarioCorreo().isBlank()) {
            throw new InvalidRequestException("La dirección de correo destinatario es obligatoria.");
        }

        // 1. Control de Idempotencia: Si ya existe la clave, evitamos duplicidad
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<NotificacionEnvio> previo = envioRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (previo.isPresent()) {
                log.info("[IDEMPOTENCIA] Envío de correo duplicado omitido para clave: {}", request.getIdempotencyKey());
                return previo.get();
            }
        } else {
            request.setIdempotencyKey("idem-" + UUID.randomUUID());
        }

        // 2. Resolver plantilla de correo
        NotificacionPlantilla plantilla = plantillaRepository.findByCodigoAndActivoTrue(request.getPlantillaCodigo())
                .or(() -> plantillaRepository.findByCodigoAndActivoTrue("NUEVA_NOTIFICACION"))
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla de notificación no encontrada o inactiva: " + request.getPlantillaCodigo()));

        // 3. Renderizar asunto y cuerpo con sanitización
        String asunto = request.getAsuntoPersonalizado() != null && !request.getAsuntoPersonalizado().isBlank()
                ? request.getAsuntoPersonalizado()
                : templateEngine.render(plantilla.getAsuntoPredeterminado(), request.getVariables(), false);

        String cuerpoHtml = templateEngine.render(plantilla.getCuerpoHtml(), request.getVariables(), true);
        String cuerpoTexto = templateEngine.render(plantilla.getCuerpoTexto(), request.getVariables(), false);

        Usuario usuario = null;
        if (request.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(request.getUsuarioId()).orElse(null);
        }

        // 4. Persistir registro en la cola Outbox con estado PENDIENTE
        NotificacionEnvio envio = NotificacionEnvio.builder()
                .destinatarioCorreo(request.getDestinatarioCorreo().trim().toLowerCase())
                .destinatarioNombre(request.getDestinatarioNombre())
                .usuario(usuario)
                .plantillaCodigo(plantilla.getCodigo())
                .asunto(asunto)
                .cuerpoHtml(cuerpoHtml)
                .cuerpoTexto(cuerpoTexto)
                .tipo(request.getTipo() != null ? request.getTipo() : "INFORMACION")
                .estado("PENDIENTE")
                .intentos(0)
                .maxIntentos(3)
                .proximoIntento(OffsetDateTime.now())
                .idempotencyKey(request.getIdempotencyKey())
                .origen("SISTEMA")
                .build();

        NotificacionEnvio guardado = envioRepository.save(envio);

        log.info("[COLA CORREO] Correo encolado exitosamente con ID: {}, Plantilla: {}, Destinatario: {}",
                guardado.getId(), plantilla.getCodigo(), guardado.getDestinatarioCorreo());

        return guardado;
    }

    @Async
    public void despacharEnvioDirectoAsync(Long envioId) {
        despacharEnvioDirecto(envioId);
    }

    @Override
    @Transactional
    public void despacharEnvioDirecto(Long envioId) {
        NotificacionEnvio envio = envioRepository.findById(envioId).orElse(null);
        if (envio == null || !"PENDIENTE".equals(envio.getEstado())) {
            return;
        }

        procesarEnvioIndividual(envio);
    }

    @Override
    @Transactional
    public void procesarColaEnvios() {
        List<NotificacionEnvio> pendientes = envioRepository.findEnviosPendientesParaProcesar(
                OffsetDateTime.now(),
                PageRequest.of(0, 20)
        );

        if (pendientes.isEmpty()) {
            return;
        }

        log.info("[COLA CORREO] Procesando lote de {} correos pendientes de despacho", pendientes.size());
        for (NotificacionEnvio envio : pendientes) {
            try {
                procesarEnvioIndividual(envio);
            } catch (Exception e) {
                log.error("[COLA CORREO] Error al procesar envío {}: {}", envio.getId(), e.getMessage());
            }
        }
    }

    private void procesarEnvioIndividual(NotificacionEnvio envio) {
        envio.setEstado("EN_PROCESO");
        envioRepository.saveAndFlush(envio);

        MailProvider provider = resolveProvider();
        SendResult result = provider.send(
                envio.getDestinatarioCorreo(),
                envio.getDestinatarioNombre(),
                envio.getAsunto(),
                envio.getCuerpoHtml(),
                envio.getCuerpoTexto()
        );

        int nuevoIntento = envio.getIntentos() + 1;
        envio.setIntentos(nuevoIntento);

        // Registrar trazabilidad del intento
        NotificacionIntento intento = NotificacionIntento.builder()
                .envio(envio)
                .numeroIntento(nuevoIntento)
                .proveedor(provider.getProviderName())
                .exitoso(result.isSuccess())
                .mensajeRespuesta(result.isSuccess() ? "MessageId: " + result.getMessageId() : result.getErrorMessage())
                .build();
        intentoRepository.save(intento);

        if (result.isSuccess()) {
            envio.setEstado("ENVIADO");
            envio.setFechaEnvio(OffsetDateTime.now());
            envio.setUltimoError(null);
            envioRepository.saveAndFlush(envio);

            auditoriaService.registrar(
                    "ENVIO_CORREO_EXITO",
                    "NotificacionEnvio",
                    envio.getId(),
                    "EXITO",
                    "Correo despachado satisfactoriamente a: " + envio.getDestinatarioCorreo() + " [" + provider.getProviderName() + "]"
            );
        } else {
            envio.setUltimoError(result.getErrorMessage());
            if (nuevoIntento >= envio.getMaxIntentos()) {
                envio.setEstado("FALLIDO");
                log.warn("[COLA CORREO] Envío {} alcanzó el límite de intentos ({}). Estado marcado como FALLIDO.", envio.getId(), envio.getMaxIntentos());
            } else {
                // Backoff exponencial: 2 ^ intento minutos
                long minutosEspera = (long) Math.pow(2, nuevoIntento);
                envio.setEstado("PENDIENTE");
                envio.setProximoIntento(OffsetDateTime.now().plusMinutes(minutosEspera));
                log.info("[COLA CORREO] Reintento programado para envío {} en {} minutos.", envio.getId(), minutosEspera);
            }
            envioRepository.saveAndFlush(envio);

            auditoriaService.registrar(
                    "ENVIO_CORREO_FALLO",
                    "NotificacionEnvio",
                    envio.getId(),
                    "FALLO",
                    "Fallo de despacho a: " + envio.getDestinatarioCorreo() + ". Intento: " + nuevoIntento + ". Error: " + result.getErrorMessage()
            );
        }
    }

    @Override
    @Transactional
    public void reintentarEnvio(Long envioId) {
        NotificacionEnvio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new ResourceNotFoundException("Envío de correo no encontrado con ID: " + envioId));

        envio.setEstado("PENDIENTE");
        envio.setIntentos(0);
        envio.setProximoIntento(OffsetDateTime.now());
        envio.setUltimoError(null);
        envioRepository.saveAndFlush(envio);

        auditoriaService.registrar(
                "REINTENTO_MANUAL_CORREO",
                "NotificacionEnvio",
                envio.getId(),
                "EXITO",
                "Reintento manual ordenado para correo ID: " + envioId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificacionEnvioDto> obtenerHistorialUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        return envioRepository.findByUsuarioIdOrderByCreatedAtDesc(usuario.getId(), PageRequest.of(0, 50))
                .getContent()
                .stream()
                .map(NotificationDtos.NotificacionEnvioDto::fromEntity)
                .collect(Collectors.toList());
    }

    private MailProvider resolveProvider() {
        try {
            if ("smtp".equalsIgnoreCase(configuredProvider) && applicationContext.containsBean("smtpMailProvider")) {
                MailProvider smtp = applicationContext.getBean("smtpMailProvider", MailProvider.class);
                if (smtp.isAvailable()) {
                    return smtp;
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el proveedor SMTP, usando MockMailProvider: {}", e.getMessage());
        }
        return applicationContext.getBean("mockMailProvider", MailProvider.class);
    }
}
