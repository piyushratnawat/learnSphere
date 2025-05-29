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
@Schema(description = "Request DTO for creating or updating a course")
public class CourseCreateRequestDto {
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255)
    @Schema(description = "Title of the course", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Detailed description of the course")
    private String description;

    @Schema(description = "Price of the course")
    private BigDecimal price;

    @Schema(description = "URL for the course cover image")
    private String coverImageUrl;

    @Schema(description = "Publication status (e.g., DRAFT, PUBLISHED)", defaultValue = "DRAFT")
    private String publishedStatus = "DRAFT";
}