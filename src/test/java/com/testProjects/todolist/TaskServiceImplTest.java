package com.testProjects.todolist;

import com.testProjects.todolist.models.Priority;
import com.testProjects.todolist.models.Task;
import com.testProjects.todolist.models.User;
import com.testProjects.todolist.repositories.TaskRepository;
import com.testProjects.todolist.repositories.UserRepository;
import com.testProjects.todolist.services.Impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TaskServiceImpl taskService;

    @BeforeEach
    void setUpSecurityContext() {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("admin");

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void saveTask_setsCurrentUser_andSaves() {
        Task task = new Task();
        task.setTitle("T1");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);

        Task saved = taskService.saveTask(task);

        assertNotNull(saved);
        assertSame(user, saved.getUser());
        verify(taskRepository).save(task);
    }

    @Test
    void saveTask_throwsIfUserNotFound() {
        Task task = new Task();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.saveTask(task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getTasksForCurrentUser_delegatesToRepo() {
        Task t1 = new Task(); t1.setId(10L);
        when(taskRepository.findByUserUsername("admin")).thenReturn(List.of(t1));

        List<Task> res = taskService.getTasksForCurrentUser();

        assertEquals(1, res.size());
        assertEquals(10L, res.get(0).getId());
        verify(taskRepository).findByUserUsername("admin");
    }

    @Test
    void findTaskById_returnsTaskIfPresent() {
        Task t = new Task();
        t.setId(7L);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(t));

        Task res = taskService.findTaskById(7L);

        assertNotNull(res);
        assertEquals(7L, res.getId());
    }

    @Test
    void findTaskById_returnsNullIfMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        Task res = taskService.findTaskById(99L);

        assertNull(res);
    }

    @Test
    void deleteTask_deletesOnlyIfOwnerMatches() {
        User owner = new User(); owner.setId(1L); owner.setUsername("admin");

        Task task = new Task();
        task.setId(5L);
        task.setUser(owner);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(owner));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        taskService.deleteTask(5L);

        verify(taskRepository).deleteById(5L);
    }

    @Test
    void deleteTask_doesNotDeleteIfNotOwner() {
        User current = new User(); current.setId(1L); current.setUsername("admin");
        User other = new User(); other.setId(2L); other.setUsername("x");

        Task task = new Task();
        task.setId(5L);
        task.setUser(other);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(current));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        taskService.deleteTask(5L);

        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void getTasksByPriority_delegatesToRepo() {
        Task t = new Task();
        t.setPriority(Priority.HIGH);

        when(taskRepository.findByPriority(Priority.HIGH)).thenReturn(List.of(t));

        List<Task> res = taskService.getTasksByPriority(Priority.HIGH);

        assertEquals(1, res.size());
        assertEquals(Priority.HIGH, res.get(0).getPriority());
        verify(taskRepository).findByPriority(Priority.HIGH);
    }
}
