package ru.study.platform;

import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.study.platform.entity.*;
import ru.study.platform.service.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
abstract class IntegrationTestSupport {
    @Autowired protected UserService users;
    @Autowired protected ClassificationService classification;
    @Autowired protected CourseService courses;
    @Autowired protected ContentService content;
    @Autowired protected EnrollmentService enrollments;
    @Autowired protected AssignmentService assignments;
    @Autowired protected QuizService quizzes;
    @Autowired protected ReviewService reviews;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbc, "course_tag", "course_reviews", "quiz_submissions",
                "submissions", "enrollments", "answer_options", "questions", "quizzes",
                "assignments", "lessons", "course_modules", "courses", "profiles",
                "tags", "categories", "app_users");
    }

    // Service calls commit independently; associations are read in a new transaction.
    protected <T> T inTransaction(Supplier<T> action) {
        return new TransactionTemplate(transactionManager).execute(status -> action.get());
    }

    protected void verifyInTransaction(Runnable action) {
        inTransaction(() -> {
            action.run();
            return null;
        });
    }

    protected LearningData createLearningData() {
        User teacher = users.create("Teacher", "teacher@example.com", Role.TEACHER);
        User student = users.create("Student", "student@example.com", Role.STUDENT);
        Category category = classification.createCategory("Programming");
        Course course = courses.create("Hibernate", "ORM course", teacher.getId(), category.getId());
        CourseModule module = content.createModule(course.getId(), "JPA", "Basics", 0);
        Lesson lesson = content.createLesson(module.getId(), "Entities", "Lesson text", null, 0);
        Assignment assignment = assignments.create(lesson.getId(), "Mapping", "Map an entity", null, 100);
        return new LearningData(teacher, student, category, course, module, lesson, assignment);
    }

    protected QuizDefinition quizDefinition() {
        return new QuizDefinition("JPA quiz", 10, List.of(
                new QuizDefinition.QuestionDefinition("Which annotation marks an entity?",
                        QuestionType.SINGLE_CHOICE, List.of(
                        new QuizDefinition.OptionDefinition("@Entity", true),
                        new QuizDefinition.OptionDefinition("@Service", false))),
                new QuizDefinition.QuestionDefinition("Which are JPA relationships?",
                        QuestionType.MULTIPLE_CHOICE, List.of(
                        new QuizDefinition.OptionDefinition("OneToMany", true),
                        new QuizDefinition.OptionDefinition("ManyToOne", true),
                        new QuizDefinition.OptionDefinition("Autowired", false)))));
    }

    protected record LearningData(User teacher, User student, Category category, Course course,
                                  CourseModule module, Lesson lesson, Assignment assignment) {
    }
}
