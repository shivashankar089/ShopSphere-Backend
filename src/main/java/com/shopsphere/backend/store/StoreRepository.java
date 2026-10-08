package com.shopsphere.backend.store;

import com.shopsphere.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findBySeller(User seller);
    Optional<Store> findBySellerId(Long sellerId);
    List<Store> findByApprovedTrue();
    List<Store> findByApprovedFalse();
    List<Store> findByNameContainingIgnoreCase(String name);
}
