package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.UUID;

public class ResumenPuestosMercadoResponse {
    private UUID mercadoId;
    private String mercadoNombre;
    private long total;
    private long disponibles;
    private long ocupados;
    private long reservados;
    private long enMantenimiento;
    private long inhabilitados;

    public ResumenPuestosMercadoResponse() {
    }

    public ResumenPuestosMercadoResponse(UUID mercadoId, String mercadoNombre, long total, long disponibles,
                                         long ocupados, long reservados, long enMantenimiento, long inhabilitados) {
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.total = total;
        this.disponibles = disponibles;
        this.ocupados = ocupados;
        this.reservados = reservados;
        this.enMantenimiento = enMantenimiento;
        this.inhabilitados = inhabilitados;
    }

    public static ResumenPuestosMercadoResponseBuilder builder() {
        return new ResumenPuestosMercadoResponseBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getTotalPuestos() { return total; }

    public long getDisponibles() { return disponibles; }
    public void setDisponibles(long disponibles) { this.disponibles = disponibles; }

    public long getOcupados() { return ocupados; }
    public void setOcupados(long ocupados) { this.ocupados = ocupados; }

    public long getReservados() { return reservados; }
    public void setReservados(long reservados) { this.reservados = reservados; }

    public long getEnMantenimiento() { return enMantenimiento; }
    public void setEnMantenimiento(long enMantenimiento) { this.enMantenimiento = enMantenimiento; }

    public long getInhabilitados() { return inhabilitados; }
    public void setInhabilitados(long inhabilitados) { this.inhabilitados = inhabilitados; }

    public static class ResumenPuestosMercadoResponseBuilder {
        private UUID mercadoId;
        private String mercadoNombre;
        private long total;
        private long disponibles;
        private long ocupados;
        private long reservados;
        private long enMantenimiento;
        private long inhabilitados;

        public ResumenPuestosMercadoResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public ResumenPuestosMercadoResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public ResumenPuestosMercadoResponseBuilder total(long total) { this.total = total; return this; }
        public ResumenPuestosMercadoResponseBuilder disponibles(long disponibles) { this.disponibles = disponibles; return this; }
        public ResumenPuestosMercadoResponseBuilder ocupados(long ocupados) { this.ocupados = ocupados; return this; }
        public ResumenPuestosMercadoResponseBuilder reservados(long reservados) { this.reservados = reservados; return this; }
        public ResumenPuestosMercadoResponseBuilder enMantenimiento(long enMantenimiento) { this.enMantenimiento = enMantenimiento; return this; }
        public ResumenPuestosMercadoResponseBuilder inhabilitados(long inhabilitados) { this.inhabilitados = inhabilitados; return this; }

        public ResumenPuestosMercadoResponse build() {
            return new ResumenPuestosMercadoResponse(mercadoId, mercadoNombre, total, disponibles, ocupados, reservados, enMantenimiento, inhabilitados);
        }
    }
}
