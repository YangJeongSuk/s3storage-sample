package kr.go.hai.s3storage.service;

import jakarta.servlet.http.HttpServletResponse;
import kr.go.hai.s3storage.service.impl.vo.S3VO;
import kr.selim.msa.cmmn.exception.ApiBizException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * S3 Storage Service
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
public interface S3Service {

    /**
     * 스토리지에 파일 업로드
     * @param file 업로드 대상
     * @param instCd 기관코드
     * @return 파일 키
     * @throws ApiBizException API 예외 처리
     */
    String uploadObject(MultipartFile file, String instCd) throws ApiBizException;

    /**
     * 스토리지에서 파일 다운로드
     * @param fileKey 파일 식별자 (업로드경로 + 파일명)
     * @throws ApiBizException 예외처리
     */
    void downloadObject(HttpServletResponse response, String fileKey) throws ApiBizException;

    /**
     * 파일을 다운로드하기 위한 임시 URL 생성
     * @param fileKey 파일 식별자 (업로드경로 + 파일명)
     * @return 임시 URL (서명된 URL, 유효시간 10분)
     * @throws ApiBizException 예외 처리
     */
    String getPresignedUrl(String fileKey) throws ApiBizException;

    /**
     * 스토리지에 저장된 파일 삭제
     * @param fileKey 삭제 대상 파일키
     * @throws ApiBizException 예외 처리
     */
    void deleteObject(String fileKey) throws ApiBizException;

    /**
     * 파일 정보 조회
     * @param fileKey 조회 대상 파일키
     * @return 파일 정보
     * @throws ApiBizException 예외 처리
     */
    S3VO viewObject(String fileKey) throws ApiBizException;

    /**
     * 저장된 S3 목록 가져오기
     * @param instCd 조회 대상 기관코드
     * @param dateString 조회 대상 일자(연 or 연월 or 연월일)
     * @return 조회 결과
     * @throws ApiBizException 예외 처리
     */
    List<S3VO> listObject(String instCd, String dateString) throws ApiBizException;

    S3VO testMapper(S3VO vo);
}
