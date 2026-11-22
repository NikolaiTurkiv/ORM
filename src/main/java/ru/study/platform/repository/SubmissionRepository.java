package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByStudent_IdAndAssignment_Id(Long studentId, Long assignmentId);
    boolean existsByStudent_Id(Long studentId);
    boolean existsByAssignment_Id(Long assignmentId);
    boolean existsByAssignment_Lesson_Id(Long lessonId);
    boolean existsByAssignment_Lesson_Module_Id(Long moduleId);
    boolean existsByAssignment_Lesson_Module_Course_Id(Long courseId);
    List<Submission> findByStudent_Id(Long studentId);
    List<Submission> findByAssignment_Id(Long assignmentId);
}
