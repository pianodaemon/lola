package com.immortalcrab.as400.formats;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class CfdiNamespaceMapper2 extends NamespacePrefixMapper {

    private static final String CFDI_PREFIX       = "cfdi";
    private static final String CFDI_URI          = "http://www.sat.gob.mx/cfd/3";
    private static final String CARTAPORTE_PREFIX = "cartaporte20";
    private static final String CARTAPORTE_URI    = "http://www.sat.gob.mx/CartaPorte20";

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

        if (CFDI_URI.equals(namespaceUri)) {
            return CFDI_PREFIX;

        } else if (CARTAPORTE_URI.equals(namespaceUri)) {
            return CARTAPORTE_PREFIX;
        }

        return suggestion;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { CFDI_URI, CARTAPORTE_URI };
    }
}
