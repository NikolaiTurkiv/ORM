package ru.study.platform;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.study.platform.entity.Role;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;

import static org.assertj.core.api.Assertions.*;

class AssignmentIntegrationTest extends IntegrationTestSupport {
    @Test
    void createsReadsUpdatesAndDeletesAssignment() {
        var data = createLearningData();
        Instant deadline = Instant.parse("2030-01-01T00:00:00Z");
        assignments.update(data.assignment().getId(), "New title", "New description", deadline, 50);
        var saved = assignments.findById(data.assignment().getId());
        assertThat(saved.getTitle()).isEqualTo("New title");
        assertThat(saved.getDescription()).isEqualTo("New description");
        assertThat(saved.getDueDate()).isEqualTo(deadline);
        assertThat(saved.getMaxScore()).isEqualTo(50);
        assertThat(assignments.findByLesson(data.lesson().getId())).extracting("id").containsExactly(saved.getId());
        assignments.delete(saved.getId());
        assertThatThrownBy(() -> assignments.findById(saved.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void submitsAndGradesWorkAndPreservesItAfterUnenrollment() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var submission = assignments.submit(data.student().getId(), data.assignment().getId(), "My answer");
        assertThat(submission.getScore()).isNull();
        assertThat(submission.getSubmittedAt()).isNotNull();
        assignments.grade(submission.getId(), data.teacher().getId(), 80, "Good work");
        var saved = assignments.findSubmission(submission.getId());
        assertThat(saved.getScore()).isEqualTo(80);
        assertThat(saved.getFeedback()).isEqualTo("Good work");
        assertThat(assignments.findSubmissionsByAssignment(data.assignment().getId()))
                .extracting("id").containsExactly(submission.getId());
        enrollments.unenroll(data.student().getId(), data.course().getId());
        assertThat(assignments.findSubmissionsByStudent(data.student().getId()))
                .extracting("id").containsExactly(submission.getId());
    }

    @Test
    void rejectsSecondSubmissionWithoutOverwritingFirst() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var first = assignments.submit(data.student().getId(), data.assignment().getId(), "First answer");
        assertThatThrownBy(() -> assignments.submit(data.student().getId(), data.assignment().getId(), "Second answer"))
                .isInstanceOf(ConflictException.class);
        assertThat(assignments.findSubmission(first.getId()).getContent()).isEqualTo("First answer");
        assertThat(assignments.findSubmissionsByAssignment(data.assignment().getId())).hasSize(1);
    }

    @Test
    void rejectsUnenrolledStudentAndExpiredAssignment() {
        var data = createLearningData();
        assertThatThrownBy(() -> assignments.submit(data.student().getId(), data.assignment().getId(), "Answer"))
                .isInstanceOf(BusinessRuleException.class);
        enrollments.enroll(data.student().getId(), data.course().getId());
        assignments.update(data.assignment().getId(), "Expired", "Description", Instant.EPOCH, 100);
        assertThatThrownBy(() -> assignments.submit(data.student().getId(), data.assignment().getId(), "Late answer"))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(assignments.findSubmissionsByAssignment(data.assignment().getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void rejectsScoreOutsideAssignmentRange(int score) {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var submission = assignments.submit(data.student().getId(), data.assignment().getId(), "Answer");
        assertThatThrownBy(() -> assignments.grade(submission.getId(), data.teacher().getId(), score, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(assignments.findSubmission(submission.getId()).getScore()).isNull();
    }

    @Test
    void onlyCourseTeacherCanGrade() {
        var data = createLearningData();
        var otherTeacher = users.create("Other", "other@example.com", Role.TEACHER);
        enrollments.enroll(data.student().getId(), data.course().getId());
        var submission = assignments.submit(data.student().getId(), data.assignment().getId(), "Answer");
        assertThatThrownBy(() -> assignments.grade(submission.getId(), otherTeacher.getId(), 50, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> assignments.grade(submission.getId(), data.student().getId(), 50, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(assignments.findSubmission(submission.getId()).getScore()).isNull();
    }

    @Test
    void submittedWorkProtectsContentAndScoreScale() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var submission = assignments.submit(data.student().getId(), data.assignment().getId(), "Answer");
        enrollments.unenroll(data.student().getId(), data.course().getId());
        assertThatThrownBy(() -> assignments.delete(data.assignment().getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> content.deleteLesson(data.lesson().getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> content.deleteModule(data.module().getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> courses.delete(data.course().getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> assignments.update(data.assignment().getId(), "Changed", "Description", null, 50))
                .isInstanceOf(ConflictException.class);
        assertThat(assignments.findById(data.assignment().getId()).getMaxScore()).isEqualTo(100);
        assertThat(assignments.findSubmission(submission.getId()).getContent()).isEqualTo("Answer");
    }
}
