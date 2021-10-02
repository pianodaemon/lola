package com.immortalcrab.as400.storage;

import com.immortalcrab.as400.error.StorageError;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.immortalcrab.as400.engine.Storage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class SthreeStorage implements Storage {

    private final AmazonS3 amazonS3;

    static public SthreeStorage configure(final String region,
            final String accessKey, final String accessSecret) throws StorageError {

        if (region == null) {
            throw new StorageError("aws region was not found");
        }

        if (accessKey == null) {
            throw new StorageError("aws key was not found");
        }

        if (accessSecret == null) {
            throw new StorageError("aws secret was not found");
        }

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, accessSecret);

        return new SthreeStorage(AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(
                new AWSStaticCredentialsProvider(awsCredentials)
        ).build());
    }

    private SthreeStorage(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void upload(
            final String cType,
            final long len,
            final String path,
            final String fileName,
            InputStream inputStream) throws StorageError {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(cType);
        objectMetadata.setContentLength(len);
        try {
            amazonS3.putObject(path, fileName, inputStream, objectMetadata);
        } catch (AmazonServiceException ex) {
            throw new StorageError("Failed to upload the file", ex);
        }
    }

    @Override
    public byte[] download(String path, String key) throws StorageError {
        try {
            S3Object object = amazonS3.getObject(path, key);
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException ex) {
            throw new StorageError("Failed to download the file", ex);
        }
    }

}
