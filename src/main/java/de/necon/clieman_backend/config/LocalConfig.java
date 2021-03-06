package de.necon.clieman_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


/**
 * This class loads optional properties for a local environment.
 * The local properties file is intended to be excluded by version control.
 * This way each developers can easily customize it.
 */
@Configuration
@PropertySource(
        ignoreResourceNotFound = true,
        value = {"file:application-local.yml"}, factory = YamlPropertySourceFactory.class)
public class LocalConfig {
}