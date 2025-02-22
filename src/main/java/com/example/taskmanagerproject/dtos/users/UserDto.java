package com.example.taskmanagerproject.dtos.users;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

/**
 * Represents a user DTO (Data Transfer Object) in the project.
 */
@Builder
@Schema(description = "Data Transfer Object representing a user")
public record UserDto(

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The unique identifier of the user",
      hidden = true
    )
    Long id,

    @NotNull(message = "Full name cannot be null!")
    @NotBlank(message = "Full name cannot be blank!")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters long!")
    @Schema(
      description = "The full name of the user",
      example = "Alice Johnson"
    )
    String fullName,

    @NotNull(message = "Username cannot be null!")
    @NotBlank(message = "Username cannot be blank!")
    @Size(min = 10, max = 55, message = "Username must be between 10 and 55 characters long!")
    @Schema(
      description = "The username of the user",
      example = "alice12345@gmail.com"
    )
    String username,

    @NotNull(message = "Slug cannot be null!")
    @NotBlank(message = "Slug cannot be blank!")
    @Size(min = 5, max = 100, message = "Slug must be between 5 and 100 characters long!")
    @Schema(
      description = "The slug of the user, URL-friendly identifier",
      example = "alice-johnson-1234"
    )
    String slug,

    @JsonProperty(access = WRITE_ONLY)
    @Schema(
      description = "The password of the user",
      example = "password123"
    )
    String password,

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "Image associated with the entity",
      example = "[\"image1.png\"]"
    )
    List<String> image
) {}
