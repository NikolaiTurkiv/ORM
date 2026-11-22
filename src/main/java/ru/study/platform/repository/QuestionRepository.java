package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz_IdOrderByOrderIndexAscIdAsc(Long quizId);
}
