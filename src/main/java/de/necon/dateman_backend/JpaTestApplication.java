package de.necon.dateman_backend;

import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpaTestApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(JpaTestApplication.class);

	private final UserRepository userRepository;

	public JpaTestApplication(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(JpaTestApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("JpaTestApplication...");

		userRepository.save(new User("dave@web.de", "1234", "dave"));
		userRepository.save(new User("testimonial.schlomo@googlemail.com", "schlomo127", "schlomo"));
		userRepository.save(new User("pumuckl@gmx.de", "r3dHairG0blin", "Pumuckl"));
		//userRepository.save(new User("pumuckl2@gmx.de", "r3dHairG0blin2", "Pumuckl"));

		System.out.println("\nfindAll()");
		userRepository.findAll().forEach(x -> System.out.println(x));

		System.out.println("\nfindByEmail('pumuckl@gmx.de')");
		userRepository.findByEmail("pumuckl@gmx.de").ifPresent(x-> System.out.println(x));

		System.out.println("\nfindByUsername('schlomo')");
		userRepository.findByUsername("schlomo").ifPresent(x-> System.out.println(x));

		System.out.println("\nfindByUsername('notExisting')");
		userRepository.findByUsername("notExisting").ifPresent(x-> System.out.println(x));
	}

}