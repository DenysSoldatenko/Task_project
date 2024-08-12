package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.PROJECT_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_DOES_NOT_HAVE_ROLE_TO_CREATE_PROJECT;

import com.example.taskmanagerproject.dtos.ProjectDto;
import com.example.taskmanagerproject.entities.Project;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating project data.
 */
@Component
public class ProjectValidator extends BaseValidator<ProjectDto> {

  private final ProjectRepository projectRepository;

  /**
   * Constructor for creating a ProjectValidator instance.
   *
   * @param validator The validator object used to validate the project.
   * @param userRepository The repository responsible for accessing user data.
   * @param projectRepository The repository responsible for accessing project data.
   */
  public ProjectValidator(
      Validator validator, UserRepository userRepository, ProjectRepository projectRepository
  ) {
    super(validator, userRepository);
    this.projectRepository = projectRepository;
  }

  /**
   * Validates a ProjectDto object.
   *
   * @param projectDto The ProjectDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateProjectDto(final ProjectDto projectDto, final Project... existingProject) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(projectDto, errorMessages);
    validateNameTaken(
        projectDto.name(),
        Arrays.stream(existingProject).findFirst().map(Project::getName).orElse(null),
        projectRepository.existsByName(projectDto.name()),
        errorMessages,
        PROJECT_ALREADY_EXISTS
    );
    validateCreatorRole(
        projectDto.creator(),
        USER_DOES_NOT_HAVE_ROLE_TO_CREATE_PROJECT,
        errorMessages
    );
    throwIfErrorsExist(errorMessages);
  }
}
