package ru.study.platform.service;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Profile;
import ru.study.platform.entity.Role;
import ru.study.platform.entity.User;
import ru.study.platform.exception.NotFoundException;
import ru.study.platform.repository.CourseRepository;
import ru.study.platform.repository.CourseReviewRepository;
import ru.study.platform.repository.EnrollmentRepository;
import ru.study.platform.repository.ProfileRepository;
import ru.study.platform.repository.QuizSubmissionRepository;
import ru.study.platform.repository.SubmissionRepository;
import ru.study.platform.repository.UserRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository quizSubmissions;
    private final CourseReviewRepository reviews;

    public List<User> findAll() {
        return users.findAll();
    }

    public User findById(Long id) {
        return get(users, id, "User");
    }

    @Transactional
    public User create(String name, String email, Role role) {
        String normalizedEmail = normalizeEmail(email);
        conflict(users.existsByEmailIgnoreCase(normalizedEmail), "Email is already registered");
        User user = new User();
        setDetails(user, name, normalizedEmail, role);
        return users.save(user);
    }

    @Transactional
    public User update(Long id, String name, String email, Role role) {
        User user = findById(id);
        String normalizedEmail = normalizeEmail(email);
        conflict(users.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id), "Email is already registered");
        if (user.getRole() != role) {
            conflict(hasAcademicData(id), "Cannot change role of a user with academic data");
        }
        setDetails(user, name, normalizedEmail, role);
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        conflict(hasAcademicData(id), "Cannot delete a user with academic data");
        users.delete(user);
    }

    public Profile findProfile(Long userId) {
        findById(userId);
        return profiles.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }

    @Transactional
    public Profile saveProfile(Long userId, String bio, String avatarUrl) {
        User user = findById(userId);
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        profile.setBio(optionalText(bio, "Bio", 2000));
        profile.setAvatarUrl(optionalText(avatarUrl, "Avatar URL", 1000));
        return profiles.save(profile);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        User user = findById(userId);
        findProfile(userId);
        user.setProfile(null);
    }

    private boolean hasAcademicData(Long id) {
        return courses.existsByTeacher_Id(id) || enrollments.existsByStudent_Id(id)
                || submissions.existsByStudent_Id(id) || quizSubmissions.existsByStudent_Id(id)
                || reviews.existsByStudent_Id(id);
    }

    private String normalizeEmail(String email) {
        return text(email, "Email", 254).toLowerCase(Locale.ROOT);
    }

    private void setDetails(User user, String name, String email, Role role) {
        check(role != null, "Role is required");
        user.setName(text(name, "Name", 120));
        user.setEmail(email);
        user.setRole(role);
    }
}
