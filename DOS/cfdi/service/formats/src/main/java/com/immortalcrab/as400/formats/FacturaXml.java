package com.immortalcrab.as400.formats;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.as400.request.FacturaRequest;
import com.immortalcrab.as400.parser.PairExtractor;
import java.io.OutputStream;

import mx.gob.sat.cfd._3.ObjectFactory;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoFactor;
import mx.gob.sat.sitio_internet.cfd.catalogos.CUsoCFDI;

public class FacturaXml {

    public static void render(CfdiRequest cfdiReq, Storage st) {
        try {
            var ds = cfdiReq.getDs();
            var cfdiFactory = new ObjectFactory();

            // Comprobante
            var cfdi = cfdiFactory.createComprobante();
            cfdi.setLugarExpedicion((String) ds.get("EMIZIP"));
            cfdi.setMetodoPago(CMetodoPago.fromValue(((String) ds.get("METPAG")).split(":")[0]));
            cfdi.setTipoDeComprobante(CTipoDeComprobante.I);
            cfdi.setTotal(new BigDecimal((String) ds.get("TOTAL")));
            cfdi.setMoneda(CMoneda.fromValue((String) ds.get("MONEDA")));
            cfdi.setCertificado(//TODO: hardcoded
                    "MIIGAjCCA+qgAwIBAgIUMDAwMDEwMDAwMDA0MTEzMTQzNDcwDQYJKoZIhvcNAQELBQAwggGyMTgwNgYDVQQDDC9BLkMuIGRlbCBTZXJ2aWNpbyBkZSBBZG1pbmlzdHJhY2nDs24gVHJpYnV0YXJpYTEvMC0GA1UECgwmU2VydmljaW8gZGUgQWRtaW5pc3RyYWNpw7NuIFRyaWJ1dGFyaWExODA2BgNVBAsML0FkbWluaXN0cmFjacOzbiBkZSBTZWd1cmlkYWQgZGUgbGEgSW5mb3JtYWNpw7NuMR8wHQYJKoZIhvcNAQkBFhBhY29kc0BzYXQuZ29iLm14MSYwJAYDVQQJDB1Bdi4gSGlkYWxnbyA3NywgQ29sLiBHdWVycmVybzEOMAwGA1UEEQwFMDYzMDAxCzAJBgNVBAYTAk1YMRkwFwYDVQQIDBBEaXN0cml0byBGZWRlcmFsMRQwEgYDVQQHDAtDdWF1aHTDqW1vYzEVMBMGA1UELRMMU0FUOTcwNzAxTk4zMV0wWwYJKoZIhvcNAQkCDE5SZXNwb25zYWJsZTogQWRtaW5pc3RyYWNpw7NuIENlbnRyYWwgZGUgU2VydmljaW9zIFRyaWJ1dGFyaW9zIGFsIENvbnRyaWJ1eWVudGUwHhcNMTgwNjIzMDEwMDQ0WhcNMjIwNjIzMDEwMDQ0WjCBojEcMBoGA1UEAxQTT01BUiBNT05URVMgQlJJU0XRTzEcMBoGA1UEKRQTT01BUiBNT05URVMgQlJJU0XRTzEcMBoGA1UEChQTT01BUiBNT05URVMgQlJJU0XRTzEWMBQGA1UELRMNTU9CTzgwMDExNDlVQTEbMBkGA1UEBRMSTU9CTzgwMDExNEhERk5STTA5MREwDwYDVQQLEwhFY2F0ZXBlYzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN/fKY47sz4iqaUcxpWBzBaa7hNkFbPb09FT8kECtUfEsK3Wqp9/sB/CVokBlt7AFRYIhQBzzqgwA5+3IkPjUy+N15qLJlAyyPA7aHJljMmmsa9QNtyoknk9BMhPTOrORiUoXppSdqxe9HvlN/KqESsvXiRyLN7aiLaJmau0rknnz+OUHkWmtZLSwSMdVfoa2iVGszCuV492Ls59BdP0b0lp2xiJetSwHhgZYvsXJh3TIP5rN1OLhr75vIP3sbeaYDa48PkjE3xDWdOTi6/DyOtIqWosw2O9l65WbVVPZJ2cfvo9YtjgGuH9YGH98nJDMq6yvUxShoqzrsesbaBgyT8CAwEAAaMdMBswDAYDVR0TAQH/BAIwADALBgNVHQ8EBAMCBsAwDQYJKoZIhvcNAQELBQADggIBAGHpqpX8gBueUlcy4f0/k2iWDH1ACMvBWsTLnSWjfl4Fuje70VWJ40LuvYdGYET1ri7JnJJLca7YVK3Wc3HKxY2V64mf8c4LG75mnih/QQJwTOWPBX1zhAxkfHljA0Bl1C5kp+DpsMN4N+o70AKFRLkr5lJ88N/hKX08NyLbLDe0B0zUOygsDaEKlWWzWBI5Bq7CjM/Hi+RfXSUwwTf24yl7ofbeYBoxO4///3ddg4Sgl1L7oLFbLHSWqrmqUpzDMMoy8ycTh894+ATsZno+XvgE+i6QLaxXjE4B8DFl0Scn4pHptxN8fg7EFeuh0ll7edeYUW+BMiRI2JgP5S54kcyRdnyUEoFwf5nxKQEApbqZTv2QtEEZ5wUiL4iSkZ7iFa4nwtLIONJecwAhaadIhGC9RwO7PdwOc/85aElRdMJOuL6OczHc8gpY5rpirneyskopwtsmzsJqpwOHMWOzO8S8FcZEUGoMG87ujgsXQGvc7nMXEd++5ZtXmyAKT0OrXeRQNaTHbZn0KfdkvXvEUgSMqKwVr71uZg+dzGVNJGvRY4uRwYCi296kOjHpg5BgmJjSSgzLSBaH9RHDZOQMy7e3wV+H9i3BOIfx1VtoQmU9KBKXvszTsYXTZm19s89yYFXKEBghc0AcgjhawfvZhQ24t0ha2SL7sIksQ1/nsU+M");
            cfdi.setSubTotal(new BigDecimal((String) ds.get("SUBTOT")));
            cfdi.setCondicionesDePago((String) ds.get("CONPAG"));
            cfdi.setNoCertificado((String) ds.get("CDIGITAL"));
            cfdi.setFormaPago(((String) ds.get("FORPAG")).split(":")[0]);
            cfdi.setSello(//TODO: hardcoded
                    "StQ+XBUuN23YX1w5u1NGQMA7pewppUL88ZkiKnNKo+Mo2fEaoSXjIwMNoFREKyXX4NH9SiljdNaVi0bYFdDyFanezMAOzysuL2/2krQTWTWcujANVaHcLFPZ2nBE43BVRrekuo5GfIomD9nCR3j7c336KV5CIJa1vSPJpv+LTx/mkiNW3iR4/vxSIOzAreioewAfnbGqe7r00R14XxxpqIjb4cUg7b9e1t/xVlNtieJFX5iRtVrgV3jFi76JJJQKF+JupDa/gd/MV+u4KweFlDmJ6qIKMchEkTtm7k4SO8WnPVcSYEJloQigauC9EcOs2W1HJOxX44u5FwGPH0Rrlg==");
            cfdi.setVersion("3.3");

            cfdi.setFecha(DatatypeFactory.newInstance().newXMLGregorianCalendar((String) ds.get("FECHOR")));

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

            var conceptos = cfdiFactory.createComprobanteConceptos();
            // Concepto 1
            var concepto = cfdiFactory.createComprobanteConceptosConcepto();
            concepto.setClaveProdServ("81111600");
            concepto.setCantidad(new BigDecimal("1.0"));
            concepto.setClaveUnidad("E48");
            concepto.setUnidad("Servicio");
            concepto.setDescripcion(
                    "DESARROLLO DE SOFTWARE PARA EL SEGUIMIENTO DE OBSERVACIONES DE AUDITORIA POR EL MES DE JUNIO 2020");
            concepto.setValorUnitario(new BigDecimal("20000.00"));
            concepto.setImporte(new BigDecimal("20000.00"));
            // Concepto 1 Impuestos - Traslados
            var conceptoImpuestos = cfdiFactory.createComprobanteConceptosConceptoImpuestos();
            var traslados = cfdiFactory.createComprobanteConceptosConceptoImpuestosTraslados();
            var trasladoList = traslados.getTraslado();
            var traslado = cfdiFactory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
            traslado.setBase(new BigDecimal("20000.00"));
            traslado.setImpuesto("002");
            traslado.setTipoFactor(CTipoFactor.TASA);
            traslado.setTasaOCuota(new BigDecimal("0.160000"));
            traslado.setImporte(new BigDecimal("3200.00"));
            trasladoList.add(traslado);
            conceptoImpuestos.setTraslados(traslados);
            // Concepto 1 Impuestos - Retenciones
            var retenciones = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetenciones();
            var retencionList = retenciones.getRetencion();
            var retencion = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetencionesRetencion();
            retencion.setBase(new BigDecimal("20000.00"));
            retencion.setImpuesto("001");
            retencion.setTipoFactor(CTipoFactor.TASA);
            retencion.setTasaOCuota(new BigDecimal("0.100000"));
            retencion.setImporte(new BigDecimal("2000.00"));
            retencionList.add(retencion);
            conceptoImpuestos.setRetenciones(retenciones);

            concepto.setImpuestos(conceptoImpuestos);
            conceptos.getConcepto().add(concepto);
            cfdi.setConceptos(conceptos);

            // Impuestos
            var impuestos = cfdiFactory.createComprobanteImpuestos();
            impuestos.setTotalImpuestosRetenidos(new BigDecimal("2000.00"));
            impuestos.setTotalImpuestosTrasladados(new BigDecimal("3200.00"));
            var impuestosTraslados = cfdiFactory.createComprobanteImpuestosTraslados();
            var impuestosTrasladoList = impuestosTraslados.getTraslado();
            var impuestosTraslado = cfdiFactory.createComprobanteImpuestosTrasladosTraslado();
            impuestosTraslado.setImpuesto("002");
            impuestosTraslado.setTipoFactor(CTipoFactor.TASA);
            impuestosTraslado.setTasaOCuota(new BigDecimal("0.160000"));
            impuestosTraslado.setImporte(new BigDecimal("3200.00"));
            impuestosTrasladoList.add(impuestosTraslado);
            impuestos.setTraslados(impuestosTraslados);
            var impuestosRetenciones = cfdiFactory.createComprobanteImpuestosRetenciones();
            var impuestosRetencionList = impuestosRetenciones.getRetencion();
            var impuestosRetencion = cfdiFactory.createComprobanteImpuestosRetencionesRetencion();
            impuestosRetencion.setImpuesto("001");
            impuestosRetencion.setImporte(new BigDecimal("2000.00"));
            impuestosRetencionList.add(impuestosRetencion);
            impuestos.setRetenciones(impuestosRetenciones);
            cfdi.setImpuestos(impuestos);

            JAXBContext context = JAXBContext.newInstance("mx.gob.sat.cfd._3:mx.gob.sat.cartaporte");
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.schemaLocation", "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd");
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CfdiNamespaceMapper());
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(cfdi, System.out);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
