package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.mappers.UserMapper;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.UserService;
import com.example.taskmanagerproject.utils.UserFactory;
import com.example.taskmanagerproject.utils.UserValidator;
import lombok.AllArgsConstructor;
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
  public UserDto getUserById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public UserDto updateUser(UserDto userDto, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

    user.setFullName(userDto.fullName());
    user.setUsername(userDto.username());
    user.setPassword(passwordEncoder.encode(userDto.password()));
    user.setConfirmPassword(passwordEncoder.encode(userDto.confirmPassword()));

    User updatedUser = userRepository.save(user);
    return userMapper.toDto(updatedUser);
  }

  @Override
  @Transactional
  public User createUser(UserDto userDto) {
    userValidator.validateUserDto(userDto);
    User createdUser = userFactory.createUserFromRequest(userDto);
    userRepository.save(createdUser);
    return createdUser;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isUserTaskOwner(Long userId, Long taskId) {
    return userRepository.isTaskOwner(userId, taskId);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getTaskAuthor(Long taskId) {
    User user = userRepository.findTaskAuthorByTaskId(taskId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void deleteUserById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
    userRepository.delete(user);
  }
}
