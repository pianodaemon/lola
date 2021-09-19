package com.immortalcrab.as400.engine;

import com.immortalcrab.as400.error.CfdiRequestError;
import java.util.List;
import org.javatuples.Pair;

public interface StepDecode {

    public CfdiRequest render(List<Pair<String, String>> kvs) throws CfdiRequestError;
}
