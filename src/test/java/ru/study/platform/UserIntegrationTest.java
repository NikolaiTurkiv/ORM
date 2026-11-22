package ru.study.platform;

import org.junit.jupiter.api.Test;
import ru.study.platform.entity.Role;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;

import static org.assertj.core.api.Assertions.*;

class UserIntegrationTest extends IntegrationTestSupport {
    @Test
    void createsReadsUpdatesAndDeletesUser() {
        var user = users.create("Alice", " ALICE@example.com ", Role.STUDENT);
        assertThat(users.findById(user.getId()).getEmail()).isEqualTo("alice@example.com");
        users.update(user.getId(), "Alice Smith", "smith@example.com", Role.STUDENT);
        var saved = users.findById(user.getId());
        assertThat(saved.getName()).isEqualTo("Alice Smith");
        assertThat(saved.getEmail()).isEqualTo("smith@example.com");
        users.delete(user.getId());
        assertThatThrownBy(() -> users.findById(user.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsDuplicateEmailWithoutChangingExistingUser() {
        var user = users.create("Alice", "alice@example.com", Role.STUDENT);
        assertThatThrownBy(() -> users.create("Other", "ALICE@example.com", Role.TEACHER))
                .isInstanceOf(ConflictException.class);
        assertThat(users.findAll()).hasSize(1);
        assertThat(users.findById(user.getId()).getName()).isEqualTo("Alice");
    }

    @Test
    void createsUpdatesAndDeletesOneProfile() {
        var user = users.create("Alice", "alice@example.com", Role.STUDENT);
        var profile = users.saveProfile(user.getId(), "Bio", "https://example.com/avatar");
        var updated = users.saveProfile(user.getId(), "Updated bio", null);
        assertThat(updated.getId()).isEqualTo(profile.getId());
        assertThat(users.findProfile(user.getId()).getBio()).isEqualTo("Updated bio");
        users.deleteProfile(user.getId());
        assertThatThrownBy(() -> users.findProfile(user.getId())).isInstanceOf(NotFoundException.class);
        assertThat(users.findById(user.getId())).isNotNull();
    }

    @Test
    void deletingUserCascadesToProfile() {
        var user = users.create("Alice", "alice@example.com", Role.STUDENT);
        users.saveProfile(user.getId(), "Bio", null);
        users.delete(user.getId());
        assertThat(jdbc.queryForObject("select count(*) from profiles", Long.class)).isZero();
    }

    @Test
    void rejectsRoleChangeAndDeletionOfTeacherWithCourses() {
        var data = createLearningData();
        assertThatThrownBy(() -> users.update(data.teacher().getId(), "Teacher", "teacher@example.com", Role.STUDENT))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> users.delete(data.teacher().getId())).isInstanceOf(ConflictException.class);
        assertThat(users.findById(data.teacher().getId()).getRole()).isEqualTo(Role.TEACHER);
    }

    @Test
    void rejectsMissingUserAndInvalidId() {
        assertThatThrownBy(() -> users.findById(Long.MAX_VALUE)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> users.findById(0L)).isInstanceOf(BusinessRuleException.class);
    }
}
