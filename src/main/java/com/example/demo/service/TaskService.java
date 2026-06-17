package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       EventRepository eventRepository, AttendeeRepository attendeeRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
    }

    private Event getVerifiedEvent(Long eventId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));

        boolean isOrganizer = event.getOrganizer() != null && event.getOrganizer().getId().equals(user.getId());        boolean isParticipant = attendeeRepository.existsByEventAndUser(event, user);

        if (!isOrganizer && !isParticipant) {
            throw new RuntimeException("Brak uprawnień do tego wydarzenia");
        }
        return event;
    }

    public List<Task> getTasksByEvent(Long eventId, String email) {
        // Najpierw sprawdzamy dostęp, potem pobieramy zadania
        getVerifiedEvent(eventId, email);
        return taskRepository.findByEventId(eventId);
    }

    public Task addTask(Task task, Long eventId, String email) {
        Event event = getVerifiedEvent(eventId, email);
        task.setEvent(event);

        if (event.getTasks() == null) {
            event.setTasks(new java.util.ArrayList<>());
        }
        event.getTasks().add(task);

        return taskRepository.save(task);
    }

    public void completeTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Zadanie nie istnieje"));

        // Sprawdzamy dostęp do eventu, w którym jest to zadanie
        getVerifiedEvent(task.getEvent().getId(), email);

        task.setCompleted(true);
        taskRepository.save(task);
    }

    public void assignTask(Long taskId, String targetEmail, String actorEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Zadanie nie istnieje"));

        getVerifiedEvent(task.getEvent().getId(), actorEmail);

        User assignee = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("Użytkownik docelowy nie istnieje"));

        task.setAssignee(assignee);
        taskRepository.save(task);
    }

    public void unassignTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Zadanie nie istnieje"));

        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        Event event = task.getEvent();
        boolean isOrganizer = event.getOrganizer() != null && event.getOrganizer().getId().equals(actor.getId());
        boolean isCurrentAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(actor.getId());

        if (!isOrganizer && !isCurrentAssignee) {
            throw new RuntimeException("Tylko przypisana osoba lub organizator może anulować przypisanie!");
        }

        task.setAssignee(null);
        taskRepository.save(task);
    }

    // 3.  NOWA METODA: CAŁKOWITE USUNIĘCIE ZADANIA Z BAZY
    // Blokada: Tylko przypisana osoba lub Organizator wyjazdu może usunąć to zadanie!
    public void deleteTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Zadanie nie istnieje"));

        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        Event event = task.getEvent();
        boolean isOrganizer = event.getOrganizer() != null && event.getOrganizer().getId().equals(actor.getId());
        boolean isCurrentAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(actor.getId());

        if (!isOrganizer && !isCurrentAssignee) {
            throw new RuntimeException("Brak uprawnień. Tylko wykonawca zadania lub organizator wyjazdu może je usunąć!");
        }

        taskRepository.delete(task);
    }
}