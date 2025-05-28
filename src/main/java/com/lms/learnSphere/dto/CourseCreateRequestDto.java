package com.lms.learnSphere.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseCreateRequestDto {
    @NotBlank(message = "Title cannot be blank") private String title;
    private String description;
    private BigDecimal price;
    private String coverImageUrl;
    private String publishedStatus = "DRAFT";
}
