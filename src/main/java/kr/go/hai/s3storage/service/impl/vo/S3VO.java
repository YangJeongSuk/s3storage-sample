package kr.go.hai.s3storage.service.impl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.selim.msa.cmmn.vo.CmmnVO;
import lombok.Getter;
import lombok.Setter;

/**
 * S3 Storage VO
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
@Schema(description = "S3 Storage")
@Getter
@Setter
public class S3VO extends CmmnVO {

    @Schema(description = "저장된 파일 키")
    private String fileKey;

    @Schema(description = "파일 크기")
    private long size;

    @Schema(description = "파일 content type")
    private String contentType;

    @Schema(description = "eTag")
    private String eTag;

    @Schema(description = "최종 수정일")
    private String lastModified;
}
