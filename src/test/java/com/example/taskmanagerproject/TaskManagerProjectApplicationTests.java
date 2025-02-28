package com.example.taskmanagerproject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.taskmanagerproject.services.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskManagerProjectApplicationTests {

  @Autowired
  private AuthenticationServiceImpl authenticationService;

  @Test
  void contextLoads() {
    assertNotNull(authenticationService);
  }
}
