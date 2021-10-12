package com.immortalcrab.as400.formats;

// import java.io.FileInputStream;
// import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.as400.error.FormatError;
import com.immortalcrab.as400.error.StorageError;
import com.immortalcrab.cfdi.utils.CadenaOriginal;
import com.immortalcrab.cfdi.utils.Certificado;
import com.immortalcrab.cfdi.utils.Signer;
// import com.immortalcrab.as400.request.FacturaRequest;
// import com.immortalcrab.as400.parser.PairExtractor;

import org.datacontract.schemas._2004._07.tes_tfd_v33.RespuestaTFD33;
import org.tempuri.IWSCFDI33;
import org.tempuri.WSCFDI33;

import mx.gob.sat.cfd._3.ObjectFactory;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoFactor;
import mx.gob.sat.sitio_internet.cfd.catalogos.CUsoCFDI;

public class FacturaXml {

    // public static void main(String[] args) {
    //     try {
    //         var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/NV139360-changed2.txt"), StandardCharsets.UTF_8);
    //         var facReq = FacturaRequest.render(PairExtractor.go4it(isr));
    //         render(facReq, null);
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    private final CfdiRequest cfdiReq;
    private final Storage st;

    private FacturaXml(CfdiRequest cfdiReq, Storage st) {
        this.cfdiReq = cfdiReq;
        this.st = st;
    }

    public static void render(CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError {

        FacturaXml ic = new FacturaXml(cfdiReq, st);
        StringWriter cfdi = ic.shape();
        ic.save(ic.timbrarCfdi(cfdi));
    }

    private void save(StringWriter sw) throws FormatError, StorageError {

        StringBuffer buf = sw.getBuffer();
        byte[] in = buf.toString().getBytes(StandardCharsets.UTF_8);
        var ds = this.cfdiReq.getDs();

        {
            final String fileName = (String) ds.get("SERIE") + (String) ds.get("FOLIO") + ".xml";

            this.st.upload("text/xml", in.length, fileName, new ByteArrayInputStream(in));
        }
    }

    private StringWriter shape() throws FormatError {

        StringWriter sw = new StringWriter();

        try {
            String resourcesDir = System.getenv("RESOURCES_DIR");
            if (resourcesDir == null) {
                resourcesDir = "/resources";
            }

            var ds = this.cfdiReq.getDs();
            var cfdiFactory = new ObjectFactory();

            // Comprobante
            var cfdi = cfdiFactory.createComprobante();
            cfdi.setLugarExpedicion((String) ds.get("EMIZIP"));
            cfdi.setMetodoPago(CMetodoPago.fromValue(((String) ds.get("METPAG")).split(":")[0]));
            cfdi.setTipoDeComprobante(CTipoDeComprobante.I);
            cfdi.setTotal(new BigDecimal((String) ds.get("TOTAL")));
            cfdi.setMoneda(CMoneda.fromValue((String) ds.get("MONEDA")));
            cfdi.setCertificado(Certificado.readFromFile(resourcesDir + "/pubkey.cer"));
            cfdi.setSubTotal(new BigDecimal((String) ds.get("SUBTOT")));
            cfdi.setCondicionesDePago((String) ds.get("CONPAG"));
            cfdi.setNoCertificado((String) ds.get("CDIGITAL"));
            cfdi.setFormaPago(((String) ds.get("FORPAG")).split(":")[0]);
            cfdi.setVersion("3.3");
            cfdi.setFecha(DatatypeFactory.newInstance().newXMLGregorianCalendar((String) ds.get("FECHOR")));
            cfdi.setSerie((String) ds.get("SERIE"));
            cfdi.setFolio((String) ds.get("FOLIO"));

            // Emisor
            var emisor = cfdiFactory.createComprobanteEmisor();
            emisor.setRfc((String) ds.get("EMIRFC"));
            emisor.setNombre((String) ds.get("EMINOM"));
            emisor.setRegimenFiscal(((String) ds.get("REGIMEN")).split(":")[0]);
            cfdi.setEmisor(emisor);

            // Receptor
            var receptor = cfdiFactory.createComprobanteReceptor();
            receptor.setRfc((String) ds.get("CTERFC"));
            receptor.setNombre((String) ds.get("CTENOM"));
            receptor.setUsoCFDI(CUsoCFDI.fromValue(((String) ds.get("USOCFDI")).split(":")[0]));
            cfdi.setReceptor(receptor);

            // Conceptos
            var conceptos = cfdiFactory.createComprobanteConceptos();
            boolean tieneRetenciones = false;

            for (var c : (ArrayList<Map<String, String>>) ds.get("CONCEPTOS")) {

                var concepto = cfdiFactory.createComprobanteConceptosConcepto();
                concepto.setClaveProdServ(c.get("DCVESERV"));
                concepto.setCantidad(new BigDecimal(c.get("DCANT")));
                concepto.setClaveUnidad(c.get("DCUME"));
                concepto.setUnidad(c.get("DUME"));
                concepto.setDescripcion(c.get("DDESL"));
                concepto.setValorUnitario(new BigDecimal(c.get("DUNIT")));
                concepto.setImporte(new BigDecimal(c.get("DIMPO")));

                // Concepto - Impuestos
                var conceptoImpuestos = cfdiFactory.createComprobanteConceptosConceptoImpuestos();

                // Traslados
                var traslados = cfdiFactory.createComprobanteConceptosConceptoImpuestosTraslados();
                var traslado = cfdiFactory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
                traslado.setBase(new BigDecimal(c.get("DBASE")));
                traslado.setImpuesto(c.get("DITI"));
                traslado.setTipoFactor(CTipoFactor.fromValue(c.get("DITTF")));
                traslado.setTasaOCuota(new BigDecimal(c.get("DITTC")));
                traslado.setImporte(new BigDecimal(c.get("DITIMP")));
                traslados.getTraslado().add(traslado);
                conceptoImpuestos.setTraslados(traslados);

                // Retenciones
                var diri = c.get("DIRI");
                if (diri != null) {
                    tieneRetenciones = true;
                    var retenciones = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetenciones();
                    var retencion = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetencionesRetencion();
                    retencion.setBase(new BigDecimal(c.get("DBASE")));
                    retencion.setImpuesto(diri);
                    retencion.setTipoFactor(CTipoFactor.fromValue(c.get("DIRTF")));
                    retencion.setTasaOCuota(new BigDecimal(c.get("DIRTC")));
                    retencion.setImporte(new BigDecimal(c.get("DIRIMP")));
                    retenciones.getRetencion().add(retencion);
                    conceptoImpuestos.setRetenciones(retenciones);
                }

                concepto.setImpuestos(conceptoImpuestos);
                conceptos.getConcepto().add(concepto);
            }

            cfdi.setConceptos(conceptos);

            // Impuestos
            var impuestos = cfdiFactory.createComprobanteImpuestos();
            impuestos.setTotalImpuestosTrasladados(new BigDecimal((String) ds.get("IVA")));
            impuestos.setTotalImpuestosRetenidos(new BigDecimal((String) ds.get("IVARET")));
            var impuestosTraslados = cfdiFactory.createComprobanteImpuestosTraslados();
            var impuestosTrasladoList = impuestosTraslados.getTraslado();
            var impuestosTraslado = cfdiFactory.createComprobanteImpuestosTrasladosTraslado();
            impuestosTraslado.setImpuesto("002");
            impuestosTraslado.setTipoFactor(CTipoFactor.TASA);
            impuestosTraslado.setTasaOCuota(new BigDecimal(String.format("0.%s0000", (String) ds.get("CIVA"))));
            impuestosTraslado.setImporte(new BigDecimal((String) ds.get("IVA")));
            impuestosTrasladoList.add(impuestosTraslado);
            impuestos.setTraslados(impuestosTraslados);
            if (tieneRetenciones) {
                var impuestosRetenciones = cfdiFactory.createComprobanteImpuestosRetenciones();
                var impuestosRetencionList = impuestosRetenciones.getRetencion();
                var impuestosRetencion = cfdiFactory.createComprobanteImpuestosRetencionesRetencion();
                impuestosRetencion.setImpuesto("002");
                impuestosRetencion.setImporte(new BigDecimal((String) ds.get("IVARET")));
                impuestosRetencionList.add(impuestosRetencion);
                impuestos.setRetenciones(impuestosRetenciones);
            }
            cfdi.setImpuestos(impuestos);

            // JAXBContext context = JAXBContext.newInstance("mx.gob.sat.cfd._3:mx.gob.sat.cartaporte");
            JAXBContext context = JAXBContext.newInstance("mx.gob.sat.cfd._3");
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.schemaLocation", "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd");
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CfdiNamespaceMapper());
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(cfdi, sw);

            var cadenaOrig = CadenaOriginal.build(sw.toString(), resourcesDir + "/cadenaoriginal_3_3.xslt");
            var sello = Signer.signMessage(resourcesDir + "/privkey.pem", cadenaOrig);
            cfdi.setSello(sello);
            sw = new StringWriter();
            marshaller.marshal(cfdi, sw);

        } catch (JAXBException | DatatypeConfigurationException ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when forming the factura xml", ex);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when reading Certificado from file", ex);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FormatError("An error occurred when building Cadena Original", ex);
        }

