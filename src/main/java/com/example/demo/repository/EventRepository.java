package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Dodaj ten import
import org.springframework.data.repository.query.Param; // Dodaj ten import
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    Optional<Event> findByJoinCode(String joinCode);

    // Pobiera wydarzenia, gdzie user jest organizatorem LUB uczestnikiem
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN e.attendees a WHERE e.organizer = :user OR a.user = :user")
    List<Event> findAllByOrganizerOrAttendee(@Param("user") User user);
}