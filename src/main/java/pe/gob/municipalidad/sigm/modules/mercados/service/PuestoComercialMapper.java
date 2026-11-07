package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapeador entre entidades de puestos comerciales y sus respectivos DTOs.
 */
@Component
public class PuestoComercialMapper {

    public PuestoComercialResponse toResponse(PuestoComercial p) {
        if (p == null) return null;
        return PuestoComercialResponse.builder()
                .id(p.getId())
                .mercadoId(p.getMercado().getId())
                .mercadoNombre(p.getMercado().getNombre())
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : null)
                .pabellonId(p.getPabellon() != null ? p.getPabellon().getId() : null)
                .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : null)
                .zonaId(p.getZona() != null ? p.getZona().getId() : null)
                .zonaNombre(p.getZona() != null ? p.getZona().getNombre() : null)
                .codigo(p.getCodigo())
                .numero(p.getNumero())
                .nombreReferencial(p.getNombreReferencial())
                .descripcion(p.getDescripcion())
                .tipoPuestoId(p.getTipoPuesto() != null ? p.getTipoPuesto().getId() : null)
                .tipoPuestoNombre(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : null)
                .areaMetrosCuadrados(p.getAreaMetrosCuadrados())
                .anchoMetros(p.getAnchoMetros())
                .largoMetros(p.getLargoMetros())
                .nivel(p.getNivel())
                .ubicacionReferencia(p.getUbicacionReferencia())
                .estadoFisico(p.getEstadoFisico())
                .estadoFisicoDescripcion(p.getEstadoFisico() != null ? p.getEstadoFisico().getDescripcion() : null)
                .estado(p.getEstado())
                .estadoDescripcion(p.getEstado() != null ? p.getEstado().getDescripcion() : null)
                .disponible(p.isDisponible())
                .version(p.getVersion())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public PuestoComercial toEntity(PuestoComercialCreateRequest request, MercadoMunicipal mercado,
                                    SectorMercado sector, PabellonMercado pabellon, ZonaComercial zona,
                                    TipoPuestoComercial tipoPuesto) {
        BigDecimal area = request.getAreaMetrosCuadrados();
        BigDecimal ancho = request.getAnchoMetros();
        BigDecimal largo = request.getLargoMetros();

        if ((area == null || area.compareTo(BigDecimal.ZERO) == 0) &&
                ancho != null && largo != null &&
                ancho.compareTo(BigDecimal.ZERO) > 0 && largo.compareTo(BigDecimal.ZERO) > 0) {
            area = ancho.multiply(largo).setScale(2, RoundingMode.HALF_UP);
        }

        return PuestoComercial.builder()
                .mercado(mercado)
                .sector(sector)
                .pabellon(pabellon)
                .zona(zona)
                .codigo(request.getCodigo().trim().toUpperCase())
                .numero(request.getNumero().trim())
                .nombreReferencial(request.getNombreReferencial() != null ? request.getNombreReferencial().trim() : null)
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .areaMetrosCuadrados(area)
                .anchoMetros(ancho)
                .largoMetros(largo)
                .nivel(request.getNivel() != null ? request.getNivel() : 1)
                .ubicacionReferencia(request.getUbicacionReferencia() != null ? request.getUbicacionReferencia().trim() : null)
                .estadoFisico(request.getEstadoFisico() != null ? request.getEstadoFisico() : EstadoFisicoPuesto.OPERATIVO)
                .estado(request.getEstado() != null ? request.getEstado() : EstadoPuesto.DISPONIBLE)
                .tipoPuesto(tipoPuesto)
                .build();
    }

    public PuestoCambioEstadoResponse toCambioEstadoResponse(PuestoCambioEstado h) {
        if (h == null) return null;
        return PuestoCambioEstadoResponse.builder()
                .id(h.getId())
                .tipoCambio(h.getTipoCambio())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .motivo(h.getMotivo())
                .usuarioResponsable(h.getUsuarioResponsable())
                .usuarioUsername(h.getUsuarioUsername())
                .fechaCambio(h.getFechaCambio() != null ? h.getFechaCambio().toLocalDateTime() : null)
                .build();
    }

    public HistorialUbicacionResponse toHistorialUbicacionResponse(PuestoCambioUbicacion u) {
        if (u == null) return null;
        return HistorialUbicacionResponse.builder()
                .id(u.getId())
                .sectorAnteriorNombre(u.getSectorAnterior() != null ? u.getSectorAnterior().getNombre() : "Sin Sector")
                .sectorNuevoNombre(u.getSectorNuevo() != null ? u.getSectorNuevo().getNombre() : "Sin Sector")
                .pabellonAnteriorNombre(u.getPabellonAnterior() != null ? u.getPabellonAnterior().getNombre() : "Sin Pabellón")
                .pabellonNuevoNombre(u.getPabellonNuevo() != null ? u.getPabellonNuevo().getNombre() : "Sin Pabellón")
                .zonaAnteriorNombre(u.getZonaAnterior() != null ? u.getZonaAnterior().getNombre() : "Sin Zona")
                .zonaNuevaNombre(u.getZonaNueva() != null ? u.getZonaNueva().getNombre() : "Sin Zona")
                .ubicacionRefAnterior(u.getUbicacionRefAnterior())
                .ubicacionRefNueva(u.getUbicacionRefNueva())
                .motivo(u.getMotivo())
                .usuarioResponsable(u.getUsuarioResponsable())
                .fechaCambio(u.getFechaCambio() != null ? u.getFechaCambio().toLocalDateTime() : null)
                .build();
    }

    public PuestoDocumentoResponse toDocumentoResponse(PuestoDocumento d) {
        if (d == null) return null;
        return PuestoDocumentoResponse.builder()
                .id(d.getId())
                .documentoId(d.getDocumento().getIdentificadorPublico())
                .tipoDocumento(d.getTipoDocumento())
                .nombreArchivo(d.getDocumento().getNombreOriginal())
                .tamanoBytes(d.getDocumento().getTamanoBytes())
                .mimeType(d.getDocumento().getMimeType())
                .descripcion(d.getDescripcion())
                .createdBy(d.getCreatedBy())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toLocalDateTime() : null)
                .build();
    }
}