        return sw;
    }

    private StringWriter timbrarCfdi(StringWriter cfdi) throws FormatError {

        var ds = this.cfdiReq.getDs();
        String[] arrCreds = null;

        try {
            String resourcesDir = System.getenv("RESOURCES_DIR");
            if (resourcesDir == null) {
                resourcesDir = "/resources";
            }

            var file = new File(resourcesDir + "/fel-credentials.txt");
            byte[] bytes = Files.readAllBytes(file.toPath());
            var creds = new String(bytes, "UTF-8");
            arrCreds = creds.split("\\|\\|");

        } catch (Exception e) {
            e.printStackTrace();
            throw new FormatError("Couldn't get PAC credentials", e);
        }

        WSCFDI33 ws = new WSCFDI33();
        IWSCFDI33 iws = ws.getSoapHttpEndpoint();
        RespuestaTFD33 res = iws.timbrarCFDI(arrCreds[0], arrCreds[1].trim(), cfdi.toString(), (String) ds.get("SERIE") + (String) ds.get("FOLIO"));

        var resultStr = String.format("Codigo respuesta: %s\nMensaje: %s\nMensaje (detallado): %s",
                res.getCodigoRespuesta().getValue(),
                res.getMensajeError().getValue(),
                res.getMensajeErrorDetallado().getValue());

        if (res.isOperacionExitosa()) {
            System.out.println(resultStr);

            String uuid = res.getTimbre().getValue().getUUID().getValue();
            ds.put("UUID", uuid);
            System.out.println("UUID: " + uuid);

            String xmlTimbrado = res.getXMLResultado().getValue();
            System.out.println(xmlTimbrado);

            var cfdiTimbrado = new StringWriter();
            cfdiTimbrado.write(xmlTimbrado);
            return cfdiTimbrado;

        } else {
            throw new FormatError("An error occurred when PAC tried to sign the cfdi.\n" + resultStr);
        }
    }
}
