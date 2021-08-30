package com.immortalcrab.cfdi.builders;

import java.util.Map;

public class FacturaXmlBuilder implements DocBuilder {

    private Map<String, Object> ds;

    private FacturaXmlBuilder(final Map<String, Object> ds) {
        this.ds = ds;
    }

    static void render(final Map<String, Object> ds) {

        FacturaXmlBuilder ic = new FacturaXmlBuilder(ds);
        ic.buildDoc();
    }

    public void buildDoc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
