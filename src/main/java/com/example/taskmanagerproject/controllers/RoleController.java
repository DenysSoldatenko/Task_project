package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.example.taskmanagerproject.dtos.users.RoleDto;
import com.example.taskmanagerproject.dtos.users.RoleHierarchyDto;
import com.example.taskmanagerproject.dtos.users.RoleHierarchyListDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling role-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/roles")
@Tag(name = "Role Controller", description = "Endpoints for managing roles")
public class RoleController {

  private final RoleService roleService;

  /**
   * Retrieves a list of all roles in the system.
   *
   * @return List of all roles.
   */
  @GetMapping()
  @Operation(
      summary = "Get all roles",
      description = "Fetches a list of all roles present in the system",
      responses = {
        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Roles not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public List<RoleDto> getAllRoles() {
    return roleService.getAllRoles();
  }

  /**
   * Retrieves a role by its name.
   *
   * @param roleName The name of the role to retrieve (for example, ADMIN, USER).
   * @return The requested role.
   */
  @GetMapping("/{roleName}")
  @Operation(
      summary = "Get a role by name",
      description = "Fetches a specific role by its name (e.g., ADMIN, USER)",
      responses = {
        @ApiResponse(responseCode = "200", description = "Role retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public RoleDto getRoleByName(@PathVariable String roleName) {
    return roleService.getRoleByName(roleName);
  }

  /**
   * Creates a new role in the system.
   *
   * @param roleDto The data transfer object containing the role's information.
   * @return The newly created role.
   */
  @PostMapping()
  @Operation(
      summary = "Create a new role",
      description = "Creates a new role with the provided details",
      responses = {
        @ApiResponse(responseCode = "201", description = "Role created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  public RoleDto createRole(@Valid @RequestBody RoleDto roleDto) {
    return roleService.createRole(roleDto);
  }

  /**
   * Updates an existing role in the system.
   *
   * @param roleDto The data transfer object containing the updated role's information.
   * @return The updated role.
   */
  @PutMapping("/{roleName}")
  @Operation(
      summary = "Update an existing role",
      description = "Updates an existing role's details with the provided information",
      responses = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public RoleDto updateRole(@PathVariable String roleName, @Valid @RequestBody RoleDto roleDto) {
    return roleService.updateRole(roleName, roleDto);
  }

  /**
   * Deletes a role by its name.
   *
   * @param roleName The name of the role to delete.
   */
  @DeleteMapping("/{roleName}")
  @Operation(
      summary = "Delete a role",
      description = "Deletes a specific role by its name",
      responses = {
        @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  public void deleteRole(@PathVariable String roleName) {
    roleService.deleteRole(roleName);
  }

  /**
   * Creates one or more role hierarchies in the system.
   *
   * @param roleHierarchyDtoList A single role hierarchy
   *                             or a list of role hierarchies to be created.
   * @return The created role hierarchies.
   */
  @PostMapping("/hierarchies")
  @Operation(
      summary = "Create role hierarchies",
      description = "Creates one or more role hierarchies with the provided details",
      responses = {
        @ApiResponse(responseCode = "201", description = "Role hierarchies created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleHierarchyDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  public List<RoleHierarchyDto> createRoleHierarchies(@Valid @RequestBody List<RoleHierarchyDto> roleHierarchyDtoList) {
    return roleService.createRoleHierarchies(roleHierarchyDtoList);
  }

  /**
   * Deletes one or more role hierarchies based on the provided role hierarchy data.
   *
   * @param roleHierarchyDtoList A list of role hierarchies to delete.
   */
  @DeleteMapping("/hierarchies")
  @Operation(
      summary = "Delete one or more role hierarchies",
      description = "Deletes one or more role hierarchies based on the provided details",
      responses = {
        @ApiResponse(responseCode = "204", description = "Role hierarchies deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Role hierarchy not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  public void deleteRoleHierarchies(@Valid @RequestBody List<RoleHierarchyDto> roleHierarchyDtoList) {
    roleService.deleteRoleHierarchies(roleHierarchyDtoList);
  }

  /**
   * Retrieves a role with all its lower and higher roles.
   *
   * @param roleName The name of the role (for example, ADMIN, USER).
   * @return A RoleHierarchyListDto containing the role's hierarchy.
   */
  @GetMapping("/{roleName}/hierarchy")
  @Operation(
      summary = "Get role with all lower and higher roles",
      description = "Fetches a specific role along with all its roles in the hierarchy",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved role hierarchy",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public RoleHierarchyListDto getRoleWithAllLowerAndHigherRoles(@PathVariable String roleName) {
    return roleService.findRoleWithHierarchy(roleName);
  }
}
