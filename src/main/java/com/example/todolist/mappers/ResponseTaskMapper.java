package com.example.todolist.mappers;

import com.example.todolist.dto.ResponseTaskDto;
import com.example.todolist.models.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ResponseTaskMapper implements EntityMapper<Task, ResponseTaskDto> {
    @Override
    public Task toEntity(ResponseTaskDto dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCreateAt(toLocalDateTime(dto.getCreateAt()));
        task.setDueDate(toLocalDateTime(dto.getDueDate()));
        task.setComplete(dto.isComplete());
        task.setShared(dto.getIsShared());
        return task;
    }

    @Override
    public ResponseTaskDto toDto(Task entity) {
        ResponseTaskDto dto = new ResponseTaskDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCreateAt(toString(entity.getCreateAt()));
        dto.setDueDate(toString(entity.getDueDate()));
        dto.setComplete(entity.isComplete());
        dto.setIsShared(entity.isShared());
        return dto;
    }

    private LocalDateTime toLocalDateTime(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(date, formatter);
    }

    private String toString(LocalDateTime date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return date.format(formatter);
    }
}
