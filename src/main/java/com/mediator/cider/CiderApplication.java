package com.mediator.cider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CiderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CiderApplication.class, args);
	}

}
