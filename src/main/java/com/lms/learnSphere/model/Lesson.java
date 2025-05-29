package com.lms.learnSphere.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Lesson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Lob @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false) private String lessonType = "VIDEO";
    private String contentUrl;
    private Integer durationMinutes;
    @Column(nullable = false) private Integer orderIndex = 0;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false) private Course course;
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(nullable = false) private LocalDateTime updatedAt;

    public Lesson(String title, String description, String contentUrl, Integer durationMinutes, Integer orderIndex, Course course) {
        this.title = title;
        this.description = description;
        this.contentUrl = contentUrl;
        this.durationMinutes = durationMinutes;
        this.orderIndex = orderIndex;
        this.course = course;
    }
}