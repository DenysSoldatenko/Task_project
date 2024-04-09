package com.example.taskmanagerproject.exceptions.errorhandling;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

/**
 * Represents details of an error response.
 */
@Schema(description = "Details of an error response")
public record ErrorDetails(

    @Schema(
      description = "Timestamp when the error occurred",
      example = "2024-07-23T12:00:00.600+00:00"
    )
    Date timestamp,

    @Schema(
      description = "HTTP status code of the error",
      example = "403"
    )
    String status,

    @Schema(
      description = "Error type",
      example = "Forbidden"
    )
    String error,

    @Schema(
      description = "Detailed error message",
      example = "Access Denied"
    )
    String message,

    @Schema(
      description = "Path of the request that caused the error",
      example = "/api/v1/tasks/333"
    )
    String path
) {
}
