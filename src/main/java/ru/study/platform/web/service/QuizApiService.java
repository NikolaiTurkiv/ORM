package ru.study.platform.web.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.QuizService;
import ru.study.platform.web.dto.ApiRequests.QuizAnswersRequest;
import ru.study.platform.web.dto.ApiRequests.QuizRequest;
import ru.study.platform.web.dto.ApiResponses.QuizResponse;
import ru.study.platform.web.dto.ApiResponses.QuizResultResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizApiService {
    private final QuizService quizzes;
    private final EntityManager entityManager;

    public QuizResponse get(Long id) {
        return QuizResponse.from(quizzes.findById(id));
    }

    @Transactional
    public QuizResponse create(Long id, QuizRequest request) {
        return QuizResponse.from(quizzes.create(id, request.toDefinition()));
    }

    @Transactional
    public QuizResponse update(Long id, QuizRequest request) {
        var quiz = quizzes.update(id, request.toDefinition());
        entityManager.flush();
        return QuizResponse.from(quiz);
    }

    @Transactional
    public void delete(Long id) {
        quizzes.delete(id);
    }

    @Transactional
    public QuizResultResponse take(Long id, QuizAnswersRequest request) {
        return QuizResultResponse.from(quizzes.takeQuiz(request.studentId(), id, request.answers()));
    }

    public QuizResultResponse result(Long id) {
        return QuizResultResponse.from(quizzes.findResult(id));
    }

    public List<QuizResultResponse> studentResults(Long id) {
        return quizzes.findResultsByStudent(id).stream().map(QuizResultResponse::from).toList();
    }

    public List<QuizResultResponse> quizResults(Long id) {
        return quizzes.findResultsByQuiz(id).stream().map(QuizResultResponse::from).toList();
    }
}
