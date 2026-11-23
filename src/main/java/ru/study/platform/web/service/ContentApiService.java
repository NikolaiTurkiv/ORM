package ru.study.platform.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.ContentService;
import ru.study.platform.web.dto.ApiRequests.LessonRequest;
import ru.study.platform.web.dto.ApiRequests.ModuleRequest;
import ru.study.platform.web.dto.ApiResponses.LessonResponse;
import ru.study.platform.web.dto.ApiResponses.ModuleResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentApiService {
    private final ContentService content;

    @Transactional
    public ModuleResponse createModule(Long id, ModuleRequest request) {
        return ModuleResponse.from(content.createModule(id, request.title(), request.description(), request.orderIndex()));
    }

    @Transactional
    public ModuleResponse updateModule(Long id, ModuleRequest request) {
        return ModuleResponse.from(content.updateModule(id, request.title(), request.description(), request.orderIndex()));
    }

    @Transactional
    public void deleteModule(Long id) {
        content.deleteModule(id);
    }

    @Transactional
    public LessonResponse createLesson(Long id, LessonRequest request) {
        return LessonResponse.from(content.createLesson(id, request.title(), request.content(), request.videoUrl(), request.orderIndex()));
    }

    @Transactional
    public LessonResponse updateLesson(Long id, LessonRequest request) {
        return LessonResponse.from(content.updateLesson(id, request.title(), request.content(), request.videoUrl(), request.orderIndex()));
    }

    @Transactional
    public void deleteLesson(Long id) {
        content.deleteLesson(id);
    }

    public ModuleResponse module(Long id) {
        return ModuleResponse.from(content.findModule(id));
    }

    public LessonResponse lesson(Long id) {
        return LessonResponse.from(content.findLesson(id));
    }
}
