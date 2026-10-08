package com.shopsphere.backend.product;

import com.shopsphere.backend.store.Store;
import com.shopsphere.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(String status);
    List<Product> findByStoreAndStatus(Store store, String status);
    List<Product> findByStoreId(Long storeId);
    List<Product> findBySeller(User seller);
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByCategoryIdAndStatus(Long categoryId, String status);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchApprovedProducts(@Param("query") String query);

    List<Product> findByRestrictedItemTrue();
}
