package com.shopsphere.backend.admin;

import com.shopsphere.backend.order.OrderRepository;
import com.shopsphere.backend.product.Product;
import com.shopsphere.backend.product.ProductRepository;
import com.shopsphere.backend.store.Store;
import com.shopsphere.backend.store.StoreRepository;
import com.shopsphere.backend.user.Role;
import com.shopsphere.backend.user.User;
import com.shopsphere.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepo;
    private final StoreRepository storeRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public AdminController(UserRepository userRepo, StoreRepository storeRepo, ProductRepository productRepo, OrderRepository orderRepo) {
        this.userRepo = userRepo;
        this.storeRepo = storeRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepo.count());
        stats.put("totalStores", storeRepo.count());
        stats.put("totalProducts", productRepo.count());
        stats.put("totalOrders", orderRepo.count());
        stats.put("pendingSellers", userRepo.findAll().stream().filter(u -> u.getRole() == Role.SELLER && !u.isApproved()).count());
        stats.put("flaggedProducts", productRepo.findByRestrictedItemTrue().size());
        stats.put("pendingProducts", productRepo.findByStatus("PENDING_REVIEW").size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pending-sellers")
    public ResponseEntity<?> getPendingSellers() {
        List<User> pendingUsers = userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.SELLER && !u.isApproved())
                .toList();

        List<Map<String, Object>> result = pendingUsers.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("user", u);
            map.put("store", storeRepo.findBySeller(u).orElse(null));
            map.put("productCount", productRepo.findBySeller(u).size());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/approve-seller/{id}")
    public ResponseEntity<?> approveSeller(@PathVariable Long id) {
        User user = userRepo.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setApproved(true);
        userRepo.save(user);

        Store store = storeRepo.findBySeller(user).orElse(null);
        if (store != null) {
            store.setApproved(true);
            store.setVerified(true);
            storeRepo.save(store);
        }

        // Approve pending clean products for this seller
        List<Product> products = productRepo.findBySeller(user);
        for (Product p : products) {
            if (!p.isRestrictedItem()) {
                p.setStatus("APPROVED");
                productRepo.save(p);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Seller and store approved successfully. Products are now live on ShopSphere."));
    }

    @PostMapping("/reject-seller/{id}")
    public ResponseEntity<?> rejectSeller(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        User user = userRepo.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setApproved(false);
        userRepo.save(user);

        Store store = storeRepo.findBySeller(user).orElse(null);
        if (store != null) {
            store.setApproved(false);
            storeRepo.save(store);
        }

        List<Product> products = productRepo.findBySeller(user);
        for (Product p : products) {
            p.setStatus("REJECTED");
            if (body != null && body.containsKey("reason")) {
                p.setModerationNotes("Rejected by Admin: " + body.get("reason"));
            }
            productRepo.save(p);
        }

        return ResponseEntity.ok(Map.of("message", "Seller application rejected."));
    }

    @GetMapping("/moderation/products")
    public ResponseEntity<List<Product>> getModerationProducts() {
        return ResponseEntity.ok(productRepo.findAll());
    }

    @PostMapping("/moderate-product/{id}")
    public ResponseEntity<?> moderateProduct(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();

        String action = body.getOrDefault("status", "APPROVED");
        String notes = body.getOrDefault("notes", "Moderated by administrator");

        product.setStatus(action);
        product.setModerationNotes(notes);
        if ("APPROVED".equalsIgnoreCase(action)) {
            product.setRestrictedItem(false);
        }
        productRepo.save(product);

        return ResponseEntity.ok(product);
    }
}
