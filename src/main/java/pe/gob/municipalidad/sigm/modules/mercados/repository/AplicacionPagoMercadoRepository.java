package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AplicacionPagoMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface AplicacionPagoMercadoRepository extends JpaRepository<AplicacionPagoMercado, UUID> {
    List<AplicacionPagoMercado> findByPagoId(UUID pagoId);
    List<AplicacionPagoMercado> findByObligacionId(UUID obligacionId);
}
