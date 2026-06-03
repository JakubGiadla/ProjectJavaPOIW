package com.example.demo.service;

import com.example.demo.model.Task;
import com.example.demo.model.User;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getTasksByEvent(Long eventId) {
        return taskRepository.findByEventId(eventId);
    }

    public Task addTask(Task task) {
        return taskRepository.save(task);
    }

    public void completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setCompleted(true);
        taskRepository.save(task);
    }

    public void assignTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();
        task.setAssignee(user);
        taskRepository.save(task);
    }
}