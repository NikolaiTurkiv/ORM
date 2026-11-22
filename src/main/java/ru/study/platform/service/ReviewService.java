package ru.study.platform.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Course;
import ru.study.platform.entity.CourseReview;
import ru.study.platform.entity.Role;
import ru.study.platform.entity.User;
import ru.study.platform.repository.CourseRepository;
import ru.study.platform.repository.CourseReviewRepository;
import ru.study.platform.repository.EnrollmentRepository;
import ru.study.platform.repository.UserRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final CourseReviewRepository reviews;
    private final CourseRepository courses;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    public CourseReview findById(Long id) {
        return get(reviews, id, "Review");
    }

    public List<CourseReview> findByCourse(Long courseId) {
        get(courses, courseId, "Course");
        return reviews.findByCourse_Id(courseId);
    }

    @Transactional
    public CourseReview create(Long studentId, Long courseId, int rating, String comment) {
        User student = get(users, studentId, "Student");
        Course course = get(courses, courseId, "Course");
        check(student.getRole() == Role.STUDENT, "Only students can review courses");
        check(enrollments.existsByStudent_IdAndCourse_Id(studentId, courseId),
                "Student must be enrolled in this course");
        conflict(reviews.existsByStudent_IdAndCourse_Id(studentId, courseId), "Student has already reviewed this course");
        CourseReview review = new CourseReview();
        review.setStudent(student);
        review.setCourse(course);
        setDetails(review, rating, comment);
        student.getReviews().add(review);
        course.getReviews().add(review);
        return reviews.save(review);
    }

    @Transactional
    public CourseReview update(Long id, int rating, String comment) {
        CourseReview review = findById(id);
        setDetails(review, rating, comment);
        return review;
    }

    @Transactional
    public void delete(Long id) {
        CourseReview review = findById(id);
        review.getStudent().getReviews().remove(review);
        review.getCourse().getReviews().remove(review);
        reviews.delete(review);
    }

    private void setDetails(CourseReview review, int rating, String comment) {
        check(rating >= 1 && rating <= 5, "Rating must be between 1 and 5");
        review.setRating(rating);
        review.setComment(optionalText(comment, "Review comment", 3000));
    }
}
