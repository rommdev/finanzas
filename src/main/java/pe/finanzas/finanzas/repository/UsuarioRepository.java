package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository <Usuario, Integer> {
}
