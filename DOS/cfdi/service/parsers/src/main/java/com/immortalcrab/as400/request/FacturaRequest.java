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

            captureSymbol("MONEDAS");

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

            captureSymbol("REMNUM");

            captureSymbol("REMCOL");

            captureSymbol("REMLOC");

            captureSymbol("REMMUN");

            captureSymbol("REMEDO");

            captureSymbol("REMPAI");

            captureSymbol("REMZIP");

            captureSymbol("REMTEL");

            captureSymbol("REMRFC");

            captureSymbol("REMTAX");
        }

        // Destinatario
        {
            captureSymbol("DESNOM");

            captureSymbol("DESDIR");

            captureSymbol("DESNUM");

            captureSymbol("DESCOL");

            captureSymbol("DESLOC");

            captureSymbol("DESMUN");

            captureSymbol("DESEDO");

            captureSymbol("DESPAI");

            captureSymbol("DESZIP");

            captureSymbol("DESTEL");

            captureSymbol("DESRFC");

            captureSymbol("DESTAX");
        }

        // Agente Aduanal data
        {
            captureSymbol("AGENOM");
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
        var autoTransporte = new HashMap<String, String>();
        mercanciasMap.put("AUTOTRANSPORTEFEDERAL", autoTransporte);
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
                    m.put("TranspInternac", p.getValue1());
                    break;
                }
                case "CPTOTALDISTREC": {
                    m.put("TotalDistRec", p.getValue1());
                    break;
                }
                case "CPENTRADASALIDAMERC": {
                    m.put("EntradaSalidaMerc", p.getValue1());
                    break;
                }
                case "CPVIAENTRADASALIDA": {
                    m.put("ViaEntradaSalida", p.getValue1());
                    break;
                }
                case "CPTIPOVIAJE": {
                    m.put("CPTIPOVIAJE", p.getValue1());
                    break;
                }
                // Ubicaciones - Origen
                case "CPREMTIPOUBICACION": {
                    origen.put("TipoUbicacion", p.getValue1());
                    break;
                }
                case "CPREMRFC": {
                    origen.put("RFCRemitenteDestinatario", p.getValue1());
                    break;
                }
                case "CPREMNOMBRE": {
                    origen.put("NombreRemitente", p.getValue1());
                    break;
                }
                case "CPREMNUMREGIDTRIB": {
                    origen.put("NumRegIdTrib", p.getValue1());
                    break;
                }
                case "CPREMRESIDENCIAFISCAL": {
                    origen.put("ResidenciaFiscal", p.getValue1());
                    break;
                }
                case "CPFECHAHORASALIDA": {
                    origen.put("FechaHoraSalida", p.getValue1());
                    break;
                }
                case "CPREMDISTANCIARECORRIDA": {
                    origen.put("DistanciaRecorrida", p.getValue1());
                    break;
                }
                case "CPREMDIR": {
                    origen.put("Calle", p.getValue1());
                    break;
                }
                case "CPREMNUM": {
                    origen.put("NumeroExterior", p.getValue1());
                    break;
                }
                case "CPREMCOL": {
                    origen.put("Colonia", p.getValue1());
                    break;
                }
                case "CPREMLOC": {
                    origen.put("Localidad", p.getValue1());
                    break;
                }
                case "CPREMMUN": {
                    origen.put("Municipio", p.getValue1());
                    break;
                }
                case "CPREMEDO": {
                    origen.put("Estado", p.getValue1());
                    break;
                }
                case "CPREMPAI": {
                    origen.put("Pais", p.getValue1());
                    break;
                }
                case "CPREMZIP": {
                    origen.put("CodigoPostal", p.getValue1());
                    break;
                }
                // Ubicaciones - Destino
                case "CPDESTIPOUBICACION": {
                    destino.put("TipoUbicacion", p.getValue1());
                    break;
                }
                case "CPDESRFC": {
                    destino.put("RFCRemitenteDestinatario", p.getValue1());
                    break;
                }
                case "CPFECHAHORALLEGADA": {
                    destino.put("FechaHoraProgLlegada", p.getValue1());
                    break;
                }
                case "CPDESDISTANCIARECORRIDA": {
                    destino.put("DistanciaRecorrida", p.getValue1());
                    break;
                }
                case "CPDESDIR": {
                    destino.put("Calle", p.getValue1());
                    break;
                }
                case "CPDESNUM": {
                    destino.put("NumeroExterior", p.getValue1());
                    break;
                }
                case "CPDESCOLC": {
                    destino.put("Colonia", p.getValue1());
                    break;
                }
                case "CPDESMUNC": {
                    destino.put("Municipio", p.getValue1());
                    break;
                }
                case "CPDESEDOC": {
                    destino.put("Estado", p.getValue1());
                    break;
                }
                case "CPDESPAI": {
                    destino.put("Pais", p.getValue1());
                    break;
                }
                case "CPDESZIP": {
                    destino.put("CodigoPostal", p.getValue1());
                    break;
                }
                // Mercancias
                case "PESOBRUTOTOTAL": {
                    mercanciasMap.put("PESOBRUTOTOTAL", p.getValue1());
                    break;
                }
                case "PESONETOTOTAL": {
                    mercanciasMap.put("PESONETOTOTAL", p.getValue1());
                    break;
                }
                case "UNIDADPESO": {
                    mercanciasMap.put("UnidadPeso", p.getValue1());
                    break;
                }
                case "NUMTOTALMERCANCIAS": {
                    mercanciasMap.put("NumTotalMercancias", p.getValue1());
                    break;
                }
                // Mercancias item
                case "CPBIENES": {
                    mercancia = new HashMap<String, String>();
                    mercancia.put("BienesTransp", p.getValue1());
                    break;
                }
                case "CPDESCRIP": {
                    mercancia.put("CPDESCRIP", p.getValue1());
                    break;
                }
                case "CPCANT": {
                    mercancia.put("Cantidad", p.getValue1());
                    break;
                }
                case "CPCUNI": {
                    mercancia.put("ClaveUnidad", p.getValue1());
                    break;
                }
                case "CPUNID": {
                    mercancia.put("Unidad", p.getValue1());
                    break;
                }
                case "CPHAZMAT": {
                    mercancia.put("CPHAZMAT", p.getValue1());
                    break;
                }
                case "CPHAZMATC": {
                    mercancia.put("CPHAZMATC", p.getValue1());
                    break;
                }
                case "CPHAZMATE": {
                    mercancia.put("CPHAZMATE", p.getValue1());
                    break;
                }
                case "CPMERPESOBRUTO": {
                    mercancia.put("CPMERPESOBRUTO", p.getValue1());
                    break;
                }
                case "CPMERPESONETO": {
                    mercancia.put("CPMERPESONETO", p.getValue1());
                    break;
                }
                case "CPMERUNIPESO": {
                    mercancia.put("CPMERUNIPESO", p.getValue1());
                    break;
                }
                case "CPPESOKG": {
                    mercancia.put("PesoEnKg", p.getValue1());
                    break;
                }
                case "CPVALOR": {
                    mercancia.put("ValorMercancia", p.getValue1());
                    break;
                }
                case "CPMONEDA": {
                    mercancia.put("CPMONEDA", p.getValue1());
                    break;
                }
                case "CPARANCEL": {
                    mercancia.put("FraccionArancelaria", p.getValue1());
                    break;
                }
                case "CPUUIDCE": {
                    mercancia.put("CPUUIDCE", p.getValue1());
                    break;
                }
                case "CPPEDIMENTO": {
                    mercancia.put("Pedimento", p.getValue1());
                    mercanciaList.add(mercancia);
                    break;
                }
                // Mercancias - Autotransporte
                case "CPCNFGV": {
                    autoTransporte.put("ConfigVehicular", p.getValue1());
                    break;
                }
                case "CPERMSCT": {
                    autoTransporte.put("PermSCT", p.getValue1());
                    break;
                }
                case "CNUMPERMSCT": {
                    autoTransporte.put("CNUMPERMSCT", p.getValue1());
                    break;
                }
                case "CPPLACAVM": {
                    autoTransporte.put("PlacaVM", p.getValue1());
                    break;
                }
                case "CPMODELOVM": {
                    autoTransporte.put("AnioModeloVM", p.getValue1());
                    break;
                }
                case "CPQSEGRESCIV": {
                    autoTransporte.put("CPQSEGRESCIV", p.getValue1());
                    break;
                }
                case "CPQSEGRESCIVN": {
                    autoTransporte.put("CPQSEGRESCIVN", p.getValue1());
                    break;
                }
                case "CPSTPOREM": {
                    autoTransporte.put("CPSTPOREM", p.getValue1());
                    break;
                }
                case "CPPLACAREM": {
                    autoTransporte.put("CPPLACAREM", p.getValue1());
                    break;
                }
                // Figura Transporte - Operadores
                case "CPTIPOFIG": {
                    operador = new HashMap<String, String>();
                    operador.put("TipoFigura", p.getValue1());
                    break;
                }
                case "CPRFCFIG": {
                    operador.put("RFCOperador", p.getValue1());
                    break;
                }
                case "CPLICFIG": {
                    operador.put("NumLicencia", p.getValue1());
                    break;
                }
                case "CPNOMFIG": {
                    operador.put("NombreOperador", p.getValue1());
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
