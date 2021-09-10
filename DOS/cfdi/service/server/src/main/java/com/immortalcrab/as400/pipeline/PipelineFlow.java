package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.request.CfdiRequestError;
import java.util.List;
import org.javatuples.Pair;

public interface PipelineFlow {

    public void render(List<Pair<String, String>> kvs) throws CfdiRequestError;
}
