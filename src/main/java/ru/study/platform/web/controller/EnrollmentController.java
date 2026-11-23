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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.study.platform.web.dto.ApiRequests.EnrollmentRequest;
import ru.study.platform.web.dto.ApiResponses.CourseResponse;
import ru.study.platform.web.dto.ApiResponses.EnrollmentResponse;
import ru.study.platform.web.dto.ApiResponses.UserResponse;
import ru.study.platform.web.service.EnrollmentApiService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Enrollment")
public class EnrollmentController {
    private final EnrollmentApiService api;

    @GetMapping("/enrollments/{id}")
    @Operation(summary = "Получить запись на курс")
    public EnrollmentResponse get(@PathVariable @Positive Long id) {
        return api.get(id);
    }

    @PostMapping("/courses/{id}/enrollments")
    @Operation(summary = "Записать студента на курс")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable @Positive Long id, @Valid @RequestBody EnrollmentRequest request) {
        var result = api.enroll(id, request);
        return ResponseEntity.created(URI.create("/api/enrollments/" + result.id())).body(result);
    }

    @DeleteMapping("/courses/{id}/enrollments/{studentId}")
    @Operation(summary = "Отписать студента от курса")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable @Positive Long id, @PathVariable @Positive Long studentId) {
        api.unenroll(id, studentId);
    }

    @GetMapping("/users/{id}/courses")
    @Operation(summary = "Курсы студента")
    public List<CourseResponse> studentCourses(@PathVariable @Positive Long id) {
        return api.studentCourses(id);
    }

    @GetMapping("/courses/{id}/students")
    @Operation(summary = "Студенты курса")
    public List<UserResponse> courseStudents(@PathVariable @Positive Long id) {
        return api.courseStudents(id);
    }
}
