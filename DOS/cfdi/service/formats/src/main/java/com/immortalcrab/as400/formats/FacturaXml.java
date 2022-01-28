package com.immortalcrab.as400.formats;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
import mx.gob.sat.sitio_internet.cfd.catalogos.cartaporte.CSubTipoRem;
import mx.gob.sat.sitio_internet.cfd.catalogos.cartaporte.CTipoPermiso;

public class FacturaXml {

    public static void main(String[] args) {
        try {
            // var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/NV139360-changed.txt"), StandardCharsets.UTF_8);
            // var isr = new InputStreamReader(new FileInputStream("/home/userd/Downloads/facmock.txt"), StandardCharsets.UTF_8);
            // var l = PairExtractor.go4it("/home/userd/Downloads/NV139360-cartaporte.txt");
            // var l = PairExtractor.go4it(isr);
            // var fileContent = new String(Files.readAllBytes(Paths.get("/home/userd/Downloads/NV140574_v2_211123_tir.txt")), StandardCharsets.UTF_8);
            var fileContent = new String(Files.readAllBytes(Paths.get("/home/userd/Downloads/NV999999 ejm con Version.Txt")), StandardCharsets.UTF_8);
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
            var facReq = FacturaRequest.render(isr2);
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

    public static String render(CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError {

        FacturaXml ic = new FacturaXml(cfdiReq, st);
        StringWriter cfdi = ic.shape();
        var results = ic.timbrarCfdi(cfdi);
        ic.save((StringWriter) results.get("cfdiTimbrado"));

        return (String) results.get("uuid");
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

            var empresa = (String) ds.get("EMPRESA");
            String certificadoFname = "";
            String privkeyFname     = "";

            if (empresa.equals("TQ")) {
                certificadoFname = "/pubkey.cer";
                privkeyFname     = "/privkey.pem";

            } else if (empresa.equals("TIR")) {
                certificadoFname = "/TIR_00001000000413586324.cer";
                privkeyFname     = "/CSD_NUEVO_LAREDO_TIR981223AX3_20190220_094457.pem";

            } else {
                throw new FormatError("EMPRESA desconocida: " + empresa);
            }

            boolean isCpNsVer2 = ((String) ds.get("CPVER")).equals("2");

            // Comprobante
            var cfdiFactory = new ObjectFactory();
            var cfdi = cfdiFactory.createComprobante();
            cfdi.setLugarExpedicion((String) ds.get("EMIZIP"));
            cfdi.setMetodoPago(CMetodoPago.fromValue(((String) ds.get("METPAG")).split(":")[0]));
            cfdi.setTipoDeComprobante(CTipoDeComprobante.I);
            cfdi.setTotal(new BigDecimal((String) ds.get("TOTAL")));
            var moneda = (String) ds.get("MONEDA");
            cfdi.setMoneda(CMoneda.fromValue(moneda));
            if (!moneda.equals("MXN") && !moneda.equals("XXX")) {
                cfdi.setTipoCambio(new BigDecimal((String) ds.get("TPOCAM")));
            }
            cfdi.setCertificado(Certificado.readFromFile(resourcesDir + certificadoFname));
            cfdi.setSubTotal(new BigDecimal((String) ds.get("SUBTOT")));
            cfdi.setCondicionesDePago((String) ds.get("CONPAG"));
            cfdi.setNoCertificado((String) ds.get("CDIGITAL"));
            cfdi.setFormaPago(((String) ds.get("FORPAG")).split(":")[0]);
            cfdi.setVersion("3.3");
            cfdi.setFecha(DatatypeFactory.newInstance().newXMLGregorianCalendar((String) ds.get("FECHOR")));
            cfdi.setSerie((String) ds.get("SERIE"));
            cfdi.setFolio((String) ds.get("FOLIO"));

            // UUID Relacionados
            var tipoRel = (String) ds.get("TIPOREL");
            if (!tipoRel.isBlank()) {
                var cfdiRelacionados = cfdiFactory.createComprobanteCfdiRelacionados();
                cfdiRelacionados.setTipoRelacion(tipoRel);
                var relacionados = (ArrayList<String>) ds.get("RELACIONADOS");
                var cfdiRelacionadoList = cfdiRelacionados.getCfdiRelacionado();

                for (String uuid : relacionados) {
                    var cfdiRelacionado = cfdiFactory.createComprobanteCfdiRelacionadosCfdiRelacionado();
                    cfdiRelacionado.setUUID(uuid);
                    cfdiRelacionadoList.add(cfdiRelacionado);
                }
                cfdi.setCfdiRelacionados(cfdiRelacionados);
            }

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
            boolean todoTrasladoExento = true;
            var civa = (String) ds.get("CIVA");
            boolean hayIvaTrasl = true;
            if (civa.equals("NO") || civa.equals("EX")) {
                hayIvaTrasl = false;
            }
            var civaret = (String) ds.get("CIVARET");
            boolean hayIvaReten = true;
            if (civaret.equals("NO") || civaret.equals("EX")) {
                hayIvaReten = false;
            }

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
                if (hayIvaTrasl) {
                    var traslados = cfdiFactory.createComprobanteConceptosConceptoImpuestosTraslados();
                    var traslado = cfdiFactory.createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
                    traslado.setBase(new BigDecimal(c.get("DBASE")));
                    traslado.setImpuesto(c.get("DITI"));
                    var dittf = c.get("DITTF");
                    traslado.setTipoFactor(CTipoFactor.fromValue(dittf));
                    if (!dittf.equals("Exento")) {
                        todoTrasladoExento = false;
                        traslado.setTasaOCuota(new BigDecimal(c.get("DITTC")));
                        traslado.setImporte(new BigDecimal(c.get("DITIMP")));
                    }
                    traslados.getTraslado().add(traslado);
                    conceptoImpuestos.setTraslados(traslados);
                }

                // Retenciones
                if (hayIvaReten) {
                    var retenciones = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetenciones();
                    var retencion = cfdiFactory.createComprobanteConceptosConceptoImpuestosRetencionesRetencion();
                    retencion.setBase(new BigDecimal(c.get("DBASE")));
                    retencion.setImpuesto(c.get("DIRI"));
                    retencion.setTipoFactor(CTipoFactor.fromValue(c.get("DIRTF")));
                    retencion.setTasaOCuota(new BigDecimal(c.get("DIRTC")));
                    retencion.setImporte(new BigDecimal(c.get("DIRIMP")));
                    retenciones.getRetencion().add(retencion);
                    conceptoImpuestos.setRetenciones(retenciones);
                }

                if (hayIvaTrasl || hayIvaReten) {
                    concepto.setImpuestos(conceptoImpuestos);
                }
                conceptos.getConcepto().add(concepto);
            }

            cfdi.setConceptos(conceptos);

            // Impuestos
            var impuestos = cfdiFactory.createComprobanteImpuestos();

            if (!todoTrasladoExento) {
                impuestos.setTotalImpuestosTrasladados(new BigDecimal((String) ds.get("IVA")));
                var impuestosTraslados = cfdiFactory.createComprobanteImpuestosTraslados();
                var impuestosTrasladoList = impuestosTraslados.getTraslado();
                var impuestosTraslado = cfdiFactory.createComprobanteImpuestosTrasladosTraslado();
                impuestosTraslado.setImpuesto("002");
                impuestosTraslado.setTipoFactor(CTipoFactor.TASA);
                impuestosTraslado.setTasaOCuota(new BigDecimal(String.format("0.%s0000", !hayIvaTrasl ? "00" : civa)));
                impuestosTraslado.setImporte(new BigDecimal((String) ds.get("IVA")));
                impuestosTrasladoList.add(impuestosTraslado);
                impuestos.setTraslados(impuestosTraslados);
            }

            if (hayIvaReten) {
                impuestos.setTotalImpuestosRetenidos(new BigDecimal((String) ds.get("IVARET")));
                var impuestosRetenciones = cfdiFactory.createComprobanteImpuestosRetenciones();
                var impuestosRetencionList = impuestosRetenciones.getRetencion();
                var impuestosRetencion = cfdiFactory.createComprobanteImpuestosRetencionesRetencion();
                impuestosRetencion.setImpuesto("002");
                impuestosRetencion.setImporte(new BigDecimal((String) ds.get("IVARET")));
                impuestosRetencionList.add(impuestosRetencion);
                impuestos.setRetenciones(impuestosRetenciones);
            }

            if (!todoTrasladoExento || hayIvaReten) {
                cfdi.setImpuestos(impuestos);
            }

            String contextPath    = "mx.gob.sat.cfd._3";
            String schemaLocation = "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd";
            boolean cpCartaPorte = ((String) ds.get("CPCARTAPORTE")).equals("Si");

            // Complementos
            if (cpCartaPorte) {

                var complementoList = cfdi.getComplemento();
                var complemento = cfdiFactory.createComprobanteComplemento();

                // Complemento - Carta Porte
                var cp = (HashMap<String, Object>) ds.get("CARTAPORTE");
                var cartaPorteFactory = new mx.gob.sat.cartaporte20.ObjectFactory();
                var cartaPorte = cartaPorteFactory.createCartaPorte();
                boolean isTranspInternac = ((String) cp.get("TranspInternac")).equals("Si") ? true : false;

                cartaPorte.setVersion("2.0");
                cartaPorte.setTranspInternac(isTranspInternac ? "Sí" : "No");
                cartaPorte.setTotalDistRec(new BigDecimal((String) cp.get("TotalDistRec")));
                String entradaSalidaMerc = "";
                if (isTranspInternac) {
                    entradaSalidaMerc = (String) cp.get("EntradaSalidaMerc");
                    cartaPorte.setEntradaSalidaMerc(entradaSalidaMerc);
                    cartaPorte.setViaEntradaSalida((String) cp.get("ViaEntradaSalida"));
                    cartaPorte.setPaisOrigenDestino(CPais.fromValue((String) cp.get("PaisOrigenDestino")));
                }

                // Complemento - Carta Porte - Ubicaciones
                var cpUbicaciones = (HashMap<String, Object>) cp.get("UBICACIONES");
                var ubicaciones = cartaPorteFactory.createCartaPorteUbicaciones();
                var ubicacionList = ubicaciones.getUbicacion();

                // Complemento - Carta Porte - Ubicaciones - Ubicación - Origen
                var cpOrigen = (HashMap<String, String>) cpUbicaciones.get("ORIGEN");
                var ubicacion = cartaPorteFactory.createCartaPorteUbicacionesUbicacion();
                ubicacion.setTipoUbicacion(cpOrigen.get("TipoUbicacion"));
                ubicacion.setRFCRemitenteDestinatario(cpOrigen.get("RFCRemitenteDestinatario"));
                ubicacion.setFechaHoraSalidaLlegada(DatatypeFactory.newInstance().newXMLGregorianCalendar(cpOrigen.get("FechaHoraSalida")));
                ubicacion.setNombreRemitenteDestinatario(cpOrigen.get("NombreRemitente"));
                String numRegIdTrib = cpOrigen.get("NumRegIdTrib");
                if (numRegIdTrib != null) {
                    ubicacion.setNumRegIdTrib(numRegIdTrib);
                    ubicacion.setResidenciaFiscal(CPais.fromValue(cpOrigen.get("ResidenciaFiscal")));
                }
                var domicilio = cartaPorteFactory.createCartaPorteUbicacionesUbicacionDomicilio();
                domicilio.setCalle(cpOrigen.get("Calle"));
                domicilio.setNumeroExterior(cpOrigen.get("NumeroExterior"));
                domicilio.setColonia(cpOrigen.get("Colonia"));
                String localidad = cpOrigen.get("Localidad");
                if (localidad != null && !localidad.isBlank()) {
                    domicilio.setLocalidad(localidad);
                }
                domicilio.setMunicipio(cpOrigen.get("Municipio"));
                domicilio.setEstado(cpOrigen.get("Estado"));
                domicilio.setPais(CPais.fromValue(cpOrigen.get("Pais")));
                domicilio.setCodigoPostal(cpOrigen.get("CodigoPostal"));
                ubicacion.setDomicilio(domicilio);
                ubicacionList.add(ubicacion);

                // Complemento - Carta Porte - Ubicaciones - Ubicación - Destino
                var cpDestino = (HashMap<String, String>) cpUbicaciones.get("DESTINO");
                ubicacion = cartaPorteFactory.createCartaPorteUbicacionesUbicacion();
                ubicacion.setTipoUbicacion(cpDestino.get("TipoUbicacion"));
                ubicacion.setRFCRemitenteDestinatario(cpDestino.get("RFCRemitenteDestinatario"));
                ubicacion.setFechaHoraSalidaLlegada(DatatypeFactory.newInstance().newXMLGregorianCalendar(cpDestino.get("FechaHoraProgLlegada")));
                ubicacion.setDistanciaRecorrida(new BigDecimal(cpDestino.get("DistanciaRecorrida")));
                ubicacion.setNombreRemitenteDestinatario(cpDestino.get("NombreDestinatario"));
                numRegIdTrib = cpDestino.get("NumRegIdTrib");
                if (numRegIdTrib != null) {
                    ubicacion.setNumRegIdTrib(numRegIdTrib);
                    ubicacion.setResidenciaFiscal(CPais.fromValue(cpDestino.get("ResidenciaFiscal")));
                }
                domicilio = cartaPorteFactory.createCartaPorteUbicacionesUbicacionDomicilio();
                domicilio.setCalle(cpDestino.get("Calle"));
                domicilio.setNumeroExterior(cpDestino.get("NumeroExterior"));
                domicilio.setColonia(cpDestino.get("Colonia"));
                localidad = cpDestino.get("Localidad");
                if (localidad != null && !localidad.isBlank()) {
                    domicilio.setLocalidad(localidad);
                }
                domicilio.setMunicipio(cpDestino.get("Municipio"));
                domicilio.setEstado(cpDestino.get("Estado"));
                domicilio.setPais(CPais.fromValue(cpDestino.get("Pais")));
                domicilio.setCodigoPostal(cpDestino.get("CodigoPostal"));
                ubicacion.setDomicilio(domicilio);
                ubicacionList.add(ubicacion);
                cartaPorte.setUbicaciones(ubicaciones);

                // Complemento - Carta Porte - Mercancías
                var cpMercancias = (HashMap<String, Object>) cp.get("MERCANCIAS");
                var cpMercanciaList = (ArrayList<HashMap<String, String>>) cpMercancias.get("LISTA");
                var mercancias = cartaPorteFactory.createCartaPorteMercancias();
                // mercancias.setNumTotalMercancias(cpMercanciaList.size());
                mercancias.setNumTotalMercancias(Integer.parseInt((String) cpMercancias.get("NumTotalMercancias")));
                mercancias.setUnidadPeso((String) cpMercancias.get("UnidadPeso"));
                mercancias.setPesoBrutoTotal(new BigDecimal((String) cpMercancias.get("PESOBRUTOTOTAL")));
                var mercanciaList = mercancias.getMercancia();

                for (HashMap<String, String> item : cpMercanciaList) {
                    var mercancia = cartaPorteFactory.createCartaPorteMercanciasMercancia();
                    mercancia.setBienesTransp(item.get("BienesTransp"));
                    mercancia.setDescripcion(item.get("CPDESCRIP"));
                    mercancia.setCantidad(new BigDecimal(item.get("Cantidad")));
                    mercancia.setClaveUnidad(item.get("ClaveUnidad"));
                    mercancia.setUnidad(item.get("Unidad"));
                    mercancia.setPesoEnKg(new BigDecimal(item.get("PesoEnKg")));
                    if (item.get("CPHAZMAT").equals("Si")) {
                        mercancia.setMaterialPeligroso("Sí");
                        mercancia.setCveMaterialPeligroso(item.get("CPHAZMATC"));
                    }
                    if (isTranspInternac) {
                        mercancia.setFraccionArancelaria(item.get("FraccionArancelaria"));
                        if (entradaSalidaMerc.equals("Entrada")) {
                            var pedimentosList = mercancia.getPedimentos();
                            var pedimentos = cartaPorteFactory.createCartaPorteMercanciasMercanciaPedimentos();
                            pedimentos.setPedimento(item.get("Pedimento"));
                            pedimentosList.add(pedimentos);
                        }
                    }

                    mercanciaList.add(mercancia);
                }

                var cpAutotransporte = (HashMap<String, String>) cpMercancias.get("AUTOTRANSPORTEFEDERAL");
                var autotransporte = cartaPorteFactory.createCartaPorteMercanciasAutotransporte();
                autotransporte.setPermSCT(CTipoPermiso.fromValue(cpAutotransporte.get("PermSCT")));
                autotransporte.setNumPermisoSCT(cpAutotransporte.get("CNUMPERMSCT"));
                var seguros = cartaPorteFactory.createCartaPorteMercanciasAutotransporteSeguros();
                seguros.setAseguraRespCivil(cpAutotransporte.get("CPQSEGRESCIV"));
                seguros.setPolizaRespCivil(cpAutotransporte.get("CPQSEGRESCIVN"));
                autotransporte.setSeguros(seguros);
                var identificacionVehicular = cartaPorteFactory.createCartaPorteMercanciasAutotransporteIdentificacionVehicular();
                identificacionVehicular.setConfigVehicular(CConfigAutotransporte.fromValue(cpAutotransporte.get("ConfigVehicular")));
                identificacionVehicular.setPlacaVM(cpAutotransporte.get("PlacaVM"));
                identificacionVehicular.setAnioModeloVM(Integer.parseInt(cpAutotransporte.get("AnioModeloVM")));
                autotransporte.setIdentificacionVehicular(identificacionVehicular);
                var remolques = cartaPorteFactory.createCartaPorteMercanciasAutotransporteRemolques();
                var remolqueList = remolques.getRemolque();
                var remolque = cartaPorteFactory.createCartaPorteMercanciasAutotransporteRemolquesRemolque();
                remolque.setSubTipoRem(CSubTipoRem.fromValue(cpAutotransporte.get("CPSTPOREM")));
                remolque.setPlaca(cpAutotransporte.get("CPPLACAREM"));
                remolqueList.add(remolque);
                autotransporte.setRemolques(remolques);
                mercancias.setAutotransporte(autotransporte);
                cartaPorte.setMercancias(mercancias);

                // Complemento - Carta Porte - Figura Transporte
                var cpFiguraTransporte = (HashMap<String, Object>) cp.get("FIGURATRANSPORTE");
                var cpOperadorList = (ArrayList<HashMap<String, String>>) cpFiguraTransporte.get("OPERADORES");
                var figuraTransporte = cartaPorteFactory.createCartaPorteFiguraTransporte();
                var tiposFiguraList = figuraTransporte.getTiposFigura();

                for (HashMap<String, String> item : cpOperadorList) {
                    var tiposFigura = cartaPorteFactory.createCartaPorteFiguraTransporteTiposFigura();
                    tiposFigura.setTipoFigura(item.get("TipoFigura"));
                    tiposFigura.setNumLicencia(item.get("NumLicencia"));
                    tiposFigura.setNombreFigura(item.get("NombreOperador"));
                    tiposFigura.setRFCFigura(item.get("RFCOperador"));
                    tiposFiguraList.add(tiposFigura);
                }

                cartaPorte.setFiguraTransporte(figuraTransporte);

                complemento.getAny().add(cartaPorte);
                complementoList.add(complemento);

                contextPath    += ":mx.gob.sat.cartaporte20";
                schemaLocation += " http://www.sat.gob.mx/CartaPorte20 http://www.sat.gob.mx/sitio_internet/cfd/CartaPorte/CartaPorte20.xsd";
            }

            // Hacer el marshalling del cfdi object
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.schemaLocation", schemaLocation);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", isCpNsVer2 ? new CfdiNamespaceMapper2() : new CfdiNamespaceMapper());
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(cfdi, sw);

            // Armar la cadena original del comprobante + complemento de carta porte
            var cfdiXml = sw.toString();
            var cadenaOrig = CadenaOriginal.build(cfdiXml, resourcesDir + "/cadenaoriginal_3_3.xslt");
            if (cpCartaPorte) {
                var cadenaOrigCartaPorte = CadenaOriginal.build(cfdiXml, resourcesDir + "/CartaPorte20.xslt");
                cadenaOrig = cadenaOrig.replaceAll(" \\|\\|", " ").trim()
                           + cadenaOrigCartaPorte.replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>", " ").trim() + "||";
            }
            cadenaOrig = cadenaOrig.replaceAll("&amp;", "&");

            // Sellar cadena original
            var sello = Signer.signMessage(resourcesDir + privkeyFname, cadenaOrig);
            cfdi.setSello(sello);

            sw = new StringWriter();
            marshaller.marshal(cfdi, sw);
            System.out.println(sw.toString());

        } catch (JAXBException | DatatypeConfigurationException ex) {
            ex.printStackTrace();
            throw new FormatError("Error al formar el xml (jaxb).", ex);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new FormatError("Error al leer el certificado digital.", ex);

        } catch (Exception ex) {
            ex.printStackTrace();
            var stackTraceElem = ex.getStackTrace()[0];
            String err = ex.toString() + " at " + stackTraceElem.toString();
            throw new FormatError("Error al generar el xml (posible omisión de un dato requerido en el input). " + err, ex);
        }

        return sw;
    }

