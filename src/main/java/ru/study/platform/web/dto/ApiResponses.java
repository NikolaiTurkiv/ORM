package ru.study.platform.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import ru.study.platform.entity.Assignment;
import ru.study.platform.entity.Category;
import ru.study.platform.entity.Course;
import ru.study.platform.entity.CourseModule;
import ru.study.platform.entity.Enrollment;
import ru.study.platform.entity.EnrollmentStatus;
import ru.study.platform.entity.Lesson;
import ru.study.platform.entity.Question;
import ru.study.platform.entity.QuestionType;
import ru.study.platform.entity.Quiz;
import ru.study.platform.entity.QuizSubmission;
import ru.study.platform.entity.Role;
import ru.study.platform.entity.Submission;
import ru.study.platform.entity.Tag;
import ru.study.platform.entity.User;

public final class ApiResponses {
    private ApiResponses() { }

    public record UserResponse(Long id, String name, String email, Role role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
        }
    }

    public record NamedResponse(Long id, String name) {
        public static NamedResponse from(Category c) { return new NamedResponse(c.getId(), c.getName()); }
        public static NamedResponse from(Tag t) { return new NamedResponse(t.getId(), t.getName()); }
    }
    public record AssignmentResponse(Long id, Long lessonId, String title, String description,
                                     Instant dueDate, int maxScore) {
        public static AssignmentResponse from(Assignment a) {
            return new AssignmentResponse(a.getId(), a.getLesson().getId(), a.getTitle(),
                    a.getDescription(), a.getDueDate(), a.getMaxScore());
        }
    }
    public record LessonResponse(Long id, Long moduleId, String title, String content, String videoUrl,
                                 int orderIndex, List<AssignmentResponse> assignments) {
        public static LessonResponse from(Lesson l) {
            return new LessonResponse(l.getId(), l.getModule().getId(), l.getTitle(), l.getContent(),
                    l.getVideoUrl(), l.getOrderIndex(), l.getAssignments().stream().map(AssignmentResponse::from).toList());
        }
    }
    public record ModuleResponse(Long id, Long courseId, String title, String description, int orderIndex,
                                 Long quizId, List<LessonResponse> lessons) {
        public static ModuleResponse from(CourseModule m) {
            return new ModuleResponse(m.getId(), m.getCourse().getId(), m.getTitle(), m.getDescription(),
                    m.getOrderIndex(), m.getQuiz() == null ? null : m.getQuiz().getId(),
                    m.getLessons().stream().map(LessonResponse::from).toList());
        }
    }
    public record CourseResponse(Long id, String title, String description, UserResponse teacher,
                                 NamedResponse category, String duration, LocalDate startDate,
                                 List<NamedResponse> tags, List<ModuleResponse> modules) {
        public static CourseResponse from(Course c) {
            return new CourseResponse(c.getId(), c.getTitle(), c.getDescription(), UserResponse.from(c.getTeacher()),
                    NamedResponse.from(c.getCategory()), c.getDuration(), c.getStartDate(),
                    c.getTags().stream().sorted(java.util.Comparator.comparing(Tag::getId)).map(NamedResponse::from).toList(),
                    c.getModules().stream().map(ModuleResponse::from).toList());
        }
    }
    public record EnrollmentResponse(Long id, Long studentId, Long courseId, Instant enrolledAt,
                                     EnrollmentStatus status) {
        public static EnrollmentResponse from(Enrollment e) {
            return new EnrollmentResponse(e.getId(), e.getStudent().getId(), e.getCourse().getId(), e.getEnrolledAt(), e.getStatus());
        }
    }
    public record SubmissionResponse(Long id, Long assignmentId, Long studentId, Instant submittedAt,
                                     String content, Integer score, String feedback) {
        public static SubmissionResponse from(Submission s) {
            return new SubmissionResponse(s.getId(), s.getAssignment().getId(), s.getStudent().getId(),
                    s.getSubmittedAt(), s.getContent(), s.getScore(), s.getFeedback());
        }
    }
    public record OptionResponse(Long id, String text) { }
    public record QuestionResponse(Long id, String text, QuestionType type, List<OptionResponse> options) {
        public static QuestionResponse from(Question q) {
            return new QuestionResponse(q.getId(), q.getText(), q.getType(), q.getOptions().stream()
                    .map(o -> new OptionResponse(o.getId(), o.getText())).toList());
        }
    }
    public record QuizResponse(Long id, Long moduleId, String title, Integer timeLimit,
                               List<QuestionResponse> questions) {
        public static QuizResponse from(Quiz q) {
            return new QuizResponse(q.getId(), q.getModule().getId(), q.getTitle(), q.getTimeLimit(),
                    q.getQuestions().stream().map(QuestionResponse::from).toList());
        }
    }
    public record QuizResultResponse(Long id, Long quizId, Long studentId, int score, int maxScore, Instant takenAt) {
        public static QuizResultResponse from(QuizSubmission s) {
            return new QuizResultResponse(s.getId(), s.getQuiz().getId(), s.getStudent().getId(),
                    s.getScore(), s.getMaxScore(), s.getTakenAt());
        }
    }

}
