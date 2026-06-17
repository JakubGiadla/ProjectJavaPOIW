package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.model.Task;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailsDTO {
    private Long id;
    private String title;
    private LocalDateTime date;
    private String location;
    private String type;
    private String joinCode;
    private boolean isClosed;
    private String organizerName;
    private List<AttendeeDTO> attendees; // Tutaj trafią dołączone osoby!
    private List<Task> tasks;
}
