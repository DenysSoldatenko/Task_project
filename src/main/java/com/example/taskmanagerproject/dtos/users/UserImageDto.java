package com.example.taskmanagerproject.dtos.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for user photo upload.
 */
@Schema(description = "Data Transfer Object representing a photo for a user")
public record UserImageDto(

    @NotNull(message = "Photo cannot be null!")
    @Schema(
      description = "The photo file associated with the user",
      example = "user_photo.jpg"
    )
    MultipartFile file
) {}
