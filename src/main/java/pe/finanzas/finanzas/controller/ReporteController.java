package pe.finanzas.finanzas.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.finanzas.finanzas.service.ReporteService;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("reporte")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("movimientos")
    public void movimientos(HttpSession httpSession, HttpServletResponse response) throws Exception {

        Integer idTipo = (Integer) httpSession.getAttribute("idTipo");

        //ruta del reporte
        String reportPath = "/reportes/reporte_movimientos_admin.jrxml";;
        //parametros para jasper
        Map<String, Object> params = new HashMap<>();

        if (idTipo ==1) {
            params.put("administrador", httpSession.getAttribute("fullName"));
        }

        //generar reporte
        JasperPrint jasperPrint = reporteService.getJasperPrint(params, reportPath);

        //configuracion de la respuesta http
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition","inline; filename=reporte_movimientos.pdf");

        //exportar a pdf
        OutputStream outputStream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        outputStream.flush();
        outputStream.close();
    }

}
