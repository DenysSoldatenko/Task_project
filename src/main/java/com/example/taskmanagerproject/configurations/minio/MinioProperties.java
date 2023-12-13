package com.example.taskmanagerproject.configurations.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for MinIO storage.
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

  private String bucket;
  private String url;
  private String accessKey;
  private String secretKey;
}
