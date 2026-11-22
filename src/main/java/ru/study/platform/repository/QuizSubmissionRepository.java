package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.QuizSubmission;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    boolean existsByStudent_Id(Long studentId);
    boolean existsByQuiz_Id(Long quizId);
    boolean existsByQuiz_Module_Id(Long moduleId);
    boolean existsByQuiz_Module_Course_Id(Long courseId);
    List<QuizSubmission> findByStudent_Id(Long studentId);
    List<QuizSubmission> findByQuiz_Id(Long quizId);
}
