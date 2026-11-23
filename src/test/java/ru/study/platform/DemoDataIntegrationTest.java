package ru.study.platform;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.study.platform.config.DemoDataInitializer;
import ru.study.platform.entity.Role;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.datasource.url=jdbc:h2:mem:learning-demo-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles({"test", "demo"})
class DemoDataIntegrationTest extends IntegrationTestSupport {
    @Autowired private DemoDataInitializer initializer;

    @Test
    void createsCompleteDemoDataInEmptyDatabase() {
        initializer.run();
        Map<String, Long> expectedCounts = Map.ofEntries(
                Map.entry("app_users", 3L), Map.entry("profiles", 3L),
                Map.entry("categories", 1L), Map.entry("tags", 2L),
                Map.entry("courses", 1L), Map.entry("course_tag", 2L),
                Map.entry("course_modules", 2L), Map.entry("lessons", 2L),
                Map.entry("assignments", 2L), Map.entry("enrollments", 2L),
                Map.entry("submissions", 1L), Map.entry("quizzes", 1L),
                Map.entry("questions", 2L), Map.entry("answer_options", 5L),
                Map.entry("quiz_submissions", 1L), Map.entry("course_reviews", 1L));
        expectedCounts.forEach((table, count) ->
                assertThat(jdbc.queryForObject("select count(*) from " + table, Long.class)).as(table).isEqualTo(count));
        var alice = users.findAll().stream().filter(u -> u.getEmail().equals("alice@example.com")).findFirst().orElseThrow();
        var bob = users.findAll().stream().filter(u -> u.getEmail().equals("bob@example.com")).findFirst().orElseThrow();
        assertThat(assignments.findSubmissionsByStudent(alice.getId()).get(0).getScore()).isEqualTo(85);
        assertThat(quizzes.findResultsByStudent(alice.getId()).get(0).getScore()).isEqualTo(2);
        assertThat(assignments.findSubmissionsByStudent(bob.getId())).isEmpty();
        assertThat(quizzes.findResultsByStudent(bob.getId())).isEmpty();
        verifyInTransaction(() -> {
            var course = courses.findAll().get(0);
            assertThat(course.getModules()).hasSize(2);
            assertThat(course.getModules().get(0).getLessons()).hasSize(1);
            assertThat(course.getTags()).hasSize(2);
        });
    }

    @Test
    void repeatedRunDoesNotDuplicateOrOverwriteData() {
        initializer.run();
        var alice = users.findAll().stream().filter(u -> u.getEmail().equals("alice@example.com")).findFirst().orElseThrow();
        users.update(alice.getId(), "Changed name", alice.getEmail(), Role.STUDENT);
        initializer.run();
        assertThat(users.findAll()).hasSize(3);
        assertThat(courses.findAll()).hasSize(1);
        assertThat(users.findById(alice.getId()).getName()).isEqualTo("Changed name");
        assertThat(assignments.findSubmissionsByStudent(alice.getId())).hasSize(1);
        assertThat(quizzes.findResultsByStudent(alice.getId())).hasSize(1);
    }

    @Test
    void skipsDatabaseContainingExistingUser() {
        var existing = users.create("Existing", "existing@example.com", Role.STUDENT);
        initializer.run();
        assertThat(users.findAll()).extracting("id").containsExactly(existing.getId());
        assertThat(courses.findAll()).isEmpty();
        assertThat(classification.findCategories()).isEmpty();
    }

    @Test
    void skipsDatabaseContainingExistingCategory() {
        var existing = classification.createCategory("Existing category");
        initializer.run();
        assertThat(classification.findCategories()).extracting("id").containsExactly(existing.getId());
        assertThat(users.findAll()).isEmpty();
        assertThat(courses.findAll()).isEmpty();
    }
}
