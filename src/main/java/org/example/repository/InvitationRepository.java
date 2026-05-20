package org.example.repository;

import org.example.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);

    // Znajdź wszystkie zaproszenia dla wydarzenia
    List<Invitation> findByEventId(Long eventId);
}