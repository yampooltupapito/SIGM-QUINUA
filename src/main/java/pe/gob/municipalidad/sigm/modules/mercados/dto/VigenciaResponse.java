package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDate;
import java.util.UUID;

public class VigenciaResponse {

    private UUID id;
    private String tipoDocumento;
    private String codigo;
    private String comercianteNombre;
    private String comercianteDocumento;
    private String puestoCodigo;
    private String puestoNumero;
    private String mercadoNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long diasRestantes;
    private String estado;
    private String alertaNivel; // VENCIDO, CRITICO_7_DIAS, ALERTA_30_DIAS, REGULAR

    public VigenciaResponse() {}

    public VigenciaResponse(UUID id, String tipoDocumento, String codigo, String comercianteNombre, String comercianteDocumento, String puestoCodigo, String puestoNumero, String mercadoNombre, LocalDate fechaInicio, LocalDate fechaFin, Long diasRestantes, String estado, String alertaNivel) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.codigo = codigo;
        this.comercianteNombre = comercianteNombre;
        this.comercianteDocumento = comercianteDocumento;
        this.puestoCodigo = puestoCodigo;
        this.puestoNumero = puestoNumero;
        this.mercadoNombre = mercadoNombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasRestantes = diasRestantes;
        this.estado = estado;
        this.alertaNivel = alertaNivel;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getComercianteNombre() { return comercianteNombre; }
    public void setComercianteNombre(String comercianteNombre) { this.comercianteNombre = comercianteNombre; }

    public String getComercianteDocumento() { return comercianteDocumento; }
    public void setComercianteDocumento(String comercianteDocumento) { this.comercianteDocumento = comercianteDocumento; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getPuestoNumero() { return puestoNumero; }
    public void setPuestoNumero(String puestoNumero) { this.puestoNumero = puestoNumero; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Long getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(Long diasRestantes) { this.diasRestantes = diasRestantes; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getAlertaNivel() { return alertaNivel; }
    public void setAlertaNivel(String alertaNivel) { this.alertaNivel = alertaNivel; }
}
