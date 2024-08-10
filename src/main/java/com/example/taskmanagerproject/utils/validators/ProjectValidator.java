package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.entities.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.RoleName.TEAM_LEAD;
import static com.example.taskmanagerproject.utils.MessageUtils.PROJECT_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_DOES_NOT_HAVE_ROLE_TO_CREATE_PROJECT;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;

import com.example.taskmanagerproject.dtos.ProjectDto;
import com.example.taskmanagerproject.entities.Project;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Utility class for validating project data.
 */
@Component
@Validated
@AllArgsConstructor
public class ProjectValidator {

  private static final List<String> PROJECT_CREATION_ALLOWED_ROLES = asList(
      ADMIN.name(), PRODUCT_OWNER.name(), SCRUM_MASTER.name(), MANAGER.name(), TEAM_LEAD.name()
  );

  private final Validator validator;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;

  /**
   * Validates a ProjectDto object.
   *
   * @param projectDto The ProjectDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateProjectDto(final ProjectDto projectDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(projectDto, errorMessages);
    validateProjectExists(projectDto, errorMessages);
    validateCreatorRole(projectDto, errorMessages);

    if (!errorMessages.isEmpty()) {
      throw new ValidationException(join(", ", errorMessages));
    }
  }

  /**
   * Validates a ProjectDto object against an existing Project entity.
   *
   * @param project The existing Project entity.
   * @param projectDto The ProjectDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateProjectDto(final Project project, final ProjectDto projectDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(projectDto, errorMessages);
    validateProjectExists(project, projectDto, errorMessages);
    validateCreatorRole(projectDto, errorMessages);

    if (!errorMessages.isEmpty()) {
      throw new ValidationException(join(", ", errorMessages));
    }
  }

  /**
   * Validates constraints such as null or empty values.
   *
   * @param projectDto The ProjectDto object to validate.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateConstraints(final ProjectDto projectDto, final Set<String> errorMessages) {
    Set<ConstraintViolation<ProjectDto>> violations = validator.validate(projectDto);
    for (ConstraintViolation<ProjectDto> violation : violations) {
      errorMessages.add(violation.getMessage());
    }
  }

  /**
   * Validates if the project already exists in the system.
   *
   * @param projectDto The ProjectDto object to check.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateProjectExists(final ProjectDto projectDto, final Set<String> errorMessages) {
    if (projectRepository.existsByName(projectDto.name())) {
      errorMessages.add(PROJECT_ALREADY_EXISTS + projectDto.name());
    }
  }

  /**
   * Validates if the project already exists in the system (used during update).
   *
   * @param project The existing Project entity.
   * @param projectDto The ProjectDto object to check.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateProjectExists(
      final Project project,
      final ProjectDto projectDto,
      final Set<String> errorMessages
  ) {
    if (!project.getName().equals(projectDto.name())
        && projectRepository.existsByName(projectDto.name())) {
      errorMessages.add(PROJECT_ALREADY_EXISTS + projectDto.name());
    }
  }

  /**
   * Validates if the creator of the project has a role allowed for project creation.
   *
   * @param projectDto The ProjectDto object containing the creator's details.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateCreatorRole(final ProjectDto projectDto, final Set<String> errorMessages) {
    String username = projectDto.creator().username();
    User creator = userRepository.findByUsername(username)
        .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_WITH_USERNAME + username));

    if (!PROJECT_CREATION_ALLOWED_ROLES.contains(creator.getRole().getName())) {
      errorMessages.add(format(USER_DOES_NOT_HAVE_ROLE_TO_CREATE_PROJECT, username));
    }
  }
}
