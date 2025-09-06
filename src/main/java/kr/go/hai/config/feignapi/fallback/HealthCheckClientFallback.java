package kr.go.hai.config.feignapi.fallback;

import java.net.URI;

import org.springframework.stereotype.Component;

import kr.go.hai.config.feignapi.HealthCheckClient;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HealthCheckClientFallback implements HealthCheckClient {

	@Override
	public String getStatus(URI baseUrl) {
		log.info("오류 발생 HealthCheckClientFallback getStatus()");
		return "error";
	}

}
