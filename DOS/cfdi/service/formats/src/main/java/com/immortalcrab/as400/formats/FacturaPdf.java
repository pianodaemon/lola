package com.immortalcrab.as400.formats;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.immortalcrab.as400.parser.PairExtractor;
import com.immortalcrab.as400.request.FacturaRequest;
import com.immortalcrab.qrcode.QRCode;

// import java.io.FileInputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import com.immortalcrab.as400.request.FacturaRequest;
// import com.immortalcrab.as400.parser.PairExtractor;

public class FacturaPdf {

    public static void main(String[] args) {
        try {
            // var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/NV139360-changed2.txt"), StandardCharsets.UTF_8);
            // var facReq = FacturaRequest.render(PairExtractor.go4it(isr));
            // var template = "/tq_carta_porte.jrxml";
            // var output   = "tq_carta_porte.pdf";
            // render(facReq, template, output);

            var fileContent = new String(Files.readAllBytes(Paths.get("/home/userd/Downloads/NV999999.txt")), StandardCharsets.UTF_8);
            System.out.println(fileContent);
            System.out.println("***********------------------------------------------***********");

            var bais = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
            var isr = new InputStreamReader(bais);
            var w = new StringWriter();
            isr.transferTo(w);
            var str = w.toString();
            System.out.println("------------------isr.toString()------------------------------------");
            System.out.println(str);
            str = str.replaceAll("\r\n", "");
            str = str.replaceAll("> <", "><");
            str = str.replaceAll("<>", "< >");
            str = str.replaceAll("<\\.>", "< >");
            str = str.replaceAll("<->", "< >");
            str = str.replaceAll("=====CARTA PORTE===================", "");
            str = str.replaceAll("<SERVICIOS>", "");
            str = str.replaceAll("<COMENTARIOS>", "");
            str = str.replaceAll("<RELACIONADOS>", "");
            str = str.replaceAll("<MERCANCIAS>", "");

            var firstSign = false;
            var sw = new StringWriter();
            for (int i = 0; i < str.length(); i++) {

                char c = str.charAt(i);

                if (c == '>') {
                    if (firstSign) {
                        sw.append(c);
                        sw.append('\n');
                        firstSign = false;
                    } else {
                        sw.append(c);
                        firstSign = true;
                    }
                } else {
                    sw.append(c);
                }
            }
            System.out.println("----------------------sw.toString()--------------------------------");
            System.out.println(sw.toString());
            var bais2 = new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
            var isr2 = new InputStreamReader(bais2);
            var l = PairExtractor.go4it(isr2);
            var facReq = FacturaRequest.render(l);
            var ds = facReq.getDs();
            // hardcode for local testing (sin timbrado)
            ds.put("UUID", "EC8A65FB-ADE0-4497-9990-15FEB46BCCD5"); //TODO: comment hardcode
            ds.put("CDIGITAL_SAT", "00001000000413073350"); //TODO: comment hardcode
            ds.put("FECHSTAMP", "2021-11-22T10:04:05"); //TODO: comment hardcode
            ds.put("SELLO_CFD", "jhkhkhuyguygasdjhIHuhishduiha"); //TODO: comment hardcode
            ds.put("SELLO_SAT", "aYhdfuhIUund78kjnfi"); //TODO: comment hardcode
            ds.put("CADENA_ORIGINAL_TFD", "sdfiooiosdiufoiiusdfouiodsf"); //TODO: comment hardcode
            render(facReq, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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

    private void save(ArrayList<byte[]> in) throws FormatError, StorageError {

        var ds = this.cfdiReq.getDs();
        var serie = (String) ds.get("SERIE");
        var folio = (String) ds.get("FOLIO");
        final String fileName = serie + folio + ".pdf";
        final String fileNameCp = serie + folio + "_CP.pdf";

        this.st.upload("application/pdf", in.get(0).length, fileName, new ByteArrayInputStream(in.get(0)));
        this.st.upload("application/pdf", in.get(1).length, fileNameCp, new ByteArrayInputStream(in.get(1)));
    }

    private ArrayList<byte[]> shape() throws FormatError {

        var pdfBytesList = new ArrayList<byte[]>();

        try {
            var ds = this.cfdiReq.getDs();

            // Translate the cfdi total into text (in Spanish)
            var total = (String) ds.get("TOTAL");
            String[] a = total.split("\\.");
            String num = translateIntegerToSpanish(Long.valueOf(a[0])).toUpperCase();

            if (a.length > 1) {
                num += String.format(" PESOS %s/100 M.N.", a[1]);
            } else {
                num += " PESOS 00/100 M.N.";
            }
            ds.put("TOTAL_LETRA", num);

            // Formatear importes
            var df = new DecimalFormat("###,###.00");
            for (var c : (ArrayList<Map<String, String>>) ds.get("CONCEPTOS")) {
                c.put("DUNIT", df.format(Double.parseDouble(c.get("DUNIT"))));
                c.put("DIMPO", df.format(Double.parseDouble(c.get("DIMPO"))));
            }
            ds.put("SUBTOT", df.format(Double.parseDouble((String) ds.get("SUBTOT"))));
            ds.put("IVA", df.format(Double.parseDouble((String) ds.get("IVA"))));
            ds.put("SUBTOT2", df.format(Double.parseDouble((String) ds.get("SUBTOT2"))));
            ds.put("IVARET", df.format(Double.parseDouble((String) ds.get("IVARET"))));
            ds.put("TOTAL", df.format(Double.parseDouble(total)));

            // Comentarios
            ds.put("COMMENTS", String.join("\n", (ArrayList<String>) ds.get("COMENTARIOS")));

            // QR Code generation
            // String verificaCfdiUrl = String.format("https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?id=%s&re=%s&rr=%s&tt=%s&fe=%s",
            String verificaCfdiUrl = String.format("https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?id=%s&re=%s&rr=%s",
                ds.get("UUID"),
                ds.get("EMIRFC"),
                ds.get("CTERFC")
                // total,
                // ds.get("SELLO_CFD")
            );
            var qrCodeBais = QRCode.generateByteStream(verificaCfdiUrl, 400, 400);
            ds.put("QRCODE", qrCodeBais);

            // Get resources dir
            String resourcesDirVarName = "RESOURCES_DIR";
            String resourcesDir = System.getenv(resourcesDirVarName);
            if (resourcesDir == null) {
                resourcesDir = "/resources";
            }

            // PDF generation (CFDI)
            var cteDir = String.format("%s No. %s, %s, %s, %s, %s, %s, C.P. %s",
                ds.get("CTEDIR"),
                ds.get("CTENUM"),
                ds.get("CTECOL"),
                ds.get("CTELOC"),
                ds.get("CTEMUN"),
                ds.get("CTEEDO"),
                ds.get("CTEPAI"),
                ds.get("CTEZIP")
            );
            var remDir = String.format("%s %s%s%s, %s, %s, %s, C.P. %s %s",
                ds.get("REMDIR"),
                ds.get("REMNUM").equals(" ") ? "" : "No. " + ds.get("REMNUM") + ", ",
                ds.get("REMCOL").equals(" ") ? "" : ds.get("REMCOL") + ", ",
                ds.get("REMLOC"),
                ds.get("REMMUN"),
                ds.get("REMEDO"),
                ds.get("REMPAI"),
                ds.get("REMZIP"),
                ds.get("REMTEL").equals(" ") ? "" : "Tel. " + ds.get("REMTEL")
            );
            var desDir = String.format("%s %s%s%s, %s, %s, %s, C.P. %s %s",
                ds.get("DESDIR"),
                ds.get("DESNUM").equals(" ") ? "" : "No. " + ds.get("DESNUM") + ", ",
                ds.get("DESCOL").equals(" ") ? "" : ds.get("DESCOL") + ", ",
                ds.get("DESLOC"),
                ds.get("DESMUN"),
                ds.get("DESEDO"),
                ds.get("DESPAI"),
                ds.get("DESZIP"),
                ds.get("DESTEL").equals(" ") ? "" : "Tel. " + ds.get("DESTEL")
            );
            ds.put("CTEDIR", cteDir);
            ds.put("REMDIR", remDir);
            ds.put("DESDIR", desDir);
            ds.put("REMTAX", ds.get("REMPAI").equals("MEX") ? "RFC: " + ds.get("REMRFC") : "Reg. IdTrib: " + ds.get("REMTAX"));
            ds.put("DESTAX", ds.get("DESPAI").equals("MEX") ? "RFC: " + ds.get("DESRFC") : "Reg. IdTrib: " + ds.get("DESTAX"));

            ds.put(resourcesDirVarName, resourcesDir);
            var template = new File(resourcesDir + "/tq_carta_porte.jrxml");
            byte[] bytes = Files.readAllBytes(template.toPath());
            var bais = new ByteArrayInputStream(bytes);
            JasperReport jasperReport = JasperCompileManager.compileReport(bais);
            JRDataSource conceptosJR = new JRBeanCollectionDataSource((ArrayList<Map<String, String>>) ds.get("CONCEPTOS"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, ds, conceptosJR);
            pdfBytesList.add(JasperExportManager.exportReportToPdf(jasperPrint));
            // JasperExportManager.exportReportToPdfFile(jasperPrint, "/home/userd/Downloads/NV999999.pdf");

            // PDF generation (Carta Porte)
            qrCodeBais.reset();
            // Se extraen algunos datos de las estructuras anidadas de la CP y se colocan a nivel del ds HashMap (para pasarlos como jrxml parameters)
            var cp = (HashMap<String, Object>) ds.get("CARTAPORTE");
            var ubicaciones = (HashMap<String, Object>) cp.get("UBICACIONES");
            var origen = (HashMap<String, Object>) ubicaciones.get("ORIGEN");
            var destino = (HashMap<String, Object>) ubicaciones.get("DESTINO");
            var mercancias = (HashMap<String, Object>) cp.get("MERCANCIAS");
            var mercanciaList = (ArrayList<HashMap<String, Object>>) mercancias.get("LISTA");
            var autotransporteFederal = (HashMap<String, String>) mercancias.get("AUTOTRANSPORTEFEDERAL");
            var figuraTransporte = (HashMap<String, Object>) cp.get("FIGURATRANSPORTE");
            var operadorList = (ArrayList<HashMap<String, String>>) figuraTransporte.get("OPERADORES");
            var operador = operadorList.get(0);
            ds.put("CPFECHAHORASALIDA", (String) origen.get("FechaHoraSalida"));
            ds.put("CPFECHAHORALLEGADA", (String) destino.get("FechaHoraProgLlegada"));
            ds.put("CPTIPOVIAJE", cp.get("CPTIPOVIAJE"));
            ds.put("TranspInternac", cp.get("TranspInternac"));
            ds.put("EntradaSalidaMerc", cp.get("EntradaSalidaMerc"));
            ds.put("ViaEntradaSalida", cp.get("ViaEntradaSalida"));
            ds.put("TotalDistRec", cp.get("TotalDistRec"));
            ds.put("PESOBRUTOTOTAL", mercancias.get("PESOBRUTOTOTAL"));
            ds.put("PESONETOTOTAL", mercancias.get("PESONETOTOTAL"));
            ds.put("NumTotalMercancias", String.valueOf(mercanciaList.size()));
            ds.put("ConfigVehicular", autotransporteFederal.get("ConfigVehicular"));
            ds.put("CNUMPERMSCT", autotransporteFederal.get("CNUMPERMSCT"));
            ds.put("PlacaVM", autotransporteFederal.get("PlacaVM"));
            ds.put("AnioModeloVM", autotransporteFederal.get("AnioModeloVM"));
            ds.put("CPQSEGRESCIV", autotransporteFederal.get("CPQSEGRESCIV"));
            ds.put("CPQSEGRESCIVN", autotransporteFederal.get("CPQSEGRESCIVN"));
            ds.put("CPSTPOREM", autotransporteFederal.get("CPSTPOREM"));
            ds.put("CPPLACAREM", autotransporteFederal.get("CPPLACAREM"));
            ds.put("RFCOperador", operador.get("RFCOperador"));
            ds.put("NumLicencia", operador.get("NumLicencia"));
            ds.put("NombreOperador", operador.get("NombreOperador"));

            for (var m : mercanciaList) {
                m.put("ValorMercancia", df.format(Double.parseDouble((String) m.get("ValorMercancia"))));
            }

            template = new File(resourcesDir + "/tq_carta_porte_comp.jrxml");
            bytes = Files.readAllBytes(template.toPath());
            bais = new ByteArrayInputStream(bytes);
            jasperReport = JasperCompileManager.compileReport(bais);
            var mercanciasJR = new JRBeanCollectionDataSource(mercanciaList);
            jasperPrint = JasperFillManager.fillReport(jasperReport, ds, mercanciasJR);
            pdfBytesList.add(JasperExportManager.exportReportToPdf(jasperPrint));
            // JasperExportManager.exportReportToPdfFile(jasperPrint, "/home/userd/Downloads/NV999999_CP.pdf");

        } catch (JRException ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when building factura pdf (jasper report). ", ex);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when building factura pdf. ", ex);
        }

        return pdfBytesList;
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
