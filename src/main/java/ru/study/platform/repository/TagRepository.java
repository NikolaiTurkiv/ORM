package ru.study.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
