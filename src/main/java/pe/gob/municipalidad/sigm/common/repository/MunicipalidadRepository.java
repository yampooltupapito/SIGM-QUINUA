package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.Municipalidad;

import java.util.Optional;

@Repository
public interface MunicipalidadRepository extends JpaRepository<Municipalidad, Long> {

    Optional<Municipalidad> findByUbigeo(String ubigeo);

    boolean existsByUbigeo(String ubigeo);
}
