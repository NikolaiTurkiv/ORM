package ru.study.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
