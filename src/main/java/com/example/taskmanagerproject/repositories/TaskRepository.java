package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.task.Task;
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

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      WHERE t.assignedTo.id = :userId
        AND t.createdAt BETWEEN :startDate AND :endDate
        AND t.project.name = :projectName
        AND t.team.name = :teamName
      """)
  Long countAllTasksForUser(@Param("userId") Long userId,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate,
                            @Param("projectName") String projectName,
                            @Param("teamName") String teamName);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      WHERE t.taskStatus = 'APPROVED'
        AND t.assignedTo.id = :userId
        AND t.createdAt BETWEEN :startDate AND :endDate
        AND t.project.name = :projectName
        AND t.team.name = :teamName
      """)
  Long countCompletedTasksForUser(@Param("userId") Long userId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("projectName") String projectName,
                                  @Param("teamName") String teamName);

  @Query("""
      SELECT ROUND(
          (CASE
              WHEN COUNT(t1) > 0 THEN (COUNT(CASE WHEN t1.taskStatus = 'APPROVED' THEN 1 END) * 100.0) / COUNT(t1)
              ELSE 0
          END), 2)
      FROM Task t1
      WHERE t1.assignedTo.id = :userId
        AND t1.createdAt BETWEEN :startDate AND :endDate
        AND t1.project.name = :projectName
        AND t1.team.name = :teamName
      """)
  Double calculateTaskCompletionRateForUser(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("projectName") String projectName,
                                            @Param("teamName") String teamName);

  @Query(value = """
      SELECT ROUND(COALESCE(AVG(EXTRACT(EPOCH FROM (t.approved_at - t.created_at)) / 60), 0), 0) AS average_minutes_spent
      FROM tasks t
      WHERE t.task_status = 'APPROVED'
        AND t.assigned_to = :userId
        AND t.approved_at IS NOT NULL
        AND t.created_at IS NOT NULL
        AND t.created_at BETWEEN :startDate AND :endDate
        AND t.project_id = :projectId
        AND t.team_id = :teamId
      """, nativeQuery = true)
  Double calculateAverageMinutesSpent(@Param("userId") Long userId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("projectId") Long projectId,
                                      @Param("teamId") Long teamId);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      WHERE t.assignedTo.id = :userId
        AND t.taskStatus = 'APPROVED'
        AND t.expirationDate >= t.approvedAt
        AND t.createdAt BETWEEN :startDate AND :endDate
        AND t.project.name = :projectName
        AND t.team.name = :teamName
      """)
  Long countCompletedOnTimeTasks(@Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 @Param("projectName") String projectName,
                                 @Param("teamName") String teamName);

  @Query("""
      SELECT COUNT(DISTINCT t)
      FROM TaskComment tc
      JOIN Task t ON t.id = tc.task.id
      WHERE t.assignedTo.id = :userId
        AND (:includeResolved = false OR tc.isResolved = true)
        AND (:includeApproved = false OR t.taskStatus = 'APPROVED')
        AND t.createdAt BETWEEN :startDate AND :endDate
        AND t.project.name = :projectName
        AND t.team.name = :teamName
      """)
  Long countTasksWithCommentsByUser(@Param("userId") Long userId,
                                    @Param("includeResolved") boolean includeResolved,
                                    @Param("includeApproved") boolean includeApproved,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("projectName") String projectName,
                                    @Param("teamName") String teamName);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      WHERE t.assignedTo.id = :userId
        AND t.priority = 'CRITICAL'
        AND (:includeSolved = false OR t.taskStatus = 'APPROVED')
        AND t.createdAt BETWEEN :startDate AND :endDate
        AND t.project.name = :projectName
        AND t.team.name = :teamName
      """)
  Long countCriticalTasks(@Param("userId") Long userId,
                          @Param("includeSolved") boolean includeSolved,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate,
                          @Param("projectName") String projectName,
                          @Param("teamName") String teamName);

  @Query("FROM Task t WHERE t.assignedBy.id = :userId")
  List<Task> findTasksAssignedBy(@Param("userId") Long userId);

  @Query("FROM Task t WHERE t.assignedTo.id = :userId")
  List<Task> findTasksAssignedTo(@Param("userId") Long userId);

  @Query("FROM Task t WHERE t.assignedBy.id = :userId AND t.project.name = :projectName AND t.team.name = :teamName")
  List<Task> findTasksAssignedBy(@Param("userId") Long userId, @Param("projectName") String projectName, @Param("teamName") String teamName);

  @Query("FROM Task t WHERE t.assignedTo.id = :userId AND t.project.name = :projectName AND t.team.name = :teamName")
  List<Task> findTasksAssignedTo(@Param("userId") Long userId, @Param("projectName") String projectName, @Param("teamName") String teamName);

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
