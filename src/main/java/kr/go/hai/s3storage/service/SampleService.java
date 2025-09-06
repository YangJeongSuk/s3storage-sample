package kr.go.hai.s3storage.service;

import java.util.List;

import kr.go.hai.s3storage.service.impl.vo.SampleVO;

/**
 * 게시물 service
 *
 * @author MSA팀
 * @version 1.0
 * @since 2023.11.13
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자            수정내용
 * ----------    --------    ---------------------------
 * 2023.11.13    양정숙        최초 생성
 * </pre>
 */
public interface SampleService {

	/**
	 * 게시물 목록 조회
	 * @return List<SampleVO>
	 */
	List<SampleVO> selectSampleList(SampleVO vo);

	/**
	 * 게시물 목록 건수 조회
	 */
	int selectSampleListCnt(SampleVO vo);

	/**
	 * 게시물 상세 조회
	 */
	SampleVO selectSampleInfo(SampleVO vo);

	/**
	 * 게시물 상세 입력
	 */
	int insertSampleInfo(SampleVO vo);

	/**
	 * 게시물 상세 수정
	 */
	int updateSampleInfo(SampleVO vo);

	/**
	 * 게시물 상세 삭제
	 */
	int deleteSampleInfo(SampleVO vo);

	/**
	 * 게시물 상세 수정
	 */
	int updateSampleInfoTrx(SampleVO vo, String trxYn);

}
