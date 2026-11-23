package ru.study.platform.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.study.platform.web.dto.ApiRequests.QuizAnswersRequest;
import ru.study.platform.web.dto.ApiRequests.QuizRequest;
import ru.study.platform.web.dto.ApiResponses.QuizResponse;
import ru.study.platform.web.dto.ApiResponses.QuizResultResponse;
import ru.study.platform.web.service.QuizApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Quiz")
public class QuizController {
    private final QuizApiService api;

    @GetMapping("/quizzes/{id}")
    @Operation(summary = "Тест без признаков правильных ответов")
    public QuizResponse get(@PathVariable @Positive Long id) {
        return api.get(id);
    }

    @PostMapping("/modules/{id}/quiz")
    @Operation(summary = "Создать тест модуля")
    public ResponseEntity<QuizResponse> create(@PathVariable @Positive Long id, @Valid @RequestBody QuizRequest request) {
        var result = api.create(id, request);
        return ResponseEntity.created(URI.create("/api/quizzes/" + result.id())).body(result);
    }

    @PutMapping("/quizzes/{id}")
    @Operation(summary = "Обновить тест")
    public QuizResponse update(@PathVariable @Positive Long id, @Valid @RequestBody QuizRequest request) {
        return api.update(id, request);
    }

    @DeleteMapping("/quizzes/{id}")
    @Operation(summary = "Удалить тест")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        api.delete(id);
    }

    @PostMapping("/quizzes/{id}/submissions")
    @Operation(summary = "Пройти тест")
    public ResponseEntity<QuizResultResponse> take(@PathVariable @Positive Long id, @Valid @RequestBody QuizAnswersRequest request) {
        var result = api.take(id, request);
        return ResponseEntity.created(URI.create("/api/quiz-submissions/" + result.id())).body(result);
    }

    @GetMapping("/quiz-submissions/{id}")
    @Operation(summary = "Получить результат теста")
    public QuizResultResponse result(@PathVariable @Positive Long id) {
        return api.result(id);
    }

    @GetMapping("/users/{id}/quiz-submissions")
    @Operation(summary = "Результаты студента")
    public List<QuizResultResponse> studentResults(@PathVariable @Positive Long id) {
        return api.studentResults(id);
    }

    @GetMapping("/quizzes/{id}/submissions")
    @Operation(summary = "Результаты теста")
    public List<QuizResultResponse> quizResults(@PathVariable @Positive Long id) {
        return api.quizResults(id);
    }
}
