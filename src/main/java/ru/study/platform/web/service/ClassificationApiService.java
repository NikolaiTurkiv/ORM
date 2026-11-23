package ru.study.platform.web.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.ClassificationService;
import ru.study.platform.web.dto.ApiRequests.NameRequest;
import ru.study.platform.web.dto.ApiResponses.NamedResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassificationApiService {
    private final ClassificationService classification;

    public List<NamedResponse> categories() {
        return classification.findCategories().stream().map(NamedResponse::from).toList();
    }

    @Transactional
    public NamedResponse createCategory(NameRequest request) {
        return NamedResponse.from(classification.createCategory(request.name()));
    }


    public NamedResponse category(Long id) {
        return NamedResponse.from(classification.findCategory(id));
    }
}
