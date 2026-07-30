package pe.finanzas.finanzas.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.Categoria;
import pe.finanzas.finanzas.model.Presupuesto;
import pe.finanzas.finanzas.model.PresupuestoCategoria;
import pe.finanzas.finanzas.repository.CategoriaRepository;
import pe.finanzas.finanzas.repository.PresupuestoCategoriaRepository;
import pe.finanzas.finanzas.repository.PresupuestoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresupuestoCategoriaService {

    private final PresupuestoCategoriaRepository presupuestoCategoriaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<PresupuestoCategoria> getAll(){
        return presupuestoCategoriaRepository.findAll();
    }

    public List<PresupuestoCategoria> getByPresupuesto(Integer idpresupuesto, Integer idusuario){

        var presupuesto = presupuestoCategoriaRepository.findByIdPresupuestoCategoriaAndIdUsuario(idpresupuesto, idusuario);

        if (presupuesto == null){
            return List.of();
        }

        return presupuestoCategoriaRepository.findByPresupuestoIdPresupuestoOrderByIdPresupuestoCategoriaDesc(idpresupuesto);
    }

    public PresupuestoCategoria getOne(Integer idpresupuestocategoira, Integer idusuario){
        return presupuestoCategoriaRepository.findByIdPresupuestoCategoriaAndIdUsuario(idusuario, idusuario);
    }

    public Double getTotalAsignado(Integer idpresupuesto, Integer idusuario){
        var presupuesto = presupuestoRepository.findByIdPresupuestoAndIdUsuario(idpresupuesto, idusuario);

        if (presupuesto == null){
            return 0.0;
        }
        return calcularTotalAsignadoActivo(idpresupuesto, null);
    }

    public Double getSaldoDisponible(Integer idpresupuesto, Integer idusuario){
        var presupuesto = presupuestoRepository.findByIdPresupuestoAndIdUsuario(idpresupuesto,idusuario);

        if (presupuesto == null){
            return 0.0;
        }

        return presupuesto.getMontototal() - getTotalAsignado(idpresupuesto, idusuario);
    }

    public ResultadoResponse create(PresupuestoCategoria presupuestocategoria, Integer idpresupuesto, Integer idusuario ){
        try {
            var presupuesto = presupuestoRepository.findByIdPresupuestoAndIdUsuario(idpresupuesto, idusuario);

            if (presupuesto == null){
                return new ResultadoResponse(false, "El presupuesto no existe o no pertenece al usuario actual");
            }

            presupuestocategoria.setActivo(true);

            var validacion = validar(presupuestocategoria, presupuesto, null, true);

            if (!validacion.success()){
                return validacion;
            }

            Integer idcategoria = presupuestocategoria.getCategoria().getIdcategoria();

            if (presupuestoCategoriaRepository.existsByPresupuestoIdPresupuestoAndCategoriaIdCategoria(idpresupuesto, idcategoria)){
                return new ResultadoResponse(false, "La categoria ya fue asignada a este presupuesto");
            }

            Presupuesto presupuestobd = new Presupuesto();
            presupuestobd.setIdpresupuesto(idpresupuesto);

            Categoria categoriabd = new Categoria();
            categoriabd.setIdcategoria(idcategoria);

            var registro = presupuestoCategoriaRepository.save(presupuestocategoria);
            var mensaje = String.format("Categoria de presupuesto con ID %s registrada", registro.getIdpresupuestocategoria());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }

    public ResultadoResponse update(PresupuestoCategoria presupuestoCategoria, Integer idusuario){
        try {
            var original = presupuestoCategoriaRepository.findByIdPresupuestoCategoriaAndIdUsuario(presupuestoCategoria.getIdpresupuestocategoria(), idusuario);

            if (original == null){
                return new ResultadoResponse(false, "El detalle de presupuesto no existe o no pertenece al usuario actual");
            }

            boolean activoParaPresupuesto = Boolean.TRUE.equals(original.getActivo());

            var validacion = validar(presupuestoCategoria, original.getPresupuesto(), original.getIdpresupuestocategoria(), activoParaPresupuesto);

            if (!validacion.success()){
                return validacion;
            }

            Integer idpresupuesto = original.getPresupuesto().getIdpresupuesto();
            Integer idcategoria = original.getCategoria().getIdcategoria();

            if (presupuestoCategoriaRepository.existsByPresupuestoIdPresupuestoAndCategoriaIdCategoriaAndIdPresupuestoCategoriaNot(idpresupuesto, idcategoria, original.getIdpresupuestocategoria())){
                return new ResultadoResponse(false, "La categoria ya fue asignada a este presupuesto");
            }

            Categoria categoriabd = new Categoria();
            categoriabd.setIdcategoria(idcategoria);

            original.setCategoria(categoriabd);
            original.setMontoasignado(presupuestoCategoria.getMontoasignado());

            var registro = presupuestoCategoriaRepository.save(original);
            var mensaje = String.format("Categoria de presupuesto con ID %s actualizada", registro.getIdpresupuestocategoria());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }

    @Transactional
    public ResultadoResponse changeActive(Integer idpresupuestocategoria, Integer idusuario){
        var original = presupuestoCategoriaRepository.findByIdPresupuestoCategoriaAndIdUsuario(idpresupuestocategoria,idusuario);

        if (original == null){
            return new ResultadoResponse(false, "El detalle de presupuesto no existe o no pertenece al usuario actual");
        }

        try {
            boolean nuevoestado = !Boolean.TRUE.equals(original.getActivo());

            if (nuevoestado){
                var validacion = validarActivacion(original);

                if (!validacion.success()){
                    return validacion;
                }
            }

            original.setActivo(nuevoestado);

            var estado = nuevoestado ? "activada" : "desactivada";
            var mensaje = String.format("Categeria de presupuesto con ID %s %s", original.getIdpresupuestocategoria(), estado);

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }
    }

    private ResultadoResponse validarActivacion(PresupuestoCategoria presupuestoCategoria) {
        var presupuesto = presupuestoCategoria.getPresupuesto();

        Double totalAsignado = calcularTotalAsignadoActivo(
                presupuesto.getIdpresupuesto(),
                presupuestoCategoria.getIdpresupuestocategoria()
        );

        Double nuevoTotal = totalAsignado + presupuestoCategoria.getMontoasignado();

        if (nuevoTotal > presupuesto.getMontototal()) {
            return new ResultadoResponse(false, "No se puede activar porque la suma asignada superaría el presupuesto total");
        }

        return new ResultadoResponse(true, "Validación correcta");
    }

    private ResultadoResponse validar(
            PresupuestoCategoria presupuestoCategoria,
            Presupuesto presupuesto,
            Integer idDetalleActual,
            boolean activoParaPresupuesto) {

        if (presupuestoCategoria.getCategoria() == null || presupuestoCategoria.getCategoria().getIdcategoria() == null) {
            return new ResultadoResponse(false, "Debe seleccionar una categoría");
        }

        if (presupuestoCategoria.getMontoasignado() == null || presupuestoCategoria.getMontoasignado() <= 0) {
            return new ResultadoResponse(false, "El monto asignado debe ser mayor a 0");
        }

        var categoria = categoriaRepository.findById(presupuestoCategoria.getCategoria().getIdcategoria()).orElse(null);

        if (categoria == null) {
            return new ResultadoResponse(false, "La categoría seleccionada no existe");
        }

        if (!"gasto".equalsIgnoreCase(categoria.getTipo())) {
            return new ResultadoResponse(false, "Solo se pueden asignar categorías de gasto al presupuesto");
        }

        if (categoria.getActivo() == null || !categoria.getActivo()) {
            return new ResultadoResponse(false, "La categoría seleccionada no está activa");
        }

        if (!activoParaPresupuesto) {
            return new ResultadoResponse(true, "Validación correcta");
        }

        Double totalAsignado = calcularTotalAsignadoActivo(
                presupuesto.getIdpresupuesto(),
                idDetalleActual
        );

        Double nuevoTotal = totalAsignado + presupuestoCategoria.getMontoasignado();

        if (nuevoTotal > presupuesto.getMontototal()) {
            return new ResultadoResponse(false, "La suma asignada por categorías no puede superar el presupuesto total");
        }

        return new ResultadoResponse(true, "Validación correcta");
    }





    private Double calcularTotalAsignadoActivo(Integer idPresupuesto, Integer idDetalleActual) {
        Double total = 0.0;

        var detalles = presupuestoCategoriaRepository
                .findByPresupuestoIdPresupuestoOrderByIdPresupuestoCategoriaDesc(idPresupuesto);

        for (PresupuestoCategoria item : detalles) {

            boolean estaActivo = Boolean.TRUE.equals(item.getActivo());
            boolean esDetalleActual = idDetalleActual != null
                    && item.getIdpresupuestocategoria().equals(idDetalleActual);

            if (estaActivo && !esDetalleActual && item.getMontoasignado() != null) {
                total += item.getMontoasignado();
            }
        }

        return total;
    }
}
