package ru.study.platform.web.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.service.UserService;
import ru.study.platform.web.dto.ApiRequests.UserRequest;
import ru.study.platform.web.dto.ApiResponses.UserResponse;

/** Maps entities to DTOs before the transaction closes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApiService {
    private final UserService users;

    public List<UserResponse> list() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse get(Long id) {
        return UserResponse.from(users.findById(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        return UserResponse.from(users.create(request.name(), request.email(), request.role()));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        return UserResponse.from(users.update(id, request.name(), request.email(), request.role()));
    }

    @Transactional
    public void delete(Long id) {
        users.delete(id);
    }

}
