package pe.finanzas.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.finanzas.finanzas.model.Tipo;

@Repository
public interface TipoRepository extends JpaRepository <Tipo, Integer> {
}
