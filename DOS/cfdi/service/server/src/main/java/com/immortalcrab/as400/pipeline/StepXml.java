package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.request.CfdiRequest;

public interface StepXml {

    public Object render(CfdiRequest cfdiReq);
}
