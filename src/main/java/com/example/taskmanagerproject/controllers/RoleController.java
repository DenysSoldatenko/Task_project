package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Controller responsible for managing roles in the system.
 * Provides endpoints to create, update, retrieve, and delete roles.
 * Restricted to users with the 'ADMIN' role for all operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
@PreAuthorize("@expressionService.hasRoleAdmin()")
@Tag(name = "Role Controller", description = "Endpoints for managing roles")
public class RoleController {

  private final RoleService roleService;

  /**
   * Retrieves a list of all roles in the system.
   *
   * @return List of all roles.
   */
  @Operation(
      summary = "Get all roles",
      description = "Fetches a list of all roles present in the system"
  )
  @GetMapping
  public List<RoleDto> getAllRoles() {
    return roleService.getAllRoles();
  }

  /**
   * Retrieves a role by its name.
   *
   * @param roleName The name of the role to retrieve (e.g., ADMIN, USER).
   * @return The requested role.
   */
  @Operation(
      summary = "Get a role by name",
      description = "Fetches a specific role by its name (e.g., ADMIN, USER)"
  )
  @GetMapping("/{roleName}")
  public RoleDto getRoleByName(
      @Parameter(description = "Name of the role (e.g. USER)") @PathVariable String roleName
  ) {
    return roleService.getRoleByName(roleName);
  }

  /**
   * Creates a new role in the system.
   *
   * @param roleDto The data transfer object containing the role's information.
   * @return The newly created role.
   */
  @Operation(
      summary = "Create a new role",
      description = "Creates a new role with the provided details"
  )
  @PostMapping
  @ResponseStatus(CREATED)
  public RoleDto createRole(
      @Parameter(description = "The role data to create") @Valid @RequestBody RoleDto roleDto
  ) {
    return roleService.createRole(roleDto);
  }

  /**
   * Updates an existing role in the system.
   *
   * @param roleDto The data transfer object containing the updated role's information.
   * @return The updated role.
   */
  @Operation(
      summary = "Update an existing role",
      description = "Updates an existing role's details with the provided information"
  )
  @PutMapping("/{roleName}")
  public RoleDto updateRole(
      @Parameter(description = "Name of the role to update") @PathVariable String roleName,
      @Parameter(description = "Updated role data") @Valid @RequestBody RoleDto roleDto
  ) {
    return roleService.updateRole(roleName, roleDto);
  }

  /**
   * Deletes a role by its name.
   *
   * @param roleName The name of the role to delete.
   */
  @Operation(
      summary = "Delete a role",
      description = "Deletes a specific role by its name"
  )
  @DeleteMapping("/{roleName}")
  @ResponseStatus(NO_CONTENT)
  public void deleteRole(
      @Parameter(description = "Name of the role to delete") @PathVariable String roleName
  ) {
    roleService.deleteRole(roleName);
  }
}
