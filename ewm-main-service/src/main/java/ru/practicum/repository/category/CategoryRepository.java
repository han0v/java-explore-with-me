package ru.practicum.repository.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c " +
            "WHERE (:ids IS NULL OR c.id IN :ids)")
    Page<Category> findCategoriesByIds(@Param("ids") List<Long> ids, Pageable pageable);
}
