package ru.study.platform.web.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.CourseService;
import ru.study.platform.web.dto.ApiRequests.CourseRequest;
import ru.study.platform.web.dto.ApiResponses.CourseResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseApiService {
    private final CourseService courses;

    public List<CourseResponse> list() {
        return courses.findAll().stream().map(CourseResponse::from).toList();
    }

    public CourseResponse get(Long id) {
        return CourseResponse.from(courses.findById(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        var course = courses.create(request.title(), request.description(), request.teacherId(), request.categoryId());
        courses.updateSchedule(course.getId(), request.duration(), request.startDate());
        return CourseResponse.from(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        var course = courses.update(id, request.title(), request.description(), request.teacherId(), request.categoryId());
        courses.updateSchedule(id, request.duration(), request.startDate());
        return CourseResponse.from(course);
    }

    @Transactional
    public void delete(Long id) {
        courses.delete(id);
    }

}
