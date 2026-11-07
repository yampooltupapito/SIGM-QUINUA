package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SubsanacionMedidaMercado;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubsanacionMedidaMercadoRepository extends JpaRepository<SubsanacionMedidaMercado, UUID> {

    List<SubsanacionMedidaMercado> findByMedidaCorrectivaIdOrderByFechaPresentacionDesc(UUID medidaCorrectivaId);
}
