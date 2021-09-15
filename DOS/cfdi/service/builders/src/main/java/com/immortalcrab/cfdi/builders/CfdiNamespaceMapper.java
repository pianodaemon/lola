package com.immortalcrab.cfdi.builders;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class CfdiNamespaceMapper extends NamespacePrefixMapper {

    private static final String CFDI_PREFIX = "cfdi";
    private static final String CFDI_URI    = "http://www.sat.gob.mx/cfd/3";

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if (CFDI_URI.equals(namespaceUri)) {
            return CFDI_PREFIX;
        }
        return suggestion;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { CFDI_URI };
    }
}
