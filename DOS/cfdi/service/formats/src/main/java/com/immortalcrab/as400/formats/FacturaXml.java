package com.immortalcrab.as400.formats;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.datatype.DatatypeConfigurationException;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.as400.error.FormatError;
import com.immortalcrab.as400.error.StorageError;
import com.immortalcrab.cfdi.utils.CadenaOriginal;
import com.immortalcrab.cfdi.utils.Certificado;
import com.immortalcrab.cfdi.utils.Signer;
import com.immortalcrab.as400.request.FacturaRequest;
import com.immortalcrab.as400.parser.PairExtractor;

import org.datacontract.schemas._2004._07.tes_tfd_v33.RespuestaTFD33;
import org.datacontract.schemas._2004._07.tes_tfd_v33.Timbre33;
import org.tempuri.IWSCFDI33;
import org.tempuri.WSCFDI33;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mx.gob.sat.cfd._3.ObjectFactory;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitio_internet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitio_internet.cfd.catalogos.CPais;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.sitio_internet.cfd.catalogos.CTipoFactor;
import mx.gob.sat.sitio_internet.cfd.catalogos.CUsoCFDI;
import mx.gob.sat.sitio_internet.cfd.catalogos.cartaporte.CConfigAutotransporte;
import mx.gob.sat.sitio_internet.cfd.catalogos.cartaporte.CTipoPermiso;

public class FacturaXml {

