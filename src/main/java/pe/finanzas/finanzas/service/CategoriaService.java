package pe.finanzas.finanzas.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.Categoria;
import pe.finanzas.finanzas.repository.CategoriaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> getAll(){
        return categoriaRepository.findAll();
    }

    public List<Categoria> getByTipo(String tipo){
        return categoriaRepository.findByTipo(tipo);
    }

    public Categoria getOne(Integer id){
        return categoriaRepository.findById(id).orElseThrow();
    }

    public ResultadoResponse create(Categoria categoria){
        try {
            categoria.setActivo(true);
            categoriaRepository.save(categoria);
            return new ResultadoResponse(true,"Categoria registrada correctamente");
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Error al registrar categoria");
        }
    }

    public ResultadoResponse update(Categoria categoria){
        try {
            var categoriabd = categoriaRepository.findById(categoria.getIdcategoria()).orElseThrow();

            categoria.setActivo(categoriabd.getActivo());

            categoriaRepository.save(categoria);
            return new ResultadoResponse(true, "Categoria actualziada correctamente");
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false,"Error al actualizar categoria");
        }
    }

    @Transactional
    public ResultadoResponse changeActive(Integer id){
        try {
            var categoria = categoriaRepository.findById(id).orElseThrow();

            Boolean activo = categoria.getActivo();

            if(activo == null){
                activo = true;
            }

            categoria.setActivo(!activo);

            var estado = categoria.getActivo() ? "Activo" : "desactivado";
            var mensaje = String.format("Usuario conn ID %s %s", categoria.getIdcategoria(), estado);

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }





}
