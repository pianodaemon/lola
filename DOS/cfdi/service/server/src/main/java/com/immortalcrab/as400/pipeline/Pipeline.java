package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.engine.ErrorCodes;
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

    public synchronized PipelineFlow incept(final String kind) throws PipelineError {

        PipelineFlow pf = ic.factory.get(kind);

        if (pf != null) {
            return pf;
        }

        throw new PipelineError("cfdi " + kind + " is unsupported", ErrorCodes.DOCBUILD_ERROR);
    }

    public static void issue(final String kind, InputStreamReader reader) throws PairExtractorError, CfdiRequestError, PipelineError {

        PipelineFlow g = Pipeline.getInstance().incept(kind);
        System.out.println(g.render(PairExtractor.go4it(reader)).getDs());
    }

}
