package ru.study.platform.service;

import java.util.List;
import ru.study.platform.entity.QuestionType;

/** Full quiz definition used when creating or editing a quiz. */
public record QuizDefinition(String title, Integer timeLimit, List<QuestionDefinition> questions) {
    public record QuestionDefinition(String text, QuestionType type, List<OptionDefinition> options) {
    }

    public record OptionDefinition(String text, boolean correct) {
    }
}
