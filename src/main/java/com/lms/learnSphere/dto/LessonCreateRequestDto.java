package com.lms.learnSphere.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Request DTO for creating or updating a lesson")
public class LessonCreateRequestDto {
    @NotBlank(message = "Lesson title cannot be blank")
    @Size(max = 255)
    @Schema(description = "Title of the lesson", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Detailed description of the lesson")
    private String description;

    @Schema(description = "Duration of the lesson in minutes (primarily for videos)")
    private Integer durationMinutes;

    @NotNull(message = "Order index cannot be null")
    @Schema(description = "Order of the lesson within the course", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderIndex;
}