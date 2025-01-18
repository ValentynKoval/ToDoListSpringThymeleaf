package com.example.todolist.controllers;

import com.example.todolist.dto.TaskDto;
import com.example.todolist.mappers.ResponseTaskMapper;
import com.example.todolist.mappers.TaskMapper;
import com.example.todolist.models.Task;
import com.example.todolist.models.User;
import com.example.todolist.services.TaskService;
import com.example.todolist.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;
    private final ResponseTaskMapper responseTaskMapper;
    private final TaskMapper taskMapper;

    @GetMapping("/create")
    public String showCreateTaskForm(Model model) {
        model.addAttribute("taskDto", new TaskDto());
        return "task-create"; // Thymeleaf template for task creation
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("taskDto") TaskDto taskDto,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            return "task-create";
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        Task task = taskService.createTask(taskDto, user);
        return String.format("redirect:/tasks/%s?created", task.getId());
    }

    @GetMapping("/{id}")
    public String getTaskById(@PathVariable long id, Model model) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "error/404";
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String userEmail = task.getUser().getEmail();
        System.out.println(email + " " + userEmail);
        if (!email.equals(task.getUser().getEmail()) && !task.isShared()) {
            return "error/403";
        }

        model.addAttribute("task", responseTaskMapper.toDto(task));
        return "task-detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditTaskForm(@PathVariable long id, Model model) {
        Task task = taskService.getTaskById(id);

        if (task == null) {
            return "error/404";
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!email.equals(task.getUser().getEmail())) {
            return "error/403";
        }

        model.addAttribute("taskDto", taskMapper.toDto(task));
        model.addAttribute("taskId", id);
        return "task-edit"; // Thymeleaf template for task editing
    }

    @PostMapping("/{id}/edit")
    public String editTask(@PathVariable long id,
                           @Valid @ModelAttribute("taskDto") TaskDto taskDto,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("taskId", id);
            return "task-edit";
        }

        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "error/404";
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!email.equals(task.getUser().getEmail())) {
            return "error/403";
        }
        Task editedTask = taskMapper.toEntity(taskDto);
        editedTask.setId(task.getId());
        editedTask.setCreateAt(task.getCreateAt());
        editedTask.setUser(task.getUser());

        taskService.updateTask(editedTask);
        return "redirect:/tasks?updated";
    }

    @GetMapping("/{id}/delete")
    public String deleteTaskById(@PathVariable long id) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "error/404";
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!email.equals(task.getUser().getEmail())) {
            return "error/403";
        }
        taskService.deleteTaskById(id);
        return "redirect:/tasks?deleted";
    }

    @GetMapping()
    public String getAllTasks(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("tasks", taskService.getAllTasks(email));
        return "task-list";
    }

    @PostMapping("/{id}/share")
    @ResponseBody
    public String shareTask(@PathVariable long id,
                            HttpServletRequest request) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!email.equals(task.getUser().getEmail())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        task.setShared(true);
        taskService.updateTask(task);

//        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
//        String sharedLink = String.format("%s/api/tasks/%d", baseUrl, task.getId());
//
//        model.addAttribute("sharedLink", sharedLink);
//        return "task-shared";
        return request.getScheme() + "://" + request.getServerName() +
                (request.getServerPort() != 80 ? ":" + request.getServerPort() : "") + "/tasks/" + task.getId();
    }
}
