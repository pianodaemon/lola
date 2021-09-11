package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.engine.ErrorCodes;
import com.immortalcrab.as400.formats.FacturaPdf;
import com.immortalcrab.as400.formats.FacturaXml;
import com.immortalcrab.as400.parser.PairExtractor;
import com.immortalcrab.as400.parser.PairExtractorError;
import com.immortalcrab.as400.request.CfdiRequest;
import com.immortalcrab.as400.request.CfdiRequestError;
import com.immortalcrab.as400.request.FacturaRequest;
import org.javatuples.Triplet;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Pipeline {

    private static Pipeline ic = null;
    private final Map<String, Triplet<StepDecode, StepXml, StepPdf>> scenarios = new HashMap<>();

    public static synchronized Pipeline getInstance() {

        if (ic == null) {
            ic = new Pipeline();
            ic.scenarios.put("fac", new Triplet<>(FacturaRequest::render, FacturaXml::render, FacturaPdf::render));
        }

        return ic;
    }

    public synchronized Triplet<StepDecode, StepXml, StepPdf> incept(final String kind) throws PipelineError {

        Triplet<StepDecode, StepXml, StepPdf> pf = ic.scenarios.get(kind);

        if (pf != null) {
            return pf;
        }

        throw new PipelineError("cfdi " + kind + " is unsupported", ErrorCodes.DOCBUILD_ERROR);
    }

    public static void issue(final String kind, InputStreamReader reader) throws PairExtractorError, CfdiRequestError, PipelineError {

        Triplet<StepDecode, StepXml, StepPdf> stages = Pipeline.getInstance().incept(kind);

        /* First stage of the pipeline
           It stands for decoding what has been read
           from the data origin (in this case the infamous as400) */
        StepDecode sdec = stages.getValue0();
        CfdiRequest cfdiReq = sdec.render(PairExtractor.go4it(reader));

        /* Second stage of the pipeline
           It stands for hand craft a valid xml at sat */
        StepXml sxml = stages.getValue1();
        Object rxml = sxml.render(cfdiReq);

        /* Third stage of the pipeline
           It stands for hand craft a arbitrary
           representation of a cfdi in pdf format  */
        StepPdf spdf = stages.getValue2();
        Object rpdf = spdf.render(cfdiReq);

        System.out.println(cfdiReq.getDs());
    }

}
