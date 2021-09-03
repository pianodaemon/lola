package com.immortalcrab.as400.request;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;

abstract class CfdiRequest {

    protected List<Pair<String, String>> kvs = null;

    abstract Map<String, Object> craft() throws CfdiRequestError;

    abstract void captureSymbolImpt(final String label, final Object value);

    public CfdiRequest(final List<Pair<String, String>> kvs) throws CfdiRequestError {

        this.kvs = kvs;
    }

    protected String bruteSearchUniqueAttr(final String label) throws CfdiRequestError {

        for (Iterator<Pair<String, String>> it = this.kvs.iterator(); it.hasNext();) {

            Pair<String, String> p = it.next();
            if (p.getValue0().equals(label)) {
                return p.getValue1();
            }
        }

        throw new CfdiRequestError("Unique attr " + label + " not found");
    }

    protected void captureSymbol(final String label) throws CfdiRequestError {
        this.captureSymbolImpt(label, bruteSearchUniqueAttr(label));
    }
}
