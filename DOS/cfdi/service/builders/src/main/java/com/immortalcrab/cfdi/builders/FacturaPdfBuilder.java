package com.immortalcrab.cfdi.builders;

import com.immortalcrab.cfdi.utils.CadenaOriginal;
import com.immortalcrab.cfdi.utils.Signer;
import com.immortalcrab.numspatrans.Translator;
import com.immortalcrab.qrcode.QRCode;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class FacturaPdfBuilder implements DocBuilder {

    private Map<String, Object> ds;
    private String templateFilename;
    private String outputFilename;

    private FacturaPdfBuilder(final Map<String, Object> ds, final String templateFilename, String outputFilename) {
        this.ds = ds;
        this.templateFilename = templateFilename;
        this.outputFilename = outputFilename;
    }

    static void render(final Map<String, Object> ds, String templateFilename, String outputFilename) {

        FacturaPdfBuilder ic = new FacturaPdfBuilder(ds, templateFilename, outputFilename);
        ic.buildDoc();
    }

    @Override
    public void buildDoc() {

        try {
            // Translate the cfdi total into text (in Spanish)
            var s = (String) ds.get("CFDI_TOTAL");
            String[] a = s.split("\\.");
            String num = Translator.translateIntegerToSpanish(Long.valueOf(a[0])).toUpperCase();

            if (a.length > 1) {
                num += String.format(" PESOS %s/100 M.N.", a[1]);
            } else {
                num += " PESOS 00/100 M.N.";
            }
            ds.put("CFDI_TOTAL_LETRA", num);

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
            InputStream is = Main.class.getResourceAsStream("/" + templateFilename);
            JasperReport jasperReport = JasperCompileManager.compileReport(is);
            JRDataSource conceptos = new JRBeanCollectionDataSource(
                    (ArrayList<Map<String, String>>) ds.get("CONCEPTOS"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, ds, conceptos);
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFilename);

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
}
