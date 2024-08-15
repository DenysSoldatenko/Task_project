package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.team.TeamUser;
import com.example.taskmanagerproject.entities.team.TeamUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing UserTeam entities.
 */
@Repository
public interface TeamUserRepository extends JpaRepository<TeamUser, TeamUserId> {

  /**
   * Finds all TeamUser entities by the name of the team.
   *
   * @param teamName the name of the team to filter by.
   * @return a list of TeamUser entities that are associated with the specified team.
   */
  @Query("SELECT tu FROM TeamUser tu WHERE tu.team.name = :teamName")
  List<TeamUser> findAllByTeamName(String teamName);
}
