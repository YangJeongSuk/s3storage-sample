package kr.go.hai.s3storage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.go.hai.s3storage.service.impl.vo.SampleVO;

/**
 * 게시물 mapper
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
@Mapper
public interface SampleMapper {

	/**
	 * 게시물 목록 조회
	 * @return List<SampleVO>
	 */
	List<SampleVO> selectSampleList(SampleVO vo);

	/**
	 * 게시물 목록 건수 조회
	 * @return int
	 */
	int selectSampleListCnt(SampleVO vo);

	/**
	 * 게시물 상세 조회
	 * @return SampleVO
	 */
	SampleVO selectSampleInfo(SampleVO vo);

	/**
	 * 게시물 조회수 증가
	 * @param vo SampleVO
	 * @return int
	 */
	int updateSampleInqCnt(SampleVO vo);

	/**
	 * 게시물 상세 입력
	 * @return int
	 */
	int insertSampleInfo(SampleVO vo);

	/**
	 * 게시물 상세 수정
	 * @param vo SampleVO
	 * @return int
	 */
	int updateSampleInfo(SampleVO vo);

	/**
	 * 게시물 상세 삭제
	 * @return int
	 */
	int deleteSampleInfo(SampleVO vo);
}
