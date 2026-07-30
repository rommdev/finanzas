package pe.finanzas.finanzas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.model.Tipo;
import pe.finanzas.finanzas.repository.TipoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoService {

    private final TipoRepository tipoRepository;

    public List<Tipo> getAll(){
        return tipoRepository.findAll();
    }
}
