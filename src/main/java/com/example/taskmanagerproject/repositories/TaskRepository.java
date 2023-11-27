package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Task;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

  Optional<Task> findById(Long id);

  List<Task> findAllByUserId(Long userId);

  void assignToUserById(@Param("taskId") Long taskId, @Param("userId") Long userId);

  void update(Task task);

  void create(Task task);

  void delete(Long id);

}