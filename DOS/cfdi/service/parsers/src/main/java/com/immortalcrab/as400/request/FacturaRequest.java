package com.immortalcrab.as400.request;

import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.error.CfdiRequestError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;

public class FacturaRequest extends CfdiRequest {

    public static FacturaRequest render(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        FacturaRequest ic = new FacturaRequest(kvs);
        ic.craft();

        return ic;
    }

    private FacturaRequest(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        super(kvs);
        this.ds = new HashMap<>();
    }

    @Override
    protected void captureSymbolImpt(final String label, final Object value) {
        this.ds.put(label, value);
    }

    @Override
    protected Map<String, Object> craftImpt() throws CfdiRequestError {

        {
            captureSymbolImpt("CONCEPTOS", new ArrayList<Map<String, String>>());
            captureSymbolImpt("COMENTARIOS", new ArrayList<String>());
            captureSymbolImpt("CARTAPORTE", new HashMap<String, Object>());
        }

        {
            // Genera remision
            captureSymbol("CVEREM");

            // Uso que se le dara al cfdi
            captureSymbol("USOCFDI");
        }

        // Data sobre la carga
        {
            // Cantidad Convenida
            captureSymbol("CNTCON");

            // Valor Declarado
            captureSymbol("VALDEC");

            // Cantidad de Bultos
            captureSymbol("CNTBUL");

            // Peso Estimado
            captureSymbol("PESEST");

            // Contenidos
            captureSymbol("CONTEN");

            captureSymbol("CAJAS");
        }

        // Oficinas
        {
            // Oficina que Elabora
            captureSymbol("OFIDOC");

            // Oficina que Cobra
            captureSymbol("OFICOB");
        }

        // Expedido en
        {
            // At cfdi is aka LugarExpedicion
            captureSymbol("EXPZIP");

            captureSymbol("EXPDIR");

            captureSymbol("EXPNOM");
        }

        // Totales
        {
            // "CFDI_TOTAL"
            captureSymbol("TOTAL");

            captureSymbol("SUBTOT");

            captureSymbol("SUBTOT2");

            // "CFDI_DES"
            captureSymbol("DESCTO");

            captureSymbol("IVA");

            captureSymbol("IVARET");

            captureSymbol("CIVA");

            captureSymbol("CIVARET");

            // "TIPO_CAMBIO"
            captureSymbol("TPOCAM");

            captureSymbol("MONEDA");
        }

        // Pago detalles
        {
            // "FORMA_PAGO"
            captureSymbol("FORPAG");

            // "METODO_PAGO"
            captureSymbol("METPAG");

            // Condiciones de pago
            captureSymbol("CONPAG");
        }

        // Remitente
        {
            captureSymbol("REMNOM");

            captureSymbol("REMDIR");
        }

        // Destinatario
        {
            captureSymbol("DESNOM");

            captureSymbol("DESDIR");
        }

        // Agente Aduanal data
        {
            captureSymbol("AGENOM");

            captureSymbol("AGEDIR");
        }

        this.pickUpDsecBlocks();
        this.pickUpComments();
        this.pickUpCartaPorte();

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
        var attributeSet = new HashSet<String>(Arrays.asList(
            "DITEM",     // NoIdentificacion
            "DCVESERV",  // ClaveProdServ
            "DCANT",     // Cantidad
            "DCUME",     // ClaveUnidad
            "DUME",      // Unidad Medida
            "DDESL",     // Descripcion
            "DUNIT",     // ValorUnitario
            "DIMPO",     // Importe
            "DESCTO",    // Descuento
            "DBASE",     // Base
            "DITIMP",    // Traslado Importe
            "DITI",      // Traslado Impuesto
            "DITTF",     // Traslado Tipo Factor
            "DITTC",     // Traslado Tasa o Cuota
            "DIRIMP",    // Retencion Importe
            "DIRI",      // Retencion Impuesto
            "DIRTF",     // Retencion Tipo Factor
            "DIRTC"      // Retencion Tasa o Cuota
        ));
        List<Map<String, String>> l = (ArrayList<Map<String, String>>) this.ds.get("CONCEPTOS");
        boolean dsecFound = false;

        for (Iterator<Pair<String, String>> it = this.kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();

            if ("DSEC".equals(p.getValue0())) {

                if (dsecFound) {
                    l.add(c);
                }
                c = new HashMap<>();
                dsecFound = true;

            } else if (dsecFound) {

                if (!attributeSet.contains(p.getValue0())) {
                    l.add(c);
                    break;
                }
                c.put(p.getValue0(), p.getValue1());
            }
        }
    }

