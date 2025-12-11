package com.testProjects.todolist.controllers;

import com.testProjects.todolist.models.Task;
import com.testProjects.todolist.models.Priority;
import com.testProjects.todolist.models.User;
import com.testProjects.todolist.repositories.UserRepository;
import com.testProjects.todolist.services.Impl.TaskServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class TaskController {

    private final TaskServiceImpl taskService;
    private final UserRepository userRepository;

    @Autowired
    public TaskController(TaskServiceImpl taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public String getTaskById(@PathVariable Long id, Model model) throws Throwable {
        Task task = (Task) taskService.findTaskById(id);
        model.addAttribute("task", task);
        return "details";
    }

    @GetMapping("/tasks/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new Task());
        // 👇 this is what Thymeleaf needs for the priority dropdown
        model.addAttribute("priorityValues", Priority.values());
        return "create";
    }

    @GetMapping()
    public String getALlTasksForCurrentUser(
            @RequestParam(value = "q", required = false) String query,
            Model model) {

        List<Task> tasks = taskService.getTasksForCurrentUser();

        if (query != null && !query.isBlank()) {
            String qLower = query.toLowerCase();
            tasks = tasks.stream()
                    .filter(t ->
                            (t.getTitle() != null && t.getTitle().toLowerCase().contains(qLower)) ||
                            (t.getDescription() != null && t.getDescription().toLowerCase().contains(qLower)) ||
                            (t.getId() != null && String.valueOf(t.getId()).contains(query))
                    )
                    .toList();
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("q", query); // keep search term in the box
        return "list";
    }


    @PostMapping
    public String createTask(@ModelAttribute("task") Task task) {

        // current logged-in username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User user = userRepository.findByUsername(currentPrincipalName).orElse(null);
        assert user != null;

        // if Task has a user field, you can link it here:
        // task.setUser(user);

        taskService.saveTask(task);

        // redirect to details page of the created task
        return "redirect:/";
    }

    private User getCurrentUser() {
        String username = ((UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
        User user = new User();
        user.setUsername(username);
        return user;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) throws Throwable {
        Task task = (Task) taskService.findTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("priorityValues", Priority.values());
        return "edit";
    }

    @PostMapping("/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute("task") Task taskDetails) throws Throwable {

        Task task = (Task) taskService.findTaskById(id);
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setPriority(taskDetails.getPriority()); // keep priority on update
        taskService.saveTask(task);
        return "redirect:/";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/";
    }
}