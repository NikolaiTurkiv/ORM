package ru.study.platform.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.CourseModule;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findByCourse_IdOrderByOrderIndexAscIdAsc(Long courseId);
}
