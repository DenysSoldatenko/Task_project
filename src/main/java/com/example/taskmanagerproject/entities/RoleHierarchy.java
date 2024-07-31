package com.example.taskmanagerproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_hierarchy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleHierarchy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @Enumerated(EnumType.STRING)
  @JoinColumn(name = "higher_role", nullable = false)
  private Role higherRole;

  @ManyToOne
  @JoinColumn(name = "lower_role", nullable = false)
  private Role lowerRole;
}
