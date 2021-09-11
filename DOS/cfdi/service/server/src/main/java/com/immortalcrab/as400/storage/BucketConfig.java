package com.immortalcrab.as400.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BucketConfig {

    private String region;
    private String accessKey;
    private String accessSecret;

    @Bean
    public AmazonS3 genClientSthree() {

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, accessSecret);

        return AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(
                new AWSStaticCredentialsProvider(awsCredentials)
        ).build();
    }
}
