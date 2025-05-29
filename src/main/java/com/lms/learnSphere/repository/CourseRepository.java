package com.lms.learnSphere.repository;

import com.lms.learnSphere.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByPublishedStatus(String publishedStatus);

    // Use this to fetch a course with its lessons eagerly if needed for specific operations
    // Be mindful of N+1 issues if used improperly in lists.
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.id = :id")
    Optional<Course> findByIdWithLessons(@Param("id") Long id);
}
