package com.example.taskmanagerproject.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for task images.
 */
@Schema(description = "Data Transfer Object representing an image for a task")
public record TaskImageDto(

    @NotNull(message = "Image cannot be null!")
    @Schema(
      description = "The image file associated with the task",
      example = "image.png"
    )
    MultipartFile file
) { }
