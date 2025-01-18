package com.example.todolist.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ResponseTaskDto {
    private Long id;

    private String title;

    private String description;

    private String createAt;

    private String dueDate;

    private boolean isComplete;

    private boolean isShared;

    public Boolean getIsShared() {
        return this.isShared;
    }

    public void setIsShared(Boolean isShared) {
        this.isShared = isShared;
    }
}
