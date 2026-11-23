package ru.study.platform.web.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.AssignmentService;
import ru.study.platform.web.dto.ApiRequests.AssignmentRequest;
import ru.study.platform.web.dto.ApiRequests.GradeRequest;
import ru.study.platform.web.dto.ApiRequests.SubmissionRequest;
import ru.study.platform.web.dto.ApiResponses.AssignmentResponse;
import ru.study.platform.web.dto.ApiResponses.SubmissionResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentApiService {
    private final AssignmentService assignments;

    public AssignmentResponse get(Long id) {
        return AssignmentResponse.from(assignments.findById(id));
    }

    @Transactional
    public AssignmentResponse create(Long id, AssignmentRequest request) {
        return AssignmentResponse.from(assignments.create(id, request.title(), request.description(), request.dueDate(), request.maxScore()));
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request) {
        return AssignmentResponse.from(assignments.update(id, request.title(), request.description(), request.dueDate(), request.maxScore()));
    }

    @Transactional
    public void delete(Long id) {
        assignments.delete(id);
    }

    @Transactional
    public SubmissionResponse submit(Long id, SubmissionRequest request) {
        return SubmissionResponse.from(assignments.submit(request.studentId(), id, request.content()));
    }

    public SubmissionResponse submission(Long id) {
        return SubmissionResponse.from(assignments.findSubmission(id));
    }

    @Transactional
    public SubmissionResponse grade(Long id, GradeRequest request) {
        return SubmissionResponse.from(assignments.grade(id, request.teacherId(), request.score(), request.feedback()));
    }

    public List<SubmissionResponse> studentSubmissions(Long id) {
        return assignments.findSubmissionsByStudent(id).stream().map(SubmissionResponse::from).toList();
    }

    public List<SubmissionResponse> assignmentSubmissions(Long id) {
        return assignments.findSubmissionsByAssignment(id).stream().map(SubmissionResponse::from).toList();
    }
}
