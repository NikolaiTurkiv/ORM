package ru.study.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.datasource.url=jdbc:h2:mem:learning-api-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ApiIntegrationTest extends IntegrationTestSupport {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Test
    void usersSupportCrudAndReturnLocation() throws Exception {
        var created = send(post("/api/users"), Map.of("name", "Alice", "email", "alice@example.com", "role", "STUDENT"), status().isCreated());
        long id = created.path("id").asLong();
        assertThat(send(get("/api/users/{id}", id), null, status().isOk()).path("name").asText()).isEqualTo("Alice");
        send(put("/api/users/{id}", id), Map.of("name", "Alice Smith", "email", "smith@example.com", "role", "STUDENT"), status().isOk());
        assertThat(send(get("/api/users"), null, status().isOk()).get(0).path("name").asText()).isEqualTo("Alice Smith");
        send(delete("/api/users/{id}", id), null, status().isNoContent());
        send(get("/api/users/{id}", id), null, status().isNotFound());
    }

    @Test
    void createsAndListsCategories() throws Exception {
        var created = send(post("/api/categories"), Map.of("name", "Programming"), status().isCreated());
        var listed = send(get("/api/categories"), null, status().isOk());
        assertThat(listed.size()).isEqualTo(1);
        assertThat(listed.get(0).path("id").asLong()).isEqualTo(created.path("id").asLong());
    }

    @Test
    void createsAndLoadsCourseStructureOverHttp() throws Exception {
        var data = createLearningData();
        var course = send(post("/api/courses"), Map.of("title", "New course", "description", "Description",
                "teacherId", data.teacher().getId(), "categoryId", data.category().getId(), "duration", "2 weeks"), status().isCreated());
        long courseId = course.path("id").asLong();
        var module = send(post("/api/courses/{id}/modules", courseId), Map.of("title", "New module", "orderIndex", 0), status().isCreated());
        var lesson = send(post("/api/modules/{id}/lessons", module.path("id").asLong()), Map.of("title", "New lesson", "orderIndex", 0), status().isCreated());
        var assignment = send(post("/api/lessons/{id}/assignments", lesson.path("id").asLong()),
                Map.of("title", "Homework", "description", "Task", "maxScore", 10), status().isCreated());
        var loaded = send(get("/api/courses/{id}", courseId), null, status().isOk());
        assertThat(loaded.path("modules").get(0).path("lessons").get(0).path("assignments").get(0).path("id").asLong())
                .isEqualTo(assignment.path("id").asLong());
        assertThat(loaded.path("teacher").path("id").asLong()).isEqualTo(data.teacher().getId());
        send(get("/api/courses"), null, status().isOk());
        send(delete("/api/courses/{id}", courseId), null, status().isNoContent());
        send(get("/api/assignments/{id}", assignment.path("id").asLong()), null, status().isNotFound());
    }

    @Test
    void updatesAndDeletesCourseContent() throws Exception {
        var data = createLearningData();
        send(put("/api/courses/{id}", data.course().getId()), Map.of("title", "Changed", "teacherId", data.teacher().getId(),
                "categoryId", data.category().getId()), status().isOk());
        send(put("/api/modules/{id}", data.module().getId()), Map.of("title", "Changed module", "orderIndex", 1), status().isOk());
        send(put("/api/lessons/{id}", data.lesson().getId()), Map.of("title", "Changed lesson", "orderIndex", 1), status().isOk());
        send(put("/api/assignments/{id}", data.assignment().getId()), Map.of("title", "Changed task", "description", "Task", "maxScore", 50), status().isOk());
        send(delete("/api/assignments/{id}", data.assignment().getId()), null, status().isNoContent());
        send(delete("/api/lessons/{id}", data.lesson().getId()), null, status().isNoContent());
        send(delete("/api/modules/{id}", data.module().getId()), null, status().isNoContent());
    }

    @Test
    void enrollsListsAndUnenrolls() throws Exception {
        var data = createLearningData();
        var enrollment = send(post("/api/courses/{id}/enrollments", data.course().getId()),
                Map.of("studentId", data.student().getId()), status().isCreated());
        long id = enrollment.path("id").asLong();
        send(post("/api/courses/{id}/enrollments", data.course().getId()), Map.of("studentId", data.student().getId()), status().isConflict());
        assertThat(send(get("/api/courses/{id}/students", data.course().getId()), null, status().isOk()).get(0).path("id").asLong())
                .isEqualTo(data.student().getId());
        assertThat(send(get("/api/users/{id}/courses", data.student().getId()), null, status().isOk()).get(0).path("id").asLong())
                .isEqualTo(data.course().getId());
        assertThat(send(get("/api/enrollments/{id}", id), null, status().isOk()).path("status").asText()).isEqualTo("ACTIVE");
        send(delete("/api/courses/{id}/enrollments/{studentId}", data.course().getId(), data.student().getId()), null, status().isNoContent());
    }

    @Test
    void submitsAndGradesHomework() throws Exception {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var submitted = send(post("/api/assignments/{id}/submissions", data.assignment().getId()),
                Map.of("studentId", data.student().getId(), "content", "Solution"), status().isCreated());
        long id = submitted.path("id").asLong();
        send(patch("/api/submissions/{id}/grade", id), Map.of("teacherId", data.teacher().getId(), "score", 80, "feedback", "Good"), status().isOk());
        assertThat(send(get("/api/submissions/{id}", id), null, status().isOk()).path("score").asInt()).isEqualTo(80);
        send(get("/api/users/{id}/submissions", data.student().getId()), null, status().isOk());
        send(get("/api/assignments/{id}/submissions", data.assignment().getId()), null, status().isOk());
        send(delete("/api/courses/{id}", data.course().getId()), null, status().isConflict());
    }

    @Test
    void createsQuizHidesCorrectAnswersAndCalculatesResult() throws Exception {
        var data = createLearningData();
        enrollments.enroll(data.student().getId(), data.course().getId());
        var quiz = send(post("/api/modules/{id}/quiz", data.module().getId()), quizBody("Quiz"), status().isCreated());
        long id = quiz.path("id").asLong();
        var loaded = send(get("/api/quizzes/{id}", id), null, status().isOk());
        assertThat(loaded.toString()).doesNotContain("\"correct\"");
        long questionId = loaded.path("questions").get(0).path("id").asLong();
        long optionId = loaded.path("questions").get(0).path("options").get(0).path("id").asLong();
        var result = send(post("/api/quizzes/{id}/submissions", id), Map.of("studentId", data.student().getId(),
                "answers", Map.of(Long.toString(questionId), Set.of(optionId))), status().isCreated());
        assertThat(result.path("score").asInt()).isEqualTo(1);
        assertThat(result.path("maxScore").asInt()).isEqualTo(1);
        send(get("/api/quiz-submissions/{id}", result.path("id").asLong()), null, status().isOk());
        send(get("/api/users/{id}/quiz-submissions", data.student().getId()), null, status().isOk());
        send(get("/api/quizzes/{id}/submissions", id), null, status().isOk());
        send(delete("/api/quizzes/{id}", id), null, status().isConflict());
    }

    @Test
    void quizUpdateReturnsPersistedQuestionAndOptionIds() throws Exception {
        var data = createLearningData();
        var quiz = send(post("/api/modules/{id}/quiz", data.module().getId()), quizBody("Quiz"), status().isCreated());
        long id = quiz.path("id").asLong();
        var updated = send(put("/api/quizzes/{id}", id), quizBody("Updated quiz"), status().isOk());
        assertThat(updated.path("questions").get(0).path("id").isIntegralNumber()).isTrue();
        assertThat(updated.path("questions").get(0).path("options").get(0).path("id").isIntegralNumber()).isTrue();
        assertThat(send(get("/api/quizzes/{id}", id), null, status().isOk())).isEqualTo(updated);
        send(delete("/api/quizzes/{id}", id), null, status().isNoContent());
    }

    @Test
    void openApiContainsOnlyTheCoreOperations() throws Exception {
        var document = send(get("/v3/api-docs"), null, status().isOk());
        var paths = document.path("paths");
        assertThat(paths.has("/api/users/{id}/profile")).isFalse();
        assertThat(paths.has("/api/tags")).isFalse();
        assertThat(paths.has("/api/reviews/{id}")).isFalse();
        assertThat(paths.has("/api/courses/{id}/modules")).isTrue();
        int operations = 0;
        for (var path : paths) {
            for (String method : List.of("get", "post", "put", "patch", "delete")) {
                if (path.has(method)) operations++;
            }
        }
        assertThat(operations).isEqualTo(43);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":\"\",\"email\":\"invalid\",\"role\":\"STUDENT\"}",
            "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"role\":\"UNKNOWN\"}", "{bad json"})
    void invalidUserInputReturns400WithoutSaving(String body) throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        assertThat(users.findAll()).isEmpty();
    }

    @Test
    void fieldValidationReturnsUsefulErrors() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists()).andExpect(jsonPath("$.errors.role").exists());
        send(get("/api/users/0"), null, status().isBadRequest());
        send(get("/api/users/not-a-number"), null, status().isBadRequest());
        send(get("/api/users/9223372036854775807"), null, status().isNotFound());
    }

    @Test
    void nestedQuizValidationRejectsMissingCorrectFlag() throws Exception {
        var data = createLearningData();
        var body = Map.of("title", "Bad quiz", "questions", List.of(Map.of("text", "Question", "type", "SINGLE_CHOICE",
                "options", List.of(Map.of("text", "One"), Map.of("text", "Two", "correct", false)))));
        var response = send(post("/api/modules/{id}/quiz", data.module().getId()), body, status().isBadRequest());
        assertThat(response.path("errors").has("questions[0].options[0].correct")).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from quizzes", Long.class)).isZero();
    }

    @Test
    void duplicateEmailAndBusinessRuleHaveExpectedStatuses() throws Exception {
        var data = createLearningData();
        send(post("/api/users"), Map.of("name", "Duplicate", "email", data.student().getEmail(), "role", "STUDENT"), status().isConflict());
        send(post("/api/courses/{id}/enrollments", data.course().getId()), Map.of("studentId", data.teacher().getId()), status().isBadRequest());
    }

    @Test
    void swaggerUiAndOpenApiAreAvailable() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Learning Platform API"))
                .andExpect(jsonPath("$.paths['/api/courses'].post").exists())
                .andExpect(jsonPath("$.paths['/api/quizzes/{id}/submissions'].post").exists());
    }

    private Map<String, Object> quizBody(String title) {
        return Map.of("title", title, "questions", List.of(Map.of("text", "Entity annotation?", "type", "SINGLE_CHOICE",
                "options", List.of(Map.of("text", "@Entity", "correct", true), Map.of("text", "@Service", "correct", false)))));
    }

    private JsonNode send(MockHttpServletRequestBuilder request, Object body, ResultMatcher expected) throws Exception {
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        var response = mvc.perform(request).andExpect(expected).andReturn().getResponse();
        if (response.getStatus() == 201) {
            assertThat(response.getHeader("Location")).startsWith("/api/");
            mvc.perform(get(response.getHeader("Location"))).andExpect(status().isOk());
        }
        if (response.getStatus() == 204) {
            assertThat(response.getContentAsString()).isEmpty();
            return json.nullNode();
        }
        return json.readTree(response.getContentAsString());
    }
}
