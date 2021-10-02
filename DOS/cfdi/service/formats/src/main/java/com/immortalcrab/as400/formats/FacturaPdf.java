package com.immortalcrab.as400.formats;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.cfdi.utils.CadenaOriginal;
import com.immortalcrab.cfdi.utils.Signer;
import com.immortalcrab.numspatrans.Translator;
import com.immortalcrab.qrcode.QRCode;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.immortalcrab.as400.request.FacturaRequest;
import com.immortalcrab.as400.parser.PairExtractor;

public class FacturaPdf {

    public static void main(String[] args) {
        try {
            var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/NV139360-changed2.txt"), StandardCharsets.UTF_8);
            var facReq = FacturaRequest.render(PairExtractor.go4it(isr));
            var template = "/tq_carta_porte.jrxml";
            var output   = "tq_carta_porte.pdf";
            render(facReq, template, output);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void render(final CfdiRequest cfdiReq, String pdfTemplate, String output) {
    // public static void render(final CfdiRequest cfdiReq, Storage st) {

        try {
            var ds = cfdiReq.getDs();
            // Translate the cfdi total into text (in Spanish)
            var s = (String) ds.get("TOTAL");
            String[] a = s.split("\\.");
            String num = Translator.translateIntegerToSpanish(Long.valueOf(a[0])).toUpperCase();

            if (a.length > 1) {
                num += String.format(" PESOS %s/100 M.N.", a[1]);
            } else {
                num += " PESOS 00/100 M.N.";
            }
            ds.put("TOTAL_LETRA", num);
            ds.put("UUID", "5b52aef2-c0a7-4267-9f79-85aaeaddb651"); //TODO: hardcode
            ds.put("FECHSTAMP", "2021-09-28T10:00:00"); //TODO: hardcode
            ds.put("CDIGITAL_SAT", "00001000000509541499"); //TODO: hardcode

            // Build cadena original
            String cfdiXml = CadenaOriginal.readXml(
                    "/home/userd/dev/lola/DOS/cfdi/service/signer-py/5b52aef2-c0a7-4267-9f79-85aaeaddb651.xml");
            String cadenaOriginal = CadenaOriginal.build(cfdiXml,
                    "/home/userd/dev/lola/DOS/cfdi/service/signer-py/cadenaoriginal_3_3.xslt");
            System.out.println(cadenaOriginal);

            // Sign cadena original
            String privateKeyPemPath = "/home/userd/dev/uploads/CSD_Ecatepec_MOBO8001149UA_20180623_002721.pem";
            String sello = Signer.signMessage(privateKeyPemPath, cadenaOriginal);
            System.out.println(sello);
            ds.put("SELLO", sello);

            // QR Code generation
            QRCode.generate("34598foijsdof89uj34oij", 1250, 1250, "/home/userd/output.png");

            System.out.println(ds);

            // PDF generation
            InputStream is = FacturaPdf.class.getResourceAsStream(pdfTemplate);
            // InputStream is = FacturaPdf.class.getResourceAsStream("/tq_carta_porte.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(is);
            JRDataSource conceptos = new JRBeanCollectionDataSource(
                    (ArrayList<Map<String, String>>) ds.get("CONCEPTOS"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, ds, conceptos);
            JasperExportManager.exportReportToPdfFile(jasperPrint, output);
            // JasperExportManager.exportReportToPdfFile(jasperPrint, "tq_carta_porte.pdf");

        } catch (IOException ex) {
            Logger.getLogger(FacturaPdf.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FacturaPdf.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SAXException ex) {
            Logger.getLogger(FacturaPdf.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(FacturaPdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
