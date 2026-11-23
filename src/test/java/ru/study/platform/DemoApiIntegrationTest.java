package ru.study.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.study.platform.config.DemoDataInitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.datasource.url=jdbc:h2:mem:learning-demo-api-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles({"test", "demo"})
@AutoConfigureMockMvc
class DemoApiIntegrationTest extends IntegrationTestSupport {
    @Autowired private DemoDataInitializer initializer;
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Test
    void demoSupportsDocumentedWorkflowThroughCurrentApi() throws Exception {
        initializer.run();
        JsonNode userList = send(get("/api/users"), null, status().isOk());
        long teacherId = userId(userList, "teacher@example.com");
        long studentId = userId(userList, "bob@example.com");
        JsonNode course = send(get("/api/courses"), null, status().isOk()).get(0);
        long courseId = course.path("id").asLong();
        JsonNode module = course.path("modules").get(0);
        long assignmentId = module.path("lessons").get(0).path("assignments").get(0).path("id").asLong();
        long quizId = module.path("quizId").asLong();
        assertThat(quizId).isPositive();
        assertThat(send(get("/api/courses/{id}/students", courseId), null, status().isOk()).size()).isEqualTo(2);
        assertThat(send(get("/api/users/{id}/courses", studentId), null, status().isOk()).get(0).path("id").asLong())
                .isEqualTo(courseId);

        JsonNode submission = send(post("/api/assignments/{id}/submissions", assignmentId),
                Map.of("studentId", studentId, "content", "My solution"), status().isCreated());
        long submissionId = submission.path("id").asLong();
        send(patch("/api/submissions/{id}/grade", submissionId),
                Map.of("teacherId", teacherId, "score", 90, "feedback", "Good work"), status().isOk());
        JsonNode saved = send(get("/api/users/{id}/submissions", studentId), null, status().isOk()).get(0);
        assertThat(saved.path("score").asInt()).isEqualTo(90);
        send(post("/api/assignments/{id}/submissions", assignmentId),
                Map.of("studentId", studentId, "content", "Repeated solution"), status().isConflict());

        JsonNode quiz = send(get("/api/quizzes/{id}", quizId), null, status().isOk());
        assertThat(quiz.toString()).doesNotContain("\"correct\"");
        JsonNode single = quiz.path("questions").get(0);
        JsonNode multiple = quiz.path("questions").get(1);
        Map<String, List<Long>> answers = new HashMap<>();
        answers.put(single.path("id").asText(), List.of(single.path("options").get(0).path("id").asLong()));
        answers.put(multiple.path("id").asText(), List.of(multiple.path("options").get(0).path("id").asLong(),
                multiple.path("options").get(1).path("id").asLong()));
        JsonNode result = send(post("/api/quizzes/{id}/submissions", quizId),
                Map.of("studentId", studentId, "answers", answers), status().isCreated());
        assertThat(result.path("score").asInt()).isEqualTo(2);
        assertThat(result.path("maxScore").asInt()).isEqualTo(2);
        assertThat(send(get("/api/quizzes/{id}/submissions", quizId), null, status().isOk()).size()).isEqualTo(2);

        send(delete("/api/courses/{id}/enrollments/{studentId}", courseId, studentId), null, status().isNoContent());
        assertThat(send(get("/api/users/{id}/courses", studentId), null, status().isOk()).size()).isZero();
        assertThat(send(get("/api/users/{id}/submissions", studentId), null, status().isOk()).get(0).path("id").asLong())
                .isEqualTo(submissionId);
        assertThat(send(get("/api/users/{id}/quiz-submissions", studentId), null, status().isOk()).get(0).path("id").asLong())
                .isEqualTo(result.path("id").asLong());
    }

    private long userId(JsonNode userList, String email) {
        for (JsonNode user : userList) {
            if (user.path("email").asText().equals(email)) return user.path("id").asLong();
        }
        throw new AssertionError("Demo user not found: " + email);
    }

    private JsonNode send(MockHttpServletRequestBuilder request, Object body, ResultMatcher expected) throws Exception {
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body));
        var response = mvc.perform(request).andExpect(expected).andReturn().getResponse();
        return response.getStatus() == 204 ? json.nullNode() : json.readTree(response.getContentAsString());
    }
}
