package ru.study.platform;

import org.junit.jupiter.api.Test;
import ru.study.platform.entity.EnrollmentStatus;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;

import static org.assertj.core.api.Assertions.*;

class EnrollmentIntegrationTest extends IntegrationTestSupport {
    @Test
    void enrollsListsUpdatesStatusAndUnenrollsStudent() {
        var data = createLearningData();
        var enrollment = enrollments.enroll(data.student().getId(), data.course().getId());
        assertThat(enrollment.getEnrolledAt()).isNotNull();
        assertThat(enrollments.findByStudent(data.student().getId())).extracting("id").containsExactly(enrollment.getId());
        assertThat(enrollments.findByCourse(data.course().getId())).extracting("id").containsExactly(enrollment.getId());
        enrollments.updateStatus(enrollment.getId(), EnrollmentStatus.COMPLETED);
        assertThat(enrollments.findById(enrollment.getId()).getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        enrollments.unenroll(data.student().getId(), data.course().getId());
        assertThat(enrollments.findByCourse(data.course().getId())).isEmpty();
        assertThat(users.findById(data.student().getId())).isNotNull();
        assertThat(courses.findById(data.course().getId())).isNotNull();
    }

    @Test
    void rejectsDuplicateEnrollment() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        assertThatThrownBy(() -> enrollments.enroll(data.student().getId(), data.course().getId()))
                .isInstanceOf(ConflictException.class);
        assertThat(enrollments.findByCourse(data.course().getId())).hasSize(1);
    }

    @Test
    void rejectsTeacherAndUnknownCourse() {
        var data = createLearningData();
        assertThatThrownBy(() -> enrollments.enroll(data.teacher().getId(), data.course().getId()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> enrollments.enroll(data.student().getId(), Long.MAX_VALUE))
                .isInstanceOf(NotFoundException.class);
        assertThat(enrollments.findByStudent(data.student().getId())).isEmpty();
    }

    @Test
    void completedEnrollmentCannotSubmitAssignments() {
        var data = createLearningData();
        var enrollment = enrollments.enroll(data.student().getId(), data.course().getId());
        enrollments.updateStatus(enrollment.getId(), EnrollmentStatus.COMPLETED);
        assertThatThrownBy(() -> assignments.submit(data.student().getId(), data.assignment().getId(), "Answer"))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(assignments.findSubmissionsByStudent(data.student().getId())).isEmpty();
    }

    @Test
    void enrollmentProtectsCourseAndStudentFromDeletion() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        assertThatThrownBy(() -> courses.delete(data.course().getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> users.delete(data.student().getId())).isInstanceOf(ConflictException.class);
        assertThat(enrollments.findByCourse(data.course().getId())).hasSize(1);
    }
}
