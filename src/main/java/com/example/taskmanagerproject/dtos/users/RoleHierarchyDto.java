package com.example.taskmanagerproject.dtos.users;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for representing Role hierarchy information.
 * Used for API requests and responses.
 */
@Schema(description = "Data Transfer Object representing a role hierarchy")
public record RoleHierarchyDto(

    @Schema(
      description = "The higher role in the hierarchy",
      example = """
        {
          "id": "1",
          "name": "ADMIN",
          "description": "Administrator with full access"
        }
        """
    )
    RoleDto higherRole,

    @Schema(
      description = "The lower role in the hierarchy",
      example = """
        {
          "id": "14",
          "name": "USER",
          "description": "Default user with the lowest access level"
        }
        """
    )
    RoleDto lowerRole
) {}
