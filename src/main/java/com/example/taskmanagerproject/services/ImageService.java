package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.task.TaskImageDto;

/**
 * Service interface for managing images.
 */
public interface ImageService {

  /**
   * Uploads an image for a task.
   *
   * @param taskImage The TaskImageDto containing the image data and task information.
   * @return A string representing the location or URL of the uploaded image.
   */
  String uploadImage(TaskImageDto taskImage);
}
