package pe.finanzas.finanzas.service;

import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Locale;
import java.util.Map;

@Service
public class ReporteService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JasperPrint getJasperPrint(Map<String, Object> params, String reportPath) throws Exception {
        Connection conn = jdbcTemplate.getDataSource().getConnection();

        try {
            params.put(JRParameter.REPORT_LOCALE, new Locale("es", "PE"));

            InputStream reportStream = this.getClass().getResourceAsStream(reportPath);
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

            return jasperPrint;

        } finally {
            conn.close();
        }

    }

}
