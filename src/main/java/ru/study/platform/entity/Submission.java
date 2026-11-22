package ru.study.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(name = "uk_submission_student_assignment", columnNames = {"student_id", "assignment_id"}))
@Check(constraints = "score is null or score >= 0")
public class Submission extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    @NotBlank @Size(max = 30000)
    @Column(nullable = false, length = 30000)
    private String content;

    @Min(0)
    private Integer score;

    @Size(max = 10000)
    @Column(length = 10000)
    private String feedback;
}
