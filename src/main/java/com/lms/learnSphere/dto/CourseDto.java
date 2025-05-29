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
@Schema(description = "Data Transfer Object for Course details")
public class CourseDto {
    @Schema(description = "Unique identifier of the course")
    private Long id;

    @NotBlank(message = "Course title cannot be blank")
    @Size(max = 255, message = "Course title cannot exceed 255 characters")
    @Schema(description = "Title of the course", example = "Advanced Java Programming", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Detailed description of the course", example = "Learn advanced concepts in Java including concurrency, streams, and more.")
    private String description;

    @Schema(description = "Price of the course", example = "99.99")
    private BigDecimal price;

    @Schema(description = "URL for the course cover image", example = "https://example.com/course_cover.jpg")
    private String coverImageUrl;

    @Schema(description = "Publication status of the course (e.g., DRAFT, PUBLISHED, ARCHIVED)", example = "PUBLISHED")
    private String publishedStatus;

    @Schema(description = "List of lessons included in the course")
    private List<LessonDto> lessons;

    @Schema(description = "Timestamp of when the course was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the course")
    private LocalDateTime updatedAt;
}