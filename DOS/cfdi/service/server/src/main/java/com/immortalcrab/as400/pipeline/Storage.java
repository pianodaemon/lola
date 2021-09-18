package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.storage.FileStorageError;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public interface Storage {

    public void upload(String path,
            String fileName,
            Optional<Map<String, String>> optionalMetaData,
            InputStream inputStream) throws FileStorageError;

    public byte[] download(String path, String key) throws FileStorageError;
}
