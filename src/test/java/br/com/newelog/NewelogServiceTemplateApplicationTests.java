package br.com.newelog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok")
class NewelogServiceTemplateApplicationTests {

	@Test
	void contextLoads() {
	}

}
