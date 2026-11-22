package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacher_Id(Long teacherId);
    List<Course> findByCategory_Id(Long categoryId);
    boolean existsByTeacher_Id(Long teacherId);
    boolean existsByCategory_Id(Long categoryId);
}
