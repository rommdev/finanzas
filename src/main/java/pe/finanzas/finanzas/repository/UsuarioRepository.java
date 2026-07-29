package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.Usuario;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository <Usuario, Integer> {

    Usuario findByCuentaAndClave(String cuenta, String clave);

    List<Usuario> findByIdUsuario(Integer idusuario);
}
