package com.lms.learnSphere.service;

import com.lms.learnSphere.dto.*;
import com.lms.learnSphere.model.Course;
import com.lms.learnSphere.model.Lesson;
import com.lms.learnSphere.repository.CourseRepository;
import com.lms.learnSphere.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final S3Service s3Service;

    @Transactional
    public CourseDto createCourse(CourseCreateRequestDto createRequestDto) {
        Course course = new Course();
        course.setTitle(createRequestDto.getTitle());
        course.setDescription(createRequestDto.getDescription());
        course.setPrice(createRequestDto.getPrice());
        course.setCoverImageUrl(createRequestDto.getCoverImageUrl()); // Can be null
        course.setPublishedStatus(createRequestDto.getPublishedStatus() != null ? createRequestDto.getPublishedStatus() : "DRAFT");

        Course savedCourse = courseRepository.save(course);
        logger.info("Created course with ID: {}", savedCourse.getId());
        return mapToCourseDto(savedCourse, false); // Don't include lessons for create response
    }

    @Transactional
    public CourseDto updateCourse(Long courseId, CourseCreateRequestDto updateRequestDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        course.setTitle(updateRequestDto.getTitle());
        course.setDescription(updateRequestDto.getDescription());
        course.setPrice(updateRequestDto.getPrice());
        course.setCoverImageUrl(updateRequestDto.getCoverImageUrl());
        if (updateRequestDto.getPublishedStatus() != null) {
            course.setPublishedStatus(updateRequestDto.getPublishedStatus());
        }

        Course updatedCourse = courseRepository.save(course);
        logger.info("Updated course with ID: {}", updatedCourse.getId());
        return mapToCourseDto(updatedCourse, false); // Typically don't need lessons in update response
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findByIdWithLessons(courseId) // Fetch with lessons to delete S3 content
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        // Delete S3 content for each lesson
        for (Lesson lesson : course.getLessons()) {
            if (lesson.getContentUrl() != null && "VIDEO".equals(lesson.getLessonType())) {
                s3Service.deleteFile(lesson.getContentUrl());
            }
        }
        courseRepository.deleteById(courseId); // This will also delete lessons due to cascade
        logger.info("Deleted course with ID: {}", courseId);
    }

    @Transactional
    public LessonDto addLessonToCourse(Long courseId, LessonCreateRequestDto lessonRequestDto, MultipartFile videoFile) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonRequestDto.getTitle());
        lesson.setDescription(lessonRequestDto.getDescription());
        lesson.setDurationMinutes(lessonRequestDto.getDurationMinutes());
        lesson.setOrderIndex(lessonRequestDto.getOrderIndex());
        lesson.setCourse(course);
        lesson.setLessonType("VIDEO"); // Defaulting to VIDEO as per DTO

        // Save lesson first to get an ID for S3 path structure
        Lesson tempSavedLesson = lessonRepository.save(lesson);

        if (videoFile != null && !videoFile.isEmpty()) {
            String s3Key = s3Service.uploadFile(videoFile, String.valueOf(courseId), String.valueOf(tempSavedLesson.getId()));
            tempSavedLesson.setContentUrl(s3Key); // Store S3 key
            lesson = lessonRepository.save(tempSavedLesson); // Save again with S3 key
        } else {
            lesson = tempSavedLesson; // No file, use the initially saved lesson
        }

        logger.info("Added lesson with ID {} to course ID {}", lesson.getId(), courseId);
        return mapToLessonDtoWithPresignedUrl(lesson); // Return with presigned URL
    }

    @Transactional
    public LessonDto updateLesson(Long courseId, Long lessonId, LessonCreateRequestDto lessonRequestDto, MultipartFile videoFile) throws IOException {
        courseRepository.findById(courseId) // Ensure course exists
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Lesson with id " + lessonId + " does not belong to course with id " + courseId);
        }

        lesson.setTitle(lessonRequestDto.getTitle());
        lesson.setDescription(lessonRequestDto.getDescription());
        lesson.setDurationMinutes(lessonRequestDto.getDurationMinutes());
        lesson.setOrderIndex(lessonRequestDto.getOrderIndex());

        if (videoFile != null && !videoFile.isEmpty()) {
            // Delete old video from S3 if it exists and is being replaced
            if (lesson.getContentUrl() != null && !lesson.getContentUrl().isEmpty()) {
                s3Service.deleteFile(lesson.getContentUrl());
            }
            String s3Key = s3Service.uploadFile(videoFile, String.valueOf(courseId), String.valueOf(lessonId));
            lesson.setContentUrl(s3Key);
        }

        Lesson updatedLesson = lessonRepository.save(lesson);
        logger.info("Updated lesson with ID {}", updatedLesson.getId());
        return mapToLessonDtoWithPresignedUrl(updatedLesson);
    }

    @Transactional
    public void deleteLesson(Long courseId, Long lessonId) {
        courseRepository.findById(courseId) // Ensure course exists
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Lesson with id " + lessonId + " does not belong to course with id " + courseId);
        }

        // Delete video from S3 if it exists
        if (lesson.getContentUrl() != null && !lesson.getContentUrl().isEmpty()) {
            s3Service.deleteFile(lesson.getContentUrl());
        }
        lessonRepository.delete(lesson);
        logger.info("Deleted lesson with ID {}", lessonId);
    }

    @Transactional(readOnly = true)
    public LessonDto getLessonById(Long courseId, Long lessonId) {
        courseRepository.findById(courseId) // Ensure course exists
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));
        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Lesson with id " + lessonId + " does not belong to course with id " + courseId);
        }
        return mapToLessonDtoWithPresignedUrl(lesson);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getAllPublishedCourses() {
        // Fetch only published courses for public view
        return courseRepository.findByPublishedStatus("PUBLISHED").stream()
                .map(course -> mapToCourseDto(course, false)) // Don't load lessons for list view
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getAllCoursesForAdmin() {
        // Admin might want to see all courses, including drafts
        return courseRepository.findAll().stream()
                .map(course -> mapToCourseDto(course, false)) // Lessons can be fetched on demand
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseDetailsById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        // For public view, you might want to check if course.getPublishedStatus().equals("PUBLISHED")
        // However, this method is also used by admin, so the check might be better in the public controller.
        return mapToCourseDto(course, true); // Load lessons for detail view
    }

    private CourseDto mapToCourseDto(Course course, boolean includeLessons) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setCoverImageUrl(course.getCoverImageUrl());
        dto.setPublishedStatus(course.getPublishedStatus());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        if (includeLessons) {
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
            dto.setLessons(lessons.stream().map(this::mapToLessonDtoWithPresignedUrl).collect(Collectors.toList()));
        } else {
            dto.setLessons(Collections.emptyList()); // Ensure lessons list is initialized
        }
        return dto;
    }

    private LessonDto mapToLessonDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setLessonType(lesson.getLessonType());
        dto.setContentUrl(lesson.getContentUrl()); // This is the S3 key
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setOrderIndex(lesson.getOrderIndex());
        if (lesson.getCourse() != null) { // Defensive check
            dto.setCourseId(lesson.getCourse().getId());
        }
        return dto;
    }

    private LessonDto mapToLessonDtoWithPresignedUrl(Lesson lesson) {
        LessonDto dto = mapToLessonDto(lesson);
        if ("VIDEO".equals(lesson.getLessonType()) && lesson.getContentUrl() != null && !lesson.getContentUrl().isEmpty()) {
            dto.setContentUrl(s3Service.getPresignedUrl(lesson.getContentUrl()));
        }
        return dto;
    }
}