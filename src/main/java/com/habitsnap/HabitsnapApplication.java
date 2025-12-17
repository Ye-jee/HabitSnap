package com.habitsnap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class HabitsnapApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitsnapApplication.class, args);
	}

	/*@Bean
	CommandLineRunner testUserRepo(UserRepository repo) {
		return args -> {
			User user = new User();
			user.setNickname("test_user");
			repo.save(user);

			System.out.println("✅ 저장된 사용자 수: " + repo.count());
		};
	}*/

}
