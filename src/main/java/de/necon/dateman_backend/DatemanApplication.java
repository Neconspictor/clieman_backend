package de.necon.dateman_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class DatemanApplication {

	private static final Logger log = LoggerFactory.getLogger(DatemanApplication.class);

	public static void main(String... args) {
		//System.out.println(Locale.getDefault().toString());
		Locale.setDefault(Locale.ENGLISH);
		SpringApplication.run(DatemanApplication.class, args);
	}

}