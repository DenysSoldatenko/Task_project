package com.example.taskmanagerproject.dtos;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for task images.
 */
public record TaskImageDto(

    @NotNull(message = "Image cannot be null!")
    MultipartFile file
) { }
