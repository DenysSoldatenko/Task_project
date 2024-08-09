package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_SLUG;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.mappers.UserMapper;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.UserService;
import com.example.taskmanagerproject.utils.UserFactory;
import com.example.taskmanagerproject.utils.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the UserService interface.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserFactory userFactory;
  private final UserValidator userValidator;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "UserService::getUserBySlug", key = "#slug")
  public UserDto getUserBySlug(final String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "UserService::getByUsername", key = "#username")
  public User getUserByUsername(final String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  @Override
  @Transactional
  @CachePut(value = "UserService::getUserBySlug", key = "#slug")
  public UserDto updateUser(final UserDto userDto, final String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));
    userValidator.validateUserDto(userDto);

    user.setFullName(userDto.fullName());
    user.setUsername(userDto.username());
    user.setPassword(passwordEncoder.encode(userDto.password()));
    user.setConfirmPassword(passwordEncoder.encode(userDto.confirmPassword()));

    User updatedUser = userRepository.save(user);
    return userMapper.toDto(updatedUser);
  }

  @Override
  @Transactional
  public User createUser(final UserDto userDto) {
    userValidator.validateUserDto(userDto);
    User createdUser = userFactory.createUserFromRequest(userDto);
    userRepository.save(createdUser);
    return createdUser;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "UserService::isTaskOwner",
      key = "#userId + '.' + #taskId"
  )
  public boolean isUserTaskOwner(final Long userId, final Long taskId) {
    return userRepository.isTaskOwner(userId, taskId);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getTaskAuthor(final Long taskId) {
    User user = userRepository.findTaskAuthorByTaskId(taskId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  @CacheEvict(value = "UserService::getById", key = "#slug")
  public void deleteUserBySlug(final String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_SLUG + slug));
    userRepository.delete(user);
  }
}
