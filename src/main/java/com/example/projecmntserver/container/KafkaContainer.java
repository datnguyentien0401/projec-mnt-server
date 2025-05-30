package com.example.projecmntserver.container;

import java.util.Collections;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class KafkaContainer {
    private static final String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.2.1";
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse(KAFKA_IMAGE_NAME);
    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new KafkaContainer(KAFKA_IMAGE)
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");
        KAFKA_CONTAINER.start();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer kafkaPropertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) {
        final String bootstrapServers = KAFKA_CONTAINER.getBootstrapServers();
        final MapPropertySource testcontainersKafka = new MapPropertySource("kafkaTestContainer",
                                                                            Collections.singletonMap("spring.kafka.bootstrap-servers", bootstrapServers));
        env.getPropertySources().addFirst(testcontainersKafka);
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        return KAFKA_CONTAINER;
    }
}
