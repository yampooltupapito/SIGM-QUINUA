package pe.gob.municipalidad.sigm.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.gob.municipalidad.sigm.common.constants.AppConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador web que renderiza la vista técnica de comprobación de Thymeleaf.
 * Muestra el estado operativo inicial de la arquitectura y la preparación de los 10 módulos municipales.
 */
@Controller
public class VerificationViewController {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @GetMapping("/verify")
    public String showVerificationPage(Model model) {
        model.addAttribute("appName", AppConstants.APP_NAME);
        model.addAttribute("appFullName", AppConstants.APP_FULL_NAME);
        model.addAttribute("municipality", AppConstants.MUNICIPALITY_NAME);
        model.addAttribute("location", AppConstants.PROVINCE + ", " + AppConstants.DEPARTMENT + " - " + AppConstants.COUNTRY);
        model.addAttribute("profile", activeProfile.toUpperCase());
        model.addAttribute("status", "ARQUITECTURA BASE Y DEPENDENCIAS OPERATIVAS");
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        List<ModuleInfo> modules = List.of(
                new ModuleInfo(1, "Mesa de Partes Virtual", "Gestión documental, expedientes y derivaciones", "mesapartes"),
                new ModuleInfo(2, "Mercados Municipales", "Comerciantes, puestos, arriendos e inspecciones", "mercados"),
                new ModuleInfo(3, "Seguridad Ciudadana", "Serenazgo, patrullajes e incidencias en Quinua", "serenazgo"),
                new ModuleInfo(4, "Recaudación Tributaria", "Contribuyentes, predial, arbitrios y pagos", "tributacion"),
                new ModuleInfo(5, "Licencias de Funcionamiento", "Comercio formal, ITSE y resoluciones", "licencias"),
                new ModuleInfo(6, "Obras Públicas", "Proyectos de inversión, valorizaciones y supervisión", "obras"),
                new ModuleInfo(7, "Programas Sociales", "Vaso de Leche, comedores populares y focalización", "programassociales"),
                new ModuleInfo(8, "Almacén y Patrimonio", "Inventario institucional, PECOSA y bienes MBN", "patrimonio"),
                new ModuleInfo(9, "Servicios Públicos", "Limpieza pública, parques y gestión ambiental", "serviciospublicos"),
                new ModuleInfo(10, "Administración Integral", "Usuarios, RBAC, oficinas y auditoría general", "administracion")
        );

        model.addAttribute("modules", modules);
        return "pages/verification";
    }

    public record ModuleInfo(int order, String name, String description, String packageKey) {}
}
