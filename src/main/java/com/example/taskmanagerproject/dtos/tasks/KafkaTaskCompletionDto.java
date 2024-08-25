package com.example.taskmanagerproject.dtos.tasks;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents an event DTO for Kafka representing task completion details
 * along with team and project information.
 */
@Schema(description = "Event DTO for Kafka representing task completion details with team and project information")
public record KafkaTaskCompletionDto(

    @Schema(description = "The unique identifier for the task.", example = "98765")
    Long taskId,

    @Schema(description = "The unique identifier for the user who completed the task.", example = "12345")
    Long userId,

    @Schema(description = "The unique identifier of the team to which the task belongs.", example = "5678")
    Long teamId,

    @Schema(description = "The unique identifier of the project the task is associated with.", example = "91011")
    Long projectId
) {}
