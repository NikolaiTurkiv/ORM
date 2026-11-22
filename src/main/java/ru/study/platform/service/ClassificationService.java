package ru.study.platform.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.platform.entity.Category;
import ru.study.platform.entity.Tag;
import ru.study.platform.repository.CategoryRepository;
import ru.study.platform.repository.CourseRepository;
import ru.study.platform.repository.TagRepository;

import static ru.study.platform.service.ServiceChecks.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassificationService {
    private final CategoryRepository categories;
    private final TagRepository tags;
    private final CourseRepository courses;

    public List<Category> findCategories() {
        return categories.findAll();
    }

    public Category findCategory(Long id) {
        return get(categories, id, "Category");
    }

    @Transactional
    public Category createCategory(String name) {
        String value = text(name, "Category name", 100);
        conflict(categories.existsByNameIgnoreCase(value), "Category already exists");
        Category category = new Category();
        category.setName(value);
        return categories.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name) {
        Category category = findCategory(id);
        String value = text(name, "Category name", 100);
        conflict(categories.existsByNameIgnoreCaseAndIdNot(value, id), "Category already exists");
        category.setName(value);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        conflict(courses.existsByCategory_Id(id), "Category is used by a course");
        categories.delete(category);
    }

    public List<Tag> findTags() {
        return tags.findAll();
    }

    public Tag findTag(Long id) {
        return get(tags, id, "Tag");
    }

    @Transactional
    public Tag createTag(String name) {
        String value = text(name, "Tag name", 100);
        conflict(tags.existsByNameIgnoreCase(value), "Tag already exists");
        Tag tag = new Tag();
        tag.setName(value);
        return tags.save(tag);
    }

    @Transactional
    public Tag updateTag(Long id, String name) {
        Tag tag = findTag(id);
        String value = text(name, "Tag name", 100);
        conflict(tags.existsByNameIgnoreCaseAndIdNot(value, id), "Tag already exists");
        tag.setName(value);
        return tag;
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = findTag(id);
        for (var course : List.copyOf(tag.getCourses())) {
            course.getTags().remove(tag);
        }
        tag.getCourses().clear();
        tags.delete(tag);
    }
}
