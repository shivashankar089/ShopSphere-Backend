package com.shopsphere.backend.order;

import com.shopsphere.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findAllByOrderByCreatedAtDesc();
}
