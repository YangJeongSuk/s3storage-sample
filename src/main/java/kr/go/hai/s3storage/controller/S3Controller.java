package kr.go.hai.s3storage.controller;

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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

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
	private final String savePath = "test/";

	/**
	 * 스토리지에 파일 업로드
	 * @param file 업로드 대상
	 * @return 파일 키
	 * @throws ApiBizException API 예외 처리
	 */
	@PostMapping(value = "/v1/s3storage/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponseVO uploadObject(@RequestPart("file") MultipartFile file) throws ApiBizException {
		log.debug("uploadObject");
		String key = savePath + file.getOriginalFilename();

		try {
			s3Client.putObject(
				req -> req.bucket(s3Info.getBucket()).key(key), //.acl(ObjectCannedACL.PUBLIC_READ),//=>다운로드 권한 체크 안하는 옵션
				RequestBody.fromInputStream(file.getInputStream(), file.getSize())
			);

		} catch(IOException e) {
			log.info("파일 업로드 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류 발생");
		}

		return ResponseUtils.build(key);
	}

	/**
	 * 스토리지에서 파일 다운로드
	 * @param filekey 파일 식별자 (업로드경로 + 파일명)
	 * @return 파일
	 * @throws ApiBizException 예외처리
	 */
	@GetMapping("/v1/s3storage/download")
	public ResponseEntity<byte[]> downloadObject(@RequestParam String filekey) throws ApiBizException {
		try (ResponseInputStream<GetObjectResponse> s3Object =
					 s3Client.getObject(GetObjectRequest.builder()
							 .bucket(s3Info.getBucket())
							 .key(filekey)
							 .build());
			 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			IoUtils.copy(s3Object, baos);
			String filename = Paths.get(filekey).getFileName().toString();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.valueOf(s3Object.response().contentType()))
					.body(baos.toByteArray());
		} catch (IOException e) {
			log.error("파일 다운로드 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
		}
	}

	/**
	 * 파일을 다운로드하기 위한 임시 URL 생성
	 * @param filekey 파일 식별자 (업로드경로 + 파일명)
	 * @return 임시 URL (서명된 URL, 유효시간 10분)
	 * @throws ApiBizException 예외 처리
	 */
	@PostMapping("/v1/s3storage/presigned")
	public ApiResponseVO getPresigendUrl(@RequestParam String filekey) throws ApiBizException {
		String preSignedUrl;

		try {
			GetObjectRequest objectRequest = GetObjectRequest.builder()
					.bucket(s3Info.getBucket())
					.key(filekey)
					.build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(s3Info.getPresignedTime()))  // The URL will expire in 10 minutes.
					.getObjectRequest(objectRequest)
					.build();

			PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
			log.info("Presigned URL: [{}]", presignedRequest.url().toString());
			log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

			preSignedUrl = presignedRequest.url().toExternalForm();
		} catch (S3Exception | IllegalArgumentException e) {
			log.error("파일 다운로드 중 오류 발생", e);
			throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
		}

		return ResponseUtils.build(preSignedUrl);
	}
}
