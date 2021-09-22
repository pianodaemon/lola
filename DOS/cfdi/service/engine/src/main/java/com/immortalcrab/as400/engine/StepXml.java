package com.immortalcrab.as400.engine;

import com.immortalcrab.as400.error.FormatError;

public interface StepXml {

    public void render(CfdiRequest cfdiReq, Storage st) throws FormatError;
}
