package ru.study.platform.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.AnswerOption;
import ru.study.platform.entity.CourseModule;
import ru.study.platform.entity.Question;
import ru.study.platform.entity.QuestionType;
import ru.study.platform.entity.Quiz;
import ru.study.platform.entity.QuizSubmission;
import ru.study.platform.exception.NotFoundException;
import ru.study.platform.repository.CourseModuleRepository;
import ru.study.platform.repository.QuizRepository;
import ru.study.platform.repository.QuizSubmissionRepository;
import ru.study.platform.repository.UserRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {
    private final QuizRepository quizzes;
    private final CourseModuleRepository modules;
    private final UserRepository users;
    private final QuizSubmissionRepository results;
    private final EnrollmentService enrollmentService;

    public Quiz findById(Long id) {
        return get(quizzes, id, "Quiz");
    }

    public Quiz findByModule(Long moduleId) {
        get(modules, moduleId, "Module");
        return quizzes.findByModule_Id(moduleId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));
    }

    @Transactional
    public Quiz create(Long moduleId, QuizDefinition definition) {
        CourseModule module = get(modules, moduleId, "Module");
        conflict(quizzes.existsByModule_Id(moduleId), "Module already has a quiz");
        Quiz quiz = new Quiz();
        setDefinition(quiz, definition);
        quiz.setModule(module);
        module.setQuiz(quiz);
        return quizzes.save(quiz);
    }

    @Transactional
    public Quiz update(Long id, QuizDefinition definition) {
        Quiz quiz = findById(id);
        conflict(results.existsByQuiz_Id(id), "Cannot edit a quiz after students have taken it");
        setDefinition(quiz, definition);
        return quiz;
    }

    @Transactional
    public void delete(Long id) {
        Quiz quiz = findById(id);
        conflict(results.existsByQuiz_Id(id), "Quiz has student results");
        quiz.getModule().setQuiz(null);
        quizzes.delete(quiz);
    }

    @Transactional
    public QuizSubmission takeQuiz(Long studentId, Long quizId, Map<Long, Set<Long>> answers) {
        Quiz quiz = findById(quizId);
        enrollmentService.requireActiveStudent(studentId, quiz.getModule().getCourse().getId());
        check(answers != null, "Answers are required");
        List<Question> questions = quiz.getQuestions();
        check(!questions.isEmpty(), "Quiz must contain questions");
        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
        check(questionIds.containsAll(answers.keySet()), "Answer contains a question from another quiz");
        int score = 0;
        for (Question question : questions) {
            Set<Long> selected = answers.getOrDefault(question.getId(), Set.of());
            check(selected != null, "Selected options must not be null");
            Set<Long> optionIds = question.getOptions().stream().map(AnswerOption::getId)
                    .collect(Collectors.toSet());
            check(optionIds.containsAll(selected), "Answer contains an option from another question");
            check(question.getType() != QuestionType.SINGLE_CHOICE || selected.size() <= 1,
                    "Single-choice question accepts at most one option");
            Set<Long> correct = question.getOptions().stream().filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId).collect(Collectors.toSet());
            if (selected.equals(correct)) {
                score++;
            }
        }
        QuizSubmission result = new QuizSubmission();
        result.setQuiz(quiz);
        result.setStudent(get(users, studentId, "Student"));
        result.setScore(score);
        result.setMaxScore(questions.size());
        quiz.getSubmissions().add(result);
        result.getStudent().getQuizSubmissions().add(result);
        return results.save(result);
    }

    public QuizSubmission findResult(Long id) {
        return get(results, id, "Quiz result");
    }

    public List<QuizSubmission> findResultsByStudent(Long studentId) {
        get(users, studentId, "Student");
        return results.findByStudent_Id(studentId);
    }

    public List<QuizSubmission> findResultsByQuiz(Long quizId) {
        findById(quizId);
        return results.findByQuiz_Id(quizId);
    }

    private void setDefinition(Quiz quiz, QuizDefinition definition) {
        check(definition != null, "Quiz definition is required");
        check(definition.timeLimit() == null || definition.timeLimit() > 0, "Time limit must be positive");
        check(definition.questions() != null && !definition.questions().isEmpty(),
                "Quiz must contain at least one question");
        quiz.setTitle(text(definition.title(), "Quiz title", 200));
        quiz.setTimeLimit(definition.timeLimit());
        quiz.getQuestions().clear();
        int order = 0;
        for (QuizDefinition.QuestionDefinition input : definition.questions()) {
            check(input != null && input.type() != null, "Question and its type are required");
            check(input.options() != null && input.options().size() >= 2,
                    "Question must contain at least two options");
            Question question = new Question();
            question.setText(text(input.text(), "Question text", 2000));
            question.setType(input.type());
            question.setOrderIndex(order++);
            int correctCount = 0;
            for (QuizDefinition.OptionDefinition optionInput : input.options()) {
                check(optionInput != null, "Option is required");
                AnswerOption option = new AnswerOption();
                option.setText(text(optionInput.text(), "Option text", 2000));
                option.setCorrect(optionInput.correct());
                question.addOption(option);
                if (option.isCorrect()) {
                    correctCount++;
                }
            }
            check(correctCount > 0, "Question must have at least one correct option");
            check(input.type() != QuestionType.SINGLE_CHOICE || correctCount == 1,
                    "Single-choice question must have exactly one correct option");
            quiz.addQuestion(question);
        }
    }
}
