package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping
    public ResponseEntity<?> getTasks(@PathVariable Long eventId) {
        return ResponseEntity.ok(taskService.getTasksByEvent(eventId));
    }

    @PostMapping
    public ResponseEntity<Task> addTask(@PathVariable Long eventId, @RequestBody Task task) {
        task.setEventId(eventId);
        return ResponseEntity.ok(taskService.addTask(task));
    }

    @PutMapping("/{taskId}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable Long taskId) {
        taskService.completeTask(taskId);
        return ResponseEntity.ok(Map.of("message", "Zadanie wykonane!"));
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<Map<String, String>> assignTask(@PathVariable Long taskId, @RequestParam String email) {
        taskService.assignTask(taskId, email);
        return ResponseEntity.ok(Map.of("message", "Zadanie przypisane!"));
    }
}