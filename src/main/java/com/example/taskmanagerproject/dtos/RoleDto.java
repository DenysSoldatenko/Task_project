package com.example.taskmanagerproject.dtos;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for representing Role information.
 * Used for API requests and responses.
 */
@Data
@Schema(description = "Data Transfer Object for Role")
public class RoleDto {

  @Schema(
      description = "The unique identifier of the role",
      example = "13",
      accessMode = READ_ONLY
  )
  private Long id;

  @Schema(
      description = "The name of the role (e.g., ADMIN, USER)",
      example = "TESTER"
  )
  private String name;

  @Schema(
      description = "A brief description of the role",
      example = "Tester responsible for verifying the tasks"
  )
  private String description;

}
