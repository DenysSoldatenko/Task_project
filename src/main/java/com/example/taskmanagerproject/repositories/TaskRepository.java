package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.tasks.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Task entities.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  @Query(value = """
      SELECT
          t.project_id,
  
          t.team_id,
  
          COUNT(t.id) AS allTasks,
  
          COUNT(CASE WHEN t.task_status = 'APPROVED' THEN 1 END) AS tasksCompleted,
  
          ROUND(
              CASE
                  WHEN COUNT(t.id) > 0 THEN (COUNT(CASE WHEN t.task_status = 'APPROVED' THEN 1 END) * 100.0) / COUNT(t.id)
                  ELSE 0
              END, 2) AS taskCompletionRate,
          COUNT(CASE WHEN t.task_status = 'APPROVED' AND t.expiration_date >= t.approved_at THEN 1 END) AS onTimeTasks,
  
          (SELECT COUNT(DISTINCT tc.task_id)
           FROM task_comments tc
           JOIN tasks t2 ON tc.task_id = t2.id
           WHERE t2.assigned_to = :assignedTo
             AND t2.created_at BETWEEN :startDate AND :endDate
             AND t2.project_id = t.project_id
             AND t2.team_id = t.team_id) AS allBugs,
  
          (SELECT COUNT(DISTINCT tc.task_id)
           FROM task_comments tc
           JOIN tasks t2 ON tc.task_id = t2.id
           WHERE t2.assigned_to = :assignedTo
             AND t2.created_at BETWEEN :startDate AND :endDate
             AND t2.project_id = t.project_id
             AND t2.team_id = t.team_id
             AND t2.task_status = 'APPROVED') AS bugFixesResolved,
  
          COUNT(CASE WHEN t.priority = 'CRITICAL' THEN 1 END) AS allCriticalTasks,
  
          COUNT(CASE WHEN t.priority = 'CRITICAL' AND t.task_status = 'APPROVED' THEN 1 END) AS criticalTasksSolved,
  
          ROUND(COALESCE(AVG(EXTRACT(EPOCH FROM (t.approved_at - t.created_at)) / 60), 0), 0) AS averageTaskDuration
      FROM tasks t
      JOIN projects p ON p.id = t.project_id
      JOIN teams tm ON tm.id = t.team_id
      WHERE t.assigned_to = :assignedTo
        AND t.created_at BETWEEN :startDate AND :endDate
        AND p.name = :projectName
        AND tm.name = :teamName
      GROUP BY t.project_id, t.team_id
      """, nativeQuery = true)
  List<Object[]> getTaskMetricsByAssignedUser(@Param("assignedTo") Long assignedTo,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("projectName") String projectName,
                                              @Param("teamName") String teamName);

  @Query("FROM Task t WHERE t.assignedBy.id = :userId")
  List<Task> findTasksAssignedBy(@Param("userId") Long userId);

  @Query("FROM Task t WHERE t.assignedTo.id = :userId")
  List<Task> findTasksAssignedTo(@Param("userId") Long userId);

  /**
   * Finds tasks where the task history indicates it has been canceled at any point.
   *
   * @param taskId The ID of the task to check.
   * @return true, if the task has been cancelled at some point, otherwise false.
   */
  @Query("""
      SELECT CASE WHEN COUNT(th) > 0 THEN true ELSE false END
      FROM TaskHistory th WHERE th.task.id = :taskId AND th.previousValue = 'CANCELLED'
      """)
  boolean hasTaskBeenCancelled(@Param("taskId") Long taskId);

  @Query("FROM Task t WHERE t.assignedBy.id = :userId AND t.project.name = :projectName AND t.team.name = :teamName")
  List<Task> findTasksAssignedBy(@Param("userId") Long userId, @Param("projectName") String projectName, @Param("teamName") String teamName);

  @Query("FROM Task t WHERE t.assignedTo.id = :userId AND t.project.name = :projectName AND t.team.name = :teamName")
  List<Task> findTasksAssignedTo(@Param("userId") Long userId, @Param("projectName") String projectName, @Param("teamName") String teamName);

  @Query("""
      SELECT t
      FROM Task t
      WHERE t.taskStatus = 'APPROVED'
        AND t.assignedTo.id = :userId
        AND t.project.id = :projectId
        AND t.team.id = :teamId
      """)
  List<Task> findAllCompletedTasksAssignedToUser(@Param("userId") Long userId,
                                  @Param("projectId") Long projectId,
                                  @Param("teamId") Long teamId);
  @Query("""
      SELECT t
      FROM Task t
      WHERE t.taskStatus = 'APPROVED'
        AND t.assignedBy.id = :userId
        AND t.project.id = :projectId
        AND t.team.id = :teamId
      """)
  List<Task> findAllCompletedTasksAssignedByUser(@Param("userId") Long userId,
                                          @Param("projectId") Long projectId,
                                          @Param("teamId") Long teamId);

  @Query(value = """
      WITH RankedTasks AS (
          SELECT t.*,
                 ROW_NUMBER() OVER (PARTITION BY t.assigned_to, t.team_id, t.project_id ORDER BY RANDOM()) AS rn
          FROM tasks t
                   JOIN users u ON t.assigned_to = u.id
                   JOIN teams tm ON t.team_id = tm.id
                   JOIN projects p ON t.project_id = p.id
          WHERE t.task_status = 'APPROVED'
      )
      SELECT *
      FROM RankedTasks
      WHERE rn = 1
      ORDER BY team_id, project_id
      """, nativeQuery = true)
  List<Task> findRandomApprovedTasksForUserByTeamAndProject();

  @Query(value = """
    SELECT * FROM tasks
    WHERE expiration_date IS NOT NULL
      AND expiration_date BETWEEN :start AND :end
      AND project_name = :projectName
      AND team_name = :teamName
    """, nativeQuery = true)
  List<Task> findExpiringTasksBetween(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("projectName") String projectName,
                                      @Param("teamName") String teamName);
}
