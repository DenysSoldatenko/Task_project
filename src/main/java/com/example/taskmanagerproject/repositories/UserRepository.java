package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a User by username and fetches the associated Role.
   *
   * @param username The username of the user.
   * @return An Optional containing the User if found, or empty if not.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.username = :username")
  Optional<User> findByUsername(String username);

  /**
   * Finds a User by slug and fetches the associated Role.
   *
   * @param slug The slug of the user.
   * @return An Optional containing the User if found, or empty if not.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.slug = :slug")
  Optional<User> findBySlug(String slug);

  @Query(value = """
            SELECT u.id,
                   u.full_name,
                   u.username,
                   u.password,
                   u.confirm_password
            FROM users_tasks ut
                     JOIN users u ON ut.user_id = u.id
            WHERE ut.task_id = :taskId
            """, nativeQuery = true)
  Optional<User> findTaskAuthorByTaskId(@Param("taskId") Long taskId);

  @Query(value = """
            SELECT exists(SELECT 1
                          FROM users_tasks
                          WHERE user_id = :userId
                            AND task_id = :taskId)
            """, nativeQuery = true)
  boolean isTaskOwner(@Param("userId") Long userId,
                      @Param("taskId") Long taskId);


  /**
   * Checks if a given user is the creator of a specific project.
   *
   * @param projectName the name of the project
   * @param userId the ID of the user
   * @return true if the user is the creator of the project, false otherwise
   */
  @Query("""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
      FROM Project p
      WHERE p.name = :projectName AND p.creator.id = :userId
      """)
  boolean isProjectCreator(@Param("projectName") String projectName, @Param("userId") Long userId);

}
