package com.example.taskmanagerproject.entities.teams;

import com.example.taskmanagerproject.entities.users.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
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

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @JsonManagedReference
  @OneToMany(mappedBy = "team")
  private List<TeamUser> teamUsers;
}
