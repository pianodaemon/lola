package com.immortalcrab.as400.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;

public class FacturaRequest {

    enum SearchSeqStages {
        SEEKOUT_DSEC, PICKUP_ATTRS
    }

    //number of elements ahead per DSEC block
    final int DESC_SIZE = 15;

    private Map<String, Object> ds = null;
    private List<Pair<String, String>> kvs = null;

    public static FacturaRequest render(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        FacturaRequest ic = new FacturaRequest(kvs);
        ic.craft();

        return ic;
    }

    public Map<String, Object> getDs() {
        return ds;
    }

    private FacturaRequest(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        this.kvs = kvs;
        this.ds = new HashMap<>();

        {
            this.ds.put("IMPTS_TRAS", Map.of(
                    "TOTAL", 0,
                    "DETALLES", new ArrayList<Map<String, String>>()
            ));

            this.ds.put("IMPTS_RET", Map.of(
                    "TOTAL", 0,
                    "DETALLES", new ArrayList<Map<String, String>>()
            ));

            this.ds.put("CONCEPTOS", new ArrayList<Map<String, String>>());

            this.ds.put("COMENTARIOS", new ArrayList<String>());
        }

    }

    private Map<String, Object> craft() throws CfdiRequestError {

        {
            // At cfdi is aka LugarExpedicion
            final String label = "EXPZIP";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "CFDI_TOTAL"
            final String label = "TOTAL";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "CFDI_DES"
            final String label = "DESCTO";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "TIPO_CAMBIO"
            final String label = "TPOCAM";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "FORMA_PAGO"
            final String label = "FORPAG";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "METODO_PAGO"
            final String label = "METPAG";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "CFDI_CERT_NO"
            final String label = "CDIGITAL";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "CFDI_FOLIO"
            final String label = "FOLIO";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "CFDI_SERIE"
            final String label = "SERIE";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "CFDI_DATE"
            final String label = "FECHOR";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "EMISOR_NOMBRE"
            final String label = "EMINOM";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "EMISOR_RFC"
            final String label = "EMIRFC";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "EMISOR_REG"
            final String label = "REGIMEN";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "EMISOR_CP"
            final String label = "EMIZIP";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "RECEPTOR_NOMBRE"
            final String label = "CTENOM";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "RECEPTOR_RFC"
            final String label = "CTERFC";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // "RECEPTOR_USO"
            final String label = "USOCFDI";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        this.pickUpDsecBlocks();
        this.pickUpComments();

        return ds;
    }

    private String bruteSearchUniqueAttr(final String label) throws CfdiRequestError {

        for (Iterator<Pair<String, String>> it = this.kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();
            if (p.getValue0().equals(label)) {
                return p.getValue1();
            }
        }

        throw new CfdiRequestError("Unique attr " + label + " not found");
    }

    private void pickUpComments() {

        for (Iterator<Pair<String, String>> it = this.kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();

            if ("COMENT".equals(p.getValue0())) {
                List<String> l = (ArrayList<String>) this.ds.get("COMENTARIOS");
                l.add(p.getValue1());
            }
        }
    }

    private void pickUpDsecBlocks() {

        Map<String, String> c = null;
        int offset = 0;
        SearchSeqStages stage = SearchSeqStages.SEEKOUT_DSEC;

        for (Iterator<Pair<String, String>> it = this.kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();

            switch (stage) {

                case SEEKOUT_DSEC: {

                    /* Row number from AS400 system
                       where the concept has been included */
                    if ("DSEC".equals(p.getValue0())) {
                        c = new HashMap<>();
                        offset = this.DESC_SIZE;
                        stage = SearchSeqStages.PICKUP_ATTRS;
                    }
                    break;
                }
                case PICKUP_ATTRS: {

                    offset--;

                    // At cfdi is aka "NoIdentificacion"
                    if ("DITEM".equals(p.getValue0())) {
                        c.put("DITEM", p.getValue1());
                    }

                    // At cfdi is aka "ClaveProdServ"
                    if ("DCVESERV".equals(p.getValue0())) {
                        c.put("DCVESERV", p.getValue1());
                    }

                    // At cfdi is aka "Cantidad"
                    if ("DCANT".equals(p.getValue0())) {
                        c.put("DCANT", p.getValue1());
                    }

                    // At cfdi is aka "ClaveUnidad"
                    if ("DCUME".equals(p.getValue0())) {
                        c.put("DCUME", p.getValue1());
                    }

                    // unidad medida
                    if ("DUME".equals(p.getValue0())) {
                        c.put("DUME", p.getValue1());
                    }

                    // At cfdi is aka "Descripcion"
                    if ("DDESL".equals(p.getValue0())) {
                        c.put("DDESL", p.getValue1());
                    }

                    // At cfdi is aka "ValorUnitario"
                    if ("DUNIT".equals(p.getValue0())) {
                        c.put("DUNIT", p.getValue1());
                    }

                    // At cfdi is aka "Importe"
                    if ("DIMPO".equals(p.getValue0())) {
                        c.put("DIMPO", p.getValue1());
                    }

                    if ("DIDESCTO".equals(p.getValue0())) {
                        c.put("DIDESCTO", p.getValue1());
                    }

                    if ("DBASE".equals(p.getValue0())) {
                        c.put("DBASE", p.getValue1());
                    }

                    if ("DITIMP".equals(p.getValue0())) {
                        c.put("DITIMP", p.getValue1());
                    }

                    if ("DITI".equals(p.getValue0())) {
                        c.put("DITI", p.getValue1());
                    }

                    if ("DITTF".equals(p.getValue0())) {
                        c.put("DITTF", p.getValue1());
                    }

                    if ("DITTC".equals(p.getValue0())) {
                        c.put("DITTC", p.getValue1());
                    }

                    if (offset == 0) {
                        List<Map<String, String>> l = (ArrayList<Map<String, String>>) this.ds.get("CONCEPTOS");
                        l.add(c);
                        stage = SearchSeqStages.SEEKOUT_DSEC;
                    }

                    break;
                }
            }

        }

    }
}
