package com.shopsphere.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlugIgnoreCase(String slug);
    Optional<Category> findByNameIgnoreCase(String name);
}
