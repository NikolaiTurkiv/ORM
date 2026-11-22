package ru.study.platform.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByModule_Id(Long moduleId);
    Optional<Quiz> findByModule_Id(Long moduleId);
}
