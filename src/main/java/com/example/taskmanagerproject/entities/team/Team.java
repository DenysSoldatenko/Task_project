package com.example.taskmanagerproject.entities.team;

import com.example.taskmanagerproject.entities.security.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Represents a Team entity in the system.
 */
@Data
@Entity
@Table(name = "teams")
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @JsonManagedReference
  @OneToMany(mappedBy = "team")
  private List<TeamUser> teamUsers;
}
