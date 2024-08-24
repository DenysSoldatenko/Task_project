package com.example.taskmanagerproject.configurations.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration class for Kafka topics.
 *
 * <p>Defines the Kafka topics used in the application.
 */
@Configuration
public class KafkaTopicConfiguration {

  @Value("${spring.kafka.topic.name}")
  private String topic;

  /**
   * Creates a Kafka topic with the specified name.
   *
   * @return A NewTopic instance representing the Kafka topic.
   */
  @Bean
  public NewTopic orderTopic() {
    return TopicBuilder.name(topic).build();
  }
}