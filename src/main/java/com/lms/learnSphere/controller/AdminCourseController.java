package com.lms.learnSphere.controller;

import com.lms.learnSphere.dto.*;
import com.lms.learnSphere.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
@EnableMethodSecurity
@RequiredArgsConstructor
public class AdminCourseController {
    private static final Logger logger = LoggerFactory.getLogger(AdminCourseController.class);
    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseCreateRequestDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseCreateRequestDto courseDto) {
        try {
            CourseDto updatedCourse = courseService.updateCourse(courseId, courseDto);
            return ResponseEntity.ok(updatedCourse);
        } catch (EntityNotFoundException e) {
            logger.warn("Update failed: Course not found with id {}", courseId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCoursesForAdmin() {
        List<CourseDto> courses = courseService.getAllCoursesForAdmin();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDto> getCourseByIdForAdmin(@PathVariable Long courseId) {
        try {
            CourseDto course = courseService.getCourseDetailsById(courseId);
            return ResponseEntity.ok(course);
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to get course: Course not found with id {}", courseId);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        try {
            courseService.deleteCourse(courseId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Delete failed: Course not found with id {}", courseId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/{courseId}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addLesson(
            @PathVariable Long courseId,
            @RequestPart("lesson") @Valid LessonCreateRequestDto lessonDto,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            LessonDto createdLesson = courseService.addLessonToCourse(courseId, lessonDto, videoFile);
            return new ResponseEntity<>(createdLesson, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to add lesson: Course not found with id {}", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to upload video for lesson in course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload video: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{courseId}/lessons/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @RequestPart("lesson") @Valid LessonCreateRequestDto lessonDto,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            LessonDto updatedLesson = courseService.updateLesson(courseId, lessonId, lessonDto, videoFile);
            return ResponseEntity.ok(updatedLesson);
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to update lesson: Resource not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update lesson: Bad request - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to upload video during lesson update for course {}, lesson {}: {}", courseId, lessonId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload video: " + e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId) {
        try {
            courseService.deleteLesson(courseId, lessonId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to delete lesson: Resource not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete lesson: Bad request - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{courseId}/lessons/{lessonId}")
    public ResponseEntity<?> getLessonById(@PathVariable Long courseId, @PathVariable Long lessonId) {
        try {
            LessonDto lesson = courseService.getLessonById(courseId, lessonId);
            return ResponseEntity.ok(lesson);
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to get lesson: Resource not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to get lesson: Bad request - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}