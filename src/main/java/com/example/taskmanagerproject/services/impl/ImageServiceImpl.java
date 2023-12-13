package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.FAILED_TO_CREATE_BUCKET;
import static com.example.taskmanagerproject.utils.MessageUtils.FAILED_TO_UPLOAD_IMAGE;
import static com.example.taskmanagerproject.utils.MessageUtils.IMAGE_MUST_NOT_BE_EMPTY;
import static com.example.taskmanagerproject.utils.MessageUtils.IMAGE_UPLOAD_FAILED;

import com.example.taskmanagerproject.configurations.minio.MinioProperties;
import com.example.taskmanagerproject.dtos.TaskImageDto;
import com.example.taskmanagerproject.entities.TaskImage;
import com.example.taskmanagerproject.exceptions.ImageUploadException;
import com.example.taskmanagerproject.mappers.TaskImageMapper;
import com.example.taskmanagerproject.services.ImageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of the ImageService interface.
 */
@Service
@RequiredArgsConstructor
public final class ImageServiceImpl implements ImageService {

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;
  private final TaskImageMapper taskImageMapper;

  @Override
  public String uploadImage(final TaskImageDto taskImageDto) {
    TaskImage taskImage = taskImageMapper.toEntity(taskImageDto);
    try {
      createBucketIfNotExists();
      MultipartFile file = taskImage.file();
      validateImage(file);
      String fileName = generateFileName(file);
      uploadImageToMinio(file.getInputStream(), fileName);
      return fileName;
    } catch (Exception e) {
      throw new ImageUploadException(IMAGE_UPLOAD_FAILED + e.getMessage());
    }
  }

  private void createBucketIfNotExists() {
    try {
      boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
          .bucket(minioProperties.getBucket())
          .build());
      if (!bucketExists) {
        minioClient.makeBucket(MakeBucketArgs.builder()
            .bucket(minioProperties.getBucket())
            .build());
      }
    } catch (Exception e) {
      throw new ImageUploadException(FAILED_TO_CREATE_BUCKET + e.getMessage());
    }
  }

  private void validateImage(final MultipartFile file) {
    if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
      throw new ImageUploadException(IMAGE_MUST_NOT_BE_EMPTY);
    }
  }

  private String generateFileName(final MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename != null) {
      String extension = originalFilename.substring(
          originalFilename.lastIndexOf(".") + 1
      );
      return UUID.randomUUID() + "." + extension;
    } else {
      throw new ImageUploadException(IMAGE_MUST_NOT_BE_EMPTY);
    }
  }

  private void uploadImageToMinio(
      final InputStream inputStream,
      final String fileName
  ) {
    try {
      minioClient.putObject(PutObjectArgs.builder()
          .stream(inputStream, inputStream.available(), -1)
          .bucket(minioProperties.getBucket())
          .object(fileName)
          .build());
    } catch (Exception e) {
      throw new ImageUploadException(FAILED_TO_UPLOAD_IMAGE + e.getMessage());
    }
  }
}
