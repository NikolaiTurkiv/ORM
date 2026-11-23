package ru.study.platform.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.study.platform.entity.QuestionType;
import ru.study.platform.entity.Role;
import ru.study.platform.service.QuizDefinition;

public final class ApiRequests {
    private ApiRequests() { }

    public record UserRequest(@NotBlank @Size(max = 120) String name,
                              @NotBlank @Email @Size(max = 254) String email, @NotNull Role role) { }

    public record NameRequest(@NotBlank @Size(max = 100) String name) { }
    public record CourseRequest(@NotBlank @Size(max = 200) String title, @Size(max = 10000) String description,
                                @NotNull @Positive Long teacherId, @NotNull @Positive Long categoryId,
                                @Size(max = 100) String duration, LocalDate startDate) { }
    public record ModuleRequest(@NotBlank @Size(max = 200) String title, @Size(max = 10000) String description,
                                @NotNull @PositiveOrZero Integer orderIndex) { }
    public record LessonRequest(@NotBlank @Size(max = 200) String title, @Size(max = 30000) String content,
                                @Size(max = 1000) String videoUrl, @NotNull @PositiveOrZero Integer orderIndex) { }
    public record AssignmentRequest(@NotBlank @Size(max = 200) String title,
                                    @NotBlank @Size(max = 10000) String description,
                                    Instant dueDate, @NotNull @Positive Integer maxScore) { }
    public record EnrollmentRequest(@NotNull @Positive Long studentId) { }

    public record SubmissionRequest(@NotNull @Positive Long studentId,
                                    @NotBlank @Size(max = 30000) String content) { }
    public record GradeRequest(@NotNull @Positive Long teacherId, @NotNull @PositiveOrZero Integer score,
                               @Size(max = 10000) String feedback) { }
    public record OptionRequest(@NotBlank @Size(max = 2000) String text, @NotNull Boolean correct) { }
    public record QuestionRequest(@NotBlank @Size(max = 2000) String text, @NotNull QuestionType type,
                                  @NotNull @Size(min = 2) List<@NotNull @Valid OptionRequest> options) { }
    public record QuizRequest(@NotBlank @Size(max = 200) String title, @Positive Integer timeLimit,
                              @NotEmpty List<@NotNull @Valid QuestionRequest> questions) {
        public QuizDefinition toDefinition() {
            return new QuizDefinition(title, timeLimit, questions.stream().map(q ->
                    new QuizDefinition.QuestionDefinition(q.text(), q.type(), q.options().stream().map(o ->
                            new QuizDefinition.OptionDefinition(o.text(), o.correct())).toList())).toList());
        }
    }
    public record QuizAnswersRequest(@NotNull @Positive Long studentId,
                                     @NotNull Map<@NotNull @Positive Long,
                                             @NotNull Set<@NotNull @Positive Long>> answers) { }

}
