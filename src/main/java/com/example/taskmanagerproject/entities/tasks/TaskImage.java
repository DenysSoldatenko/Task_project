package com.example.taskmanagerproject.entities.tasks;

import org.springframework.web.multipart.MultipartFile;

/**
 * Represents an image associated with a task.
 */
public record TaskImage(MultipartFile file) {}
