package com.example.taskmanagerproject.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.RoleHierarchy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class RoleHierarchyRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RoleHierarchyRepository roleHierarchyRepository;

  private Long roleHierarchyId, adminRoleId, memberRoleId;
  private Role adminRole, memberRole;
  private RoleHierarchy roleHierarchy;

  @BeforeEach
  void setUp() {
    adminRole = createRole("ADMIN123");
    adminRoleId = adminRole.getId();

    memberRole = createRole("MEMBER123");
    memberRoleId = memberRole.getId();

    roleHierarchy = createRoleHierarchy(adminRole, memberRole);
    roleHierarchyId = roleHierarchy.getId();

    entityManager.flush();
  }

  @Test
  public void findByHigherRole_shouldReturnRoleHierarchies() {
    List<RoleHierarchy> res = roleHierarchyRepository.findByHigherRole(adminRole);
    assertEquals(1, res.size());
    assertEquals(adminRoleId, res.get(0).getHigherRole().getId());
    assertEquals(memberRoleId, res.get(0).getLowerRole().getId());
  }

  @Test
  public void findByHigherRole_shouldReturnEmpty() {
    Role nonExistentRole = new Role();
    nonExistentRole.setId(999L);
    List<RoleHierarchy> res = roleHierarchyRepository.findByHigherRole(nonExistentRole);
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByHigherRole_shouldHandleMultipleHierarchies() {
    Role teamLeaderRole = createRole("TEAM_LEADER123");
    RoleHierarchy secondHierarchy = createRoleHierarchy(adminRole, teamLeaderRole);
    entityManager.persist(secondHierarchy);
    entityManager.flush();

    List<RoleHierarchy> res = roleHierarchyRepository.findByHigherRole(adminRole);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(rh -> rh.getLowerRole().getId().equals(memberRoleId)));
    assertTrue(res.stream().anyMatch(rh -> rh.getLowerRole().getId().equals(teamLeaderRole.getId())));
  }

  @Test
  public void findByLowerRole_shouldReturnRoleHierarchies() {
    List<RoleHierarchy> res = roleHierarchyRepository.findByLowerRole(memberRole);
    assertEquals(1, res.size());
    assertEquals(adminRoleId, res.get(0).getHigherRole().getId());
    assertEquals(memberRoleId, res.get(0).getLowerRole().getId());
  }

  @Test
  public void findByLowerRole_shouldReturnEmpty() {
    Role nonExistentRole = new Role();
    nonExistentRole.setId(999L);
    List<RoleHierarchy> res = roleHierarchyRepository.findByLowerRole(nonExistentRole);
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByLowerRole_shouldHandleMultipleHierarchies() {
    Role teamLeaderRole = createRole("TEAM_LEADER123");
    RoleHierarchy secondHierarchy = createRoleHierarchy(teamLeaderRole, memberRole);
    entityManager.persist(secondHierarchy);
    entityManager.flush();

    List<RoleHierarchy> res = roleHierarchyRepository.findByLowerRole(memberRole);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(rh -> rh.getHigherRole().getId().equals(adminRoleId)));
    assertTrue(res.stream().anyMatch(rh -> rh.getHigherRole().getId().equals(teamLeaderRole.getId())));
  }

  @Test
  public void findAll_shouldReturnAllRoleHierarchies() {
    Role teamLeaderRole = createRole("TEAM_LEADER123");
    RoleHierarchy secondHierarchy = createRoleHierarchy(teamLeaderRole, memberRole);
    entityManager.persist(secondHierarchy);
    entityManager.flush();

    List<RoleHierarchy> res = roleHierarchyRepository.findAll();
    assertTrue(res.stream().anyMatch(rh -> rh.getHigherRole().getId().equals(adminRoleId) && rh.getLowerRole().getId().equals(memberRoleId)));
    assertTrue(res.stream().anyMatch(rh -> rh.getHigherRole().getId().equals(teamLeaderRole.getId()) && rh.getLowerRole().getId().equals(memberRoleId)));
  }

  @Test
  public void findAll_shouldReturnEmptyWhenNoHierarchies() {
    entityManager.getEntityManager().createQuery("DELETE FROM RoleHierarchy").executeUpdate();
    entityManager.flush();

    List<RoleHierarchy> res = roleHierarchyRepository.findAll();
    assertTrue(res.isEmpty());
  }

  @Test
  public void isHigherRoleAssigned_shouldReturnTrue() {
    boolean exists = roleHierarchyRepository.isHigherRoleAssigned(adminRoleId, memberRoleId);
    assertTrue(exists);
  }

  @Test
  public void isHigherRoleAssigned_shouldReturnFalseForNonExistentHigherRole() {
    boolean exists = roleHierarchyRepository.isHigherRoleAssigned(999L, memberRoleId);
    assertFalse(exists);
  }

  @Test
  public void isHigherRoleAssigned_shouldReturnFalseForNonExistentLowerRole() {
    boolean exists = roleHierarchyRepository.isHigherRoleAssigned(adminRoleId, 999L);
    assertFalse(exists);
  }

  @Test
  public void isHigherRoleAssigned_shouldReturnFalseForSameRole() {
    boolean exists = roleHierarchyRepository.isHigherRoleAssigned(adminRoleId, adminRoleId);
    assertFalse(exists);
  }

  private Role createRole(String name) {
    Role r = new Role();
    r.setName(name);
    entityManager.persist(r);
    return r;
  }

  private RoleHierarchy createRoleHierarchy(Role higherRole, Role lowerRole) {
    RoleHierarchy rh = new RoleHierarchy();
    rh.setHigherRole(higherRole);
    rh.setLowerRole(lowerRole);
    entityManager.persist(rh);
    return rh;
  }
}