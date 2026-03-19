package org.howread.infra.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트 설정.
 *
 * 자격증명 우선순위:
 * 1. 환경변수(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)가 설정된 경우 → StaticCredentialsProvider
 * 2. 미설정 시 → DefaultCredentialsProvider (EC2 인스턴스 프로파일, IAM Role 등 자동 탐색)
 *
 * 이 방식으로 로컬 개발(명시적 키)과 프로덕션(인스턴스 프로파일) 모두 대응한다.
 */
@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region));

        if (accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
