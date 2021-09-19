package com.immortalcrab.as400.engine;

import com.immortalcrab.as400.error.StorageError;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public interface Storage {

    public void upload(String path,
            String fileName,
            Optional<Map<String, String>> optionalMetaData,
            InputStream inputStream) throws StorageError;

    public byte[] download(String path, String key) throws StorageError;
}
