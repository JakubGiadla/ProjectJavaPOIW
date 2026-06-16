package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

/**
 * Kontroler zarządzający listą zadań (Tasks) w ramach konkretnego wydarzenia.
 * Umożliwia pobieranie, dodawanie, oznaczanie oraz przypisywanie zadań uczestnikom.
 */
@RestController
@RequestMapping("/api/events/{eventId}/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    /**
     * GET /api/events/{eventId}/tasks
     * Pobiera listę wszystkich zadań przypisanych do danego wydarzenia.
     */
    @GetMapping
    public ResponseEntity<?> getTasks(@PathVariable Long eventId, Principal principal) {
        return ResponseEntity.ok(taskService.getTasksByEvent(eventId, principal.getName()));
    }

    /**
     * POST /api/events/{eventId}/tasks
     * Dodaje nowe zadanie do listy zadań wydarzenia.
     */
    @PostMapping
    public ResponseEntity<Task> addTask(@PathVariable Long eventId, @RequestBody Task task, Principal principal) {
        return ResponseEntity.ok(taskService.addTask(task, eventId, principal.getName()));
    }

    /**
     * PUT /api/events/{eventId}/tasks/{taskId}/complete
     * Oznacza zadanie jako wykonane.
     */
    @PutMapping("/{taskId}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable Long taskId, Principal principal) {
        taskService.completeTask(taskId, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Zadanie wykonane!"));
    }

    /**
     * PUT /api/events/{eventId}/tasks/{taskId}/assign
     * Przypisuje wykonawcę do zadania na podstawie jego adresu e-mail.
     */
    @PutMapping("/{taskId}/assign")
    public ResponseEntity<Map<String, String>> assignTask(@PathVariable Long taskId, @RequestParam String email, Principal principal) {
        taskService.assignTask(taskId, email, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Zadanie przypisane!"));
    }
}