package com.lms.learnSphere.controller;

import com.lms.learnSphere.dto.CourseDto;
import com.lms.learnSphere.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses") // Public base path
@RequiredArgsConstructor
public class PublicCourseController {
    private static final Logger logger = LoggerFactory.getLogger(PublicCourseController.class);
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllPublishedCourses() {
        List<CourseDto> courses = courseService.getAllPublishedCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseDetails(@PathVariable Long courseId) {
        try {
            CourseDto course = courseService.getCourseDetailsById(courseId);
            // Additional check for public visibility if needed (e.g. course.getPublishedStatus())
            if (!"PUBLISHED".equals(course.getPublishedStatus())) {
                logger.warn("Attempt to access non-published course {} publicly", courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found or not published.");
            }
            return ResponseEntity.ok(course);
        } catch (EntityNotFoundException e) {
            logger.warn("Public access failed: Course not found with id {}", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}