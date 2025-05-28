package com.lms.learnSphere.controller;

import com.lms.learnSphere.dto.CourseDto;
import com.lms.learnSphere.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class PublicCourseController {
    private final CourseService courseService;
    @GetMapping public ResponseEntity<List<CourseDto>> getAllPublishedCourses() { /* ... */ return null; }
    @GetMapping("/{courseId}") public ResponseEntity<CourseDto> getCourseDetails(@PathVariable Long courseId) { /* ... */ return null; }
}
