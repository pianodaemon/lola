package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.error.PipelineError;
import com.immortalcrab.as400.engine.StepDecode;
import com.immortalcrab.as400.engine.StepXml;
import com.immortalcrab.as400.engine.StepPdf;
import com.immortalcrab.as400.engine.Storage;
import com.immortalcrab.as400.error.ErrorCodes;
import com.immortalcrab.as400.formats.FacturaPdf;
import com.immortalcrab.as400.formats.FacturaXml;
import com.immortalcrab.as400.parser.PairExtractor;
import com.immortalcrab.as400.error.PairExtractorError;
import com.immortalcrab.as400.engine.CfdiRequest;
import com.immortalcrab.as400.error.CfdiRequestError;
import com.immortalcrab.as400.request.FacturaRequest;
import com.immortalcrab.as400.error.StorageError;
import com.immortalcrab.as400.storage.SthreeStorage;
import org.javatuples.Triplet;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Pipeline {

    private static Pipeline ic = null;
    private static Storage st = null;
    private final Map<String, Triplet<StepDecode, StepXml, StepPdf>> scenarios = new HashMap<>();

    public static synchronized Pipeline getInstance() throws StorageError {

        if (ic == null) {
            ic = new Pipeline();

            st = SthreeStorage.configure(
                    System.getenv("BUCKET_REGION"),
                    System.getenv("BUCKET_KEY"),
                    System.getenv("BUCKET_SECRET"));

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

    public static void issue(final String kind, InputStreamReader reader) throws PairExtractorError, CfdiRequestError, PipelineError, StorageError {

        Triplet<StepDecode, StepXml, StepPdf> stages = Pipeline.getInstance().incept(kind);

        /* First stage of the pipeline
           It stands for decoding what has been read
           from the data origin (in this case the infamous as400) */
        StepDecode sdec = stages.getValue0();
        CfdiRequest cfdiReq = sdec.render(PairExtractor.go4it(reader));

        /* Second stage of the pipeline
           It stands for hand craft a valid xml at sat */
        StepXml sxml = stages.getValue1();
        sxml.render(cfdiReq, System.out);

        /* Third stage of the pipeline
           It stands for hand craft a arbitrary
           representation of a cfdi in pdf format  */
        StepPdf spdf = stages.getValue2();
        Object rpdf = spdf.render(cfdiReq);

        System.out.println(cfdiReq.getDs());
    }
}
