package com.example.taskmanagerproject.dtos.images;

import static java.nio.file.Files.write;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO representing a file stored as a byte array.
 * Implements {@link MultipartFile} for file upload handling.
 */
public record ImageMultipartFileDto(
      byte[] content,
      String name,
      String originalFilename,
      String contentType
) implements MultipartFile {

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return originalFilename;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return content == null || content.length == 0;
  }

  @Override
  public long getSize() {
    return content.length;
  }

  @NotNull
  @Override
  public byte[] getBytes() {
    return content;
  }

  @NotNull
  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(content);
  }

  @Override
  public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
    write(dest.toPath(), content);
  }
}
