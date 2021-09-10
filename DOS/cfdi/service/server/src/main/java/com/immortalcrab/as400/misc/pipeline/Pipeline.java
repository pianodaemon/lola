package com.immortalcrab.as400.misc.pipeline;

import com.immortalcrab.as400.parser.PairExtractor;
import com.immortalcrab.as400.parser.PairExtractorError;
import com.immortalcrab.as400.request.CfdiRequestError;
import com.immortalcrab.as400.request.FacturaRequest;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Pipeline {

    private static Pipeline ic = null;
    private final Map<String, PipelineFlow> factory = new HashMap<>();

    public static synchronized Pipeline getInstance() {

        if (ic == null) {
            ic = new Pipeline();
            ic.factory.put("factura", FacturaRequest::render);
        }

        return ic;
    }

    public synchronized PipelineFlow incept(final String kind) {
        return ic.factory.get(kind);
    }

    public static void issue(final String kind, InputStreamReader reader) throws PairExtractorError, CfdiRequestError {

        PipelineFlow g = Pipeline.getInstance().incept(kind);
        g.track(PairExtractor.go4it(reader));
    }

}
