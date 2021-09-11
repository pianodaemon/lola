package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.request.CfdiRequest;

public interface StepPdf {

    public Object render(final CfdiRequest cfdiReq);
}
