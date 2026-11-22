package ru.study.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(name = "course_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_student_course", columnNames = {"student_id", "course_id"}))
@Check(constraints = "rating between 1 and 5")
public class CourseReview extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Size(max = 3000)
    @Column(length = 3000)
    private String comment;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
