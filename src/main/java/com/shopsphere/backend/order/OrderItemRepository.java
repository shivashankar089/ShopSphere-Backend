package com.shopsphere.backend.order;

import com.shopsphere.backend.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByStoreOrderByOrderCreatedAtDesc(Store store);
    List<OrderItem> findByStoreId(Long storeId);
}
