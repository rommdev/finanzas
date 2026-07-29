package pe.finanzas.finanzas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.MetaFilter;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.Meta;
import pe.finanzas.finanzas.model.Usuario;
import pe.finanzas.finanzas.repository.MetaRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {
    private final MetaRepository metaRepository;

    public List<Meta> getAll(){
        return metaRepository.findAll();
    }

    public List<Meta> getByUsuario(Integer idusuario){
        return metaRepository.findByUsuarioIdUsuarioOrderByIdMetaDesc(idusuario);
    }

    public List<Meta> search(MetaFilter filter, Integer idusuario){
        return metaRepository.findAllByFilter(idusuario, filter.getEstado());
    }

    public Meta getOne(Integer idmeta, Integer idusuario){
        return metaRepository.findByIdMetaAndIdUsuario(idmeta, idusuario);
    }

    public ResultadoResponse create (Meta meta, Integer idusuario){
        try {
            var validacion = validar(meta, true);

            if (!validacion.success()){
                return validacion;
            }

            Usuario usuario = new Usuario();
            usuario.setIdusuario(idusuario);

            meta.setUsuario(usuario);
            meta.setCompletada(false);

            if (meta.getMontoactual() == null){
                meta.setMontoactual(0.0);
            }

            var registro = metaRepository.save(meta);
            var mensaje = String.format("Meta con ID %s registrada", registro.getIdmeta());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false,"Hubo un error en la transaccion");
        }

    }

    public ResultadoResponse update(Meta meta, Integer idusuario){
        try {
            var original = metaRepository.findByIdMetaAndIdUsuario(meta.getIdmeta(), idusuario);

            if (original == null){
                return new ResultadoResponse(false, "La meta no existe o no pertenece al usuario actual");
            }

            var validacion = validar(meta, false);

            if (!validacion.success()){
                return validacion;
            }

            original.setNombre(meta.getNombre());
            original.setDescripcion(meta.getDescripcion());
            original.setMontoobjetivo(meta.getMontoobjetivo());
            original.setMontoactual(meta.getMontoactual());
            original.setFechaobjetivo(meta.getFechaobjetivo());
            original.setCompletada(meta.getMontoactual() >= meta.getMontoobjetivo());

            var registro = metaRepository.save(original);
            var mensaje = String.format("Meta con ID %s actualizada", registro.getIdmeta());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }



    private ResultadoResponse validar(Meta meta, boolean registroNuevo) {
        if (meta.getNombre() == null || meta.getNombre().isBlank()) {
            return new ResultadoResponse(false, "Debe ingresar el nombre de la meta");
        }

        if (meta.getMontoobjetivo() == null || meta.getMontoobjetivo() <= 0) {
            return new ResultadoResponse(false, "El monto objetivo debe ser mayor a 0");
        }

        if (meta.getMontoactual() == null) {
            meta.setMontoactual(0.0);
        }

        if (meta.getMontoactual() < 0) {
            return new ResultadoResponse(false, "El monto actual no puede ser negativo");
        }

        if (registroNuevo && meta.getMontoactual() >= meta.getMontoactual()) {
            return new ResultadoResponse(false, "El monto actual debe ser menor al monto objetivo");
        }

        if (!registroNuevo && meta.getMontoactual() > meta.getMontoobjetivo()) {
            return new ResultadoResponse(false, "El monto actual no puede superar el monto objetivo");
        }

        if (meta.getFechaobjetivo() == null) {
            return new ResultadoResponse(false, "Debe ingresar la fecha límite");
        }

        if (meta.getFechaobjetivo().isBefore(LocalDate.now())) {
            return new ResultadoResponse(false, "La fecha límite no puede ser anterior a la fecha actual");
        }

        return new ResultadoResponse(true, "Validación correcta");
    }
}
