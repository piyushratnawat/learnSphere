package com.lms.learnSphere.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseDto {
    private Long id;
    @NotBlank(message = "Course title cannot be blank") private String title;
    private String description;
    private BigDecimal price;
    private String coverImageUrl;
    private String publishedStatus;
    private List<LessonDto> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
