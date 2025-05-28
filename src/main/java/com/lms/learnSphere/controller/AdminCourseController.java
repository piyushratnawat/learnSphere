package com.lms.learnSphere.controller;

import com.lms.learnSphere.dto.*;
import com.lms.learnSphere.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCourseController {
    private final CourseService courseService;
    @PostMapping public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseCreateRequestDto courseDto) { /* ... */ return null; }
    @PutMapping("/{courseId}") public ResponseEntity<CourseDto> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseCreateRequestDto courseDto) { /* ... */ return null; }
    @GetMapping public ResponseEntity<List<CourseDto>> getAllCoursesForAdmin() { /* ... */ return null; }
    @GetMapping("/{courseId}") public ResponseEntity<CourseDto> getCourseByIdForAdmin(@PathVariable Long courseId) { /* ... */ return null; }
    @DeleteMapping("/{courseId}") public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) { /* ... */ return null; }
    @PostMapping(value = "/{courseId}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonDto> addLesson(@PathVariable Long courseId, @RequestPart("lesson") @Valid LessonCreateRequestDto lessonDto, @RequestPart("videoFile") MultipartFile videoFile) throws IOException { /* ... */ return null; }
    @PutMapping(value = "/{courseId}/lessons/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long courseId, @PathVariable Long lessonId, @RequestPart("lesson") @Valid LessonCreateRequestDto lessonDto, @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) throws IOException { /* ... */ return null; }
    @DeleteMapping("/{courseId}/lessons/{lessonId}") public ResponseEntity<Void> deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId) { /* ... */ return null; }
    @GetMapping("/{courseId}/lessons/{lessonId}") public ResponseEntity<LessonDto> getLessonById(@PathVariable Long courseId, @PathVariable Long lessonId) { /* ... */ return null; }
}
