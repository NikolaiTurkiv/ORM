package ru.study.platform.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Assignment;
import ru.study.platform.entity.Lesson;
import ru.study.platform.entity.Role;
import ru.study.platform.entity.Submission;
import ru.study.platform.entity.User;
import ru.study.platform.repository.AssignmentRepository;
import ru.study.platform.repository.LessonRepository;
import ru.study.platform.repository.SubmissionRepository;
import ru.study.platform.repository.UserRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {
    private final AssignmentRepository assignments;
    private final LessonRepository lessons;
    private final SubmissionRepository submissions;
    private final UserRepository users;
    private final EnrollmentService enrollmentService;

    public Assignment findById(Long id) {
        return get(assignments, id, "Assignment");
    }

    public List<Assignment> findByLesson(Long lessonId) {
        get(lessons, lessonId, "Lesson");
        return assignments.findByLesson_Id(lessonId);
    }

    @Transactional
    public Assignment create(Long lessonId, String title, String description, Instant dueDate, int maxScore) {
        Lesson lesson = get(lessons, lessonId, "Lesson");
        Assignment assignment = new Assignment();
        setDetails(assignment, title, description, dueDate, maxScore);
        lesson.addAssignment(assignment);
        return assignments.save(assignment);
    }

    @Transactional
    public Assignment update(Long id, String title, String description, Instant dueDate, int maxScore) {
        Assignment assignment = findById(id);
        conflict(assignment.getMaxScore() != maxScore && submissions.existsByAssignment_Id(id),
                "Cannot change maximum score after students have submitted work");
        setDetails(assignment, title, description, dueDate, maxScore);
        return assignment;
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = findById(id);
        conflict(submissions.existsByAssignment_Id(id), "Assignment has student submissions");
        assignment.getLesson().getAssignments().remove(assignment);
        assignments.delete(assignment);
    }

    @Transactional
    public Submission submit(Long studentId, Long assignmentId, String content) {
        Assignment assignment = findById(assignmentId);
        Long courseId = assignment.getLesson().getModule().getCourse().getId();
        enrollmentService.requireActiveStudent(studentId, courseId);
        conflict(submissions.existsByStudent_IdAndAssignment_Id(studentId, assignmentId),
                "Student has already submitted this assignment");
        check(assignment.getDueDate() == null || !Instant.now().isAfter(assignment.getDueDate()),
                "Assignment deadline has passed");
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(get(users, studentId, "Student"));
        submission.setContent(text(content, "Submission content", 30000));
        assignment.getSubmissions().add(submission);
        submission.getStudent().getSubmissions().add(submission);
        return submissions.save(submission);
    }

    public Submission findSubmission(Long id) {
        return get(submissions, id, "Submission");
    }

    public List<Submission> findSubmissionsByStudent(Long studentId) {
        get(users, studentId, "Student");
        return submissions.findByStudent_Id(studentId);
    }

    public List<Submission> findSubmissionsByAssignment(Long assignmentId) {
        findById(assignmentId);
        return submissions.findByAssignment_Id(assignmentId);
    }

    @Transactional
    public Submission grade(Long submissionId, Long teacherId, int score, String feedback) {
        Submission submission = findSubmission(submissionId);
        Assignment assignment = submission.getAssignment();
        User teacher = get(users, teacherId, "Teacher");
        check(teacher.getRole() == Role.TEACHER
                && assignment.getLesson().getModule().getCourse().getTeacher().getId().equals(teacherId),
                "Only the course teacher can grade submissions");
        check(score >= 0 && score <= assignment.getMaxScore(), "Score is outside the assignment range");
        submission.setScore(score);
        submission.setFeedback(optionalText(feedback, "Feedback", 10000));
        return submission;
    }

    private void setDetails(Assignment assignment, String title, String description, Instant dueDate, int maxScore) {
        check(maxScore > 0, "Maximum score must be positive");
        assignment.setTitle(text(title, "Assignment title", 200));
        assignment.setDescription(text(description, "Assignment description", 10000));
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(maxScore);
    }
}
