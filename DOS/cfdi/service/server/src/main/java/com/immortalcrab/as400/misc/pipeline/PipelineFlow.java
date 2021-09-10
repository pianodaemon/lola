package com.immortalcrab.as400.misc.pipeline;

import com.immortalcrab.as400.request.CfdiRequestError;
import java.util.List;
import org.javatuples.Pair;

public interface PipelineFlow {

    public void track(List<Pair<String, String>> kvs) throws CfdiRequestError;
}
