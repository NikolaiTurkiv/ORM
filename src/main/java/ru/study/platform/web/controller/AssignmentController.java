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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.study.platform.web.dto.ApiRequests.AssignmentRequest;
import ru.study.platform.web.dto.ApiRequests.GradeRequest;
import ru.study.platform.web.dto.ApiRequests.SubmissionRequest;
import ru.study.platform.web.dto.ApiResponses.AssignmentResponse;
import ru.study.platform.web.dto.ApiResponses.SubmissionResponse;
import ru.study.platform.web.service.AssignmentApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Assignment")
public class AssignmentController {
    private final AssignmentApiService api;

    @GetMapping("/assignments/{id}")
    @Operation(summary = "Получить задание")
    public AssignmentResponse get(@PathVariable @Positive Long id) {
        return api.get(id);
    }

    @PostMapping("/lessons/{id}/assignments")
    @Operation(summary = "Добавить задание")
    public ResponseEntity<AssignmentResponse> create(@PathVariable @Positive Long id, @Valid @RequestBody AssignmentRequest request) {
        var result = api.create(id, request);
        return ResponseEntity.created(URI.create("/api/assignments/" + result.id())).body(result);
    }

    @PutMapping("/assignments/{id}")
    @Operation(summary = "Обновить задание")
    public AssignmentResponse update(@PathVariable @Positive Long id, @Valid @RequestBody AssignmentRequest request) {
        return api.update(id, request);
    }

    @DeleteMapping("/assignments/{id}")
    @Operation(summary = "Удалить задание")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        api.delete(id);
    }

    @PostMapping("/assignments/{id}/submissions")
    @Operation(summary = "Сдать решение")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable @Positive Long id, @Valid @RequestBody SubmissionRequest request) {
        var result = api.submit(id, request);
        return ResponseEntity.created(URI.create("/api/submissions/" + result.id())).body(result);
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Получить решение")
    public SubmissionResponse submission(@PathVariable @Positive Long id) {
        return api.submission(id);
    }

    @PatchMapping("/submissions/{id}/grade")
    @Operation(summary = "Оценить решение")
    public SubmissionResponse grade(@PathVariable @Positive Long id, @Valid @RequestBody GradeRequest request) {
        return api.grade(id, request);
    }

    @GetMapping("/users/{id}/submissions")
    @Operation(summary = "Решения студента")
    public List<SubmissionResponse> studentSubmissions(@PathVariable @Positive Long id) {
        return api.studentSubmissions(id);
    }

    @GetMapping("/assignments/{id}/submissions")
    @Operation(summary = "Решения задания")
    public List<SubmissionResponse> assignmentSubmissions(@PathVariable @Positive Long id) {
        return api.assignmentSubmissions(id);
    }
}
