package kr.go.hai.config.info;

import kr.selim.msa.cmmn.info.CmmnConstantInfo;

/**
 * static 변수 목록
 *
 * @author MSA팀
 * @version 1.0
 * @since 2023/04/05
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자       수정내용
 * ----------    --------   ---------------------------
 * 2023.04.05.   MSA팀       최초 생성
 * 2023.11.13    양정숙       MSA팀 적용
 * </pre>
 */
public final class ConstantInfo extends CmmnConstantInfo {

	public static final String FWD_URL = "fwdUrl";
	public static final String TOT_CNT = "totalRecordCount";
	public static final String UPLD_MULTI_FILE = "upldMultiFile";

	/** token */
	public static final String TOKEN_CLAIM_NAME = "authorities";
	public static final String TOKEN_ACCESS_KEY = "access-token";
	public static final String TOKEN_REFRESH_KEY = "refresh-token";
	public static final String TOKEN_USER_ID = "token-id";
	public static final String TOKEN_USER_INFO = "userInfo";

	public static final String UPLOAD_FILE_PATH = "UPLOAD_FILE_PATH";
	public static final String UPLOAD_FILE_BASE_PATH = "UPLOAD_FILE_BASE_PATH";
	public static final String CKEDITOR_FILE_PATH = "CKEDITOR_FILE_PATH";

	public static final String AUTH_STTS_CD_OK = "AT000001";
	public static final String AUTH_STTS_CD_REFRESH = "AT000002";
	public static final String AUTH_STTS_CD_NO = "AT000003";

	public static final String SYS_AUTH_ADMIN = "A000001";
	public static final String SYS_AUTH_USER = "A000002";
	public static final String SYS_AUTH_NOBODY = "A000099";

	public static final String ANONYMOUS_USER = "anonymousUser";

	private ConstantInfo() {
		throw new IllegalStateException("Utility class");
	}
}
