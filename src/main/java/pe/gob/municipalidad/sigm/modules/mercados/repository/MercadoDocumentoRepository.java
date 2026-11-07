package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoDocumento;

import java.util.List;
import java.util.UUID;

@Repository
public interface MercadoDocumentoRepository extends JpaRepository<MercadoDocumento, UUID> {

    List<MercadoDocumento> findByMercadoIdOrderByCreatedAtDesc(UUID mercadoId);

    long countByMercadoId(UUID mercadoId);
}
