package org.example.eventsplitapplication.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Data;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String password;
    private boolean enabled = true;
    private String email;
    private String name;
    private String avatar;
    private String provider;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
