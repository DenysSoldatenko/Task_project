package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.utils.MessageUtils.IMAGE_DOWNLOAD_ERROR;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.users.UserImageDto;
import com.example.taskmanagerproject.exceptions.ImageProcessingException;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.UserService;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Generates profile images for users.
 */
@Service
@RequiredArgsConstructor
public class ImageGeneratorService {

  private static final String IMAGE_URL_TEMPLATE = "https://randomuser.me/api/portraits/%s/%d.jpg";
  private static final Random RANDOM = new Random();

  private final UserService userService;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  /**
   * Generates and assigns profile images for all users.
   */
  public void generateImageForUser() {
    userRepository.findAll().forEach(user -> {
      String gender = determineGender(user.getSlug());
      String imageUrl = getRandomImageUrl(gender);
      MultipartFile imageFile = downloadImage(imageUrl);
      userService.uploadUserPhoto(user.getSlug(), new UserImageDto(imageFile));
    });
  }

  private String determineGender(String slug) {
    String[] nameParts = slug.split("-");
    String firstName = nameParts[0].toLowerCase();
    return firstName.matches(".*[aeiouy]$") ? "female" : "male"; // Simple heuristic
  }

  private String getRandomImageUrl(String gender) {
    return format(IMAGE_URL_TEMPLATE, gender.equals("female") ? "women" : "men", RANDOM.nextInt(99) + 1);
  }

  @SneakyThrows
  private MultipartFile downloadImage(String imageUrl) {
    byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
    if (imageBytes == null || imageBytes.length == 0) {
      throw new ImageProcessingException(IMAGE_DOWNLOAD_ERROR + imageUrl);
    }
    return new MockMultipartFile("file", "profile.jpg", "image/jpeg", imageBytes);
  }
}
