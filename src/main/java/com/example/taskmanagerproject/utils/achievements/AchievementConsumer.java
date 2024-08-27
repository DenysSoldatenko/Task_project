package com.example.taskmanagerproject.utils.achievements;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.utils.factories.AchievementFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumer service that listens to task completion events from Kafka.
 * This service processes task completion events and evaluates corresponding achievements
 * using the AchievementFactory.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class AchievementConsumer {

  private static final String ACHIEVEMENT_TOPIC = "achievement-topic";
  private static final String ACHIEVEMENT_GROUP = "achievement-group";

  private final AchievementFactory achievementFactory;

  /**
   * Processes the task completion events received from the Kafka topic.
   * Upon receiving the task completion event, this method delegates the task to the
   * AchievementFactory to evaluate potential achievements.
   *
   * @param event The Kafka event containing details about the completed task.
   */
  @KafkaListener(topics = ACHIEVEMENT_TOPIC, groupId = ACHIEVEMENT_GROUP)
  public void processTaskCompletion(KafkaTaskCompletionDto event) {
    log.info("Received task completion event for user: {}", event.userId());
    achievementFactory.evaluateAchievements(event);
  }
}
