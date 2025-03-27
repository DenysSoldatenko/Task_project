package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_PROJECT;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectValidatorTest {

  private Validator validator;
  private UserRepository userRepository;
  private ProjectRepository projectRepository;
  private ProjectValidator projectValidator;

  private final UserDto validUser = new UserDto(100L, "validuser@gmail.com", "Valid User", "slug1", "", singletonList(""));
  private final ProjectDto validProject = new ProjectDto(100L, "New Project", "Description", validUser);

  @BeforeEach
  void setUp() {
    validator = mock(Validator.class);
    userRepository = mock(UserRepository.class);
    projectRepository = mock(ProjectRepository.class);
    projectValidator = new ProjectValidator(validator, userRepository, projectRepository);
  }

  @Test
  void validateProjectDto_shouldPassWithValidInput() {
    when(validator.validate(validProject)).thenReturn(Collections.emptySet());
    when(projectRepository.existsByName("New Project")).thenReturn(false);
    when(userRepository.isUserInLeadershipPosition(validUser.username())).thenReturn(true);

    assertDoesNotThrow(() -> projectValidator.validateProjectDto(validProject));
  }

  @Test
  void validateProjectDto_shouldThrowIfProjectNameExistsAndNotUpdating() {
    when(validator.validate(validProject)).thenReturn(Collections.emptySet());
    when(projectRepository.existsByName("New Project")).thenReturn(true);
    when(userRepository.isUserInLeadershipPosition(validUser.username())).thenReturn(true);

    ValidationException ex = assertThrows(ValidationException.class, () -> projectValidator.validateProjectDto(validProject));
    assertTrue(ex.getMessage().contains(PROJECT_ALREADY_EXISTS + "New Project"));
  }

  @Test
  void validateProjectDto_shouldThrowIfUserNotAllowedToCreate() {
    when(validator.validate(validProject)).thenReturn(Collections.emptySet());
    when(projectRepository.existsByName("New Project")).thenReturn(false);
    when(userRepository.isUserInLeadershipPosition(validUser.username())).thenReturn(false);

    ValidationException ex = assertThrows(ValidationException.class, () -> projectValidator.validateProjectDto(validProject));
    assertTrue(ex.getMessage().contains(USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_PROJECT + validUser.username()));
  }

  @Test
  void validateProjectDto_shouldThrowIfUserNotAllowedToUpdate() {
    Project existing = new Project();
    existing.setName("New Project");

    when(validator.validate(validProject)).thenReturn(Collections.emptySet());
    when(projectRepository.existsByName("New Project")).thenReturn(false);
    when(userRepository.isProjectCreator("New Project", validUser.username())).thenReturn(false);

    ValidationException ex = assertThrows(ValidationException.class, () -> projectValidator.validateProjectDto(validProject, existing));
    assertTrue(ex.getMessage().contains(USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_PROJECT + validUser.username()));
  }

  @Test
  void validateProjectDto_shouldThrowIfConstraintViolationsExist() {
    ConstraintViolation<ProjectDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Name must not be blank");

    when(validator.validate(validProject)).thenReturn(Set.of(violation));

    ValidationException ex = assertThrows(ValidationException.class, () -> projectValidator.validateProjectDto(validProject));
    assertTrue(ex.getMessage().contains("Name must not be blank"));
  }
}
