package com.example.taskmanagerproject.controllers;

import static com.example.taskmanagerproject.utils.MessageUtil.DATA_INITIALIZATION_FAIL_MESSAGE;
import static com.example.taskmanagerproject.utils.MessageUtil.DATA_INITIALIZATION_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.example.taskmanagerproject.configurations.initializers.DataInitializer;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling data initialization operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/data-initialization")
@Tag(name = "Data Initialization Controller", description = "Endpoints for user data initialization")
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
      description = "Initializes roles, users, and projects in the database",
      responses = {
        @ApiResponse(responseCode = "201", description = "Data initialized successfully",
          content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<String> initializeDatabase() {
    try {
      dataInitializer.initializeTasks();
      dataInitializer.updateTaskStatuses();
      dataInitializer.updateTaskHistoryDates();
      dataInitializer.generateAchievementsForUsers();
      return ResponseEntity.status(CREATED).body(DATA_INITIALIZATION_SUCCESS_MESSAGE);
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(DATA_INITIALIZATION_FAIL_MESSAGE);
    }
  }
}
