package ru.study.platform.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.QuestionType;
import ru.study.platform.entity.Role;
import ru.study.platform.service.AssignmentService;
import ru.study.platform.service.ClassificationService;
import ru.study.platform.service.ContentService;
import ru.study.platform.service.CourseService;
import ru.study.platform.service.EnrollmentService;
import ru.study.platform.service.QuizDefinition;
import ru.study.platform.service.QuizService;
import ru.study.platform.service.ReviewService;
import ru.study.platform.service.UserService;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private final UserService users;
    private final ClassificationService classification;
    private final CourseService courses;
    private final ContentService content;
    private final EnrollmentService enrollments;
    private final AssignmentService assignments;
    private final QuizService quizzes;
    private final ReviewService reviews;

    @Override
    @Transactional
    public void run(String... args) {
        if (!users.findAll().isEmpty() || !courses.findAll().isEmpty()
                || !classification.findCategories().isEmpty() || !classification.findTags().isEmpty()) {
            log.info("Demo data skipped: database already contains data");
            return;
        }
        var teacher = users.create("Анна Преподаватель", "teacher@example.com", Role.TEACHER);
        var alice = users.create("Алиса Студент", "alice@example.com", Role.STUDENT);
        var bob = users.create("Борис Студент", "bob@example.com", Role.STUDENT);
        users.saveProfile(teacher.getId(), "Преподаватель Java", null);
        users.saveProfile(alice.getId(), "Изучаю Hibernate", null);
        users.saveProfile(bob.getId(), "Изучаю базы данных", null);
        var category = classification.createCategory("Программирование");
        var java = classification.createTag("Java");
        var jpa = classification.createTag("JPA");
        var course = courses.create("Основы Hibernate и JPA", "Сущности, связи и запросы", teacher.getId(), category.getId());
        courses.updateSchedule(course.getId(), "4 недели", null);
        courses.addTag(course.getId(), java.getId());
        courses.addTag(course.getId(), jpa.getId());
        var basics = content.createModule(course.getId(), "Основы ORM", "Модель данных", 0);
        var queries = content.createModule(course.getId(), "Запросы", "JPQL", 1);
        var entities = content.createLesson(basics.getId(), "Сущности и связи", "Entity, Id, OneToMany", null, 0);
        var jpql = content.createLesson(queries.getId(), "Запросы JPQL", "Поиск курсов по категории", null, 0);
        var mapping = assignments.create(entities.getId(), "Создать сущность", "Опишите сущность учебного курса", null, 100);
        assignments.create(jpql.getId(), "Написать запрос", "Найдите курсы по категории", null, 100);
        enrollments.enroll(alice.getId(), course.getId());
        enrollments.enroll(bob.getId(), course.getId());
        var submitted = assignments.submit(alice.getId(), mapping.getId(), "@Entity class Course { @Id Long id; }");
        assignments.grade(submitted.getId(), teacher.getId(), 85, "Добавьте ограничения обязательных полей");
        var quiz = quizzes.create(basics.getId(), new QuizDefinition("Основы JPA", 10, List.of(
                new QuizDefinition.QuestionDefinition("Какая аннотация обозначает сущность?", QuestionType.SINGLE_CHOICE,
                        List.of(new QuizDefinition.OptionDefinition("@Entity", true),
                                new QuizDefinition.OptionDefinition("@Service", false))),
                new QuizDefinition.QuestionDefinition("Какие связи поддерживает JPA?", QuestionType.MULTIPLE_CHOICE,
                        List.of(new QuizDefinition.OptionDefinition("OneToMany", true),
                                new QuizDefinition.OptionDefinition("ManyToOne", true),
                                new QuizDefinition.OptionDefinition("Autowired", false))))));
        var first = quiz.getQuestions().get(0);
        var second = quiz.getQuestions().get(1);
        quizzes.takeQuiz(alice.getId(), quiz.getId(), Map.of(
                first.getId(), Set.of(first.getOptions().get(0).getId()),
                second.getId(), Set.of(second.getOptions().get(0).getId(), second.getOptions().get(1).getId())));
        reviews.create(alice.getId(), course.getId(), 5, "Понятный вводный курс");
        log.info("Demo data created: course={}, teacher={}, alice={}, bob={}",
                course.getId(), teacher.getId(), alice.getId(), bob.getId());
    }
}
