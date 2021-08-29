package com.immortalcrab.pdfgen;

import com.immortalcrab.cfdi.parser.FacturaParser;
import com.immortalcrab.numspatrans.Translator;
import com.immortalcrab.cfdi.utils.CadenaOriginal;
import com.immortalcrab.cfdi.utils.Signer;
import com.immortalcrab.qrcode.QRCode;
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
            // Parse the cfdi file
            FacturaParser fp = new FacturaParser(xmlPath);
            Map<String, Object> ds = fp.getDs();

            // Translate the cfdi total into text (in Spanish)
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

            // Build cadena original
            String cadenaOriginal = "";
            try {
                String cfdiXml = CadenaOriginal.readXml("/home/userd/dev/lola/DOS/cfdi/service/signer-py/5b52aef2-c0a7-4267-9f79-85aaeaddb651.xml");
                cadenaOriginal = CadenaOriginal.build(cfdiXml, "/home/userd/dev/lola/DOS/cfdi/service/signer-py/cadenaoriginal_3_3.xslt");
                System.out.println(cadenaOriginal);
    
                if (cadenaOriginal.equals("||...||")) {
                    System.out.println("----------OK-------------");
                } else {
                    System.out.println("ERROR!!!!!!!");
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            // Sign cadena original
            String privateKeyPemPath = "/home/userd/dev/uploads/CSD_Ecatepec_MOBO8001149UA_20180623_002721.pem";
            String sello = "";
            try {
                sello = Signer.signMessage(privateKeyPemPath, cadenaOriginal);
                System.out.println(sello);

            } catch (Exception e) {
                System.out.println(e);
            }
            ds.put("SELLO", sello);

            System.out.println(ds);

            QRCode.generate("34598foijsdof89uj34oij", 1250, 1250, "/home/userd/output.png");

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
