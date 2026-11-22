package ru.study.platform.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser_Id(Long userId);
}
