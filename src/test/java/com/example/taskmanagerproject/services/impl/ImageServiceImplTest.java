package com.example.taskmanagerproject.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.configurations.minio.MinioProperties;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class ImageServiceImplTest {

  @Mock private MinioClient minioClient;
  @Mock private MinioProperties minioProperties;
  @Mock private MultipartFile multipartFile;

  @InjectMocks private ImageServiceImpl imageService;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    when(minioProperties.getBucket()).thenReturn("test-bucket");

    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("test.png");
    when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
    when(multipartFile.getSize()).thenReturn(4L);

    when(minioClient.bucketExists(any())).thenReturn(true);
    when(minioClient.putObject(any())).thenReturn(mock(ObjectWriteResponse.class));
  }

  @Test
  void uploadUserImage_shouldUploadSuccessfully() throws Exception {
    String fileName = imageService.uploadUserImage(new UserImageDto(multipartFile));
    assertNotNull(fileName);
    verify(minioClient).putObject(any());
  }

  @Test
  void createBucketIfNotExists_shouldCreateBucket() throws Exception {
    when(minioClient.bucketExists(any())).thenReturn(false);
    doNothing().when(minioClient).makeBucket(any());

    imageService.uploadTaskImage(new TaskImageDto(multipartFile));

    verify(minioClient).makeBucket(any());
    verify(minioClient).putObject(any());
  }

  @Test
  void deleteImage_shouldCallRemoveObject() throws Exception {
    doNothing().when(minioClient).removeObject(any());

    assertDoesNotThrow(() -> imageService.deleteImage("img.png"));
    verify(minioClient).removeObject(any());
  }
}
