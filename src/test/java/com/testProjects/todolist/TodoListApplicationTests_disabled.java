package com.testProjects.todolist;

import com.testProjects.todolist.models.Priority;
import com.testProjects.todolist.models.Task;
import com.testProjects.todolist.models.User;
import com.testProjects.todolist.repositories.TaskRepository;
import com.testProjects.todolist.repositories.UserRepository;
import com.testProjects.todolist.services.Impl.TaskServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simple unit tests for TaskServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void saveTask_shouldSetCurrentUserAndSave() {
        // --- set up SecurityContext ONLY for this test ---
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
        // -------------------------------------------------

        // arrange
        Task task = new Task();
        task.setTitle("Test task");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);

        // act
        Task result = taskService.saveTask(task);

        // assert
        assertNotNull(result);
        assertEquals("Test task", result.getTitle());
        assertSame(user, result.getUser());            // user is set on task
        verify(userRepository).findByUsername("admin");
        verify(taskRepository).save(task);
    }

    @Test
    void getTasksByPriority_shouldDelegateToRepository() {
        // no need for SecurityContext in this test

        Task t = new Task();
        t.setId(1L);
        t.setPriority(Priority.HIGH);
        t.setDeadline(LocalDate.now().plusDays(1));

        when(taskRepository.findByPriority(Priority.HIGH))
                .thenReturn(List.of(t));

        List<Task> result = taskService.getTasksByPriority(Priority.HIGH);

        assertEquals(1, result.size());
        assertEquals(Priority.HIGH, result.get(0).getPriority());
        verify(taskRepository).findByPriority(Priority.HIGH);
    }

        @Test
    void findTaskById_shouldReturnTask_whenExists() {
        Task task = new Task();
        task.setId(42L);
        task.setTitle("Find me");

        when(taskRepository.findById(42L)).thenReturn(Optional.of(task));

        Task result = taskService.findTaskById(42L);

        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals("Find me", result.getTitle());
        verify(taskRepository).findById(42L);
    }

    @Test
    void deleteTask_shouldDeleteWhenUserOwnsTask() {
        // --- fake logged-in user "admin" ---
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
        // -----------------------------------

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");

        Task task = new Task();
        task.setId(99L);
        task.setUser(user);  // owner is the same instance

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(taskRepository.findById(99L)).thenReturn(Optional.of(task));

        // act
        taskService.deleteTask(99L);

        // assert: deleteById must be called
        verify(userRepository).findByUsername("admin");
        verify(taskRepository).findById(99L);
        verify(taskRepository).deleteById(99L);
    }

}