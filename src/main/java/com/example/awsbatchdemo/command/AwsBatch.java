package com.example.awsbatchdemo.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.*;

import java.net.URI;


@Slf4j
@Component
public class AwsBatch implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        ProxyConfiguration proxyConfig =
                ProxyConfiguration.builder()
                        .endpoint(URI.create("http://localhost:8888"))
                        .build();

        SdkHttpClient httpClient =
                ApacheHttpClient.builder()
//                        .proxyConfiguration(proxyConfig)
                        .build();

        AwsCredentialsProvider provider = DefaultCredentialsProvider
                .create();

//        https://docs.aws.amazon.com/ja_jp/sdk-for-java/latest/developer-guide/credentials-explicit.html


        BatchClient batchClient = BatchClient.builder()
                .credentialsProvider(provider)
                .httpClient(httpClient) // Proxy経由接続
//                .endpointOverride(URI.create("")) // VPCエンドポイントを指定
                .region(Region.AP_NORTHEAST_1)
                .build();


        ListJobsResponse listJobsResponse = batchClient.listJobs(
                ListJobsRequest.builder().jobQueue("getting-started-job-queue").build()
        );
        listJobsResponse.jobSummaryList().stream().forEach(x -> {
            log.info("running jobId = {}", x.jobId());
        });

        log.info("new job submitting");
        SubmitJobResponse submitJobResponse = batchClient.submitJob(SubmitJobRequest.builder()
                .jobName("test-abc")
                .jobDefinition("getting-started-job-definition2")
                .jobQueue("getting-started-job-queue")
                .build());

        log.info("jobId = {}", submitJobResponse.jobId());

        log.info("waiting");
        Thread.sleep(30000);

        log.info("job terminating");
        TerminateJobResponse terminateJobResponse = batchClient.terminateJob(TerminateJobRequest.builder()
                .jobId(submitJobResponse.jobId())
                .reason("test")
                .build()
        );

        log.info(terminateJobResponse.toString());
    }
}
