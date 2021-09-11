package com.immortalcrab.as400.storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.immortalcrab.as400.engine.ErrorCodes;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

class FileStore {

    private final AmazonS3 amazonS3;

    FileStore(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void upload(String path,
            String fileName,
            Optional<Map<String, String>> optionalMetaData,
            InputStream inputStream) throws FileStorageError {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        optionalMetaData.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        try {
            amazonS3.putObject(path, fileName, inputStream, objectMetadata);
        } catch (AmazonServiceException ex) {
            throw new FileStorageError("Failed to upload the file", ex, ErrorCodes.THIRD_PARTY_ISSUES);
        }
    }

    public byte[] download(String path, String key) throws FileStorageError {
        try {
            S3Object object = amazonS3.getObject(path, key);
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException ex) {
            throw new FileStorageError("Failed to download the file", ex, ErrorCodes.THIRD_PARTY_ISSUES);
        }
    }

}
