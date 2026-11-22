package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModule_IdOrderByOrderIndexAscIdAsc(Long moduleId);
}
