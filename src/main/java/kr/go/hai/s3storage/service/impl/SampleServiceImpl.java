package kr.go.hai.s3storage.service.impl;

import java.sql.Timestamp;
import java.util.List;

import kr.selim.msa.cmmn.exception.ApiBizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import kr.go.hai.config.info.ConstantInfo;
import kr.go.hai.s3storage.mapper.SampleMapper;
import kr.go.hai.s3storage.service.SampleService;
import kr.go.hai.s3storage.service.impl.vo.SampleVO;
import lombok.RequiredArgsConstructor;

/**
 * 게시물 service implements
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
@Service("pstService")
@RequiredArgsConstructor
@Slf4j
public class SampleServiceImpl implements SampleService {

	private final SampleMapper pstMapper;

	/**
	 * 게시물 목록 조회
	 */
	@Override
	public List<SampleVO> selectSampleList(SampleVO vo) {
		return pstMapper.selectSampleList(vo);
	}

	/**
	 * 게시물 목록 건수 조회
	 */
	@Override
	public int selectSampleListCnt(SampleVO vo) {
		return pstMapper.selectSampleListCnt(vo);
	}

	/**
	 * 게시물 상세 조회
	 */
	@Override
	public SampleVO selectSampleInfo(SampleVO vo) {
		return pstMapper.selectSampleInfo(vo);
	}

	/**
	 * 게시물 상세 입력
	 */
	@Override
	public int insertSampleInfo(SampleVO vo) {
		return pstMapper.insertSampleInfo(vo);
	}

	/**
	 * 게시물 상세 수정
	 */
	@Override
	public int updateSampleInfo(SampleVO vo) {
		return pstMapper.updateSampleInfo(vo);
	}

	/**
	 * 게시물 상세 수정
	 */
	@Override
	public int updateSampleInfoTrx(SampleVO vo, String trxYn) {
		//update 1
		Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
		vo.setPopupTtl("update1:" + timestamp1);

		pstMapper.updateSampleInfo(vo);

		//update 2
		Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
		vo.setPopupTtl("update2:" + timestamp2);

		if(trxYn.equals(ConstantInfo.Y_VALUE)) {
			//update2 전에 exception 발생
			throw new ApiBizException(HttpStatus.BAD_REQUEST, "test");
		}

		//update 2
		return pstMapper.updateSampleInfo(vo);
	}

	/**
	 * 게시물 상세 삭제
	 */
	@Override
	public int deleteSampleInfo(SampleVO vo) {
		return pstMapper.deleteSampleInfo(vo);
	}

}
