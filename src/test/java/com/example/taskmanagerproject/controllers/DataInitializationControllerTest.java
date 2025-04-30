package com.example.taskmanagerproject.controllers;

import static com.example.taskmanagerproject.utils.MessageUtil.DATA_INITIALIZATION_FAIL_MESSAGE;
import static com.example.taskmanagerproject.utils.MessageUtil.DATA_INITIALIZATION_SUCCESS_MESSAGE;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanagerproject.configurations.initializers.DataInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = DataInitializationController.class)
class DataInitializationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private DataInitializer dataInitializer;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .build();
  }

  @Nested
  @DisplayName("Initialize Database Tests")
  class InitializeDatabaseTests {

    @Test
    void shouldReturn201AndSuccessMessage() throws Exception {
      doNothing().when(dataInitializer).initializeTasks();
      doNothing().when(dataInitializer).updateTaskStatuses();
      doNothing().when(dataInitializer).updateTaskHistoryDates();
      doNothing().when(dataInitializer).generateAchievementsForUsers();

      mockMvc.perform(post("/api/v2/data-initialization/initialize"))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(DATA_INITIALIZATION_SUCCESS_MESSAGE));

      verify(dataInitializer).initializeTasks();
      verify(dataInitializer).updateTaskStatuses();
      verify(dataInitializer).updateTaskHistoryDates();
      verify(dataInitializer).generateAchievementsForUsers();
      verifyNoMoreInteractions(dataInitializer);
    }

    @Test
    void shouldReturn500WhenInitializeTasksFails() throws Exception {
      doThrow(new RuntimeException("Task initialization failed")).when(dataInitializer).initializeTasks();

      mockMvc.perform(post("/api/v2/data-initialization/initialize"))
          .andExpect(status().isInternalServerError())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(DATA_INITIALIZATION_FAIL_MESSAGE));

      verify(dataInitializer).initializeTasks();
      verifyNoMoreInteractions(dataInitializer);
    }

    @Test
    void shouldReturn500WhenUpdateTaskStatusesFails() throws Exception {
      doNothing().when(dataInitializer).initializeTasks();
      doThrow(new IllegalStateException("Status update failed")).when(dataInitializer).updateTaskStatuses();

      mockMvc.perform(post("/api/v2/data-initialization/initialize"))
          .andExpect(status().isInternalServerError())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(DATA_INITIALIZATION_FAIL_MESSAGE));

      verify(dataInitializer).initializeTasks();
      verify(dataInitializer).updateTaskStatuses();
      verifyNoMoreInteractions(dataInitializer);
    }

    @Test
    void shouldReturn500WhenUpdateTaskHistoryDatesFails() throws Exception {
      doNothing().when(dataInitializer).initializeTasks();
      doNothing().when(dataInitializer).updateTaskStatuses();
      doThrow(new RuntimeException("History update failed")).when(dataInitializer).updateTaskHistoryDates();

      mockMvc.perform(post("/api/v2/data-initialization/initialize"))
          .andExpect(status().isInternalServerError())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(DATA_INITIALIZATION_FAIL_MESSAGE));

      verify(dataInitializer).initializeTasks();
      verify(dataInitializer).updateTaskStatuses();
      verify(dataInitializer).updateTaskHistoryDates();
      verifyNoMoreInteractions(dataInitializer);
    }

    @Test
    void shouldReturn500WhenGenerateAchievementsFails() throws Exception {
      doNothing().when(dataInitializer).initializeTasks();
      doNothing().when(dataInitializer).updateTaskStatuses();
      doNothing().when(dataInitializer).updateTaskHistoryDates();
      doThrow(new RuntimeException("Achievement generation failed")).when(dataInitializer).generateAchievementsForUsers();

      mockMvc.perform(post("/api/v2/data-initialization/initialize"))
          .andExpect(status().isInternalServerError())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(DATA_INITIALIZATION_FAIL_MESSAGE));

      verify(dataInitializer).initializeTasks();
      verify(dataInitializer).updateTaskStatuses();
      verify(dataInitializer).updateTaskHistoryDates();
      verify(dataInitializer).generateAchievementsForUsers();
      verifyNoMoreInteractions(dataInitializer);
    }

    @Test
    void shouldNotRequireAuthentication() throws Exception {
      doNothing().when(dataInitializer).initializeTasks();
      doNothing().when(dataInitializer).updateTaskStatuses();
      doNothing().when(dataInitializer).updateTaskHistoryDates();
      doNothing().when(dataInitializer).generateAchievementsForUsers();

      mockMvc.perform(post("/api/v2/data-initialization/initialize"))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(DATA_INITIALIZATION_SUCCESS_MESSAGE));

      verify(dataInitializer).initializeTasks();
      verify(dataInitializer).updateTaskStatuses();
      verify(dataInitializer).updateTaskHistoryDates();
      verify(dataInitializer).generateAchievementsForUsers();
      verifyNoMoreInteractions(dataInitializer);
    }
  }
}