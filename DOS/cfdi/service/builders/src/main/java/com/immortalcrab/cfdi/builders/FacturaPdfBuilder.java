package com.immortalcrab.cfdi.builders;

import java.util.Map;

public class FacturaPdfBuilder implements DocBuilder {

    private Map<String, Object> ds;

    private FacturaPdfBuilder(final Map<String, Object> ds) {
        this.ds = ds;
    }

    static void render(final Map<String, Object> ds) {

        FacturaPdfBuilder ic = new FacturaPdfBuilder(ds);
        ic.buildDoc();
    }

    @Override
    public void buildDoc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
