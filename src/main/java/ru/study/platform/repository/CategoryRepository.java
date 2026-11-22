package ru.study.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
