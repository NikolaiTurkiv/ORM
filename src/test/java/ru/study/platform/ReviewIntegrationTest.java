package ru.study.platform;

import org.junit.jupiter.api.Test;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;

import static org.assertj.core.api.Assertions.*;

class ReviewIntegrationTest extends IntegrationTestSupport {
    @Test
    void createsReadsUpdatesAndDeletesReview() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var review = reviews.create(data.student().getId(), data.course().getId(), 5, "Great");
        reviews.update(review.getId(), 4, "Good");
        var saved = reviews.findById(review.getId());
        assertThat(saved.getRating()).isEqualTo(4);
        assertThat(saved.getComment()).isEqualTo("Good");
        assertThat(reviews.findByCourse(data.course().getId())).extracting("id").containsExactly(review.getId());
        reviews.delete(review.getId());
        assertThatThrownBy(() -> reviews.findById(review.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsDuplicateAndOutOfRangeRating() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        assertThatThrownBy(() -> reviews.create(data.student().getId(), data.course().getId(), 6, null))
                .isInstanceOf(BusinessRuleException.class);
        var first = reviews.create(data.student().getId(), data.course().getId(), 5, "First");
        assertThatThrownBy(() -> reviews.create(data.student().getId(), data.course().getId(), 4, "Second"))
                .isInstanceOf(ConflictException.class);
        assertThat(reviews.findById(first.getId()).getRating()).isEqualTo(5);
    }

    @Test
    void unenrolledStudentCannotReviewCourse() {
        var data = createLearningData();
        assertThatThrownBy(() -> reviews.create(data.student().getId(), data.course().getId(), 5, null))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(reviews.findByCourse(data.course().getId())).isEmpty();
    }
}
