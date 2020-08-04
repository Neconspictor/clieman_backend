package de.necon.clieman_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class CliemanApplication {

	private static final Logger log = LoggerFactory.getLogger(CliemanApplication.class);

	public static void main(String... args) {
		//System.out.println(Locale.getDefault().toString());
		Locale.setDefault(Locale.ENGLISH);
		SpringApplication.run(CliemanApplication.class, args);
	}

}