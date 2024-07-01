package com.tenten.studybadge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StudyBadgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyBadgeApplication.class, args);
	}

}
