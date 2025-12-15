package com.testProjects.todolist.services.Impl;

import com.testProjects.todolist.models.Priority;
import com.testProjects.todolist.models.Task;
import com.testProjects.todolist.models.User;
import com.testProjects.todolist.repositories.TaskRepository;
import com.testProjects.todolist.repositories.UserRepository;
import com.testProjects.todolist.services.TaskService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    //@ spec_public
    private TaskRepository taskRepository;
    @Autowired
    //@ spec_public
    private UserRepository userRepository;

    //@ ensures \result != null;
    //@ ensures \result.size() >= 0;
    @Override
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * JML-style specification for saveTask:
     *  - requires: task is not null and a user with the current username exists
     *  - ensures: returned task is not null and is associated to that user
     */
    //@ requires task != null;
    //@ requires (\exists User u; userRepository.findByUsername(
    //@     SecurityContextHolder.getContext().getAuthentication().getName()
    //@ ).isPresent());
    //@ ensures \result != null;
    //@ ensures \result.getUser() != null;
    @Transactional
    @Override
    public Task saveTask(Task task) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        task.setUser(user);
        return taskRepository.save(task);
    }

    /**
     * Returns the tasks of the currently authenticated user.
     */
    //@ ensures \result != null;
    //@ ensures \result.size() >= 0;
    public List<Task> getTasksForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return taskRepository.findByUserUsername(username);
    }

    /**
     * JML-style spec:
     *  - requires: id is not null
     *  - ensures: result is either null or a task with that id
     */
    //@ requires id != null;
    //@ ensures (\result == null) || (\result.getId().equals(id));
    @Override
    public Task findTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    /**
     * JML-style spec:
     *  - requires: id is not null
     *  - requires: a task with that id exists AND belongs to the current user
     *  - ensures: after execution, that task is no longer present in the repository
     */
    //@ requires id != null;
    //@ requires taskRepository.findById(id).isPresent();
    //@ ensures !taskRepository.findById(id).isPresent();
    @Override
    public void deleteTask(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Task task = taskRepository.findById(id).orElseThrow();

        if (task.getUser() == user) {
            taskRepository.deleteById(id);
        }
    }

    /**
     * JML-style spec for filtering by priority.
     */
    //@ requires priority != null;
    //@ ensures \result != null;
    //@ ensures (\forall Task t; \result.contains(t); t.getPriority() == priority);
    @Override
    public List<Task> getTasksByPriority(Priority priority) {
        return taskRepository.findByPriority(priority);
    }

    // Other service methods for task operations
}