    private void pickUpCartaPorte() {

        var m = (HashMap<String, Object>) this.ds.get("CARTAPORTE");

        var ubicacionesMap = new HashMap<String, Object>();
        var origen = new HashMap<String, String>();
        ubicacionesMap.put("ORIGEN", origen);
        var destino = new HashMap<String, String>();
        ubicacionesMap.put("DESTINO", destino);
        m.put("UBICACIONES", ubicacionesMap);

        var mercanciasMap = new HashMap<String, Object>();
        var mercanciaList = new ArrayList<HashMap<String, String>>();
        mercanciasMap.put("LISTA", mercanciaList);
        var autoTranspFederal = new HashMap<String, String>();
        mercanciasMap.put("AUTOTRANSPORTEFEDERAL", autoTranspFederal);
        m.put("MERCANCIAS",  mercanciasMap);

        var figuraTransporteMap = new HashMap<String, Object>();
        var operadorList = new ArrayList<HashMap<String, String>>();
        figuraTransporteMap.put("OPERADORES", operadorList);
        m.put("FIGURATRANSPORTE", figuraTransporteMap);

        HashMap<String, String> mercancia = null;
        HashMap<String, String> operador = null;

        for (Iterator<Pair<String, String>> it = this.kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();

            switch(p.getValue0()) {

                // Carta Porte - Generales
                case "CPTRANSPINTERNAC": {
                    m.put("TRANSPINTERNAC", p.getValue1());
                    break;
                }
                case "CPTOTALDISTREC": {
                    m.put("TOTALDISTREC", p.getValue1());
                    break;
                }
                // Ubicaciones - Valor aplicado a Origen y Destino
                case "CPDISTANCIARECORRIDA": {
                    var distRec = "DISTANCIARECORRIDA";
                    origen.put(distRec, p.getValue1());
                    destino.put(distRec, p.getValue1());
                    break;
                }
                // Ubicaciones - Origen
                case "CPREMDIR": {
                    origen.put("CALLE", p.getValue1());
                    break;
                }
                case "CPREMNUM": {
                    origen.put("NUMEROEXTERIOR", p.getValue1());
                    break;
                }
                case "CPREMCOLC": {
                    origen.put("COLONIA", p.getValue1());
                    break;
                }
                case "CPREMMUNC": {
                    origen.put("MUNICIPIO", p.getValue1());
                    break;
                }
                case "CPREMEDOC": {
                    origen.put("ESTADO", p.getValue1());
                    break;
                }
                case "CPREMPAI": {
                    origen.put("PAIS", p.getValue1());
                    break;
                }
                case "CPREMZIP": {
                    origen.put("CODIGOPOSTAL", p.getValue1());
                    break;
                }
                // Ubicaciones - Destino
                case "CPDESDIR": {
                    destino.put("CALLE", p.getValue1());
                    break;
                }
                case "CPDESNUM": {
                    destino.put("NUMEROEXTERIOR", p.getValue1());
                    break;
                }
                case "CPDESCOLC": {
                    destino.put("COLONIA", p.getValue1());
                    break;
                }
                case "CPDESMUNC": {
                    destino.put("MUNICIPIO", p.getValue1());
                    break;
                }
                case "CPDESEDOC": {
                    destino.put("ESTADO", p.getValue1());
                    break;
                }
                case "CPDESPAI": {
                    destino.put("PAIS", p.getValue1());
                    break;
                }
                case "CPDESZIP": {
                    destino.put("CODIGOPOSTAL", p.getValue1());
                    break;
                }
                // Mercancias
                case "CPBIENES": {
                    mercancia = new HashMap<String, String>();
                    mercancia.put("BIENESTRANSP", p.getValue1());
                    break;
                }
                case "CPCANT": {
                    mercancia.put("CANTIDAD", p.getValue1());
                    break;
                }
                case "CPCUNI": {
                    mercancia.put("CLAVEUNIDAD", p.getValue1());
                    break;
                }
                case "CPPESOKG": {
                    mercancia.put("PESOENKG", p.getValue1());
                    mercanciaList.add(mercancia);
                    break;
                }
                // Mercancias - Autotransporte Federal
                case "CPCNFGCAR": {
                    autoTranspFederal.put("CONFIGVEHICULAR", p.getValue1());
                    break;
                }
                case "CPPLACAVM": {
                    autoTranspFederal.put("PLACAVM", p.getValue1());
                    break;
                }
                case "CPMODELOVM": {
                    autoTranspFederal.put("ANIOMODELOVM", p.getValue1());
                    break;
                }
                // Figura Transporte - Operadores
                case "CPRFCOPE": {
                    operador = new HashMap<String, String>();
                    operador.put("RFCOPERADOR", p.getValue1());
                    break;
                }
                case "CPLICENCIA": {
                    operador.put("NUMLICENCIA", p.getValue1());
                    break;
                }
                case "CPNOMOPE": {
                    operador.put("NOMBREOPERADOR", p.getValue1());
                    operadorList.add(operador);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }
}
