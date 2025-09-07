package kr.go.hai.s3storage.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import kr.go.hai.config.info.ConstantInfo;
import kr.go.hai.config.info.S3Info;
import kr.go.hai.s3storage.mapper.S3Mapper;
import kr.go.hai.s3storage.service.S3Service;
import kr.go.hai.s3storage.service.impl.vo.S3VO;
import kr.selim.msa.cmmn.exception.ApiBizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * S3 Storage Service implement
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
@Service("s3Service")
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Info s3Info;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Mapper s3Mapper;

    /**
     * 스토리지에 파일 업로드
     * @param file 업로드 대상
     * @param instCd 기관코드
     * @return 파일 키
     * @throws ApiBizException API 예외 처리
     */
    @Override
    public String uploadObject(MultipartFile file, String instCd) throws ApiBizException {
        String fileKey = getUploadFileKey(instCd, file.getOriginalFilename());

        try {
            s3Client.putObject(
                    req -> req.bucket(s3Info.getBucket()).key(fileKey),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (S3Exception | IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
        }

        return fileKey;
    }

    /**
     * 스토리지에서 파일 다운로드
     * @param fileKey 파일 식별자 (업로드경로 + 파일명)
     * @throws ApiBizException 예외처리
     */
    @Override
    public void downloadObject(HttpServletResponse response, String fileKey) throws ApiBizException {
        try (ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(
                GetObjectRequest.builder().bucket(s3Info.getBucket()).key(fileKey).build()
        )) {
            // 파일명 추출
            String filename = Paths.get(fileKey).getFileName().toString();

            // HTTP 헤더 설정
            this.setResponse(response, filename, s3InputStream.response().contentType(), s3InputStream.response().contentLength());

            // 스트리밍 전송
            StreamUtils.copy(s3InputStream, response.getOutputStream());
            response.flushBuffer();

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
    @Override
    public String getPresignedUrl(String fileKey) throws ApiBizException {
        try {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(s3Info.getBucket())
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(s3Info.getPresignedTime()))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toExternalForm();
        } catch (S3Exception | IllegalArgumentException e) {
            log.error("Presigned URL 발급 실패", e);
            throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "URL 발급 실패");
        }
    }

    /**
     * 스토리지에서 여러 파일을 zip으로 다운로드
     * @param response http 응답 객체
     * @param fileKeyList 파일 식별자 목록
     * @throws ApiBizException 예외 처리
     */
    @Override
    public void downloadZip(HttpServletResponse response, String[] fileKeyList) throws ApiBizException {
        if(fileKeyList == null) {
            return;
        }

        // 다운 파일명
        String zipName = LocalDate.now() + ".zip";

        // HTTP 헤더 설정
        this.setResponse(response, zipName, "application/zip", null);

        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for(String fileKey:fileKeyList) {
                try (ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(
                        GetObjectRequest.builder().bucket(s3Info.getBucket()).key(fileKey).build()
                )) {
                    // 파일명 추출
                    String filename = Paths.get(fileKey).getFileName().toString();

                    // ZIPentry(압축될 파일명)
                    ZipEntry zipEntry = new ZipEntry(filename);
                    zos.putNextEntry(zipEntry);

                    // 파일 데이터 쓰기
                    StreamUtils.copy(s3InputStream, zos);
                    zos.closeEntry();

                } catch (S3Exception | IOException e) {
                    log.error("파일 다운로드 중 오류 발생", e);
                    throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 다운로드 실패");
                }
            }
        } catch (IOException e) {
            throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "ZIP 파일을 생성할 수 없습니다.");
        }
    }

    /**
     * 스토리지에 저장된 파일 삭제
     * @param fileKey 삭제 대상 파일키
     * @throws ApiBizException 예외 처리
     */
    @Override
    public void deleteObject(String fileKey) throws ApiBizException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Info.getBucket())
                    .key(fileKey)
                    .build());
        } catch (S3Exception e) {
            log.error("파일 삭제 중 오류 발생", e);
            throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "삭제 실패");
        }
    }

    /**
     * 파일 정보 조회
     * @param fileKey 조회 대상 파일키
     * @return 파일 정보
     * @throws ApiBizException 예외 처리
     */
    @Override
    public S3VO viewObject(String fileKey) throws ApiBizException {
        S3VO s3VO = new S3VO();

        try {
            HeadObjectResponse headResponse = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(s3Info.getBucket())
                            .key(fileKey)
                            .build()
            );

            s3VO.setFileKey(fileKey);
            s3VO.setSize(headResponse.contentLength());
            s3VO.setContentType(headResponse.contentType());
            s3VO.setETag(headResponse.eTag());
            s3VO.setLastModified(String.valueOf(headResponse.lastModified()));

        } catch (S3Exception e) {
            log.error("조회 중 오류 발생", e);
            throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "조회 실패");
        }

        return s3VO;
    }

    /**
     * 저장된 S3 목록 가져오기
     * @param instCd 조회 대상 기관코드
     * @param dateString 조회 대상 일자(연 or 연월 or 연월일)
     * @return 조회 결과
     * @throws ApiBizException 예외 처리
     */
    @Override
    public List<S3VO> listObject(String instCd, String dateString) throws ApiBizException {
        List<S3VO> list = new ArrayList<>();

        try {
            String prefix = buildPrefix(instCd, dateString);

            ListObjectsV2Request.Builder listBuilder = ListObjectsV2Request.builder()
                    .bucket(s3Info.getBucket())
                    .maxKeys(s3Info.getPageSize());

            if (prefix != null && !prefix.isBlank()) {
                listBuilder.prefix(prefix);
            }

            // 전체 페이지를 순회하며 모든 데이터 가져오기
            ListObjectsV2Iterable listRes = s3Client.listObjectsV2Paginator(listBuilder.build());
            listRes.stream()
                    .flatMap(r -> r.contents().stream())
                    .forEach(content -> {
                        S3VO s3VO = new S3VO();
                        s3VO.setFileKey(content.key());
                        s3VO.setSize(content.size());
                        s3VO.setETag(content.eTag());
                        s3VO.setLastModified(String.valueOf(content.lastModified()));
                        list.add(s3VO);
                    });


            // 한번에 한 페이지의 데이터만 가져오는 경우
//			ListObjectsV2Response response = s3Client.listObjectsV2(listBuilder.build());
//			List<S3Object> objects = response.contents();
//			objects.forEach(content -> map.put(content.key(), content.size()));

        } catch (S3Exception e) {
            log.error("목록 조회 실패", e);
            throw new ApiBizException(HttpStatus.INTERNAL_SERVER_ERROR, "목록 조회 실패");
        }
        return list;
    }

    /**
     * 스토리지에 저장할 파일 키 생성
     * 생성규칙 : 기관코드(7) + "/" + 연도(4) + "/" + 월(2) + "/" + 일(2) + "/" + UUID(32) + "/" + 원본파일명(확장자 포함, 900byte 제한)
     * @param instCd 기관코드
     * @param originalFilename 원본파일명
     * @return 파일 저장키
     */
    private String getUploadFileKey(String instCd, String originalFilename) {
        if (instCd == null || instCd.isBlank()) {
            throw new ApiBizException(HttpStatus.BAD_REQUEST, "기관코드가 없습니다.");
        }

        if (originalFilename.getBytes(StandardCharsets.UTF_8).length > ConstantInfo.S3_FILE_MAX_LENGTH) {
            throw new ApiBizException(HttpStatus.BAD_REQUEST, "파일명이 너무 깁니다.");
        }

        return String.join(ConstantInfo.S3_PREFIX_DELIMITER,
                instCd,
                LocalDate.now().format(DateTimeFormatter.ofPattern(ConstantInfo.S3_DATE_PREFIX_FORMAT)),
                UUID.randomUUID().toString(),
                originalFilename
        );
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
            if (dateString.length() == 4) {
                prefix.append(dateString).append(ConstantInfo.S3_PREFIX_DELIMITER);
            } else if (dateString.length() == 6) {
                prefix.append(dateString, 0, 4).append(ConstantInfo.S3_PREFIX_DELIMITER)
                        .append(dateString, 4, 6).append(ConstantInfo.S3_PREFIX_DELIMITER);
            } else if (dateString.length() == 8) {
                prefix.append(dateString, 0, 4).append(ConstantInfo.S3_PREFIX_DELIMITER)
                        .append(dateString, 4, 6).append(ConstantInfo.S3_PREFIX_DELIMITER)
                        .append(dateString, 6, 8).append(ConstantInfo.S3_PREFIX_DELIMITER);
            } else {
                prefix.append(dateString);
            }
        }
        return prefix.toString();
    }

    /**
     * Response 세팅
     * @param response http 응답
     * @param filename 저장할 파일명
     * @param contentType 파일 유형
     * @param fileSize 파일 크기
     */
    private void setResponse(HttpServletResponse response, String filename, String contentType, Long fileSize) {
        if(fileSize != null && fileSize != 0) {
            response.setContentLengthLong(fileSize);
        }

        response.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20") + "\";");
        response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
    }

    @Override
    public S3VO testMapper(S3VO vo) {
        return s3Mapper.selectTest(vo);
    }
}
