package ru.study.platform.web.controller;

import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.study.platform.web.dto.ApiRequests.NameRequest;
import ru.study.platform.web.dto.ApiResponses.NamedResponse;
import ru.study.platform.web.service.ClassificationApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Classification")
public class ClassificationController {
    private final ClassificationApiService api;

    @GetMapping("/categories")
    @Operation(summary = "Список категорий")
    public List<NamedResponse> categories() {
        return api.categories();
    }

    @PostMapping("/categories")
    @Operation(summary = "Создать категорию")
    public ResponseEntity<NamedResponse> createCategory(@Valid @RequestBody NameRequest request) {
        var result = api.createCategory(request);
        return ResponseEntity.created(URI.create("/api/categories/" + result.id())).body(result);
    }


    @GetMapping("/categories/{id}")
    @Operation(summary = "Получить категорию")
    public NamedResponse category(@PathVariable @Positive Long id) {
        return api.category(id);
    }
}
