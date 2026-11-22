package ru.study.platform.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Enrollment;
import ru.study.platform.entity.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);
    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);
    boolean existsByStudent_IdAndCourse_IdAndStatus(Long studentId, Long courseId, EnrollmentStatus status);
    boolean existsByStudent_Id(Long studentId);
    boolean existsByCourse_Id(Long courseId);
    List<Enrollment> findByStudent_Id(Long studentId);
    List<Enrollment> findByCourse_Id(Long courseId);
}
