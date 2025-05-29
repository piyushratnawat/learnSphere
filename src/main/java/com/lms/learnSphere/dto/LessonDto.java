package com.lms.learnSphere.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "Data Transfer Object for Lesson details")
public class LessonDto {
    @Schema(description = "Unique identifier of the lesson")
    private Long id;

    @NotBlank(message = "Lesson title cannot be blank")
    @Size(max = 255, message = "Lesson title cannot exceed 255 characters")
    @Schema(description = "Title of the lesson", example = "Introduction to Variables", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Detailed description of the lesson", example = "This lesson covers the basics of variable declaration and usage.")
    private String description;

    @Schema(description = "Type of the lesson (e.g., VIDEO, TEXT, QUIZ)", defaultValue = "VIDEO", example = "VIDEO")
    private String lessonType = "VIDEO";

    @Schema(description = "URL for the lesson content (e.g., S3 pre-signed URL for video, or text content)", example = "https://s3.bucket/video.mp4")
    private String contentUrl;

    @Schema(description = "Duration of the lesson in minutes (primarily for videos)", example = "15")
    private Integer durationMinutes;

    @NotNull(message = "Order index cannot be null")
    @Schema(description = "Order of the lesson within the course", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderIndex;

    @Schema(description = "ID of the course this lesson belongs to (used for linking during creation/update)")
    private Long courseId;
}