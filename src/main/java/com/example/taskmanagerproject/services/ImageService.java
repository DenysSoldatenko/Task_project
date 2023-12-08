package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.TaskImageDto;

/**
 * Service interface for managing images.
 */
public interface ImageService {

  String uploadImage(TaskImageDto taskImage);
}
