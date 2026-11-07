package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContactoContribuyente;

import java.util.UUID;

/**
 * Canal o medio de contacto registrado para el Contribuyente Tributario.
 */
@Entity
@Table(name = "tributaria_contactos_contribuyente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoContribuyente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contacto", nullable = false, length = 40)
    private TipoContactoContribuyente tipoContacto;

    @Column(name = "valor", nullable = false, length = 150)
    private String valor;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "es_principal", nullable = false)
    @Builder.Default
    private boolean esPrincipal = false;

    @Column(name = "autorizado_notificacion", nullable = false)
    @Builder.Default
    private boolean autorizadoNotificacion = false;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVO";

    public String getValorEnmascarado() {
        if (valor == null || valor.length() < 4) {
            return "***";
        }
        if (tipoContacto == TipoContactoContribuyente.CORREO_ELECTRONICO && valor.contains("@")) {
            String[] parts = valor.split("@");
            String user = parts[0];
            if (user.length() <= 2) {
                return user + "***@" + parts[1];
            }
            return user.charAt(0) + "***" + user.charAt(user.length() - 1) + "@" + parts[1];
        }
        int len = valor.length();
        return valor.substring(0, 2) + "*".repeat(len - 4) + valor.substring(len - 2);
    }
}
