package ru.study.platform;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.study.platform.entity.CourseModule;
import ru.study.platform.entity.QuestionType;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.repository.CourseModuleRepository;
import ru.study.platform.repository.QuizRepository;
import ru.study.platform.repository.QuizSubmissionRepository;
import ru.study.platform.repository.UserRepository;
import ru.study.platform.service.EnrollmentService;
import ru.study.platform.service.QuizDefinition;
import ru.study.platform.service.QuizService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {
    @Mock private QuizRepository quizzes;
    @Mock private CourseModuleRepository modules;
    @Mock private UserRepository users;
    @Mock private QuizSubmissionRepository results;
    @Mock private EnrollmentService enrollmentService;
    @InjectMocks private QuizService service;

    @BeforeEach
    void existingModule() {
        when(modules.findById(1L)).thenReturn(Optional.of(new CourseModule()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDefinitions")
    void doesNotSaveInvalidQuiz(String scenario, QuizDefinition definition) {
        assertThatThrownBy(() -> service.create(1L, definition))
                .as(scenario).isInstanceOf(BusinessRuleException.class);
        verify(quizzes, never()).save(any());
    }

    static Stream<Arguments> invalidDefinitions() {
        var correct = new QuizDefinition.OptionDefinition("Correct", true);
        var wrong = new QuizDefinition.OptionDefinition("Wrong", false);
        var validQuestion = new QuizDefinition.QuestionDefinition("Question", QuestionType.SINGLE_CHOICE,
                List.of(correct, wrong));
        return Stream.of(
                Arguments.of("missing definition", null),
                Arguments.of("missing questions", new QuizDefinition("Quiz", 10, null)),
                Arguments.of("empty questions", new QuizDefinition("Quiz", 10, List.of())),
                Arguments.of("non-positive time limit", new QuizDefinition("Quiz", 0, List.of(validQuestion))),
                Arguments.of("one option", new QuizDefinition("Quiz", 10, List.of(
                        new QuizDefinition.QuestionDefinition("Question", QuestionType.SINGLE_CHOICE, List.of(correct))))),
                Arguments.of("no correct options", new QuizDefinition("Quiz", 10, List.of(
                        new QuizDefinition.QuestionDefinition("Question", QuestionType.MULTIPLE_CHOICE,
                                List.of(wrong, wrong))))),
                Arguments.of("several correct options in single-choice question", new QuizDefinition("Quiz", 10, List.of(
                        new QuizDefinition.QuestionDefinition("Question", QuestionType.SINGLE_CHOICE,
                                List.of(correct, correct))))));
    }
}
