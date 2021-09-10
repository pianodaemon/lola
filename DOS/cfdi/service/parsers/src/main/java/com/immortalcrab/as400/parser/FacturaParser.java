package com.immortalcrab.as400.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FacturaParser {

    private final Map<String, Object> ds = new HashMap<>();

    public Map<String, Object> getDs() {
        return ds;
    }

    public FacturaParser(String fileWithPath) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {

        String comprobante = new String();

        File file = new File(fileWithPath);

        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        while ((str = in.readLine()) != null) {
            comprobante += str;
        }
        in.close();

        InputStream is = new ByteArrayInputStream(comprobante.getBytes("UTF-8"));
        this.reset();
        this.read(is);
    }

    private void reset() {

        this.ds.put("IMPTS_TRAS", Map.of(
                "TOTAL", 0,
                "DETALLES", new ArrayList<Map<String, String>>()
        ));

        this.ds.put("IMPTS_RET", Map.of(
                "TOTAL", 0,
                "DETALLES", new ArrayList<Map<String, String>>()
        ));

        this.ds.put("CONCEPTOS", new ArrayList<Map<String, String>>());
    }

    private void read(InputStream is) throws IOException, ParserConfigurationException, SAXException {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        DefaultHandler handler = new DefaultHandler() {

            public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {

                if (qName.equalsIgnoreCase("tfd:TimbreFiscalDigital")) {
                    for (int i = 0; i < attr.getLength(); i++) {
                        String k = attr.getQName(i);
                        String v = attr.getValue(i);

                        if ("Version".equals(k)) {
                            ds.put("TVER", v);
                        }
                        if ("UUID".equals(k)) {
                            ds.put("UUID", v);
                        }
                        if ("SelloSAT".equals(k)) {
                            ds.put("SAT_SELLO", v);
                        }
                        if ("SelloCFD".equals(k)) {
                            ds.put("CFD_SELLO", v);
                        }
                        if ("NoCertificadoSAT".equals(k)) {
                            ds.put("SAT_CERT_NO", v);
                        }
                        if ("FechaTimbrado".equals(k)) {
                            ds.put("STAMP_DATE", v);
                        }
                        if ("RfcProvCertif".equals(k)) {
                            ds.put("PAC", v);
                        }
                    }
                }

                if (qName.equalsIgnoreCase("cfdi:Comprobante")) {
                    for (int i = 0; i < attr.getLength(); i++) {
                        String k = attr.getQName(i);
                        String v = attr.getValue(i);

                        if ("Total".equals(k)) {
                            ds.put("CFDI_TOTAL", v);
                        }
                        if ("SubTotal".equals(k)) {
                            ds.put("CFDI_SUBTOTAL", v);
                        }
                        if ("Descuento".equals(k)) {
                            ds.put("CFDI_DES", v);
                        }
                        if ("TipoCambio".equals(k)) {
                            ds.put("TIPO_CAMBIO", v);
                        }
                        if ("Serie".equals(k)) {
                            ds.put("CFDI_SERIE", v);
                        }
                        if ("Folio".equals(k)) {
                            ds.put("CFDI_FOLIO", v);
                        }
                        if ("Fecha".equals(k)) {
                            ds.put("CFDI_DATE", v);
                        }
                        if ("NoCertificado".equals(k)) {
                            ds.put("CFDI_CERT_NO", v);
                        }
                        if ("LugarExpedicion".equals(k)) {
                            ds.put("EMISOR_CP", v);
                        }
                        if ("FormaPago".equals(k)) {
                            ds.put("FORMA_PAGO", v);
                        }
                        if ("MetodoPago".equals(k)) {
                            ds.put("METODO_PAGO", v);
                        }
                    }
                }

                if (qName.equalsIgnoreCase("cfdi:Emisor")) {

                    for (int i = 0; i < attr.getLength(); i++) {
                        String k = attr.getQName(i);
                        String v = attr.getValue(i);

                        if ("Nombre".equals(k)) {
                            ds.put("EMISOR_NOMBRE", v);
                        }

                        if ("Rfc".equals(k)) {
                            ds.put("EMISOR_RFC", v);
                        }

                        if ("RegimenFiscal".equals(k)) {
                            ds.put("EMISOR_REG", v);
                        }
                    }

                }

                if (qName.equalsIgnoreCase("cfdi:Receptor")) {

                    for (int i = 0; i < attr.getLength(); i++) {
                        String k = attr.getQName(i);
                        String v = attr.getValue(i);

                        if ("Nombre".equals(k)) {
                            ds.put("RECEPTOR_NOMBRE", v);
                        }
                        if ("Rfc".equals(k)) {
                            ds.put("RECEPTOR_RFC", v);
                        }
                        if ("UsoCFDI".equals(k)) {
                            ds.put("RECEPTOR_USO", v);
                        }
                    }
                }

                if (qName.equalsIgnoreCase("cfdi:Traslado")) {

                    Map<String, String> c = new HashMap<>();
                    for (int i = 0; i < attr.getLength(); i++) {

                        String k = attr.getQName(i);
                        String v = attr.getValue(i);
                        if ("Importe".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("Impuesto".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("TasaOCuota".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                    }

                    ((List<Map<String, String>>) ((Map<String, Object>) ds.get("IMPTS_TRAS")).get("DETALLES")).add(c);
                }

                if (qName.equalsIgnoreCase("cfdi:Retencion")) {

                    Map<String, String> c = new HashMap<>();
                    for (int i = 0; i < attr.getLength(); i++) {

                        String k = attr.getQName(i);
                        String v = attr.getValue(i);
                        if ("Importe".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("Impuesto".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("TasaOCuota".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                    }

                    ((List<Map<String, String>>) ((Map<String, Object>) ds.get("IMPTS_RET")).get("DETALLES")).add(c);
                }

                if (qName.equalsIgnoreCase("cfdi:Concepto")) {

                    Map<String, String> c = new HashMap<>();

                    for (int i = 0; i < attr.getLength(); i++) {
                        String k = attr.getQName(i);
                        String v = attr.getValue(i);

                        if ("Cantidad".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("Descripcion".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("Importe".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("ClaveProdServ".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("NoIdentificacion".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("ClaveUnidad".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                        if ("ValorUnitario".equals(k)) {
                            c.put(k.toUpperCase(), v);
                        }
                    }

                    List<Map<String, String>> l = (ArrayList<Map<String, String>>) ds.get("CONCEPTOS");
                    l.add(c);
                }
            }
        };

        sp.parse(is, handler);
    }
}
