package com.immortalcrab.as400.request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;

public class CfdiRequest {

    public static Map<String, Object> craft(List<Pair<String, String>> kvs) throws CfdiRequestError {

        Map<String, Object> ds = new HashMap<>();

        {
            // previously "FORMA_PAGO"
            final String label = "FORPAG";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "METODO_PAGO"
            final String label = "METPAG";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "CFDI_CERT_NO"
            final String label = "CDIGITAL";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "CFDI_FOLIO"
            final String label = "FOLIO";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "CFDI_SERIE"
            final String label = "SERIE";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "CFDI_DATE"
            final String label = "FECHOR";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "EMISOR_NOMBRE"
            final String label = "EMINOM";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "EMISOR_RFC"
            final String label = "EMIRFC";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "EMISOR_REG"
            final String label = "REGIMEN";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "EMISOR_CP"
            final String label = "EMIZIP";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "RECEPTOR_NOMBRE"
            final String label = "CTENOM";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "RECEPTOR_RFC"
            final String label = "CTERFC";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        {
            // previously "RECEPTOR_USO"
            final String label = "USOCFDI";
            ds.put(label, bruteSearchUniqueAttr(kvs, label));
        }

        return ds;
    }

    private static String bruteSearchUniqueAttr(List<Pair<String, String>> kvs, final String label) throws CfdiRequestError {

        for (Iterator<Pair<String, String>> it = kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();
            if (p.getValue0().equals(label)) {
                return p.getValue1();
            }
        }

        throw new CfdiRequestError("Unique attr " + label + " not found");
    }
}
