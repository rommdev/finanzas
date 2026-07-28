package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.Categoria;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findByTipo(String tipo);

    List<Categoria> findByTipoIgnoreCaseAndActivoTrue(String tipo);

    Categoria findByDescripcionIgnoreCaseAndTipoIgnoreCaseAndActivoTrue(String descripcion, String tipo);
}
