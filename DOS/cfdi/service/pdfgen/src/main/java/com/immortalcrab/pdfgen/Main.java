package com.immortalcrab.pdfgen;

import com.immortalcrab.cfdi.parser.FacturaParser;
import com.immortalcrab.numspatrans.Translator;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.util.ResourceUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;


public class Main 
{
    public static void main(String[] args) {

        String xmlPath = "/home/userd/Downloads/NV139010.XML";

        try {
            FacturaParser fp = new FacturaParser(xmlPath);
            Map<String, Object> ds = fp.getDs();

            var s = (String) ds.get("CFDI_TOTAL");
            String[] a = s.split("\\.");
            String num = Translator.translateIntegerToSpanish(Long.valueOf(a[0])).toUpperCase();
            System.out.println(num);

            if (a.length > 1) {
                num += String.format(" PESOS %s/100 M.N.", a[1]);
            } else {
                num += " PESOS 00/100 M.N.";
            }
            ds.put("CFDI_TOTAL_LETRA", num);
            System.out.println(ds);

            JasperReport jasperReport = getJasperReport("tq_carta_porte.jrxml");
            // JasperReport jasperReport = getJasperReport("report.jrxml");
            JRDataSource conceptos = new JRBeanCollectionDataSource((ArrayList<Map<String, String>>) ds.get("CONCEPTOS"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, ds, conceptos);

            JasperExportManager.exportReportToPdfFile(jasperPrint, "NV139010-5.pdf");

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static JasperReport getJasperReport(String jreport) throws FileNotFoundException, JRException {

        File template = ResourceUtils.getFile("classpath:" + jreport);
        return JasperCompileManager.compileReport(template.getAbsolutePath());
    }
}
