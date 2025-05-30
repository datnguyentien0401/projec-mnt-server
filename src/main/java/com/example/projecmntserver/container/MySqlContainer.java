package com.example.projecmntserver.container;

import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MySqlContainer {
    private static final String MYSQL_IMAGE_NAME = "mysql:8.0.28";
    private static final String MYSQL_DATABASE_NAME = "shorturl-test";
    private static final String MYSQL_USERNAME = "test";
    private static final String MYSQL_PASSWORD = "test";
    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse(MYSQL_IMAGE_NAME);
    private static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>(MYSQL_IMAGE).withDatabaseName(MYSQL_DATABASE_NAME)
                                                           .withUsername(MYSQL_USERNAME)
                                                           .withPassword(MYSQL_PASSWORD)
                                                           .withCommand("--character-set-server=utf8mb4",
                                                                        "--collation-server=utf8mb4_bin");
        MYSQL_CONTAINER.start();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer mysqlPropertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) {
        final Map<String, Object> properties = Map.of("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?useSSL=false",
                                                      "spring.datasource.hikari.username", MYSQL_CONTAINER.getUsername(),
                                                      "spring.datasource.hikari.password", MYSQL_CONTAINER.getPassword(),
                                                      "spring.datasource.driverClassName", MYSQL_CONTAINER.getDriverClassName(),
                                                      "spring.jpa.hibernate.ddl-auto", "create");
        final MapPropertySource testcontainersMySQL = new MapPropertySource("mysqlTestContainer", properties);

        env.getPropertySources().addFirst(testcontainersMySQL);
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public MySQLContainer<?> mysqlContainer() {
        return MYSQL_CONTAINER;
    }
}

