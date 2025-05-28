package com.lms.learnSphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class LessonCreateRequestDto {
    @NotBlank(message = "Lesson title cannot be blank") private String title;
    private String description;
    private Integer durationMinutes;
    @NotNull(message = "Order index cannot be null") private Integer orderIndex;
}
