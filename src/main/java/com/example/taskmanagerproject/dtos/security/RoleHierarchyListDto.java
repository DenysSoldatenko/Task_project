package com.example.taskmanagerproject.dtos.security;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Data Transfer Object (DTO) for representing Role hierarchy with lists of higher and lower roles.
 * Used for API requests and responses.
 */
@Schema(
    description = """ 
      Data Transfer Object representing a role hierarchy with lists of higher and lower roles
    """
)
public record RoleHierarchyListDto(

    @Schema(
      description = "Name of the role",
      example = "TESTER"
    )
    String name,

    @Schema(
      description = "List of higher roles in the hierarchy",
      example = "[{\"id\": \"1\", \"name\": \"ADMIN\", "
        + "\"description\": \"Administrator with full access\"}]"
    )
    List<RoleDto> higherRoles,

    @Schema(
      description = "List of lower roles in the hierarchy",
      example = "[{\"id\": \"14\", \"name\": \"USER\", "
        + "\"description\": \"Default user with the lowest access level\"}]"
    )
    List<RoleDto> lowerRoles
) {}
