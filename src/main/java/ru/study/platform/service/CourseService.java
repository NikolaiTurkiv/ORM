package ru.study.platform.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Category;
import ru.study.platform.entity.Course;
import ru.study.platform.entity.Role;
import ru.study.platform.entity.Tag;
import ru.study.platform.entity.User;
import ru.study.platform.repository.CategoryRepository;
import ru.study.platform.repository.CourseRepository;
import ru.study.platform.repository.CourseReviewRepository;
import ru.study.platform.repository.EnrollmentRepository;
import ru.study.platform.repository.QuizSubmissionRepository;
import ru.study.platform.repository.SubmissionRepository;
import ru.study.platform.repository.TagRepository;
import ru.study.platform.repository.UserRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courses;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final TagRepository tags;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository quizSubmissions;
    private final CourseReviewRepository reviews;

    public List<Course> findAll() {
        return courses.findAll();
    }

    public Course findById(Long id) {
        return get(courses, id, "Course");
    }

    public List<Course> findByTeacher(Long teacherId) {
        get(users, teacherId, "Teacher");
        return courses.findByTeacher_Id(teacherId);
    }

    public List<Course> findByCategory(Long categoryId) {
        get(categories, categoryId, "Category");
        return courses.findByCategory_Id(categoryId);
    }

    @Transactional
    public Course create(String title, String description, Long teacherId, Long categoryId) {
        Course course = new Course();
        setDetails(course, title, description, teacherId, categoryId);
        return courses.save(course);
    }

    @Transactional
    public Course update(Long id, String title, String description, Long teacherId, Long categoryId) {
        Course course = findById(id);
        setDetails(course, title, description, teacherId, categoryId);
        return course;
    }

    @Transactional
    public Course updateSchedule(Long id, String duration, LocalDate startDate) {
        Course course = findById(id);
        course.setDuration(optionalText(duration, "Duration", 100));
        course.setStartDate(startDate);
        return course;
    }

    @Transactional
    public Course addTag(Long courseId, Long tagId) {
        Course course = findById(courseId);
        Tag tag = get(tags, tagId, "Tag");
        course.getTags().add(tag);
        tag.getCourses().add(course);
        return course;
    }

    @Transactional
    public Course removeTag(Long courseId, Long tagId) {
        Course course = findById(courseId);
        Tag tag = get(tags, tagId, "Tag");
        course.getTags().remove(tag);
        tag.getCourses().remove(course);
        return course;
    }

    @Transactional
    public void delete(Long id) {
        Course course = findById(id);
        conflict(enrollments.existsByCourse_Id(id)
                || submissions.existsByAssignment_Lesson_Module_Course_Id(id)
                || quizSubmissions.existsByQuiz_Module_Course_Id(id)
                || reviews.existsByCourse_Id(id), "Cannot delete a course with academic data");
        for (Tag tag : course.getTags()) {
            tag.getCourses().remove(course);
        }
        course.getTags().clear();
        courses.delete(course);
    }

    private void setDetails(Course course, String title, String description, Long teacherId, Long categoryId) {
        User teacher = get(users, teacherId, "Teacher");
        check(teacher.getRole() == Role.TEACHER, "Course teacher must have role TEACHER");
        course.setTitle(text(title, "Course title", 200));
        course.setDescription(optionalText(description, "Course description", 10000));
        course.setTeacher(teacher);
        course.setCategory(get(categories, categoryId, "Category"));
    }
}
