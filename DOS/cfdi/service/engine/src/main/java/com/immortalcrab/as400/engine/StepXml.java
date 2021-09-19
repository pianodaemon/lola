package com.immortalcrab.as400.engine;

import java.io.OutputStream;

public interface StepXml {

    public void render(CfdiRequest cfdiReq, OutputStream out);
}
