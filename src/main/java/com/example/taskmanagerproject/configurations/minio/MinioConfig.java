package com.example.taskmanagerproject.configurations.minio;

import static io.minio.MinioClient.builder;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up MinIO client.
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

  private final MinioProperties minioProperties;

  /**
   * Creates a MinioClient bean to interact with MinIO server.
   *
   * @return A MinioClient instance.
   */
  @Bean
  public MinioClient minioClient() {
    return builder()
      .endpoint(minioProperties.getUrl())
      .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
      .build();
  }
}
