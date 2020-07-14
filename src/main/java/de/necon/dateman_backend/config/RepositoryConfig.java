package de.necon.dateman_backend.config;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("de.necon.jpatest.repository")
public final class RepositoryConfig {

    public static final int MAX_STRING_SIZE = 100;
}
