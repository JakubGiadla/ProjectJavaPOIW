package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Spring sam wie, że ma szukać po polu 'organizer' w encji Event
    List<Event> findByOrganizer(User organizer);

    // Spring sam wie, że ma szukać po polu 'joinCode'
    Optional<Event> findByJoinCode(String joinCode);
}