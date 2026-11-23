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
import ru.study.platform.web.dto.ApiRequests.UserRequest;
import ru.study.platform.web.dto.ApiResponses.UserResponse;
import ru.study.platform.web.service.UserApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserApiService api;

    @GetMapping("/users")
    @Operation(summary = "Список пользователей")
    public List<UserResponse> list() {
        return api.list();
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Получить пользователя")
    public UserResponse get(@PathVariable @Positive Long id) {
        return api.get(id);
    }

    @PostMapping("/users")
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        var result = api.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + result.id())).body(result);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Обновить пользователя")
    public UserResponse update(@PathVariable @Positive Long id, @Valid @RequestBody UserRequest request) {
        return api.update(id, request);
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удалить пользователя")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        api.delete(id);
    }

}
