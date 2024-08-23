package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Slf4j
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5JakartaModule hibernate5Module() {
        // JSON 생성 시점에 LAZY 로딩으로 프록시 인것을 DB조회해서 다 가져와버린다
		//hibernateModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, true);
		return new Hibernate5JakartaModule();
	}
}
