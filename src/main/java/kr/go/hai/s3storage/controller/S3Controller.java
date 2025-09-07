package kr.go.hai.s3storage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import kr.go.hai.s3storage.service.S3Service;
import kr.go.hai.s3storage.service.impl.vo.S3VO;
import kr.selim.msa.cmmn.exception.ApiBizException;
import kr.selim.msa.cmmn.utils.ResponseUtils;
import kr.selim.msa.cmmn.vo.ApiResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3 Storage controller
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
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "S3 Storage", description = "S3 Storage 서비스를 이용하기 위한 API")
public class S3Controller {

	private final S3Service s3Service;

	/**
	 * 스토리지에 파일 업로드
	 * @param file 업로드 대상
	 * @return 파일 키
	 * @throws ApiBizException API 예외 처리
	 */
	@PostMapping(value = "/v1/s3storage/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponseVO uploadObject(@RequestPart("file") MultipartFile file,
									  @RequestParam String instCd) throws ApiBizException {
		log.debug("uploadObject");
		return ResponseUtils.build(s3Service.uploadObject(file, instCd));
	}

	/**
	 * 스토리지에서 파일 다운로드
	 * @param fileKey 파일 식별자 (업로드경로 + 파일명)
	 * @throws ApiBizException 예외처리
	 */
	@GetMapping("/v1/s3storage/download")
	public void downloadObject(HttpServletResponse response, @RequestParam String fileKey) throws ApiBizException {
		log.debug("downloadObject");
		s3Service.downloadObject(response, fileKey);
	}

	/**
	 * 파일을 다운로드하기 위한 임시 URL 생성
	 * @param fileKey 파일 식별자 (업로드경로 + 파일명)
	 * @return 임시 URL (서명된 URL, 유효시간 10분)
	 * @throws ApiBizException 예외 처리
	 */
	@PostMapping("/v1/s3storage/presigned")
	public ApiResponseVO getPresignedUrl(@RequestParam String fileKey) throws ApiBizException {
		log.debug("getPresignedUrl");
		return ResponseUtils.build(s3Service.getPresignedUrl(fileKey));
	}

	/**
	 * 스토리지에서 여러 파일을 zip으로 다운로드
	 * @param response http 응답 객체
	 * @param fileKeyList 파일 식별자 목록
	 * @throws ApiBizException 예외 처리
	 */
	@GetMapping("/v1/s3storage/download-zip")
	public void downloadZip(HttpServletResponse response, @RequestParam String[] fileKeyList) throws ApiBizException {
		log.debug("downloadObject");
		s3Service.downloadZip(response, fileKeyList);
	}

	/**
	 * 스토리지에 저장된 파일 삭제
	 * @param fileKey 삭제 대상 파일키
	 * @return 삭제 결과
	 * @throws ApiBizException 예외 처리
	 */
	@PostMapping("/v1/s3storage/delete")
	public ApiResponseVO deleteObject(@RequestParam String fileKey) throws ApiBizException {
		log.info("deleteObject");
		s3Service.deleteObject(fileKey);

		return ResponseUtils.build(fileKey + " 삭제");
	}

	/**
	 * 파일 정보 조회
	 * @param fileKey 조회 대상 파일키
	 * @return 파일 정보
	 * @throws ApiBizException 예외 처리
	 */
	@PostMapping("/v1/s3storage/info")
	public ApiResponseVO viewObject(@RequestParam String fileKey) throws ApiBizException {
		log.info("viewObject");
		return ResponseUtils.build(s3Service.viewObject(fileKey));
	}

	/**
	 * 저장된 S3 목록 가져오기
	 * @param instCd 조회 대상 기관코드
	 * @param dateString 조회 대상 일자(연 or 연월 or 연월일)
	 * @return 조회 결과
	 * @throws ApiBizException 예외 처리
	 */
	@PostMapping("/v1/s3storage/list")
	public ApiResponseVO listObject(@RequestParam(required = false) String instCd,
									@RequestParam(required = false) String dateString) throws ApiBizException {
		log.debug("listObject");
		return ResponseUtils.build(s3Service.listObject(instCd, dateString));
	}

	//@PostMapping("/v1/s3storage/test-mapper")
	public ApiResponseVO testMapper() throws ApiBizException {
		return ResponseUtils.build(s3Service.testMapper(new S3VO()));
	}
}
