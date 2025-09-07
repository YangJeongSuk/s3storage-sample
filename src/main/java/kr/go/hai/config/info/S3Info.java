package kr.go.hai.config.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * S3 Information
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
@Component
@ConfigurationProperties("s3")
@Getter
@Setter
public class S3Info {
    @Schema(description = "S3 Access Key")
    private String accessKey;

    @Schema(description = "S3 Secret Key")
    private String secretKey;

    @Schema(description = "SCP S3 Endpoint URL")
    private String endpoint;

    @Schema(description = "Region")
    private String region;

    @Schema(description = "bucket name")
    private String bucket;

    @Schema(description = "Presigned Url expiration time")
    private int presignedTime;

    @Schema(description = "S3 Object List Page Size")
    private int pageSize;
}
