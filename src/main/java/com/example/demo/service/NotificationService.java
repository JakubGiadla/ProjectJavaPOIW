package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // 1. To jest metoda, która naprawi błąd "cannot find symbol method findAllByUser"
    public List<Notification> findAllByUser(String email) {
        return notificationRepository.findByUserEmail(email);
    }

    // 2. To jest metoda, która naprawi błąd "cannot find symbol method markAsRead"
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }


    public void markAllAsRead(String email) {
        List<Notification> notifications = notificationRepository.findByUserEmail(email);
        for (Notification n : notifications) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}