package ru.study.platform;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

class PersistenceConstraintsIntegrationTest extends IntegrationTestSupport {
    @Test
    void databaseRejectsDuplicateEmailEvenWithoutServiceChecks() {
        var data = createLearningData();
        assertThatThrownBy(() -> jdbc.update("insert into app_users (name, email, role) values (?, ?, ?)",
                "Duplicate", data.student().getEmail(), "STUDENT"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(users.findAll()).hasSize(2);
    }

    @Test
    void databaseRejectsDuplicateEnrollment() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        assertThatThrownBy(() -> jdbc.update("""
                insert into enrollments (student_id, course_id, enrolled_at, status)
                values (?, ?, current_timestamp, 'ACTIVE')
                """, data.student().getId(), data.course().getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(enrollments.findByCourse(data.course().getId())).hasSize(1);
    }

    @Test
    void databaseRejectsDuplicateSubmission() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        assignments.submit(data.student().getId(), data.assignment().getId(), "First");
        assertThatThrownBy(() -> jdbc.update("""
                insert into submissions (student_id, assignment_id, submitted_at, content)
                values (?, ?, current_timestamp, 'Duplicate')
                """, data.student().getId(), data.assignment().getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(assignments.findSubmissionsByAssignment(data.assignment().getId())).hasSize(1);
    }

    @Test
    void databaseEnforcesForeignKeys() {
        var data = createLearningData();
        assertThatThrownBy(() -> jdbc.update("""
                insert into enrollments (student_id, course_id, enrolled_at, status)
                values (?, ?, current_timestamp, 'ACTIVE')
                """, data.student().getId(), Long.MAX_VALUE))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(enrollments.findByStudent(data.student().getId())).isEmpty();
    }

    @Test
    void databaseEnforcesOneProfilePerUserAndRatingRange() {
        var data = createLearningData();
        users.saveProfile(data.student().getId(), "Bio", null);
        assertThatThrownBy(() -> jdbc.update("insert into profiles (user_id, bio) values (?, 'Duplicate')",
                data.student().getId())).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                insert into course_reviews (student_id, course_id, rating, created_at)
                values (?, ?, 6, current_timestamp)
                """, data.student().getId(), data.course().getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(reviews.findByCourse(data.course().getId())).isEmpty();
    }
}
