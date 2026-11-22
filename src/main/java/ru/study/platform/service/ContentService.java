package ru.study.platform.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Course;
import ru.study.platform.entity.CourseModule;
import ru.study.platform.entity.Lesson;
import ru.study.platform.repository.CourseModuleRepository;
import ru.study.platform.repository.CourseRepository;
import ru.study.platform.repository.LessonRepository;
import ru.study.platform.repository.QuizSubmissionRepository;
import ru.study.platform.repository.SubmissionRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final SubmissionRepository submissions;
    private final QuizSubmissionRepository quizSubmissions;

    public CourseModule findModule(Long id) {
        return get(modules, id, "Module");
    }

    public List<CourseModule> findModules(Long courseId) {
        get(courses, courseId, "Course");
        return modules.findByCourse_IdOrderByOrderIndexAscIdAsc(courseId);
    }

    @Transactional
    public CourseModule createModule(Long courseId, String title, String description, int orderIndex) {
        Course course = get(courses, courseId, "Course");
        CourseModule module = new CourseModule();
        setModuleDetails(module, title, description, orderIndex);
        course.addModule(module);
        return modules.save(module);
    }

    @Transactional
    public CourseModule updateModule(Long id, String title, String description, int orderIndex) {
        CourseModule module = findModule(id);
        setModuleDetails(module, title, description, orderIndex);
        return module;
    }

    @Transactional
    public void deleteModule(Long id) {
        CourseModule module = findModule(id);
        conflict(submissions.existsByAssignment_Lesson_Module_Id(id)
                || quizSubmissions.existsByQuiz_Module_Id(id), "Module has student results");
        module.getCourse().getModules().remove(module);
        modules.delete(module);
    }

    public Lesson findLesson(Long id) {
        return get(lessons, id, "Lesson");
    }

    public List<Lesson> findLessons(Long moduleId) {
        findModule(moduleId);
        return lessons.findByModule_IdOrderByOrderIndexAscIdAsc(moduleId);
    }

    @Transactional
    public Lesson createLesson(Long moduleId, String title, String content, String videoUrl, int orderIndex) {
        CourseModule module = findModule(moduleId);
        Lesson lesson = new Lesson();
        setLessonDetails(lesson, title, content, videoUrl, orderIndex);
        module.addLesson(lesson);
        return lessons.save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Long id, String title, String content, String videoUrl, int orderIndex) {
        Lesson lesson = findLesson(id);
        setLessonDetails(lesson, title, content, videoUrl, orderIndex);
        return lesson;
    }

    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = findLesson(id);
        conflict(submissions.existsByAssignment_Lesson_Id(id), "Lesson has student submissions");
        lesson.getModule().getLessons().remove(lesson);
        lessons.delete(lesson);
    }

    private void setModuleDetails(CourseModule module, String title, String description, int orderIndex) {
        check(orderIndex >= 0, "Module order must be non-negative");
        module.setTitle(text(title, "Module title", 200));
        module.setDescription(optionalText(description, "Module description", 10000));
        module.setOrderIndex(orderIndex);
    }

    private void setLessonDetails(Lesson lesson, String title, String content, String videoUrl, int orderIndex) {
        check(orderIndex >= 0, "Lesson order must be non-negative");
        lesson.setTitle(text(title, "Lesson title", 200));
        lesson.setContent(optionalText(content, "Lesson content", 30000));
        lesson.setVideoUrl(optionalText(videoUrl, "Video URL", 1000));
        lesson.setOrderIndex(orderIndex);
    }
}
