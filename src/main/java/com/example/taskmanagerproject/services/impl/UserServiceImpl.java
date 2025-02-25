package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_SLUG;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.services.UserService;
import com.example.taskmanagerproject.utils.factories.UserFactory;
import com.example.taskmanagerproject.utils.mappers.UserMapper;
import com.example.taskmanagerproject.utils.validators.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the UserService interface.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final UserFactory userFactory;
  private final ImageService imageService;
  private final UserValidator userValidator;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "UserService::getUserBySlug", key = "#slug")
  public UserDto getUserBySlug(String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "UserService::getByUsername", key = "#username")
  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  @Override
  @Transactional
  @CachePut(value = "UserService::getUserBySlug", key = "#slug")
  public UserDto updateUser(UserDto userDto, String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));
    userValidator.validateUserDto(userDto);

    user.setFullName(userDto.fullName());
    user.setSlug(userDto.slug());

    User updatedUser = userRepository.save(user);
    return userMapper.toDto(updatedUser);
  }

  @Override
  @Transactional
  public UserDto createUser(JwtAuthenticationToken jwtAuth) {
    User user = userFactory.createUserFromRequest(jwtAuth);
    UserDto userDto = userMapper.toDto(user);

    userValidator.validateUserDto(userDto);
    user = userRepository.save(user);

    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "UserService::isTaskOwner", key = "#userId + '.' + #taskId")
  public boolean isUserTaskOwner(Long userId, Long taskId) {
    return userRepository.isTaskOwner(userId, taskId);
  }

  @Override
  @Transactional
  @CacheEvict(value = "UserService::getById", key = "#slug")
  public void deleteUserBySlug(String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));
    userRepository.delete(user);
  }

  @Override
  public boolean hasTeamAccess(String teamName, String username) {
    boolean isCreator = userRepository.isTeamCreator(teamName, username);
    boolean isUserInLeadershipPosition = userRepository.isUserInLeadershipPositionInTeam(teamName, username);
    return isCreator || isUserInLeadershipPosition;
  }

  @Override
  public boolean hasProjectAccess(String projectName, String username) {
    boolean isCreator = userRepository.isProjectCreator(projectName, username);
    boolean isUserInLeadershipPosition = userRepository.isUserInLeadershipPositionInProject(projectName, username);
    return isCreator || isUserInLeadershipPosition;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "UserService::isUserAssignedToTask", key = "#userId + '.' + #taskId")
  public boolean isUserAssignedToTask(Long userId, Long taskId) {
    return userRepository.isUserAssignedToTask(userId, taskId);
  }

  @Override
  @Transactional
  public void uploadUserPhoto(String slug, UserImageDto imageDto) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));

    String fileName = imageService.uploadUserImage(imageDto);
    user.getImage().add(fileName);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void updateUserPhoto(String slug, UserImageDto imageDto) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));

    String newFileName = imageService.uploadUserImage(imageDto);

    if (user.getImage() != null) {
      deleteUserPhoto(slug);
    }

    user.getImage().add(newFileName);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteUserPhoto(String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));

    if (user.getImage() != null) {
      user.getImage().clear();
      userRepository.save(user);
    }
  }
}
