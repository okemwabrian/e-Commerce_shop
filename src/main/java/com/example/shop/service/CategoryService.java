package com.example.shop.service;

import com.example.shop.dto.CategoryDtos.CategoryRequest;
import com.example.shop.dto.CategoryDtos.CategoryResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.Category;
import com.example.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository repo;

    public static String slugify(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> tree() {
        List<Category> all = repo.findAll();
        Map<Long, List<Category>> byParent = all.stream()
                .filter(category -> category.getParent() != null)
                .collect(Collectors.groupingBy(category -> category.getParent().getId()));

        return all.stream()
                .filter(category -> category.getParent() == null)
                .map(category -> toNode(category, byParent))
                .toList();
    }

    private CategoryResponse toNode(Category category, Map<Long, List<Category>> byParent) {
        List<CategoryResponse> children = byParent.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> toNode(child, byParent))
                .toList();
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                parentId(category), children);
    }

    private Long parentId(Category category) {
        return category.getParent() == null ? null : category.getParent().getId();
    }

    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        apply(category, request);
        category = repo.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                parentId(category), List.of());
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        apply(category, request);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                parentId(category), List.of());
    }

    public void delete(Long id) {
        getEntity(id);
        if (!repo.findByParentId(id).isEmpty()) {
            throw new BadRequestException("Delete the sub-categories first");
        }
        repo.deleteById(id);
        repo.flush();
    }

    /** The category and its direct children, used when filtering products. */
    @Transactional(readOnly = true)
    public List<Long> idsWithChildren(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        repo.findByParentId(id).forEach(category -> ids.add(category.getId()));
        return ids;
    }

    private void apply(Category category, CategoryRequest request) {
        category.setName(request.name().trim());
        category.setSlug(slugify(request.name()));
        if (request.parentId() == null) {
            category.setParent(null);
        } else {
            if (request.parentId().equals(category.getId())) {
                throw new BadRequestException("A category cannot be its own parent");
            }
            category.setParent(getEntity(request.parentId()));
        }
    }
}
