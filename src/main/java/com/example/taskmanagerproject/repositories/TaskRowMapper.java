package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.TaskStatus;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TaskRowMapper {

  @SneakyThrows
  public static Task mapRow(ResultSet resultSet) {
    if (resultSet.next()) {
      return extractTask(resultSet);
    }
    return null;
  }

  @SneakyThrows
  public static List<Task> mapRows(ResultSet resultSet) {
    List<Task> tasks = new ArrayList<>();
    while (resultSet.next()) {
      tasks.add(extractTask(resultSet));
    }
    return tasks;
  }

  @SneakyThrows
  private static Task extractTask(ResultSet resultSet) {
    Task task = new Task();
    task.setId(resultSet.getLong("task_id"));
    if (!resultSet.wasNull()) {
      task.setTitle(resultSet.getString("task_title"));
      task.setDescription(resultSet.getString("task_description"));
      task.setTaskStatus(TaskStatus.valueOf(resultSet.getString("task_status")));
      Timestamp timestamp = resultSet.getTimestamp("task_expiration_date");
      if (timestamp != null) {
        task.setExpirationDate(timestamp.toLocalDateTime());
      }
      return task;
    }
    return null;
  }
}
