package ru.study.platform.service;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.study.platform.exception.BusinessRuleException;
import ru.study.platform.exception.ConflictException;
import ru.study.platform.exception.NotFoundException;

final class ServiceChecks {
    private ServiceChecks() {
    }

    static <T> T get(JpaRepository<T, Long> repository, Long id, String entity) {
        check(id != null && id > 0, entity + " ID must be positive");
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(entity + " not found: " + id));
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new BusinessRuleException(message);
        }
    }

    static void conflict(boolean condition, String message) {
        if (condition) {
            throw new ConflictException(message);
        }
    }

    static String text(String value, String field, int maxLength) {
        check(value != null && !value.isBlank(), field + " is required");
        check(value.trim().length() <= maxLength, field + " is too long");
        return value.trim();
    }

    static String optionalText(String value, String field, int maxLength) {
        check(value == null || value.length() <= maxLength, field + " is too long");
        return value;
    }
}
