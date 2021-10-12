package com.immortalcrab.as400.formats;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.as400.error.FormatError;
import com.immortalcrab.as400.error.StorageError;
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
            String num = translateIntegerToSpanish(Long.valueOf(a[0])).toUpperCase();

            if (a.length > 1) {
                num += String.format(" PESOS %s/100 M.N.", a[1]);
            } else {
                num += " PESOS 00/100 M.N.";
            }
            ds.put("TOTAL_LETRA", num);

            ds.put("FECHSTAMP", "2021-09-28T10:00:00"); //TODO: hardcode
            ds.put("CDIGITAL_SAT", "00001000000509541499"); //TODO: hardcode

            String resourcesDirVarName = "RESOURCES_DIR";
            String resourcesDir = System.getenv(resourcesDirVarName);
            if (resourcesDir == null) {
                resourcesDir = "/resources";
            }
            // QR Code generation
            QRCode.generate("34598foijsdof89uj34oij", 1250, 1250, resourcesDir + "/out_qrcode.png");

            // PDF generation
            ds.put(resourcesDirVarName, resourcesDir);
            var template = new File(resourcesDir + "/tq_carta_porte.jrxml");
            byte[] bytes = Files.readAllBytes(template.toPath());
            var bais = new ByteArrayInputStream(bytes);
            JasperReport jasperReport = JasperCompileManager.compileReport(bais);
            JRDataSource conceptos = new JRBeanCollectionDataSource((ArrayList<Map<String, String>>) ds.get("CONCEPTOS"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, ds, conceptos);
            pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when building factura pdf (jasper report). ", ex);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when building factura pdf. ", ex);
        }

        return pdfBytes;
    }

    public static String translateIntegerToSpanish(long number) throws Exception {

        String resourcesDir = System.getenv("RESOURCES_DIR");
        if (resourcesDir == null) {
            resourcesDir = "/resources";
        }

        var preProps = System.getProperties();
        var postProps = new Properties();
        var argv = new String[] { String.valueOf(number) };
        PyString result;

        var pyScript = new File(resourcesDir + "/numspatrans.py");
        byte[] bytes = Files.readAllBytes(pyScript.toPath());
        var bais = new ByteArrayInputStream(bytes);

        PythonInterpreter.initialize(preProps, postProps, argv);

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.execfile(bais);
            result = (PyString) pyInterp.get("res");
        }
        return result.asString();
    }
}
