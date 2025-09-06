package kr.go.hai.s3storage.service.impl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.selim.msa.cmmn.vo.CmmnVO;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시물 vo
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
@Schema(description = "팝업관리")
@Getter
@Setter
public class SampleVO extends CmmnVO {
	private static final long serialVersionUID = -7384031555947545531L;

	@Schema(description = "팝업일련번호")
	private String popupSn;
	
	@Schema(description = "팝업제목")
	private String popupTtl;
	
	@Schema(description = "팝업URL주소")
	private String popupUrlAddr;
	
	@Schema(description = "팝업게시시작일시")
	private String popupPstgBgngDt;
	
	@Schema(description = "팝업게시종료일시")
	private String popupPstgEndDt;
	
	@Schema(description = "팝업가로길이")
	private String popupWdthLen;
	
	@Schema(description = "팝업세로길이")
	private String popupVrtcLen;
	
	@Schema(description = "팝업사용여부")
	private String popupUseYn;
	
	@Schema(description = "팝업미표시사용여부")
	private String popupUnmrUseYn;
	
	@Schema(description = "팝업내용")
	private String popupCn;
}