    public static void main(String[] args) {
        try {
            // var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/NV139360-changed.txt"), StandardCharsets.UTF_8);
            // var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/facmock.txt"), StandardCharsets.UTF_8);
            // var l = PairExtractor.go4it("/home/userd/Downloads/NV139360-cartaporte.txt");
            // var l = PairExtractor.go4it(isr);
            var fileContent = new String(Files.readAllBytes(Paths.get("/home/userd/Downloads/NV140573 v2 211112.txt")), StandardCharsets.UTF_8);
            System.out.println(fileContent);
            System.out.println("***********------------------------------------------***********");
            // fileContent = fileContent.replaceAll("\r\n", "");
            // fileContent = fileContent.replaceAll("> <", "><");
            // fileContent = fileContent.replaceAll("<>", "< >");
            // fileContent = fileContent.replaceAll("=====CARTA PORTE===================", "");

            // var firstSign = false;
            // var sw = new StringWriter();
            // for (int i = 0; i < fileContent.length(); i++) {

            //     char c = fileContent.charAt(i);

            //     if (c == '>') {
            //         if (firstSign) {
            //             sw.append(c);
            //             sw.append('\n');
            //             firstSign = false;
            //         } else {
            //             sw.append(c);
            //             firstSign = true;
            //         }
            //     } else {
            //         sw.append(c);
            //     }
            // }

            // System.out.println(sw.toString());
            // var bais = new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
            // var isr = new InputStreamReader(bais);
            // var l = PairExtractor.go4it(isr);
            // var facReq = FacturaRequest.render(l);
            // render(facReq, null);

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
            render(facReq, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
        // ic.save(cfdi);
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
            // var cp = (HashMap<String, Object>) ds.get("CARTAPORTE");
            // var cpMercancias = (HashMap<String, Object>) cp.get("MERCANCIAS");
            // var cpMercanciaList = (ArrayList<HashMap<String, String>>) cpMercancias.get("LISTA");
            // int idx = 0;
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
                // var informacionAduanera = cfdiFactory.createComprobanteConceptosConceptoInformacionAduanera();
                // informacionAduanera.setNumeroPedimento(cpMercanciaList.get(idx++).get("Pedimento"));
                // concepto.getInformacionAduanera().add(informacionAduanera);
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

            // // Complementos
            // var complementoList = cfdi.getComplemento();
            // var complemento = cfdiFactory.createComprobanteComplemento();
            // List<Object> l = complemento.getAny();

            // // Complemento - Carta Porte
            // var cp = (HashMap<String, Object>) ds.get("CARTAPORTE");
            // var cartaPorteFactory = new mx.gob.sat.cartaporte.ObjectFactory();
            // var cartaPorte = cartaPorteFactory.createCartaPorte();
            // cartaPorte.setVersion("1.0");
            // cartaPorte.setTranspInternac((String) cp.get("TranspInternac"));
            // cartaPorte.setTotalDistRec(new BigDecimal((String) cp.get("TotalDistRec")));
            // cartaPorte.setEntradaSalidaMerc((String) cp.get("EntradaSalidaMerc"));
            // cartaPorte.setViaEntradaSalida((String) cp.get("ViaEntradaSalida"));

            // // Complemento - Carta Porte - Ubicaciones
            // var cpUbicaciones = (HashMap<String, Object>) cp.get("UBICACIONES");
            // var ubicaciones = cartaPorteFactory.createCartaPorteUbicaciones();
            // var ubicacionList = ubicaciones.getUbicacion();

            // // Complemento - Carta Porte - Ubicaciones - Ubicación - Origen
            // var cpOrigen = (HashMap<String, String>) cpUbicaciones.get("ORIGEN");
            // var ubicacion = cartaPorteFactory.createCartaPorteUbicacionesUbicacion();
            // // ubicacion.setTipoEstacion("01");
            // ubicacion.setDistanciaRecorrida(new BigDecimal("1000"));
            // var origen = cartaPorteFactory.createCartaPorteUbicacionesUbicacionOrigen();
            // origen.setFechaHoraSalida(DatatypeFactory.newInstance().newXMLGregorianCalendar(cpOrigen.get("FechaHoraSalida")));
            // origen.setNombreRemitente(cpOrigen.get("NombreRemitente"));
            // origen.setNumRegIdTrib("202452070");
            // origen.setResidenciaFiscal(CPais.fromValue(cpOrigen.get("ResidenciaFiscal")));
            // ubicacion.setOrigen(origen);
            // var domicilio = cartaPorteFactory.createCartaPorteUbicacionesUbicacionDomicilio();
            // domicilio.setCalle(cpOrigen.get("Calle"));
            // domicilio.setNumeroExterior(cpOrigen.get("NumeroExterior"));
            // domicilio.setColonia(cpOrigen.get("Colonia"));
            // domicilio.setLocalidad(cpOrigen.get("Localidad"));
            // domicilio.setMunicipio(cpOrigen.get("Municipio"));
            // domicilio.setEstado(cpOrigen.get("Estado"));
            // domicilio.setPais(CPais.fromValue(cpOrigen.get("Pais")));
            // domicilio.setCodigoPostal(cpOrigen.get("CodigoPostal"));
            // ubicacion.setDomicilio(domicilio);
            // ubicacionList.add(ubicacion);

            // // Complemento - Carta Porte - Ubicaciones - Ubicación - Destino
            // var cpDestino = (HashMap<String, String>) cpUbicaciones.get("DESTINO");
            // ubicacion = cartaPorteFactory.createCartaPorteUbicacionesUbicacion();
            // ubicacion.setTipoEstacion("03");
            // ubicacion.setDistanciaRecorrida(new BigDecimal("242"));
            // var destino = cartaPorteFactory.createCartaPorteUbicacionesUbicacionDestino();
            // destino.setFechaHoraProgLlegada(DatatypeFactory.newInstance().newXMLGregorianCalendar(cpDestino.get("FechaHoraProgLlegada")));
            // ubicacion.setDestino(destino);
            // domicilio = cartaPorteFactory.createCartaPorteUbicacionesUbicacionDomicilio();
            // domicilio.setCalle(cpDestino.get("Calle"));
            // domicilio.setNumeroExterior(cpDestino.get("NumeroExterior"));
            // domicilio.setColonia(cpDestino.get("Colonia"));
            // domicilio.setMunicipio(cpDestino.get("Municipio"));
            // domicilio.setEstado(cpDestino.get("Estado"));
            // domicilio.setPais(CPais.fromValue(cpDestino.get("Pais")));
            // domicilio.setCodigoPostal(cpDestino.get("CodigoPostal"));
            // ubicacion.setDomicilio(domicilio);
            // ubicacionList.add(ubicacion);
            // cartaPorte.setUbicaciones(ubicaciones);

            // // Complemento - Carta Porte - Mercancías
            // var cpMercancias = (HashMap<String, Object>) cp.get("MERCANCIAS");
            // var cpMercanciaList = (ArrayList<HashMap<String, String>>) cpMercancias.get("LISTA");
            // var mercancias = cartaPorteFactory.createCartaPorteMercancias();
            // mercancias.setNumTotalMercancias(cpMercanciaList.size());
            // mercancias.setUnidadPeso("KGM");
            // var mercanciaList = mercancias.getMercancia();

            // for (HashMap<String, String> item : cpMercanciaList) {
            //     var mercancia = cartaPorteFactory.createCartaPorteMercanciasMercancia();
            //     mercancia.setBienesTransp(item.get("BienesTransp"));
            //     mercancia.setCantidad(new BigDecimal(item.get("Cantidad")));
            //     mercancia.setClaveUnidad(item.get("ClaveUnidad"));
            //     mercancia.setUnidad(item.get("Unidad"));
            //     mercancia.setPesoEnKg(new BigDecimal(item.get("PesoEnKg")));
            //     mercancia.setFraccionArancelaria("8544429901");
            //     mercanciaList.add(mercancia);
            // }

            // var cpAutotransporteFederal = (HashMap<String, String>) cpMercancias.get("AUTOTRANSPORTEFEDERAL");
            // var autotransporteFederal = cartaPorteFactory.createCartaPorteMercanciasAutotransporteFederal();
            // autotransporteFederal.setPermSCT(CTipoPermiso.TPAF_01);
            // autotransporteFederal.setNumPermisoSCT("45245555");
            // autotransporteFederal.setNombreAseg("asaa");
            // autotransporteFederal.setNumPolizaSeguro("1245");
            // var identificacionVehicular = cartaPorteFactory.createCartaPorteMercanciasAutotransporteFederalIdentificacionVehicular();
            // identificacionVehicular.setConfigVehicular(CConfigAutotransporte.fromValue(cpAutotransporteFederal.get("ConfigVehicular")));
            // identificacionVehicular.setPlacaVM(cpAutotransporteFederal.get("PlacaVM"));
            // identificacionVehicular.setAnioModeloVM(Integer.parseInt(cpAutotransporteFederal.get("AnioModeloVM")));
            // autotransporteFederal.setIdentificacionVehicular(identificacionVehicular);
            // mercancias.setAutotransporteFederal(autotransporteFederal);
            // cartaPorte.setMercancias(mercancias);

            // // Complemento - Carta Porte - Figura Transporte
            // var cpFiguraTransporte = (HashMap<String, Object>) cp.get("FIGURATRANSPORTE");
            // var cpOperadorList = (ArrayList<HashMap<String, String>>) cpFiguraTransporte.get("OPERADORES");
            // var figuraTransporte = cartaPorteFactory.createCartaPorteFiguraTransporte();
            // figuraTransporte.setCveTransporte("01");
            // var operadoresList = figuraTransporte.getOperadores();
            // var operadores = cartaPorteFactory.createCartaPorteFiguraTransporteOperadores();
            // operadoresList.add(operadores);
            // var operadorList = operadores.getOperador();

            // for (HashMap<String, String> item : cpOperadorList) {
            //     var operador = cartaPorteFactory.createCartaPorteFiguraTransporteOperadoresOperador();
            //     operador.setNumLicencia(item.get("NumLicencia"));
            //     operador.setNombreOperador(item.get("NombreOperador"));
            //     operador.setRFCOperador(item.get("RFCOperador"));
            //     operadorList.add(operador);
            // }

            // cartaPorte.setFiguraTransporte(figuraTransporte);

            // l.add(cartaPorte);
            // complementoList.add(complemento);

            // Hacer el marshalling del cfdi object
            // JAXBContext context = JAXBContext.newInstance("mx.gob.sat.cfd._3:mx.gob.sat.cartaporte");
            JAXBContext context = JAXBContext.newInstance("mx.gob.sat.cfd._3");
            Marshaller marshaller = context.createMarshaller();
            // marshaller.setProperty("jaxb.schemaLocation", "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd http://www.sat.gob.mx/CartaPorte http://www.sat.gob.mx/sitio_internet/cfd/CartaPorte/CartaPorte.xsd");
            marshaller.setProperty("jaxb.schemaLocation", "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd");
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CfdiNamespaceMapper());
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(cfdi, sw);

            // Armar la cadena original del comprobante + complemento de carta porte
            var cfdiXml = sw.toString();
            var cadenaOrig = CadenaOriginal.build(cfdiXml, resourcesDir + "/cadenaoriginal_3_3.xslt");
            // var str1 = cadenaOrig.replaceAll(" \\|\\|", " ").trim();
            // var cadenaOrigCartaPorte = CadenaOriginal.build(cfdiXml, resourcesDir + "/CartaPorte.xslt");
            // var str2 = cadenaOrigCartaPorte.replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>", " ").trim() + "||";
            // var nuevaCadenaOrig = str1 + str2;

            // Sellar cadena original
            // var sello = Signer.signMessage(resourcesDir + "/privkey.pem", nuevaCadenaOrig);
            var sello = Signer.signMessage(resourcesDir + "/privkey.pem", cadenaOrig);
            cfdi.setSello(sello);
            
            sw = new StringWriter();
            marshaller.marshal(cfdi, sw);
            System.out.println(sw.toString());

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

    private StringWriter timbrarCfdi(StringWriter cfdiSw) throws FormatError {

        var ds = this.cfdiReq.getDs();
        String[] arrCreds = null;
        String resourcesDir;
        String cfdiXml = cfdiSw.toString();

        try {
            resourcesDir = System.getenv("RESOURCES_DIR");
            if (resourcesDir == null) {
                resourcesDir = "/resources";
            }

            var file = new File(resourcesDir + "/fel-credentials.txt");
            byte[] bytes = Files.readAllBytes(file.toPath());
            var creds = new String(bytes, "UTF-8");
            arrCreds = creds.split("\\|\\|");

        } catch (Exception e) {
            e.printStackTrace();
            throw new FormatError("No fue posible obtener las credenciales del PAC", e);
        }

        var svcfile = new File(resourcesDir + "/WSCFDI33.svc.xml");
        WSCFDI33 ws = null;
        try {
            ws = new WSCFDI33(svcfile.toURI().toURL());

        } catch (Exception e) {
            e.printStackTrace();
            throw new FormatError("No fue posible obtener el archivo wsdl para timbrado", e);
        }
        IWSCFDI33 iws = ws.getSoapHttpEndpoint();
        RespuestaTFD33 res = iws.timbrarCFDI(arrCreds[0], arrCreds[1].trim(), cfdiXml, (String) ds.get("SERIE") + (String) ds.get("FOLIO"));

        var resultStr = String.format("Codigo respuesta: %s\nMensaje: %s\nMensaje (detallado): %s",
                res.getCodigoRespuesta().getValue(),
                res.getMensajeError().getValue(),
                res.getMensajeErrorDetallado().getValue());

        if (res.isOperacionExitosa()) {
            System.out.println(resultStr);

            Timbre33 timbre = res.getTimbre().getValue();
            String uuid = timbre.getUUID().getValue();
            ds.put("UUID", uuid);
            ds.put("CDIGITAL_SAT", timbre.getNumeroCertificadoSAT().getValue());
            ds.put("FECHSTAMP", timbre.getFechaTimbrado().toString());
            ds.put("SELLO_CFD", timbre.getSelloCFD().getValue());
            ds.put("SELLO_SAT", timbre.getSelloSAT().getValue());
            System.out.println("UUID: " + uuid);

            String xmlTimbrado = res.getXMLResultado().getValue();
            try {
                var docBuilderFactory = DocumentBuilderFactory.newInstance();
                var docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new ByteArrayInputStream(xmlTimbrado.getBytes(StandardCharsets.UTF_8)));
                var e = (Element) doc.getDocumentElement().getElementsByTagName("tfd:TimbreFiscalDigital").item(0);

                String cadenaOrigTfd = String.format("||%s|%s|%s|%s|%s|%s||",
                    e.getAttribute("Version"),
                    e.getAttribute("UUID"),
                    e.getAttribute("FechaTimbrado"),
                    e.getAttribute("RfcProvCertif"),
                    e.getAttribute("SelloCFD"),
                    e.getAttribute("NoCertificadoSAT")
                );

                ds.put("CADENA_ORIGINAL_TFD", cadenaOrigTfd);
                System.out.println("*********cadenaOrigTFD: " + cadenaOrigTfd);

            } catch (Exception e) {
                e.printStackTrace();
                throw new FormatError("No fue posible calcular la cadena original del timbre", e);
            }
            System.out.println(xmlTimbrado);

            var cfdiTimbrado = new StringWriter();
            cfdiTimbrado.write(xmlTimbrado);
            return cfdiTimbrado;

        } else {
            throw new FormatError("An error occurred when PAC tried to sign the cfdi.\n" + resultStr);
        }
    }
}
