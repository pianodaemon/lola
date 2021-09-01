package com.immortalcrab.as400.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;

public class CfdiRequest {

    private Map<String, Object> ds;
    private List<Pair<String, String>> kvs = null;

    public static CfdiRequest render(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        CfdiRequest ic = new CfdiRequest(kvs);
        ic.craft();

        return ic;
    }

    public Map<String, Object> getDs() {
        return ds;
    }

    private CfdiRequest(final List<Pair<String, String>> kvs) throws CfdiRequestError {

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
        }

    }

    private Map<String, Object> craft() throws CfdiRequestError {

        Map<String, Object> ds = new HashMap<>();

        {
            // previously "CFDI_TOTAL"
            final String label = "TOTAL";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "CFDI_DES"
            final String label = "DESCTO";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "TIPO_CAMBIO"
            final String label = "TPOCAM";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "FORMA_PAGO"
            final String label = "FORPAG";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "METODO_PAGO"
            final String label = "METPAG";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "CFDI_CERT_NO"
            final String label = "CDIGITAL";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "CFDI_FOLIO"
            final String label = "FOLIO";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "CFDI_SERIE"
            final String label = "SERIE";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "CFDI_DATE"
            final String label = "FECHOR";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "EMISOR_NOMBRE"
            final String label = "EMINOM";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "EMISOR_RFC"
            final String label = "EMIRFC";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "EMISOR_REG"
            final String label = "REGIMEN";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "EMISOR_CP"
            final String label = "EMIZIP";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "RECEPTOR_NOMBRE"
            final String label = "CTENOM";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "RECEPTOR_RFC"
            final String label = "CTERFC";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

        {
            // previously "RECEPTOR_USO"
            final String label = "USOCFDI";
            ds.put(label, bruteSearchUniqueAttr(label));
        }

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
}
