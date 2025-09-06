package kr.go.hai.config.feignapi;

import java.net.URI;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import kr.go.hai.config.feignapi.fallback.HealthCheckClientFallback;

/**
 * 헬스체크 API 인터페이스 정의
 *
 * @author MSA팀
 * @version 1.0
 * @since 2024.07.19
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자            수정내용
 * ----------    --------    ---------------------------
 * 2024.07.19    양정숙        최초 생성
 * </pre>
 */
@FeignClient(name = "healthCheckclient", url = "localhost", fallback = HealthCheckClientFallback.class)
public interface HealthCheckClient {

    @GetMapping(value = "/actuator/health-info")
    String getStatus(URI baseUrl);

}