    private HashMap<String, Object> timbrarCfdi(StringWriter cfdiSw) throws FormatError {

        var ds = this.cfdiReq.getDs();
        String[] arrCreds = null;
        String resourcesDir;
        String cfdiXml = cfdiSw.toString();

        try {
            resourcesDir = System.getenv("RESOURCES_DIR");
            if (resourcesDir == null) {
                resourcesDir = "/resources";
            }

            var empresa = (String) ds.get("EMPRESA");
            String credFname = "";

            if (empresa.equals("TQ")) {
                credFname = "/fel-credentials.txt";

            } else if (empresa.equals("TIR")) {
                credFname = "/fel-credentials-tir.txt";

            } else {
                throw new FormatError("EMPRESA desconocida: " + empresa);
            }

            var file = new File(resourcesDir + credFname);
            byte[] bytes = Files.readAllBytes(file.toPath());
            var creds = new String(bytes, StandardCharsets.UTF_8);
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

            } catch (Exception e) {
                e.printStackTrace();
                throw new FormatError("No fue posible calcular la cadena original del timbre", e);
            }
            System.out.println(xmlTimbrado);

            var cfdiTimbrado = new StringWriter();
            cfdiTimbrado.write(xmlTimbrado);

            var results = new HashMap<String, Object>();
            results.put("uuid", uuid);
            results.put("cfdiTimbrado", cfdiTimbrado);

            return results;

        } else {
            throw new FormatError("An error occurred when PAC tried to sign the cfdi.\n" + resultStr);
        }
    }
}
