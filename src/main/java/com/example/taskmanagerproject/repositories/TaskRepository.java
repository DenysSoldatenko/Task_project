package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.tasks.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Task entities.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  /**
   * Retrieves task metrics for a user in a specific project and team within a date range.
   * Metrics include counts for tasks, completed tasks, task completion rate, on-time tasks, bugs,
   * critical tasks, and average task duration.
   *
   * @param assignedTo The ID of the user.
   * @param startDate  The start date of the date range.
   * @param endDate    The end date of the date range.
   * @param projectName The name of the project.
   * @param teamName    The name of the team.
   * @return A list of metrics (for example, total tasks, task completion rate, critical tasks solved).
   */
  @Query(value = """
      SELECT
          t.project_id,
          t.team_id,
          r.name AS user_role,

          COUNT(t.id) AS allTasks,
  
          COUNT(CASE WHEN t.task_status = 'APPROVED' THEN 1 END) AS tasksCompleted,
  
          ROUND(
              CASE
                  WHEN COUNT(t.id) > 0
                      THEN (COUNT(CASE WHEN t.task_status = 'APPROVED' THEN 1 END) * 100.0) / COUNT(t.id)
                  ELSE 0
              END, 2) AS taskCompletionRate,

          COUNT(CASE WHEN t.task_status = 'APPROVED' AND t.expiration_date >= t.approved_at THEN 1 END) AS onTimeTasks,
  
          (SELECT COUNT(DISTINCT tc.task_id)
           FROM task_list.task_comments tc
           JOIN task_list.tasks t2 ON tc.task_id = t2.id
           WHERE t2.assigned_to = :assignedTo
             AND t2.created_at BETWEEN :startDate AND :endDate
             AND t2.project_id = t.project_id
             AND t2.team_id = t.team_id) AS allBugs,
  
          (SELECT COUNT(DISTINCT tc.task_id)
           FROM task_list.task_comments tc
           JOIN task_list.tasks t2 ON tc.task_id = t2.id
           WHERE t2.assigned_to = :assignedTo
             AND t2.created_at BETWEEN :startDate AND :endDate
             AND t2.project_id = t.project_id
             AND t2.team_id = t.team_id
             AND t2.task_status = 'APPROVED') AS bugFixesResolved,
  
          COUNT(CASE WHEN t.priority = 'CRITICAL' THEN 1 END) AS allCriticalTasks,
  
          COUNT(CASE WHEN t.priority = 'CRITICAL' AND t.task_status = 'APPROVED' THEN 1 END) AS criticalTasksSolved,
  
          ROUND(COALESCE(AVG(EXTRACT(EPOCH FROM (t.approved_at - t.created_at)) / 60), 0), 0) AS averageTaskDuration
      FROM task_list.tasks t
      JOIN task_list.projects p ON p.id = t.project_id
      JOIN task_list.teams tm ON tm.id = t.team_id
      JOIN task_list.teams_users tu ON tu.team_id = t.team_id AND tu.user_id = :assignedTo
      JOIN task_list.roles r ON r.id = tu.role_id
      WHERE t.assigned_to = :assignedTo
        AND t.created_at BETWEEN :startDate AND :endDate
        AND p.name = :projectName
        AND tm.name = :teamName
      GROUP BY t.project_id, t.team_id, r.name;
      """, nativeQuery = true)
  List<Object[]> getTaskMetricsByAssignedUser(@Param("assignedTo") Long assignedTo,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("projectName") String projectName,
                                              @Param("teamName") String teamName);

  /**
   * Retrieves task metrics for the top performers in a specific team.
   * Metrics include task counts, completed tasks, task completion rate, and related user information.
   * If a user has no tasks, default "No Data" entries will be returned to ensure three rows are displayed.
   *
   * @param teamName The name of the team to retrieve task metrics for.
   * @return A list of task metrics, including username, image, role, task counts, and completion rates.
   */
  @Query(value = """
        WITH TeamTasks AS (
            SELECT
                tu.user_id,
                COUNT(t.id) FILTER (WHERE t.task_status = 'APPROVED') AS tasks_completed,
                COUNT(t.id) AS total_tasks
            FROM task_list.teams_users tu
            LEFT JOIN task_list.tasks t ON t.assigned_to = tu.user_id AND t.team_id = tu.team_id
            WHERE tu.team_id = (SELECT id FROM task_list.teams WHERE name = :teamName)
              AND t.project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
              AND t.created_at BETWEEN :startDate AND :endDate
            GROUP BY tu.user_id
        ),
        UserAchievements AS (
            SELECT
                au.user_id,
                COUNT(au.achievement_id) AS achievement_count
            FROM task_list.achievements_users au
            JOIN task_list.users u ON au.user_id = u.id
            WHERE u.id IN (SELECT user_id FROM task_list.teams_users WHERE team_id = (SELECT id FROM task_list.teams WHERE name = :teamName))
            AND au.team_id IN (SELECT team_id FROM task_list.teams_users WHERE team_id = (SELECT id FROM task_list.teams WHERE name = :teamName))
            GROUP BY au.user_id
        ),
        RankedUsers AS (
            SELECT
                u.full_name AS user_name,
                COALESCE(ui.image, 'https://dummyimage.com/150x150/000/fff&text=No+Data') AS user_image,
                COALESCE(r.name, 'N/A') AS user_role,
                COALESCE(tt.total_tasks, 0) AS all_tasks,
                COALESCE(tt.tasks_completed, 0) AS tasks_completed,
                ROUND(COALESCE(tt.tasks_completed * 100.0 / NULLIF(tt.total_tasks, 0), 0), 2) AS task_completion_rate,
                COALESCE(ua.achievement_count, 0) AS all_achievements,
                ROW_NUMBER() OVER (
                        ORDER BY COALESCE(tt.tasks_completed, 0) DESC,
                        ROUND(COALESCE(tt.tasks_completed * 100.0 / NULLIF(tt.total_tasks, 0), 0), 2) DESC
                    ) AS rank
            FROM task_list.teams_users tu
            JOIN task_list.users u ON tu.user_id = u.id
            LEFT JOIN task_list.roles r ON tu.role_id = r.id
            LEFT JOIN task_list.users_images ui ON u.id = ui.user_id
            LEFT JOIN TeamTasks tt ON tu.user_id = tt.user_id
            LEFT JOIN UserAchievements ua ON tu.user_id = ua.user_id
            WHERE tu.team_id = (SELECT id FROM task_list.teams WHERE name = :teamName)
        )
        SELECT user_name, user_image, user_role, all_tasks, tasks_completed, task_completion_rate, all_achievements
        FROM RankedUsers
        WHERE rank <= 3
        UNION ALL SELECT 'No Data', 'https://dummyimage.com/150x150/000/fff&text=No+Data', 'N/A', 0, 0, 0, 0
        UNION ALL SELECT 'No Data', 'https://dummyimage.com/150x150/000/fff&text=No+Data', 'N/A', 0, 0, 0, 0
        UNION ALL SELECT 'No Data', 'https://dummyimage.com/150x150/000/fff&text=No+Data', 'N/A', 0, 0, 0, 0
        FROM (SELECT COUNT(*) FROM RankedUsers WHERE rank <= 3) AS subquery
        WHERE (SELECT COUNT(*) FROM RankedUsers WHERE rank <= 3) < 3
        ORDER BY tasks_completed DESC, task_completion_rate DESC;
        """, nativeQuery = true)
  List<Object[]> getTopPerformerMetricsByTeamName(@Param("teamName") String teamName,
                                                  @Param("projectName") String projectName,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

  /**
   * Retrieves project metrics for teams associated with a specific project within a given date range.
   * The metrics include achievements, total tasks, completed tasks, critical tasks, and bugs.
   *
   * @param projectName the name of the project.
   * @param startDate the start date for filtering tasks and achievements.
   * @param endDate the end date for filtering tasks and achievements.
   * @return a list of Object arrays containing the team name and their respective metrics.
   */
  @Query(value = """
      SELECT
          t.name AS team_name,
          COALESCE(achievements.total_achievements, 0) AS achievement_count,
          COALESCE(tasks.total_tasks, 0) AS total_tasks,
          COALESCE(tasks.completed_tasks, 0) AS completed_tasks,
          COALESCE(tasks.on_time_tasks, 0) AS on_time_tasks,
          COALESCE(tasks.total_critical_tasks, 0) AS total_critical_tasks,
          COALESCE(tasks.critical_tasks_completed, 0) AS critical_tasks_completed,
          COALESCE(tasks.total_bugs, 0) AS total_bugs,
          COALESCE(tasks.bugs_completed, 0) AS bugs_completed
      FROM task_list.teams t
      LEFT JOIN (
          SELECT
              t.name AS team_name,
              COUNT(au.achievement_id) AS total_achievements
          FROM task_list.teams t
                   LEFT JOIN task_list.achievements_users au ON t.id = au.team_id
              AND au.project_id IN (SELECT id FROM task_list.projects WHERE name = :projectName)
          GROUP BY t.name
      ) achievements ON t.name = achievements.team_name
      LEFT JOIN (
          SELECT
              t1.name AS team_name,
              COUNT(DISTINCT t2.id) AS total_tasks,
              COUNT(DISTINCT t2.id) FILTER (WHERE t2.task_status = 'APPROVED') AS completed_tasks,
              COUNT(DISTINCT CASE WHEN t2.task_status = 'APPROVED' AND t2.approved_at <= t2.expiration_date THEN t2.id END) AS on_time_tasks,
              COUNT(DISTINCT CASE WHEN t2.priority = 'CRITICAL' THEN t2.id END) AS total_critical_tasks,
              COUNT(DISTINCT CASE WHEN t2.priority = 'CRITICAL' AND t2.task_status = 'APPROVED' THEN t2.id END) AS critical_tasks_completed,
              COUNT(DISTINCT tc.task_id) AS total_bugs,
              COUNT(DISTINCT CASE WHEN t2.task_status = 'APPROVED' THEN tc.task_id END) AS bugs_completed
          FROM task_list.teams t1
                   LEFT JOIN task_list.teams_users tu ON t1.id = tu.team_id
                   LEFT JOIN task_list.tasks t2 ON tu.user_id = t2.assigned_to AND t2.team_id = t1.id
                   LEFT JOIN task_list.task_comments tc ON t2.id = tc.task_id
          WHERE t2.project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
            AND t2.created_at BETWEEN :startDate AND :endDate
          GROUP BY t1.name
      ) tasks ON t.name = tasks.team_name
      WHERE t.id IN (
          SELECT team_id
          FROM task_list.projects_teams
          WHERE project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
      );
      """, nativeQuery = true)
  List<Object[]> getProjectMetricsByProjectName(@Param("projectName") String projectName,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

  /**
   * Retrieves task metrics for all team members in a specific team, including those with no tasks.
   * Metrics include task counts, completed tasks, task completion rate, achievements,
   * on-time task completion, and average task duration.
   *
   * @param teamName The name of the team to retrieve task metrics for.
   * @param projectName The name of the project to filter tasks by.
   * @param startDate The start date for task creation.
   * @param endDate The end date for task creation.
   * @return A list of task metrics, including username, image, role, task counts,
   *     task completion rates, on-time completion, and task duration.
   */
  @Query(value = """
        WITH TeamTasks AS (
            SELECT
                tu.user_id,
                COUNT(t.id) FILTER (WHERE t.task_status = 'APPROVED') AS tasks_completed,
                COUNT(t.id) AS total_tasks
            FROM task_list.teams_users tu
                     LEFT JOIN task_list.tasks t ON t.assigned_to = tu.user_id AND t.team_id = tu.team_id
                AND t.project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
                AND t.created_at BETWEEN :startDate AND :endDate
            WHERE tu.team_id = (SELECT id FROM task_list.teams WHERE name = :teamName)
            GROUP BY tu.user_id
        ),
        UserAchievements AS (
            SELECT
                au.user_id,
                COUNT(au.achievement_id) AS achievement_count
            FROM task_list.achievements_users au
                    JOIN task_list.users u ON au.user_id = u.id
            WHERE u.id IN (SELECT user_id FROM task_list.teams_users WHERE team_id = (SELECT id FROM task_list.teams WHERE name = :teamName))
            AND au.team_id = (SELECT id FROM task_list.teams WHERE name = :teamName)
            GROUP BY au.user_id
        ),
        UserBugs AS (
            SELECT
                t2.assigned_to AS user_id,
                COUNT(DISTINCT tc.task_id) AS allBugs,
                COUNT(DISTINCT CASE WHEN t2.task_status = 'APPROVED' THEN tc.task_id END) AS bugFixesResolved
            FROM task_list.task_comments tc
                    JOIN task_list.tasks t2 ON tc.task_id = t2.id
            WHERE t2.created_at BETWEEN :startDate AND :endDate
            AND t2.project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
            GROUP BY t2.assigned_to
        ),
        UserCriticalTasks AS (
            SELECT
                t.assigned_to AS user_id,
                COUNT(CASE WHEN t.priority = 'CRITICAL' THEN 1 END) AS allCriticalTasks,
                COUNT(CASE WHEN t.priority = 'CRITICAL' AND t.task_status = 'APPROVED' THEN 1 END) AS criticalTasksSolved
            FROM task_list.tasks t
            WHERE t.created_at BETWEEN :startDate AND :endDate
            AND t.project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
            GROUP BY t.assigned_to
        ),
        UserTaskDetails AS (
            SELECT
                u.full_name AS user_name,
                COALESCE(ui.image, 'https://dummyimage.com/150x150/000/fff&text=No+Data') AS user_image,
                COALESCE(r.name, 'N/A') AS user_role,
                COALESCE(tt.total_tasks, 0) AS all_tasks,
                COALESCE(tt.tasks_completed, 0) AS tasks_completed,
                ROUND(COALESCE(tt.tasks_completed * 100.0 / NULLIF(tt.total_tasks, 0), 0), 2) AS task_completion_rate,
                COALESCE(ua.achievement_count, 0) AS all_achievements,
                COALESCE(ub.allBugs, 0) AS allBugs,
                COALESCE(ub.bugFixesResolved, 0) AS bugFixesResolved,
                COALESCE(uct.allCriticalTasks, 0) AS allCriticalTasks,
                COALESCE(uct.criticalTasksSolved, 0) AS criticalTasksSolved,
                COUNT(CASE WHEN t.task_status = 'APPROVED' AND t.approved_at <= t.expiration_date THEN 1 END) AS onTimeTasks,
                ROUND(COALESCE(AVG(EXTRACT(EPOCH FROM (t.approved_at - t.created_at)) / 60), 0), 2) AS averageTaskDuration
            FROM task_list.teams_users tu
                    JOIN task_list.users u ON tu.user_id = u.id
                    LEFT JOIN task_list.roles r ON tu.role_id = r.id
                    LEFT JOIN task_list.users_images ui ON u.id = ui.user_id
                    LEFT JOIN TeamTasks tt ON tu.user_id = tt.user_id
                    LEFT JOIN UserAchievements ua ON tu.user_id = ua.user_id
                    LEFT JOIN UserBugs ub ON tu.user_id = ub.user_id
                    LEFT JOIN UserCriticalTasks uct ON tu.user_id = uct.user_id
                    LEFT JOIN task_list.tasks t ON t.assigned_to = tu.user_id AND t.team_id = tu.team_id
                AND t.project_id = (SELECT id FROM task_list.projects WHERE name = :projectName)
                AND t.created_at BETWEEN :startDate AND :endDate
            WHERE tu.team_id = (SELECT id FROM task_list.teams WHERE name = :teamName)
            GROUP BY u.full_name, ui.image, r.name, tt.total_tasks, tt.tasks_completed,
                     ua.achievement_count, ub.allBugs, ub.bugFixesResolved, uct.allCriticalTasks, uct.criticalTasksSolved
        )
        SELECT
            user_name,
            user_image,
            user_role,
            all_tasks,
            tasks_completed,
            onTimeTasks,
            averageTaskDuration,
            task_completion_rate,
            ROUND(COALESCE(bugFixesResolved * 100.0 / NULLIF(allBugs, 0), 0), 2) AS bugFixResolutionRate,
            ROUND(COALESCE(criticalTasksSolved * 100.0 / NULLIF(allCriticalTasks, 0), 0), 2) AS criticalTaskResolutionRate,
            all_achievements
        FROM UserTaskDetails
        ORDER BY tasks_completed DESC, task_completion_rate DESC;
        """, nativeQuery = true)
  List<Object[]> getAllTeamMemberMetricsByTeamName(@Param("teamName") String teamName,
                                                   @Param("projectName") String projectName,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

  /**
   * Retrieves the daily task completion rates for a specific user, project, and team within a given date range.
   * The completion rate is calculated as the percentage of tasks marked as "APPROVED"
   * relative to the total number of tasks.
   *
   * @param startDate   the start date of the period for which the task completion rates are calculated.
   * @param endDate     the end date of the period for which the task completion rates are calculated.
   * @param assignedTo  the user ID to filter tasks assigned to a specific user.
   * @param projectName the name of the project to filter tasks by project.
   * @param teamName    the name of the team to filter tasks by team.
   * @return a list of Object arrays, where each element represents a row of the query result containing the
   *         task date and the corresponding task completion rate.
   */
  @Query(value = """ 
        WITH date_series AS (SELECT generate_series(:startDate, :endDate, INTERVAL '1 day') AS task_date)
        SELECT ds.task_date,
               ROUND(
                 CASE
                     WHEN COUNT(t.id) > 0 THEN (COUNT(CASE WHEN t.task_status = 'APPROVED' THEN 1 END) * 100.0) / COUNT(t.id)
                     ELSE 0
                 END, 2
               ) AS taskCompletionRate
        FROM date_series ds
        LEFT JOIN task_list.tasks t
            ON t.created_at >= ds.task_date
            AND t.created_at < ds.task_date + INTERVAL '1 day'
            AND t.assigned_to = :assignedTo
            AND t.created_at BETWEEN :startDate AND :endDate
            AND t.project_id IN (SELECT id FROM task_list.projects WHERE name = :projectName)
            AND t.team_id IN (SELECT id FROM task_list.teams WHERE name = :teamName)
        GROUP BY ds.task_date
        ORDER BY ds.task_date;
        """, nativeQuery = true)
  List<Object[]> getDailyCompletionRates(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("assignedTo") Long assignedTo,
                                         @Param("projectName") String projectName,
                                         @Param("teamName") String teamName);

  /**
   * Retrieves the monthly task completion rates for a specific user, project, and team within a given date range.
   * The completion rate is calculated as the percentage of tasks marked as "APPROVED"
   * relative to the total number of tasks.
   *
   * @param startDate   the start date of the period for which the task completion rates are calculated.
   * @param endDate     the end date of the period for which the task completion rates are calculated.
   * @param assignedTo  the user ID to filter tasks assigned to a specific user.
   * @param projectName the name of the project to filter tasks by project.
   * @param teamName    the name of the team to filter tasks by team.
   * @return a list of Object arrays, where each element represents a row of the query result containing the
   *         task month and the corresponding task completion rate.
   */
  @Query(value = """
      WITH date_series AS (SELECT generate_series(:startDate, :endDate, INTERVAL '1 month') AS task_month)
      SELECT
          to_char(ds.task_month, 'YYYY-FMMon') AS task_month,
          ROUND(
              CASE
                  WHEN COUNT(t.id) > 0 THEN
                      (COUNT(CASE WHEN t.task_status = 'APPROVED' THEN 1 END) * 100.0) / COUNT(t.id)
                  ELSE 0
              END, 2) AS taskCompletionRate
      FROM
          date_series ds
      LEFT JOIN task_list.tasks t
          ON t.created_at >= ds.task_month
          AND t.created_at < ds.task_month + INTERVAL '1 month'
          AND t.assigned_to = :assignedTo
          AND t.created_at BETWEEN :startDate AND :endDate
          AND t.project_id IN (SELECT id FROM task_list.projects WHERE name = :projectName)
          AND t.team_id IN (SELECT id FROM task_list.teams WHERE name = :teamName)
      GROUP BY ds.task_month
      ORDER BY ds.task_month
      """, nativeQuery = true)
  List<Object[]> getMonthlyCompletionRates(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("assignedTo") Long assignedTo,
                                           @Param("projectName") String projectName,
                                           @Param("teamName") String teamName);


  /**
   * Finds tasks assigned to a user for a specific project and team.
   *
   * @param slug The slug of the user.
   * @param projectName The name of the project.
   * @param teamName The name of the team.
   * @param pageable The pageable object for pagination.
   * @return A paginated list of tasks assigned to the user.
   */
  @Query("FROM Task t WHERE t.assignedTo.slug = :slug AND t.project.name = :projectName AND t.team.name = :teamName")
  Page<Task> findTasksAssignedToUser(@Param("slug") String slug,
                                     @Param("projectName") String projectName,
                                     @Param("teamName") String teamName,
                                     Pageable pageable);

  /**
   * Finds tasks assigned by a user for a specific project and team.
   *
   * @param slug The slug of the user.
   * @param projectName The name of the project.
   * @param teamName The name of the team.
   * @param pageable The pageable object for pagination.
   * @return A paginated list of tasks assigned by the user.
   */
  @Query("FROM Task t WHERE t.assignedBy.slug = :slug AND t.project.name = :projectName AND t.team.name = :teamName")
  Page<Task> findTasksAssignedByUser(@Param("slug") String slug,
                                     @Param("projectName") String projectName,
                                     @Param("teamName") String teamName,
                                     Pageable pageable);

  /**
   * Retrieves all approved tasks assigned to a specific user within a given project and team.
   *
   * @param userId The ID of the user to whom the tasks are assigned.
   * @param projectId The ID of the project to which the tasks belong.
   * @param teamId The ID of the team to which the tasks belong.
   * @return A list of completed tasks assigned to the user in the specified project and team.
   */
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

  /**
   * Retrieves a random approved task assigned to a user within each team and project.
   * The task is selected randomly per user, team, and project combination, ensuring one task per team and project.
   *
   * @return A list of random approved tasks assigned to users, grouped by team and project.
   */
  @Query(value = """
      WITH RankedTasks AS (
          SELECT t.*,
                 ROW_NUMBER() OVER (PARTITION BY t.assigned_to, t.team_id, t.project_id ORDER BY RANDOM()) AS rn
          FROM task_list.tasks t
                   JOIN task_list.users u ON t.assigned_to = u.id
                   JOIN task_list.teams tm ON t.team_id = tm.id
                   JOIN task_list.projects p ON t.project_id = p.id
          WHERE t.task_status = 'APPROVED'
      )
      SELECT *
      FROM RankedTasks
      WHERE rn = 1
      ORDER BY team_id, project_id
      """, nativeQuery = true)
  List<Task> findRandomApprovedTasksForUserByTeamAndProject();

  /**
   * Retrieves tasks assigned to a specific user that are set to expire within a specified time range.
   * Only tasks with a non-null expiration date are considered. Results are filtered by project and team names.
   *
   * @param start        the start of the expiration window (inclusive)
   * @param end          the end of the expiration window (inclusive)
   * @param projectName  the name of the project to filter tasks by
   * @param teamName     the name of the team to filter tasks by
   * @param userId       the ID of the user to whom the tasks are assigned
   * @return a list of tasks that match the expiration window, project, team, and assignee
   */
  @Query(value = """
      SELECT t.*
      FROM task_list.tasks t
      JOIN task_list.projects p ON t.project_id = p.id
      JOIN task_list.teams tm ON t.team_id = tm.id
      WHERE t.expiration_date IS NOT NULL
        AND t.expiration_date BETWEEN :start AND :end
        AND p.name = :projectName
        AND tm.name = :teamName
        AND t.assigned_to = :userId
      """, nativeQuery = true)
  List<Task> findExpiringTasksForUser(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("projectName") String projectName,
                                      @Param("teamName") String teamName,
                                      @Param("userId") Long userId);

  /**
   * Finds tasks where the task history indicates it has been canceled at any point.
   *
   * @param taskId The ID of the task to check.
   * @return true, if the task has been canceled at some point, otherwise false.
   */
  @Query("""
      SELECT CASE WHEN COUNT(th) > 0 THEN true ELSE false END
      FROM TaskHistory th WHERE th.task.id = :taskId AND th.previousValue = 'CANCELLED'
      """)
  boolean hasTaskBeenCancelled(@Param("taskId") Long taskId);
}
