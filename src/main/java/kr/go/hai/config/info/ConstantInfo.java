package kr.go.hai.config.info;

import kr.selim.msa.cmmn.info.CmmnConstantInfo;

/**
 * static 변수 목록
 *
 * @author AX사업팀
 * @version 1.0
 * @since 2025.09.05
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자       수정내용
 * ----------    --------   ---------------------------
 * 2025.09.05    양정숙       최초 생성
 * </pre>
 */
public final class ConstantInfo extends CmmnConstantInfo {

	// S3 Storage
	public static final int S3_FILE_MAX_LENGTH = 900;
	public static final String S3_DATE_PREFIX_FORMAT = "yyyy/MM/dd";
	public static final String S3_PREFIX_DELIMITER = "/";

	private ConstantInfo() {
		throw new IllegalStateException("Utility class");
	}
}
