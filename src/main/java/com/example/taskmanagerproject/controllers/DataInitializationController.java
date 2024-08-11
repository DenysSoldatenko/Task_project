package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;

import com.example.taskmanagerproject.configurations.initializers.DataInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing data initialization operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data-initialization")
@Tag(
    name = "Data Initialization Controller",
    description = "Endpoints for user data initialization"
)
public class DataInitializationController {

  private final DataInitializer dataInitializer;

  /**
   * Initializes all necessary data (roles, users, projects) in the database.
   *
   * @return a message indicating the success or failure of the data initialization.
   */
  @PostMapping("/initialize")
  @Operation(
      summary = "Initialize Database Data",
      description = "Initializes roles, users, and projects in the database"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Data initialized successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<String> initializeDatabaseData() {
    return new ResponseEntity<>(dataInitializer.initData(), CREATED);
  }
}
