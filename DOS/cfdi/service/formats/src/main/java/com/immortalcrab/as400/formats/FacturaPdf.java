package com.immortalcrab.as400.formats;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.as400.error.FormatError;
import com.immortalcrab.as400.error.StorageError;
import com.immortalcrab.numspatrans.Translator;
import com.immortalcrab.qrcode.QRCode;

// import java.io.FileInputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import com.immortalcrab.as400.request.FacturaRequest;
// import com.immortalcrab.as400.parser.PairExtractor;

public class FacturaPdf {

    // public static void main(String[] args) {
    //     try {
    //         var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/NV139360-changed2.txt"), StandardCharsets.UTF_8);
    //         var facReq = FacturaRequest.render(PairExtractor.go4it(isr));
    //         var template = "/tq_carta_porte.jrxml";
    //         var output   = "tq_carta_porte.pdf";
    //         render(facReq, template, output);

    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    private final CfdiRequest cfdiReq;
    private final Storage st;

    private FacturaPdf(CfdiRequest cfdiReq, Storage st) {
        this.cfdiReq = cfdiReq;
        this.st = st;
    }

    public static void render(CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError {

        FacturaPdf ic = new FacturaPdf(cfdiReq, st);
        ic.save(ic.shape());
    }

    private void save(byte[] in) throws FormatError, StorageError {

        var ds = this.cfdiReq.getDs();
        final String fileName = (String) ds.get("SERIE") + (String) ds.get("FOLIO") + ".pdf";

        this.st.upload("application/pdf", in.length, fileName, new ByteArrayInputStream(in));
    }

    private byte[] shape() throws FormatError {

        byte[] pdfBytes = null;

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

            // QR Code generation
            QRCode.generate("34598foijsdof89uj34oij", 1250, 1250, "/resources/out_qrcode.png");

            // PDF generation
            InputStream is = FacturaPdf.class.getResourceAsStream("/tq_carta_porte.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(is);
            JRDataSource conceptos = new JRBeanCollectionDataSource((ArrayList<Map<String, String>>) ds.get("CONCEPTOS"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, ds, conceptos);
            pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException ex) {
            throw new FormatError("An error occurred when building factura pdf (jasper report). ", ex);

        } catch (Exception ex) {
            throw new FormatError("An error occurred when building factura pdf. ", ex);
        }

        return pdfBytes;
    }
}
