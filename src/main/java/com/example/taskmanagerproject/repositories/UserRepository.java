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

  Optional<User> findByUsername(String username);

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

}
