package pe.finanzas.finanzas.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.AporteMeta;
import pe.finanzas.finanzas.model.Movimiento;
import pe.finanzas.finanzas.repository.AporteMetaRepository;
import pe.finanzas.finanzas.repository.MovimientoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public List<Movimiento> getAll(){
        return movimientoRepository.findAll();
    }

    public List<Movimiento> getByTipo(String tipo){
        return movimientoRepository.findByCategoriaTipoIgnoreCaseOrderByIdMovimientoDesc(tipo);
    }

    public Movimiento getOne(Integer id){
        return movimientoRepository.findById(id).orElseThrow();
    }

    public ResultadoResponse create(Movimiento movimiento){
        try {
            movimiento.setActivo(true);
            movimientoRepository.save(movimiento);

            return new ResultadoResponse(true, "Movimiento registrado correctamente");
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error enla transaccion");
        }
    }

    public ResultadoResponse update(Movimiento movimiento){
        try {
            if (movimiento.getIdmovimiento() != null
                && aporteMetaRepository.existsByMovimientoIdMovimiento(movimiento.getIdmovimiento())){
                return new ResultadoResponse(false, "Este movimiento pertenece a un aporte de meta y debe editarse desde aportes");
            }
            movimiento.setActivo(true);
            movimientoRepository.save(movimiento);

            return new ResultadoResponse(true, "Moviemiento actualizado correctamente");
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false,"Error al actualizar moviemiento");
        }
    }

    @Transactional
    public ResultadoResponse desactivar(Integer id){
        try {
            if (aporteMetaRepository.existsByMovimientoIdMovimiento(id)){
                return new ResultadoResponse(false, "Este movimiento pertenece a un aporte de meta y debe activarse o desactivarse desde Aportes");
            }

            var movimiento = movimientoRepository.findById(id).orElseThrow();

            movimiento.setActivo(!movimiento.getActivo());

            String estado = movimiento.getActivo() ? "activado" : "desactivado";

            return new ResultadoResponse(true, "Movimiento" + estado + " correctamente");
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false,"Error al cambiar estado del movimiento");
        }
    }
}
