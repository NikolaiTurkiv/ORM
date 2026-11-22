package ru.study.platform.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Course;
import ru.study.platform.entity.Enrollment;
import ru.study.platform.entity.EnrollmentStatus;
import ru.study.platform.entity.Role;
import ru.study.platform.entity.User;
import ru.study.platform.exception.NotFoundException;
import ru.study.platform.repository.CourseRepository;
import ru.study.platform.repository.EnrollmentRepository;
import ru.study.platform.repository.UserRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final CourseRepository courses;

    @Transactional
    public Enrollment enroll(Long studentId, Long courseId) {
        User student = get(users, studentId, "Student");
        check(student.getRole() == Role.STUDENT, "Only students can enroll");
        Course course = get(courses, courseId, "Course");
        conflict(enrollments.existsByStudent_IdAndCourse_Id(studentId, courseId), "Student is already enrolled");
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        student.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);
        return enrollments.save(enrollment);
    }

    public Enrollment findById(Long id) {
        return get(enrollments, id, "Enrollment");
    }

    public List<Enrollment> findByStudent(Long studentId) {
        get(users, studentId, "Student");
        return enrollments.findByStudent_Id(studentId);
    }

    public List<Enrollment> findByCourse(Long courseId) {
        get(courses, courseId, "Course");
        return enrollments.findByCourse_Id(courseId);
    }

    @Transactional
    public Enrollment updateStatus(Long id, EnrollmentStatus status) {
        check(status != null, "Enrollment status is required");
        Enrollment enrollment = findById(id);
        enrollment.setStatus(status);
        return enrollment;
    }

    @Transactional
    public void unenroll(Long studentId, Long courseId) {
        get(users, studentId, "Student");
        get(courses, courseId, "Course");
        Enrollment enrollment = enrollments.findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        enrollment.getStudent().getEnrollments().remove(enrollment);
        enrollment.getCourse().getEnrollments().remove(enrollment);
        enrollments.delete(enrollment);
    }

    public void requireActiveStudent(Long studentId, Long courseId) {
        User student = get(users, studentId, "Student");
        check(student.getRole() == Role.STUDENT, "User must have role STUDENT");
        check(enrollments.existsByStudent_IdAndCourse_IdAndStatus(studentId, courseId, EnrollmentStatus.ACTIVE),
                "Student must have an active enrollment in this course");
    }
}
