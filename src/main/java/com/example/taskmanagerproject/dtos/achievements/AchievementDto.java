package com.example.taskmanagerproject.dtos.achievements;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents an Achievement DTO for transferring achievement data.
 */
@Schema(description = "Data Transfer Object representing an achievement")
public record AchievementDto(

    @Schema(
      description = "The unique identifier of the achievement",
      hidden = true
    )
    Long id,

    @Schema(
      description = "The title of the achievement",
      example = "Task Master123"
    )
    @NotNull(message = "Achievement title cannot be null")
    @NotBlank(message = "Achievement title cannot be blank")
    @Size(min = 3, max = 100, message = "Achievement title must be between 3 and 100 characters")
    String title,

    @Schema(
      description = "A brief description of the achievement",
      example = "Completed 1+ tasks in a month."
    )
    @NotNull(message = "Achievement description cannot be null")
    @NotBlank(message = "Achievement description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @Schema(
      description = "The URL to an image representing the achievement",
      example = "https://img.icons8.com/ios/452/trophy.png"
    )
    @NotNull(message = "Achievement image URL cannot be null")
    @NotBlank(message = "Achievement image URL cannot be blank")
    String imageUrl
) {}
