package org.rb;

import org.rb.entity.Person;
import org.rb.repo.PersonRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootInterApplication /*extends SpringBootServletInitializer */{

	public static void main(String[] args) {
		SpringApplication.run(SpringBootInterApplication.class, args);
	}

	/**
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		
		return builder.sources(SpringBootInterApplication.class);
	}
	*/
	
	@Bean
	public CommandLineRunner initDB(PersonRepo repo) {

		return new CommandLineRunner() {

			@Override
			public void run(String... args) throws Exception {

				System.out.println("Init DB...");
				for (int i = 1; i <= 3; i++) {
					repo.save(new Person("Name_" + i, 10 + i));
				}

			}

		};
	};

}
