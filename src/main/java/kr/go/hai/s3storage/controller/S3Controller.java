package kr.go.hai.s3storage.controller;

import kr.go.hai.config.info.ConstantInfo;
import kr.go.hai.config.info.S3Info;
import kr.selim.msa.cmmn.exception.ApiBizException;
import kr.selim.msa.cmmn.utils.ResponseUtils;
import kr.selim.msa.cmmn.vo.ApiResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
public class S3Controller {

	private final S3Info s3Info;
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

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
		String fileKey = this.getUploadFileKey(instCd, file.getOriginalFilename());

		try {
			s3Client.putObject(
				req -> req.bucket(s3Info.getBucket()).key(fileKey), //.acl(ObjectCannedACL.PUBLIC_READ),//=>다운로드 권한 체크 안하는 옵션 (보안상 문제 있음)
				RequestBody.fromInputStream(file.getInputStream(), file.getSize())
			);

		} catch(S3Exception | IOException e) {
			log.info("파일 업로드 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류 발생");
		}

		return ResponseUtils.build(fileKey);
	}

	/**
	 * 스토리지에 저장할 파일 키 생성
	 * 생성규칙 : 기관코드(7) + "/" + 연도(4) + "/" + 월(2) + "/" + 일(2) + "/" + UUID(36) + "/" + 원본파일명(확장자 포함, 900byte 제한)
	 * @param instCd 기관코드
	 * @param originalFilename 원본파일명
	 * @return 파일 저장키
	 */
	private String getUploadFileKey(String instCd, String originalFilename) {

		if (instCd == null || instCd.trim().isEmpty()) {
			throw new ApiBizException(HttpStatus.BAD_REQUEST, "기관코드가 없습니다.");
		}

		if (originalFilename.getBytes(StandardCharsets.UTF_8).length > ConstantInfo.S3_FILE_MAX_LENGTH) {
			throw new ApiBizException(HttpStatus.BAD_REQUEST, "파일명이 너무 깁니다.");
		}

		return String.join(ConstantInfo.S3_PREFIX_DELIMITER
				, instCd
				, LocalDate.now().format(DateTimeFormatter.ofPattern(ConstantInfo.S3_DATE_PREFIX_FORMAT))
				, UUID.randomUUID().toString()
				, originalFilename
		);
	}

	/**
	 * 스토리지에서 파일 다운로드
	 * @param fileKey 파일 식별자 (업로드경로 + 파일명)
	 * @return 파일
	 * @throws ApiBizException 예외처리
	 */
	@GetMapping("/v1/s3storage/download")
	public ResponseEntity<byte[]> downloadObject(@RequestParam String fileKey) throws ApiBizException {
		try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(GetObjectRequest.builder()
				.bucket(s3Info.getBucket()).key(fileKey).build());
			 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			IoUtils.copy(s3Object, byteArrayOutputStream);
			String filename = Paths.get(fileKey).getFileName().toString();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.valueOf(s3Object.response().contentType()))
					.body(byteArrayOutputStream.toByteArray());
		} catch (S3Exception | IOException e) {
			log.error("파일 다운로드 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
		}
	}

	/**
	 * 파일을 다운로드하기 위한 임시 URL 생성
	 * @param fileKey 파일 식별자 (업로드경로 + 파일명)
	 * @return 임시 URL (서명된 URL, 유효시간 10분)
	 * @throws ApiBizException 예외 처리
	 */
	@PostMapping("/v1/s3storage/presigned")
	public ApiResponseVO getPresignedUrl(@RequestParam String fileKey) throws ApiBizException {
		String preSignedUrl;

		try {
			GetObjectRequest objectRequest = GetObjectRequest.builder()
					.bucket(s3Info.getBucket())
					.key(fileKey)
					.build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(s3Info.getPresignedTime()))  // The URL will expire in 10 minutes.
					.getObjectRequest(objectRequest)
					.build();

			PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
			preSignedUrl = presignedRequest.url().toExternalForm();

			log.info("Presigned URL: [{}]", presignedRequest.url().toString());
			log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

		} catch (S3Exception | IllegalArgumentException e) {
			log.error("파일 다운로드 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
		}

		return ResponseUtils.build(preSignedUrl);
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

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
					.bucket(s3Info.getBucket())
					.key(fileKey)
					.build();

			s3Client.deleteObject(deleteObjectRequest);

		} catch (S3Exception e) {
			log.error("삭제 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "삭제 중 오류 발생");
		}

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
		Map<String, Object> result = new HashMap<>();

		try {
			HeadObjectRequest headRequest = HeadObjectRequest.builder()
					.bucket(s3Info.getBucket())
					.key(fileKey)
					.build();

			HeadObjectResponse headResponse = s3Client.headObject(headRequest);

			result.put("fileKey", fileKey);
			result.put("size", headResponse.contentLength());
			result.put("contentType", headResponse.contentType());
			result.put("eTag", headResponse.eTag());
			result.put("lastModified", headResponse.lastModified());

		} catch(S3Exception e) {
			log.error("조회 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "조회 중 오류 발생");
		}

		return ResponseUtils.build(result);
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
		List<Map<String, Object>> list = new ArrayList<>();

		try {
			String prefix = this.buildPrefix(instCd, dateString);

			ListObjectsV2Request.Builder listBuilder = ListObjectsV2Request.builder()
					.bucket(s3Info.getBucket())
					.maxKeys(s3Info.getPageSize());

			if(prefix != null && !prefix.trim().isEmpty()) {
				listBuilder.prefix(prefix);
			}

			// 전체 페이지를 순회하며 모든 데이터 가져오기
			ListObjectsV2Iterable listRes = s3Client.listObjectsV2Paginator(listBuilder.build());
			listRes.stream()
					.flatMap(r -> r.contents().stream())
					.forEach(content -> {
						Map<String, Object> map = new HashMap<>();
						map.put("fileKey", content.key());
						map.put("size", content.size());
						map.put("eTag", content.eTag());
						map.put("lastModified", content.lastModified());
						list.add(map);
					});

			// 한번에 한 페이지의 데이터만 가져오는 경우
//			ListObjectsV2Response response = s3Client.listObjectsV2(listBuilder.build());
//			List<S3Object> objects = response.contents();
//			objects.forEach(content -> map.put(content.key(), content.size()));

		} catch (S3Exception e) {
			log.info("조회 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "조회 중 오류 발생");
		}

		return ResponseUtils.build(list);
	}

	/**
	 * 입력 조건을 이용하여 조회 대상의 prefix 생성
	 * @param instCd 기관코드
	 * @param dateString 날짜(연, 연월, 연월일)
	 * @return 기관코드(4)/연(4)/ or 기관코드(4)/연(4)/월(2)/ or 기관코드(4)/연(4)/월(2)/일(2)/
	 */
	private String buildPrefix(String instCd, String dateString) {
		if (instCd == null || instCd.isBlank()) {
			return "";
		}

		StringBuilder prefix = new StringBuilder(instCd).append(ConstantInfo.S3_PREFIX_DELIMITER);

		if (dateString != null && !dateString.isBlank()) {
			if(dateString.length() == 4) {
				prefix.append(dateString).append(ConstantInfo.S3_PREFIX_DELIMITER);
			} else if(dateString.length() == 6) {
				prefix.append(dateString.substring(0, 4)).append(ConstantInfo.S3_PREFIX_DELIMITER)
						.append(dateString.substring(4, 6)).append(ConstantInfo.S3_PREFIX_DELIMITER);
			} else if(dateString.length() == 8) {
				prefix.append(dateString.substring(0, 4)).append(ConstantInfo.S3_PREFIX_DELIMITER)
						.append(dateString.substring(4, 6)).append(ConstantInfo.S3_PREFIX_DELIMITER)
						.append(dateString.substring(6, 8)).append(ConstantInfo.S3_PREFIX_DELIMITER);
			} else {
				prefix.append(dateString);
			}
		}

		return prefix.toString();
	}
}
