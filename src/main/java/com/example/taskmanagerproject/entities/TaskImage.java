package com.example.taskmanagerproject.entities;

import org.springframework.web.multipart.MultipartFile;

/**
 * Represents an image associated with a task.
 */
public record TaskImage(MultipartFile file) { }
