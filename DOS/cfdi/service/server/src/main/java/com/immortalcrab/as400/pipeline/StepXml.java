package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.request.CfdiRequest;
import java.io.OutputStream;

public interface StepXml {

    public void render(CfdiRequest cfdiReq, OutputStream out);
}
