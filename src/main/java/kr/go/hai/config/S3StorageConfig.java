package kr.go.hai.config;

import kr.go.hai.config.info.S3Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * S3 Client Bean
 *
 * @author AX사업팀
 * @version 1.0
 * @since 2025.09.05
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자            수정내용
 * ----------    --------    ---------------------------
 * 2025.09.05    양정숙        최초 생성
 * </pre>
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3StorageConfig {
    private final S3Info s3Info;

    /**
     * S3 Storage를 이용하기 위한 Bean 등록
     * @return S3Client
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(s3Info.getRegion()))
                .endpointOverride(URI.create(s3Info.getEndpoint()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Info.getAccessKey(), s3Info.getSecretKey())
                ))
                .build();
    }

    /**
     * 다른 경로에서 S3 파일 업로드/다운로드를 위한 서명된 URL 생성 Bean 등록
     * @return S3Presigner
     */
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(s3Info.getRegion()))
                .endpointOverride(URI.create(s3Info.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Info.getAccessKey(), s3Info.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // path-style 강제
                        .build())
                .build();
    }
}
