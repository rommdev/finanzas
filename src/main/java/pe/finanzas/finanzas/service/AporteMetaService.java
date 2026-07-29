package pe.finanzas.finanzas.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import pe.finanzas.finanzas.dto.ResultadoResponse;
import pe.finanzas.finanzas.model.*;
import pe.finanzas.finanzas.repository.AporteMetaRepository;
import pe.finanzas.finanzas.repository.CategoriaRepository;
import pe.finanzas.finanzas.repository.MetaRepository;
import pe.finanzas.finanzas.repository.MovimientoRepository;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AporteMetaService {

    private final AporteMetaRepository aporteMetaRepository;
    private final MetaRepository metaRepository;
    private final MovimientoRepository movimientoRepository;
    private final CategoriaRepository categoriaRepository;

    private static final String CATEGORIA_APORTE_META ="Aporte a meta";
    private static final String TIPO_INGRESO = "ingreso";

    public List<AporteMeta> getByMeta(Integer idmeta, Integer idusuario){
        var meta = metaRepository.findByIdMetaAndIdUsuario(idmeta, idusuario);

        if (meta == null){
            return List.of();
        }
        return aporteMetaRepository.findByMetaIdOrderByIdAporteDesc(idmeta);
    }

    public AporteMeta getOne(Integer idaporte, Integer idusuario){
        return aporteMetaRepository.findByIdAporteAndIdUsuario(idaporte, idusuario);
    }

    @Transactional
    public ResultadoResponse create (AporteMeta aporteMeta, Integer idmeta, Integer idusuario) {
        try {
           var meta = metaRepository.findByIdMetaAndIdUsuario(idmeta, idusuario);

            if (meta == null) {
                return new ResultadoResponse(false, "La meta no existe o no pertenece al usuario actual");
            }
            aporteMeta.setActivo(true);

            var validacion = validar(aporteMeta, meta, true);

            if (!validacion.success()) {
                return validacion;
            }

            var categoria =  getCategoriaAporteMeta();

            if (categoria == null) {
                return new ResultadoResponse(false, "Debe existir una categoria activa ded ingreso llamada Aporte a meta");
            }

            Meta metabd = new Meta();
            metabd.setIdmeta(idmeta);
            aporteMeta.setMeta(metabd);

            var movimiento = crearMovimientoIngreso(aporteMeta, meta, categoria, idusuario);
            aporteMeta.setMovimiento(movimiento);

            actualizarMontoMeta(meta, aporteMeta.getMontoaporte());

            var registro = aporteMetaRepository.save(aporteMeta);
            var mensaje = String.format("Aporte con ID %s registrado como ingreso", registro.getIdaporte());


            return new ResultadoResponse(true, mensaje);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }

    }

    @Transactional
    public ResultadoResponse update(AporteMeta aportemeta, Integer idusuario){
        try {
            var original = aporteMetaRepository.findByIdAporteAndIdUsuario(aportemeta.getIdaporte(), idusuario);

            if (original == null) {
                return new ResultadoResponse(false, "El aporte no existe o no pertenecec al usuario actual");
            }

            var meta = original.getMeta();
            var validacion = validar(aportemeta, meta, false);

            if (!validacion.success()) {
                return validacion;
            }
            if (getCategoriaAporteMeta() == null) {
                return new ResultadoResponse(false, "Debe existir una categoria activa de ingreso llamada aporte a meta");
            }

            if (Boolean.TRUE.equals(original.getActivo())) {
                Double montoAnterior = original.getMontoaporte() == null ? 0.0 : original.getMontoaporte();
                Double montoNuevo = aportemeta.getMontoaporte() == null ? 0.0 : aportemeta.getMontoaporte();
                Double diferencia = montoNuevo - montoAnterior;
                Double montoActual = meta.getMontoactual() == null ? 0.0 : meta.getMontoactual();

                if (montoActual + diferencia > meta.getMontoobjetivo()) {
                    return new ResultadoResponse(false, "El aporte no puede superar el monto restante de la meta");
                }

                actualizarMontoMeta(meta, diferencia);
            }

            original.setFecha(aportemeta.getFecha());
            original.setMontoaporte(aportemeta.getMontoaporte());
            original.setObservacion(aportemeta.getObservacion());

            var movimientoResponse = sincronizarMovimiento(original, meta, idusuario);

            if (!movimientoResponse.success()) {
                return movimientoResponse;
            }

            var registro = aporteMetaRepository.save(original);
            var mensaje = String.format("Aporte con ID %s actualizado", registro.getIdaporte());

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false,"Hubo un error en la transaccion");
        }
    }

    @Transactional
    public ResultadoResponse changeActive(Integer idaporte, Integer idusuario){
        try {
            var aporte = aporteMetaRepository.findByIdAporteAndIdUsuario(idaporte, idusuario);

            if (aporte == null){
                return new ResultadoResponse(false, "El aporte no existe o no pertenece al usuario actual");
            }

            var meta = aporte.getMeta();
            boolean nuevoEstado = !Boolean.TRUE.equals(aporte.getActivo());

            if (getCategoriaAporteMeta() == null){
                return new ResultadoResponse(false, "Debe existir una categoria activa de ingreso llamada aporte a meta");
            }

            if (nuevoEstado){
                var validacion = validarActivacion(aporte, meta);

                if (!validacion.success()){
                    return validacion;
                }

                actualizarMontoMeta(meta,aporte.getMontoaporte());
            }else {
                Double monto = aporte.getMontoaporte() == null ? 0.0 : aporte.getMontoaporte();
                actualizarMontoMeta(meta, monto * -1);
            }

            aporte.setActivo(nuevoEstado);

            var movimientoResponse = sincronizarMovimiento(aporte, meta, idusuario);

            if (!movimientoResponse.success()){
                return movimientoResponse;
            }
            aporteMetaRepository.save(aporte);

            var estado = nuevoEstado ? "activado" : "desactivado";
            var mensaje = String.format("Aporte con ID %s %s", aporte.getIdaporte(), estado);

            return new ResultadoResponse(true, mensaje);
        }catch (Exception e){
            e.printStackTrace();
            return new ResultadoResponse(false, "Hubo un error en la transaccion");
        }


    }

    private ResultadoResponse validarActivacion(AporteMeta aportemeta, Meta meta) {
        if (meta.getFechaobjetivo() != null && LocalDate.now().isAfter(meta.getFechaobjetivo())) {
            return new ResultadoResponse(false, "No se puede activar aportes después de la fecha límite");
        }

        Double montoActual = meta.getMontoactual() == null ? 0.0 : meta.getMontoactual();
        Double montoAporte = aportemeta.getMontoaporte() == null ? 0.0 : aportemeta.getMontoaporte();

        if (montoActual + montoAporte > meta.getMontoobjetivo()) {
            return new ResultadoResponse(false, "No se puede activar porque el aporte superaría el monto objetivo de la meta");
        }

        return new ResultadoResponse(true, "Validación correcta");
    }


    private void actualizarMontoMeta(Meta meta, Double diferencia) {
        Double montoActual = meta.getMontoactual() == null ? 0.0 : meta.getMontoactual();
        Double nuevoMonto = montoActual + diferencia;

        if (nuevoMonto < 0) {
            nuevoMonto = 0.0;
        }

        meta.setMontoactual(nuevoMonto);
        meta.setCompletada(nuevoMonto >= meta.getMontoactual());
    }


    private ResultadoResponse validar(AporteMeta aporteMeta, Meta meta, boolean afectaMeta){
        if (aporteMeta.getFecha() == null){
            return new ResultadoResponse(false, "Debe ingresar la fecha del aporte");
        }

        if (aporteMeta.getMontoaporte() == null || aporteMeta.getMontoaporte()<=0){
            return new ResultadoResponse(false, "El monto del aporte debe ser mayor a 0");
        }

        if (meta.getFechaobjetivo() != null && LocalDate.now().isAfter(meta.getFechaobjetivo())){
            return new ResultadoResponse(false, "No se puede registrar o editar aportes despues de la fecha limite");
        }

        if (meta.getFechaobjetivo() != null && aporteMeta.getFecha().isAfter(meta.getFechaobjetivo())){
            return new ResultadoResponse(false, "La fecha del aporte no puede ser posterior a la fecha limite de la meta");
        }

        if (afectaMeta){
            Double montoactual = meta.getMontoactual() == null ? 0.0 : meta.getMontoactual();
            Double montoaporte = aporteMeta.getMontoaporte() == null ? 0.0 : aporteMeta.getMontoaporte();

            if (montoactual + montoaporte > meta.getMontoobjetivo()){
                return new ResultadoResponse(false, "El aporte no puede superar el monto restante de la meta");
            }
        }
        return new ResultadoResponse(true, "Validacion correcta");
    }

    private Categoria getCategoriaAporteMeta() {
        return categoriaRepository.findByDescripcionIgnoreCaseAndTipoIgnoreCaseAndActivoTrue(
                CATEGORIA_APORTE_META,
                TIPO_INGRESO
        );
    }
    private Movimiento crearMovimientoIngreso(AporteMeta aportemeta, Meta meta, Categoria categoria, Integer idusuario) {
        Usuario usuario = new Usuario();
        usuario.setIdusuario(idusuario);

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(aportemeta.getFecha());
        movimiento.setDescripcion("Aporte a meta: " + meta.getNombre());
        movimiento.setMonto(aportemeta.getMontoaporte());
        movimiento.setActivo(Boolean.TRUE.equals(aportemeta.getActivo()));
        movimiento.setCategoria(categoria);
        movimiento.setUsuario(usuario);

        return movimientoRepository.save(movimiento);
    }

    private ResultadoResponse sincronizarMovimiento(AporteMeta aportemeta, Meta meta, Integer idusuario) {
        var categoria = getCategoriaAporteMeta();

        if (categoria == null) {
            return new ResultadoResponse(false, "Debe existir una categoría activa de ingreso llamada Aporte a meta");
        }

        Movimiento movimiento = aportemeta.getMovimiento();

        if (movimiento == null) {
            movimiento = crearMovimientoIngreso(aportemeta, meta, categoria, idusuario);
            aportemeta.setMovimiento(movimiento);
            return new ResultadoResponse(true, "Movimiento creado");
        }

        Usuario usuario = new Usuario();
        usuario.setIdusuario(idusuario);

        movimiento.setFecha(aportemeta.getFecha());
        movimiento.setDescripcion("Aporte a meta: " + meta.getNombre());
        movimiento.setMonto(aportemeta.getMontoaporte());
        movimiento.setActivo(Boolean.TRUE.equals(aportemeta.getActivo()));
        movimiento.setCategoria(categoria);
        movimiento.setUsuario(usuario);

        movimientoRepository.save(movimiento);

        return new ResultadoResponse(true, "Movimiento sincronizado");
    }


}
