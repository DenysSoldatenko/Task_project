package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.exceptions.ResourceMappingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

  private final DataSourceConfig dataSourceConfig;

  private final String FIND_TASK_BY_ID = """
            SELECT t.id              AS task_id,
                   t.title           AS task_title,
                   t.description     AS task_description,
                   t.expiration_date AS task_expiration_date,
                   t.status          AS task_status
            FROM tasks t
            WHERE t.id = ?""";

  private final String FIND_ALL_TASKS_BY_USER_ID = """
            SELECT t.id              AS task_id,
                   t.title           AS task_title,
                   t.description     AS task_description,
                   t.expiration_date AS task_expiration_date,
                   t.status          AS task_status
            FROM tasks t
                     JOIN users_tasks ut ON t.id = ut.task_id
            WHERE ut.user_id = ?""";

  private final String ASSIGN_TASK_TO_USER = """
            INSERT INTO users_tasks (task_id, user_id)
            VALUES (?, ?)""";

  private final String UPDATE_TASK = """
            UPDATE tasks
            SET title = ?,
                description = ?,
                expiration_date = ?,
                status = ?
            WHERE id = ?
            """;

  private final String CREATE_TASK = """
            INSERT INTO tasks (title, description, expiration_date, status)
            VALUES (?, ?, ?, ?)""";

  private final String DELETE_TASK = """
            DELETE FROM tasks
            WHERE id = ?""";

  @Override
  public Optional<Task> findById(Long id) {
    try {
      Connection connection = dataSourceConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(FIND_TASK_BY_ID);
      statement.setLong(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        return Optional.ofNullable(TaskRowMapper.mapRow(rs));
      }
    } catch (SQLException throwables) {
      throw new ResourceMappingException("Error while finding user by id.");
    }
  }

  @Override
  public List<Task> findAllByUserId(Long userId) {
    try {
      Connection connection = dataSourceConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(FIND_ALL_TASKS_BY_USER_ID);
      statement.setLong(1, userId);
      try (ResultSet rs = statement.executeQuery()) {
        return TaskRowMapper.mapRows(rs);
      }
    } catch (SQLException throwables) {
      throw new ResourceMappingException("Error while finding all by user id.");
    }
  }

  @Override
  public void assignToUserById(Long taskId, Long userId) {
    try {
      Connection connection = dataSourceConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(ASSIGN_TASK_TO_USER);
      statement.setLong(1, taskId);
      statement.setLong(2, userId);
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throw new ResourceMappingException("Error while assigning to user.");
    }
  }

  @Override
  public void update(Task task) {
    try {
      Connection connection = dataSourceConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(UPDATE_TASK);
      statement.setString(1, task.getTitle());
      if (task.getDescription() == null) {
        statement.setNull(2, Types.VARCHAR);
      } else {
        statement.setString(2, task.getDescription());
      }
      if (task.getExpirationDate() == null) {
        statement.setNull(3, Types.TIMESTAMP);
      } else {
        statement.setTimestamp(3, Timestamp.valueOf(task.getExpirationDate()));
      }
      statement.setString(4, task.getTaskStatus().name());
      statement.setLong(5, task.getId());
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throw new ResourceMappingException("Error while updating task.");
    }
  }

  @Override
  public void create(Task task) {
    try {
      Connection connection = dataSourceConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(CREATE_TASK, PreparedStatement.RETURN_GENERATED_KEYS);
      statement.setString(1, task.getTitle());
      if (task.getDescription() == null) {
        statement.setNull(2, Types.VARCHAR);
      } else {
        statement.setString(2, task.getDescription());
      }
      if (task.getExpirationDate() == null) {
        statement.setNull(3, Types.TIMESTAMP);
      } else {
        statement.setTimestamp(3, Timestamp.valueOf(task.getExpirationDate()));
      }
      statement.setString(4, task.getTaskStatus().name());
      statement.executeUpdate();
      try (ResultSet rs = statement.getGeneratedKeys()) {
        rs.next();
        task.setId(rs.getLong(1));
      }
    } catch (SQLException throwables) {
      throw new ResourceMappingException("Error while creating task.");
    }
  }

  @Override
  public void delete(Long id) {
    try {
      Connection connection = dataSourceConfig.getConnection();
      PreparedStatement statement = connection.prepareStatement(DELETE_TASK);
      statement.setLong(1, id);
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throw new ResourceMappingException("Error while deleting task.");
    }
  }
}
