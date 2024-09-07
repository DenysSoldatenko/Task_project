package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;

/**
 * Service interface for managing images.
 */
public interface ImageService {

  /**
   * Uploads an image for a task.
   *
   * @param taskImage The image and task details.
   * @return The image location or URL.
   */
  String uploadTaskImage(TaskImageDto taskImage);

  /**
   * Uploads an image for a user.
   *
   * @param userImageDto The image and user details.
   * @return The image location or URL.
   */
  String uploadUserImage(UserImageDto userImageDto);
}
