package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.UserTeam;
import com.example.taskmanagerproject.entities.UserTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing UserTeam entities.
 */
@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UserTeamId> {}
