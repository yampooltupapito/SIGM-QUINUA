package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.CajaMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CajaMercadoRepository extends JpaRepository<CajaMercado, UUID> {
    Optional<CajaMercado> findByCodigo(String codigo);
    List<CajaMercado> findByEstado(String estado);
}
