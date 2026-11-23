package ru.study.platform.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.study.platform.web.dto.ApiRequests.LessonRequest;
import ru.study.platform.web.dto.ApiRequests.ModuleRequest;
import ru.study.platform.web.dto.ApiResponses.LessonResponse;
import ru.study.platform.web.dto.ApiResponses.ModuleResponse;
import ru.study.platform.web.service.ContentApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Content")
public class ContentController {
    private final ContentApiService api;

    @PostMapping("/courses/{id}/modules")
    @Operation(summary = "Добавить модуль")
    public ResponseEntity<ModuleResponse> createModule(@PathVariable @Positive Long id, @Valid @RequestBody ModuleRequest request) {
        var result = api.createModule(id, request);
        return ResponseEntity.created(URI.create("/api/modules/" + result.id())).body(result);
    }

    @PutMapping("/modules/{id}")
    @Operation(summary = "Обновить модуль")
    public ModuleResponse updateModule(@PathVariable @Positive Long id, @Valid @RequestBody ModuleRequest request) {
        return api.updateModule(id, request);
    }

    @DeleteMapping("/modules/{id}")
    @Operation(summary = "Удалить модуль")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable @Positive Long id) {
        api.deleteModule(id);
    }

    @PostMapping("/modules/{id}/lessons")
    @Operation(summary = "Добавить урок")
    public ResponseEntity<LessonResponse> createLesson(@PathVariable @Positive Long id, @Valid @RequestBody LessonRequest request) {
        var result = api.createLesson(id, request);
        return ResponseEntity.created(URI.create("/api/lessons/" + result.id())).body(result);
    }

    @PutMapping("/lessons/{id}")
    @Operation(summary = "Обновить урок")
    public LessonResponse updateLesson(@PathVariable @Positive Long id, @Valid @RequestBody LessonRequest request) {
        return api.updateLesson(id, request);
    }

    @DeleteMapping("/lessons/{id}")
    @Operation(summary = "Удалить урок")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable @Positive Long id) {
        api.deleteLesson(id);
    }

    @GetMapping("/modules/{id}")
    @Operation(summary = "Получить модуль")
    public ModuleResponse module(@PathVariable @Positive Long id) {
        return api.module(id);
    }

    @GetMapping("/lessons/{id}")
    @Operation(summary = "Получить урок")
    public LessonResponse lesson(@PathVariable @Positive Long id) {
        return api.lesson(id);
    }
}
