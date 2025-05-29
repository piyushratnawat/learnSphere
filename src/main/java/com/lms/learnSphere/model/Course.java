package com.lms.learnSphere.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Lob @Column(columnDefinition = "TEXT") private String description;
    private BigDecimal price;
    private String coverImageUrl;
    @Column(nullable = false) private String publishedStatus = "DRAFT";
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lesson> lessons = new ArrayList<>();
    @CreatedDate @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(nullable = false) private LocalDateTime updatedAt;

    public Course(String title, String description, BigDecimal price, String coverImageUrl) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.coverImageUrl = coverImageUrl;
    }
}