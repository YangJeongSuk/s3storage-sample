package kr.selim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"logging.config=classpath:config/log/logback-junit.xml"})
class StartApplicationTests {

	@Test
	void contextLoads() {
	}

}
