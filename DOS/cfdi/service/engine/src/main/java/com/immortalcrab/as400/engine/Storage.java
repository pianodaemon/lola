package com.immortalcrab.as400.engine;

import com.immortalcrab.as400.error.StorageError;
import java.io.InputStream;

public interface Storage {

    public void upload(final String cType,
            final long len,
            final String fileName,
            InputStream inputStream) throws StorageError;
}
