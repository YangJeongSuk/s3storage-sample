package kr.go.hai.s3storage.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import kr.selim.msa.cmmn.exception.ApiBizException;
import kr.selim.msa.cmmn.utils.ResponseUtils;
import kr.selim.msa.cmmn.vo.ApiResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.go.hai.config.info.ConstantInfo;
import kr.go.hai.s3storage.service.SampleService;
import kr.go.hai.s3storage.service.impl.vo.SampleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 팝업 controller
 *
 * @author MSA팀
 * @version 1.0
 * @since 2024.06.11
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자            수정내용
 * ----------    --------    ---------------------------
 * 2024.06.11    양정숙        최초 생성
 * </pre>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "팝업 관리", description = "팝업관리 서비스를 이용하기 위한 API (테스트용 임시 API)")
public class SampleController {

	private final SampleService sampleService;

	/**
	 * 팝업 목록 조회
	 * @param vo SampleVO
	 * @return ApiResponseVO
	 * @throws ApiBizException ApiBizException
	 */
	@PostMapping("/v1/sample/list")
	@Operation(summary = "팝업 목록 조회", description = "팝업 목록 조회")
	public ApiResponseVO getSampleList(@RequestBody SampleVO vo) throws ApiBizException {
		log.debug("getSampleList");

		List<SampleVO> list = sampleService.selectSampleList(vo);

		HashMap<String, Object> rtnMap = new HashMap<>();
		rtnMap.put("list", list);

		return ResponseUtils.build(rtnMap);
	}

	/**
	 * 팝업 목록 건수 조회
	 * @param vo SampleVO
	 * @return ApiResponseVO
	 * @throws ApiBizException ApiBizException
	 */
	@PostMapping("/v1/sample/list-count")
	@Operation(summary = "팝업 목록 건수 조회", description = "팝업 목록 건수 조회")
	public ApiResponseVO getSampleListCnt(@RequestBody SampleVO vo) throws ApiBizException {
		log.debug("getSampleListCnt");

		int listCnt = sampleService.selectSampleListCnt(vo);

		HashMap<String, Object> rtnMap = new HashMap<>();
		rtnMap.put("listCnt", listCnt);

		return ResponseUtils.build(rtnMap);
	}

	/**
	 * 팝업 상세 조회
	 * @param vo SampleVO
	 * @return ApiResponseVO
	 * @throws ApiBizException ApiBizException
	 */
	@PostMapping("/v1/sample/info")
	@Operation(summary = "팝업 조회", description = "팝업 상세 조회")
	public ApiResponseVO getSampleInfo(@RequestBody SampleVO vo) throws ApiBizException {
		log.debug("getSampleInfo");

		SampleVO sampleVo = sampleService.selectSampleInfo(vo);

		HashMap<String, Object> rtnMap = new HashMap<>();
		rtnMap.put("sampleVo", sampleVo);

		return ResponseUtils.build(rtnMap);
	}

	/**
	 * 팝업 상세 입력
	 * @param vo SampleVO
	 * @return ApiResponseVO
	 * @throws ApiBizException ApiBizException
	 */
	@PostMapping("/v1/sample/insert")
	@Operation(summary = "팝업 입력", description = "회원은 로그인 정보를 이용하여 작성자 등록, 비회원인 경우 작성자명과 비밀번호 입력")
	public ApiResponseVO insertSampleInfo(@RequestBody SampleVO vo) throws ApiBizException {
		log.debug("insertSampleList");

		//입력 수행
		int resultCnt = sampleService.insertSampleInfo(vo);
		SampleVO resultVO = sampleService.selectSampleInfo(vo);

		HashMap<String, Object> rtnMap = new HashMap<>();
		rtnMap.put(ConstantInfo.RESULT_CNT, resultCnt);
		rtnMap.put(ConstantInfo.RESULT_VO, resultVO);

		return ResponseUtils.build(rtnMap);
	}

	/**
	 * 팝업 상세 수정
	 * @param vo SampleVO
	 * @return ApiResponseVO
	 * @throws ApiBizException ApiBizException
	 * @throws SQLException SQLException
	 */
	@PostMapping("/v1/sample/update")
	@Operation(summary = "팝업 수정", description = "회원은 자신이 작성한 팝업 수정, 비회원은 비밀번호가 같은 경우 수정 가능")
	public ApiResponseVO updateSampleInfo(@RequestBody SampleVO vo) throws ApiBizException, SQLException {
		log.debug("updateSampleList");

		/*
		//UserVO userVO = Optional.ofNullable(CurrentUserUtils.getCurrentUser())
		//		.orElseThrow(() -> new ApiBizException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
		*/

		//팝업 수정
		int resultCnt = sampleService.updateSampleInfo(vo);

		//수정 결과 확인
		SampleVO resultVO = sampleService.selectSampleInfo(vo);

		HashMap<String, Object> rtnMap = new HashMap<>();
		rtnMap.put(ConstantInfo.RESULT_CNT, resultCnt);
		rtnMap.put(ConstantInfo.RESULT_VO, resultVO);

		return ResponseUtils.build(rtnMap);
	}

	/**
	 * 팝업 상세 삭제
	 * @param vo SampleVO
	 * @return ApiResponseVO
	 * @throws ApiBizException ApiBizException
	 */
	@PostMapping("/v1/sample/delete")
	@Operation(summary = "팝업 삭제", description = "회원은 자신이 작성한 팝업 삭제, 비회원은 비밀번호가 같은 경우 삭제 가능")
	public ApiResponseVO deleteSampleInfo(@RequestBody SampleVO vo) throws ApiBizException {
		log.debug("deleteSampleInfo");

		//삭제 수행
		int resultCnt = sampleService.deleteSampleInfo(vo);

		HashMap<String, Object> rtnMap = new HashMap<>();
		rtnMap.put(ConstantInfo.RESULT_CNT, resultCnt);

		return ResponseUtils.build(rtnMap);
	}
}
