package com.immortalcrab.as400.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;

public class FacturaRequest extends CfdiRequest {

    enum SearchSeqStages {
        SEEKOUT_DSEC, PICKUP_ATTRS
    }

    //number of elements ahead per DSEC block
    final int DESC_SIZE = 15;

    private Map<String, Object> ds = null;

    public static FacturaRequest render(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        FacturaRequest ic = new FacturaRequest(kvs);
        ic.craft();

        return ic;
    }

    public Map<String, Object> getDs() {
        return ds;
    }

    private FacturaRequest(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        super(kvs);
    }

    @Override
    void captureSymbolImpt(final String label, final Object value) {
        this.ds.put(label, (String) value);
    }

    @Override
    Map<String, Object> craft() throws CfdiRequestError {

        // We start off with fresh 
        {
            this.ds = new HashMap<>();
            this.ds.put("CONCEPTOS", new ArrayList<Map<String, String>>());
            this.ds.put("COMENTARIOS", new ArrayList<String>());
        }

        {
            // Data sobre la carga
            {
                // Cantidad Convenida
                captureSymbol("CNTCON");
            }

            {
                // Valor Declarado
                captureSymbol("VALDEC");
            }

            {
                // Cantidad de Bultos
                captureSymbol("CNTBUL");
            }

            {
                // Peso Estimado
                captureSymbol("PESEST");
            }

            {
                // Contenidos
                captureSymbol("CONTEN");
            }

            {
                captureSymbol("CAJAS");
            }

            {
                captureSymbol("TRACTOR");
            }

            {
                captureSymbol("DOCUMENTA");
            }
        }

        {
            // Oficinas
            {
                // Oficina que Elabora
                captureSymbol("OFIDOC");
            }

            {
                // Oficina que Cobra
                captureSymbol("OFICOB");
            }
        }

        {
            // Expedido en
            {
                // At cfdi is aka LugarExpedicion
                captureSymbol("EXPZIP");
            }

            {
                captureSymbol("EXPDIR");
            }

            {
                captureSymbol("EXPNOM");
            }
        }

        {
            // Totales
            {
                // "CFDI_TOTAL"
                captureSymbol("TOTAL");
            }

            {
                captureSymbol("SUBTOT");
            }

            {
                captureSymbol("SUBTOT2");
            }

            {
                // "CFDI_DES"
                captureSymbol("DESCTO");
            }

            {
                captureSymbol("IVA");
            }

            {
                captureSymbol("IVARET");
            }

            {
                captureSymbol("CIVA");
            }

            {
                captureSymbol("CIVARET");
            }

            {
                // "TIPO_CAMBIO"
                captureSymbol("TPOCAM");
            }

            {
                captureSymbol("MONEDA");
            }
        }

        {
            // Pago detalles
            {
                // "FORMA_PAGO"
                captureSymbol("FORPAG");
            }

            {
                // "METODO_PAGO"
                captureSymbol("METPAG");
            }

            {
                // Condiciones de pago
                captureSymbol("CONPAG");
            }
        }

        captureControlSymbols();
        captureEmisorSymbols();
        captureReceptorSymbols();

        {
            // Remitente
            {
                captureSymbol("REMNOM");
            }

            {
                captureSymbol("REMDIR");
            }
        }

        {
            // Destinatario
            {
                captureSymbol("DESNOM");
            }

            {
                captureSymbol("DESDIR");
            }
        }

        {
            // Agente Aduanal data
            {
                captureSymbol("AGENOM");
            }

            {
                captureSymbol("AGEDIR");
            }
        }

        this.pickUpDsecBlocks();
        this.pickUpComments();

        return ds;
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
