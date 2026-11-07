package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ReversoPagoMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReversoPagoMercadoRepository extends JpaRepository<ReversoPagoMercado, UUID> {
    List<ReversoPagoMercado> findByPagoId(UUID pagoId);
}
