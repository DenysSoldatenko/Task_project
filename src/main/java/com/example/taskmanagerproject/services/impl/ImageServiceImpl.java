package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.FAILED_TO_CREATE_BUCKET;
import static com.example.taskmanagerproject.utils.MessageUtils.FAILED_TO_DELETE_IMAGE;
import static com.example.taskmanagerproject.utils.MessageUtils.FAILED_TO_UPLOAD_IMAGE;
import static com.example.taskmanagerproject.utils.MessageUtils.IMAGE_MUST_NOT_BE_EMPTY;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

import com.example.taskmanagerproject.configurations.minio.MinioProperties;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;
import com.example.taskmanagerproject.exceptions.ImageUploadException;
import com.example.taskmanagerproject.services.ImageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of the ImageService interface.
 */
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;

  @Override
  @Transactional
  public String uploadUserImage(UserImageDto userImageDto) {
    return uploadImage(userImageDto.file());
  }

  @Override
  @Transactional
  public String uploadTaskImage(TaskImageDto taskImageDto) {
    return uploadImage(taskImageDto.file());
  }

  @Override
  @Transactional
  public void deleteImage(String imageName) {
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioProperties.getBucket()).object(imageName).build());
    } catch (Exception e) {
      throw new ImageUploadException(format(FAILED_TO_DELETE_IMAGE, imageName, e.getMessage()));
    }
  }

  private String uploadImage(MultipartFile file) {
    validateImage(file);
    createBucketIfNotExists();
    String fileName = generateFileName(file);

    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder()
            .bucket(minioProperties.getBucket())
            .object(fileName)
            .stream(inputStream, inputStream.available(), -1)
            .build()
      );
      return fileName;
    } catch (Exception e) {
      throw new ImageUploadException(FAILED_TO_UPLOAD_IMAGE + e.getMessage());
    }
  }

  private void createBucketIfNotExists() {
    try {
      if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
      }
    } catch (Exception e) {
      throw new ImageUploadException(FAILED_TO_CREATE_BUCKET + e.getMessage());
    }
  }

  private void validateImage(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
      throw new ImageUploadException(IMAGE_MUST_NOT_BE_EMPTY);
    }
  }

  private String generateFileName(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    return randomUUID() + "." + (originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1) : "");
  }
}
