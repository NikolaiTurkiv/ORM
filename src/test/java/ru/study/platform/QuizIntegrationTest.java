package ru.study.platform;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.study.platform.entity.EnrollmentStatus;
import ru.study.platform.entity.QuestionType;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;
import ru.study.platform.service.QuizDefinition;

import static org.assertj.core.api.Assertions.*;

class QuizIntegrationTest extends IntegrationTestSupport {
    @Test
    void persistsUpdatesAndDeletesQuizQuestionsAndOptions() {
        var data = createLearningData();
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        assertThat(quizzes.findByModule(data.module().getId()).getId()).isEqualTo(quiz.getId());
        var ids = answerIds(quiz.getId());
        var updated = new QuizDefinition("Updated quiz", 20, quizDefinition().questions());
        quizzes.update(quiz.getId(), updated);
        verifyInTransaction(() -> {
            var saved = quizzes.findById(quiz.getId());
            assertThat(saved.getTitle()).isEqualTo("Updated quiz");
            assertThat(saved.getTimeLimit()).isEqualTo(20);
            assertThat(saved.getQuestions()).hasSize(2);
            assertThat(saved.getQuestions()).extracting("id").doesNotContain(ids.singleQuestion());
            assertThat(saved.getQuestions().get(0).getOptions()).extracting("text")
                    .containsExactly("@Entity", "@Service");
        });
        assertThat(jdbc.queryForObject("select count(*) from questions", Long.class)).isEqualTo(2L);
        assertThat(jdbc.queryForObject("select count(*) from answer_options", Long.class)).isEqualTo(5L);
        quizzes.delete(quiz.getId());
        assertThatThrownBy(() -> quizzes.findById(quiz.getId())).isInstanceOf(NotFoundException.class);
        assertThat(jdbc.queryForObject("select count(*) from questions", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from answer_options", Long.class)).isZero();
        assertThat(content.findModule(data.module().getId())).isNotNull();
    }

    @Test
    void scoresSingleAndMultipleChoiceAndStoresEveryAttempt() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        var ids = answerIds(quiz.getId());
        var perfect = quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of(
                ids.singleQuestion(), Set.of(ids.correctSingle()),
                ids.multipleQuestion(), Set.of(ids.firstMultiple(), ids.secondMultiple())));
        assertThat(perfect.getScore()).isEqualTo(2);
        assertThat(perfect.getMaxScore()).isEqualTo(2);
        var partial = quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of(
                ids.singleQuestion(), Set.of(ids.correctSingle()),
                ids.multipleQuestion(), Set.of(ids.firstMultiple())));
        assertThat(partial.getScore()).isEqualTo(1);
        var skipped = quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of());
        assertThat(skipped.getScore()).isZero();
        assertThat(quizzes.findResult(perfect.getId()).getScore()).isEqualTo(2);
        assertThat(quizzes.findResultsByStudent(data.student().getId())).hasSize(3);
        assertThat(quizzes.findResultsByQuiz(quiz.getId())).hasSize(3);
    }

    @Test
    void extraWrongOptionMakesMultipleChoiceAnswerIncorrect() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        var ids = answerIds(quiz.getId());
        var result = quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of(
                ids.singleQuestion(), Set.of(ids.wrongSingle()),
                ids.multipleQuestion(), Set.of(ids.firstMultiple(), ids.secondMultiple(), ids.wrongMultiple())));
        assertThat(result.getScore()).isZero();
    }

    @Test
    void rejectsForeignQuestionsOptionsAndMultipleSingleChoiceSelections() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        var ids = answerIds(quiz.getId());
        assertThatThrownBy(() -> quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of(Long.MAX_VALUE, Set.of())))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> quizzes.takeQuiz(data.student().getId(), quiz.getId(),
                Map.of(ids.singleQuestion(), Set.of(ids.firstMultiple())))).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> quizzes.takeQuiz(data.student().getId(), quiz.getId(),
                Map.of(ids.singleQuestion(), Set.of(ids.correctSingle(), ids.wrongSingle()))))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(quizzes.findResultsByQuiz(quiz.getId())).isEmpty();
    }

    @Test
    void requiresActiveEnrollment() {
        var data = createLearningData();
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        assertThatThrownBy(() -> quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of()))
                .isInstanceOf(BusinessRuleException.class);
        var enrollment = enrollments.enroll(data.student().getId(), data.course().getId());
        enrollments.updateStatus(enrollment.getId(), EnrollmentStatus.COMPLETED);
        assertThatThrownBy(() -> quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(quizzes.findResultsByQuiz(quiz.getId())).isEmpty();
    }

    @Test
    void resultsSurviveUnenrollmentAndProtectQuizAndCourse() {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        var result = quizzes.takeQuiz(data.student().getId(), quiz.getId(), Map.of());
        enrollments.unenroll(data.student().getId(), data.course().getId());
        assertThatThrownBy(() -> quizzes.update(quiz.getId(), quizDefinition())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> quizzes.delete(quiz.getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> content.deleteModule(data.module().getId())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> courses.delete(data.course().getId())).isInstanceOf(ConflictException.class);
        assertThat(quizzes.findResult(result.getId()).getMaxScore()).isEqualTo(2);
    }

    @Test
    void moduleCanHaveOnlyOneQuiz() {
        var data = createLearningData();
        quizzes.create(data.module().getId(), quizDefinition());
        assertThatThrownBy(() -> quizzes.create(data.module().getId(), quizDefinition()))
                .isInstanceOf(ConflictException.class);
        assertThat(jdbc.queryForObject("select count(*) from quizzes", Long.class)).isEqualTo(1L);
    }

    @Test
    void invalidQuizUpdateRollsBackTitleAndQuestionReplacement() {
        var data = createLearningData();
        var quiz = quizzes.create(data.module().getId(), quizDefinition());
        var originalIds = answerIds(quiz.getId());
        var invalid = new QuizDefinition("Invalid", 5, List.of(
                new QuizDefinition.QuestionDefinition("Bad question", QuestionType.SINGLE_CHOICE, List.of(
                        new QuizDefinition.OptionDefinition("First", true),
                        new QuizDefinition.OptionDefinition("Second", true)))));
        assertThatThrownBy(() -> quizzes.update(quiz.getId(), invalid)).isInstanceOf(BusinessRuleException.class);
        assertThat(quizzes.findById(quiz.getId()).getTitle()).isEqualTo("JPA quiz");
        assertThat(answerIds(quiz.getId())).isEqualTo(originalIds);
    }

    private AnswerIds answerIds(Long quizId) {
        return inTransaction(() -> {
            var questions = quizzes.findById(quizId).getQuestions();
            var single = questions.get(0);
            var multiple = questions.get(1);
            return new AnswerIds(single.getId(), single.getOptions().get(0).getId(),
                    single.getOptions().get(1).getId(), multiple.getId(),
                    multiple.getOptions().get(0).getId(), multiple.getOptions().get(1).getId(),
                    multiple.getOptions().get(2).getId());
        });
    }

    private record AnswerIds(Long singleQuestion, Long correctSingle, Long wrongSingle,
                             Long multipleQuestion, Long firstMultiple, Long secondMultiple,
                             Long wrongMultiple) {
    }
}
