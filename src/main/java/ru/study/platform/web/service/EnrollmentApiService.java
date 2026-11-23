package ru.study.platform.web.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.EnrollmentService;
import ru.study.platform.web.dto.ApiRequests.EnrollmentRequest;
import ru.study.platform.web.dto.ApiResponses.CourseResponse;
import ru.study.platform.web.dto.ApiResponses.EnrollmentResponse;
import ru.study.platform.web.dto.ApiResponses.UserResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentApiService {
    private final EnrollmentService enrollments;

    public EnrollmentResponse get(Long id) {
        return EnrollmentResponse.from(enrollments.findById(id));
    }

    @Transactional
    public EnrollmentResponse enroll(Long id, EnrollmentRequest request) {
        return EnrollmentResponse.from(enrollments.enroll(request.studentId(), id));
    }

    @Transactional
    public void unenroll(Long id, Long studentId) {
        enrollments.unenroll(studentId, id);
    }

    public List<CourseResponse> studentCourses(Long id) {
        return enrollments.findByStudent(id).stream().map(e -> CourseResponse.from(e.getCourse())).toList();
    }

    public List<UserResponse> courseStudents(Long id) {
        return enrollments.findByCourse(id).stream().map(e -> UserResponse.from(e.getStudent())).toList();
    }
}
