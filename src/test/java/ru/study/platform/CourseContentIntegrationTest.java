package ru.study.platform;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import ru.study.platform.entity.Role;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;

import static org.assertj.core.api.Assertions.*;

class CourseContentIntegrationTest extends IntegrationTestSupport {
    @Test
    void createsAndUpdatesCourseWithTeacherCategoryAndSchedule() {
        var data = createLearningData();
        var teacher = users.create("Other teacher", "other@example.com", Role.TEACHER);
        var category = classification.createCategory("Databases");
        courses.update(data.course().getId(), "Advanced JPA", "Updated", teacher.getId(), category.getId());
        courses.updateSchedule(data.course().getId(), "4 weeks", LocalDate.of(2027, 1, 10));
        verifyInTransaction(() -> {
            var saved = courses.findById(data.course().getId());
            assertThat(saved.getTitle()).isEqualTo("Advanced JPA");
            assertThat(saved.getTeacher().getId()).isEqualTo(teacher.getId());
            assertThat(saved.getCategory().getId()).isEqualTo(category.getId());
            assertThat(saved.getDuration()).isEqualTo("4 weeks");
            assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2027, 1, 10));
        });
        assertThat(courses.findByTeacher(teacher.getId())).extracting("id").containsExactly(data.course().getId());
        assertThat(courses.findByCategory(category.getId())).extracting("id").containsExactly(data.course().getId());
    }

    @Test
    void loadsCourseStructureInModuleAndLessonOrder() {
        var data = createLearningData();
        var secondModule = content.createModule(data.course().getId(), "Second", null, 2);
        var firstModule = content.createModule(data.course().getId(), "First", null, 1);
        content.createLesson(firstModule.getId(), "Later", "Later text", null, 2);
        content.createLesson(firstModule.getId(), "Earlier", "Earlier text", null, 1);
        verifyInTransaction(() -> {
            var saved = courses.findById(data.course().getId());
            assertThat(saved.getModules()).extracting("id")
                    .containsExactly(data.module().getId(), firstModule.getId(), secondModule.getId());
            assertThat(saved.getModules().get(1).getLessons()).extracting("title")
                    .containsExactly("Earlier", "Later");
            assertThat(saved.getModules().get(0).getLessons().get(0).getAssignments())
                    .extracting("id").containsExactly(data.assignment().getId());
        });
    }

    @Test
    void updatesAndDeletesModuleAndLesson() {
        var data = createLearningData();
        content.updateModule(data.module().getId(), "New module", "Description", 5);
        content.updateLesson(data.lesson().getId(), "New lesson", "Content", "https://example.com/video", 3);
        assertThat(content.findModule(data.module().getId()).getTitle()).isEqualTo("New module");
        assertThat(content.findLesson(data.lesson().getId()).getVideoUrl()).isEqualTo("https://example.com/video");
        content.deleteLesson(data.lesson().getId());
        assertThatThrownBy(() -> assignments.findById(data.assignment().getId())).isInstanceOf(NotFoundException.class);
        content.deleteModule(data.module().getId());
        assertThatThrownBy(() -> content.findModule(data.module().getId())).isInstanceOf(NotFoundException.class);
        assertThat(courses.findById(data.course().getId())).isNotNull();
    }

    @Test
    void deletingCourseCascadesToContentButPreservesSharedData() {
        var data = createLearningData();
        var tag = classification.createTag("Java");
        courses.addTag(data.course().getId(), tag.getId());
        quizzes.create(data.module().getId(), quizDefinition());
        courses.delete(data.course().getId());
        for (String table : new String[]{"courses", "course_modules", "lessons", "assignments",
                "quizzes", "questions", "answer_options", "course_tag"}) {
            assertThat(jdbc.queryForObject("select count(*) from " + table, Long.class)).as(table).isZero();
        }
        assertThat(users.findById(data.teacher().getId())).isNotNull();
        assertThat(classification.findCategory(data.category().getId())).isNotNull();
        assertThat(classification.findTag(tag.getId())).isNotNull();
    }

    @Test
    void managesManyToManyTagsWithoutDuplicateLinks() {
        var data = createLearningData();
        var tag = classification.createTag("Java");
        courses.addTag(data.course().getId(), tag.getId());
        courses.addTag(data.course().getId(), tag.getId());
        verifyInTransaction(() -> {
            assertThat(courses.findById(data.course().getId()).getTags()).extracting("id").containsExactly(tag.getId());
            assertThat(classification.findTag(tag.getId()).getCourses()).extracting("id").containsExactly(data.course().getId());
        });
        courses.removeTag(data.course().getId(), tag.getId());
        assertThat(jdbc.queryForObject("select count(*) from course_tag", Long.class)).isZero();
        courses.addTag(data.course().getId(), tag.getId());
        classification.deleteTag(tag.getId());
        assertThat(jdbc.queryForObject("select count(*) from course_tag", Long.class)).isZero();
        assertThat(courses.findById(data.course().getId())).isNotNull();
    }

    @Test
    void categoriesAndTagsSupportCrudAndRejectDuplicates() {
        var category = classification.createCategory("Programming");
        classification.updateCategory(category.getId(), "Databases");
        assertThat(classification.findCategory(category.getId()).getName()).isEqualTo("Databases");
        assertThatThrownBy(() -> classification.createCategory("DATABASES")).isInstanceOf(ConflictException.class);
        classification.deleteCategory(category.getId());
        assertThat(classification.findCategories()).isEmpty();
        var tag = classification.createTag("Java");
        classification.updateTag(tag.getId(), "JPA");
        assertThat(classification.findTag(tag.getId()).getName()).isEqualTo("JPA");
        assertThatThrownBy(() -> classification.createTag("jpa")).isInstanceOf(ConflictException.class);
        classification.deleteTag(tag.getId());
        assertThat(classification.findTags()).isEmpty();
    }

    @Test
    void rejectsStudentAsTeacherAndDeletionOfUsedCategory() {
        var data = createLearningData();
        assertThatThrownBy(() -> courses.create("Bad course", null, data.student().getId(), data.category().getId()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> classification.deleteCategory(data.category().getId())).isInstanceOf(ConflictException.class);
        assertThat(courses.findAll()).hasSize(1);
    }
}
