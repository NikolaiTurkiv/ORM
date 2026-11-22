package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.CourseReview;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);
    boolean existsByStudent_Id(Long studentId);
    boolean existsByCourse_Id(Long courseId);
    List<CourseReview> findByCourse_Id(Long courseId);
}
