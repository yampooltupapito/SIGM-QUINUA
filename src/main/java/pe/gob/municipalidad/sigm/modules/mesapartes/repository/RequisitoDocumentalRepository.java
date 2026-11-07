package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RequisitoDocumental;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequisitoDocumentalRepository extends JpaRepository<RequisitoDocumental, Long> {

    List<RequisitoDocumental> findByTipoTramiteIdAndEstadoOrderByOrdenAsc(Long tipoTramiteId, String estado);

    List<RequisitoDocumental> findByTipoTramiteIdOrderByOrdenAsc(Long tipoTramiteId);

    Optional<RequisitoDocumental> findByTipoTramiteIdAndCodigo(Long tipoTramiteId, String codigo);

    long countByTipoTramiteIdAndObligatorioTrueAndEstado(Long tipoTramiteId, String estado);
}
