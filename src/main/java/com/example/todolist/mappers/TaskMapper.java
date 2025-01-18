package com.example.todolist.mappers;

import com.example.todolist.dto.TaskDto;
import com.example.todolist.models.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TaskMapper implements EntityMapper<Task, TaskDto> {
    @Override
    public Task toEntity(TaskDto dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(toLocalDateTime(dto.getDueDate()));
        task.setComplete(dto.getIsComplete());
        task.setShared(dto.getIsShared());
        return task;
    }

    @Override
    public TaskDto toDto(Task entity) {
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle(entity.getTitle());
        taskDto.setDescription(entity.getDescription());
        taskDto.setDueDate(toString(entity.getDueDate()));
        taskDto.setIsComplete(entity.isComplete());
        taskDto.setIsShared(entity.isShared());
        return taskDto;
    }

    private LocalDateTime toLocalDateTime(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return LocalDateTime.parse(date, formatter);
    }

    private String toString(LocalDateTime date){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return date.format(formatter);
    }
}
