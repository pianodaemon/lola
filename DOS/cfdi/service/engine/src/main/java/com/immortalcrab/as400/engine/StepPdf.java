package com.immortalcrab.as400.engine;

import com.immortalcrab.as400.error.FormatError;
import com.immortalcrab.as400.error.StorageError;

public interface StepPdf {

    public void render(final CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError;
}
