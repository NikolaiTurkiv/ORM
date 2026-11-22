package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findByQuestion_Id(Long questionId);
}
