package com.shopsphere.backend.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public ProductController(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepo.findAll());
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String search,
                                                     @RequestParam(required = false) Long categoryId,
                                                     @RequestParam(required = false) Long storeId,
                                                     @RequestParam(required = false) Double maxDistance,
                                                     @RequestParam(required = false) Double maxPrice) {
        List<Product> products;
        if (search != null && !search.trim().isBlank()) {
            products = productRepo.searchApprovedProducts(search.trim());
        } else if (categoryId != null && categoryId > 0) {
            products = productRepo.findByCategoryIdAndStatus(categoryId, "APPROVED");
        } else if (storeId != null && storeId > 0) {
            products = productRepo.findByStoreId(storeId).stream()
                    .filter(p -> "APPROVED".equalsIgnoreCase(p.getStatus()))
                    .toList();
        } else {
            products = productRepo.findByStatus("APPROVED");
        }

        if (maxDistance != null && maxDistance > 0) {
            products = products.stream()
                    .filter(p -> p.getStore() != null && p.getStore().getDistanceKm() <= maxDistance)
                    .toList();
        }

        if (maxPrice != null && maxPrice > 0) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .toList();
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
