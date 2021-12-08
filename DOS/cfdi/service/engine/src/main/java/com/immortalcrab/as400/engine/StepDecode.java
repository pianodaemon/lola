package com.immortalcrab.as400.engine;

import com.immortalcrab.as400.error.CfdiRequestError;
import com.immortalcrab.as400.error.DecodeError;
import java.io.InputStreamReader;

public interface StepDecode {

    public CfdiRequest render(InputStreamReader inReader) throws CfdiRequestError, DecodeError;
}
