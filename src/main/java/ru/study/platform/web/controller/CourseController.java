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
import ru.study.platform.web.dto.ApiRequests.CourseRequest;
import ru.study.platform.web.dto.ApiResponses.CourseResponse;
import ru.study.platform.web.service.CourseApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Course")
public class CourseController {
    private final CourseApiService api;

    @GetMapping("/courses")
    @Operation(summary = "Список курсов")
    public List<CourseResponse> list() {
        return api.list();
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Курс с модулями и уроками")
    public CourseResponse get(@PathVariable @Positive Long id) {
        return api.get(id);
    }

    @PostMapping("/courses")
    @Operation(summary = "Создать курс")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        var result = api.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + result.id())).body(result);
    }

    @PutMapping("/courses/{id}")
    @Operation(summary = "Обновить курс")
    public CourseResponse update(@PathVariable @Positive Long id, @Valid @RequestBody CourseRequest request) {
        return api.update(id, request);
    }

    @DeleteMapping("/courses/{id}")
    @Operation(summary = "Удалить курс")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        api.delete(id);
    }

}
