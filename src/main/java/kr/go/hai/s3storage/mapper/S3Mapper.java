package kr.go.hai.s3storage.mapper;

import kr.go.hai.s3storage.service.impl.vo.S3VO;
import org.apache.ibatis.annotations.Mapper;

/**
 * S3 Storage Mapper
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
@Mapper
public interface S3Mapper {
    S3VO selectTest(S3VO vo);
}
