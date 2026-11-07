package pe.gob.municipalidad.sigm.modules.administracion.perfil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.administracion.perfil.entity.UsuarioPreferencia;

import java.util.Optional;

@Repository
public interface UsuarioPreferenciaRepository extends JpaRepository<UsuarioPreferencia, Long> {

    Optional<UsuarioPreferencia> findByUsuarioId(Long usuarioId);

    Optional<UsuarioPreferencia> findByUsuarioUsername(String username);

    boolean existsByUsuarioId(Long usuarioId);
}
