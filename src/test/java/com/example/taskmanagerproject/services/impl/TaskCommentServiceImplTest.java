package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.TASK_COMMENT_FOUND_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.utils.factories.TaskCommentFactory;
import com.example.taskmanagerproject.utils.mappers.TaskCommentMapper;
import com.example.taskmanagerproject.utils.validators.TaskCommentValidator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TaskCommentServiceImplTest {

  @Mock
  private TaskCommentRepository taskCommentRepository;

  @Mock
  private TaskCommentMapper taskCommentMapper;

  @Mock
  private TaskCommentFactory taskCommentFactory;

  @Mock
  private TaskCommentValidator taskCommentValidator;

  @InjectMocks
  private TaskCommentServiceImpl taskCommentService;

  private Pageable pageable;
  private final Long taskId = 1L;
  private final Long commentId = 1L;

  private final String slug = "test-slug";
  private final String message = "Test Comment";

  private TaskComment taskComment;
  private TaskCommentDto taskCommentDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    taskComment = mock(TaskComment.class);
    taskCommentDto = mock(TaskCommentDto.class);
    pageable = PageRequest.of(0, 10);

    when(taskCommentDto.message()).thenReturn(message);
    when(taskCommentMapper.toDto(taskComment)).thenReturn(taskCommentDto);
    when(taskCommentFactory.createTaskCommentFromDto(taskCommentDto)).thenReturn(taskComment);
  }

  @Test
  void createComment_shouldCreateAndReturnCommentDto() {
    doNothing().when(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    when(taskCommentRepository.save(taskComment)).thenReturn(taskComment);
    TaskCommentDto result = taskCommentService.createComment(taskCommentDto);
    assertNotNull(result);
    assertEquals(taskCommentDto, result);
    verify(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    verify(taskCommentFactory).createTaskCommentFromDto(taskCommentDto);
    verify(taskCommentRepository).save(taskComment);
    verify(taskCommentMapper).toDto(taskComment);
  }

  @Test
  void createComment_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    doThrow(new IllegalArgumentException("Invalid comment")).when(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> taskCommentService.createComment(taskCommentDto));
    assertEquals("Invalid comment", exception.getMessage());
    verify(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
  }

  @Test
  void createComment_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    doNothing().when(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(taskCommentRepository).save(taskComment);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> taskCommentService.createComment(taskCommentDto));
    assertEquals("Constraint violation", exception.getMessage());
    verify(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    verify(taskCommentFactory).createTaskCommentFromDto(taskCommentDto);
    verify(taskCommentRepository).save(taskComment);
  }

  @Test
  void createComment_shouldHandleLongMessage() {
    when(taskCommentDto.message()).thenReturn("A".repeat(1000));
    doNothing().when(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    when(taskCommentRepository.save(taskComment)).thenReturn(taskComment);
    TaskCommentDto result = taskCommentService.createComment(taskCommentDto);
    assertNotNull(result);
    assertEquals(taskCommentDto, result);
    verify(taskCommentValidator).validateTaskCommentDto(taskCommentDto);
    verify(taskCommentFactory).createTaskCommentFromDto(taskCommentDto);
    verify(taskCommentRepository).save(taskComment);
    verify(taskCommentMapper).toDto(taskComment);
  }

  @Test
  void updateTaskComment_shouldUpdateAndReturnCommentDto() {
    when(taskCommentRepository.findById(commentId)).thenReturn(Optional.of(taskComment));
    when(taskCommentRepository.save(taskComment)).thenReturn(taskComment);
    TaskCommentDto result = taskCommentService.updateTaskComment(taskCommentDto, commentId);
    assertNotNull(result);
    assertEquals(taskCommentDto, result);
    verify(taskCommentRepository).findById(commentId);
    verify(taskComment).setMessage(message);
    verify(taskCommentRepository).save(taskComment);
    verify(taskCommentMapper).toDto(taskComment);
  }

  @Test
  void updateTaskComment_shouldThrowResourceNotFoundExceptionWhenCommentNotFound() {
    when(taskCommentRepository.findById(commentId)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskCommentService.updateTaskComment(taskCommentDto, commentId));
    assertEquals(TASK_COMMENT_FOUND_WITH_ID + commentId, exception.getMessage());
    verify(taskCommentRepository).findById(commentId);
  }

  @Test
  void updateTaskComment_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    when(taskCommentRepository.findById(commentId)).thenReturn(Optional.of(taskComment));
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(taskCommentRepository).save(taskComment);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> taskCommentService.updateTaskComment(taskCommentDto, commentId));
    assertEquals("Constraint violation", exception.getMessage());
    verify(taskCommentRepository).findById(commentId);
    verify(taskComment).setMessage(message);
    verify(taskCommentRepository).save(taskComment);
  }

  @Test
  void updateTaskComment_shouldHandleLongMessage() {
    when(taskCommentDto.message()).thenReturn("A".repeat(1000));
    when(taskCommentRepository.findById(commentId)).thenReturn(Optional.of(taskComment));
    when(taskCommentRepository.save(taskComment)).thenReturn(taskComment);
    TaskCommentDto result = taskCommentService.updateTaskComment(taskCommentDto, commentId);
    assertNotNull(result);
    assertEquals(taskCommentDto, result);
    verify(taskCommentRepository).findById(commentId);
    verify(taskComment).setMessage("A".repeat(1000));
    verify(taskCommentRepository).save(taskComment);
    verify(taskCommentMapper).toDto(taskComment);
  }

  @Test
  void deleteTaskComment_shouldDeleteCommentWhenExists() {
    when(taskCommentRepository.findById(commentId)).thenReturn(Optional.of(taskComment));
    doNothing().when(taskCommentRepository).deleteById(commentId);
    taskCommentService.deleteTaskComment(commentId);
    verify(taskCommentRepository).findById(commentId);
    verify(taskCommentRepository).deleteById(commentId);
  }

  @Test
  void deleteTaskComment_shouldThrowResourceNotFoundExceptionWhenCommentNotFound() {
    when(taskCommentRepository.findById(commentId)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskCommentService.deleteTaskComment(commentId));
    assertEquals(TASK_COMMENT_FOUND_WITH_ID + commentId, exception.getMessage());
    verify(taskCommentRepository).findById(commentId);
  }

  @Test
  void deleteTaskComment_shouldHandleZeroId() {
    when(taskCommentRepository.findById(0L)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskCommentService.deleteTaskComment(0L));
    assertEquals(TASK_COMMENT_FOUND_WITH_ID + 0L, exception.getMessage());
    verify(taskCommentRepository).findById(0L);
  }

  @Test
  void getCommentsByTaskSlug_shouldReturnPagedCommentsWhenCommentsExist() {
    Page<TaskComment> commentPage = new PageImpl<>(List.of(taskComment));
    when(taskCommentRepository.findByTaskSlug(slug, pageable)).thenReturn(commentPage);
    Page<TaskCommentDto> result = taskCommentService.getCommentsByTaskSlug(slug, pageable);
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(taskCommentDto, result.getContent().get(0));
    verify(taskCommentRepository).findByTaskSlug(slug, pageable);
    verify(taskCommentMapper).toDto(taskComment);
  }

  @Test
  void getCommentsByTaskSlug_shouldReturnEmptyPageWhenNoComments() {
    Page<TaskComment> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskCommentRepository.findByTaskSlug(slug, pageable)).thenReturn(emptyPage);
    Page<TaskCommentDto> result = taskCommentService.getCommentsByTaskSlug(slug, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskCommentRepository).findByTaskSlug(slug, pageable);
  }

  @Test
  void getCommentsByTaskSlug_shouldHandleEmptySlug() {
    Page<TaskComment> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskCommentRepository.findByTaskSlug("", pageable)).thenReturn(emptyPage);
    Page<TaskCommentDto> result = taskCommentService.getCommentsByTaskSlug("", pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskCommentRepository).findByTaskSlug("", pageable);
  }

  @Test
  void getCommentsByTaskSlug_shouldHandleSpecialCharactersInSlug() {
    String specialSlug = "slug#2025!";
    Page<TaskComment> commentPage = new PageImpl<>(List.of(taskComment));
    when(taskCommentRepository.findByTaskSlug(specialSlug, pageable)).thenReturn(commentPage);
    Page<TaskCommentDto> result = taskCommentService.getCommentsByTaskSlug(specialSlug, pageable);
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(taskCommentDto, result.getContent().get(0));
    verify(taskCommentRepository).findByTaskSlug(specialSlug, pageable);
    verify(taskCommentMapper).toDto(taskComment);
  }

  @Test
  void getTaskIdBySlug_shouldReturnTaskIdWhenSlugExists() {
    when(taskCommentRepository.findDistinctTaskIdBySlug(slug)).thenReturn(taskId);
    Long result = taskCommentService.getTaskIdBySlug(slug);
    assertEquals(taskId, result);
    verify(taskCommentRepository).findDistinctTaskIdBySlug(slug);
  }

  @Test
  void getTaskIdBySlug_shouldReturnNullWhenSlugNotFound() {
    when(taskCommentRepository.findDistinctTaskIdBySlug(slug)).thenReturn(null);
    Long result = taskCommentService.getTaskIdBySlug(slug);
    assertNull(result);
    verify(taskCommentRepository).findDistinctTaskIdBySlug(slug);
  }

  @Test
  void getTaskIdBySlug_shouldHandleEmptySlug() {
    when(taskCommentRepository.findDistinctTaskIdBySlug("")).thenReturn(null);
    Long result = taskCommentService.getTaskIdBySlug("");
    assertNull(result);
    verify(taskCommentRepository).findDistinctTaskIdBySlug("");
  }

  @Test
  void getTaskIdBySlug_shouldHandleSpecialCharactersInSlug() {
    String specialSlug = "slug#2025!";
    when(taskCommentRepository.findDistinctTaskIdBySlug(specialSlug)).thenReturn(taskId);
    Long result = taskCommentService.getTaskIdBySlug(specialSlug);
    assertEquals(taskId, result);
    verify(taskCommentRepository).findDistinctTaskIdBySlug(specialSlug);
  }

  @Test
  void getTaskIdByTaskCommentId_shouldReturnTaskIdWhenCommentExists() {
    when(taskCommentRepository.findDistinctTaskIdById(commentId)).thenReturn(taskId);
    Long result = taskCommentService.getTaskIdByTaskCommentId(commentId);
    assertEquals(taskId, result);
    verify(taskCommentRepository).findDistinctTaskIdById(commentId);
  }

  @Test
  void getTaskIdByTaskCommentId_shouldReturnNullWhenCommentNotFound() {
    when(taskCommentRepository.findDistinctTaskIdById(commentId)).thenReturn(null);
    Long result = taskCommentService.getTaskIdByTaskCommentId(commentId);
    assertNull(result);
    verify(taskCommentRepository).findDistinctTaskIdById(commentId);
  }

  @Test
  void getTaskIdByTaskCommentId_shouldHandleZeroId() {
    when(taskCommentRepository.findDistinctTaskIdById(0L)).thenReturn(null);
    Long result = taskCommentService.getTaskIdByTaskCommentId(0L);
    assertNull(result);
    verify(taskCommentRepository).findDistinctTaskIdById(0L);
  }
}