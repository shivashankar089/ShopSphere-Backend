package com.shopsphere.backend.seller;

import com.shopsphere.backend.order.OrderItem;
import com.shopsphere.backend.order.OrderItemRepository;
import com.shopsphere.backend.product.Category;
import com.shopsphere.backend.product.CategoryRepository;
import com.shopsphere.backend.product.Product;
import com.shopsphere.backend.product.ProductRepository;
import com.shopsphere.backend.store.Store;
import com.shopsphere.backend.store.StoreRepository;
import com.shopsphere.backend.user.User;
import com.shopsphere.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    public record ProductRequest(
            String name,
            String description,
            double price,
            double originalPrice,
            int stock,
            String imageUrl,
            String brand,
            Long categoryId,
            String deliveryTimeEstimate,
            String tags
    ) {}

    public record BecomeSellerRequest(
            String storeName,
            String description,
            String address,
            String city,
            double distanceKm,
            String phone,
            String gstin,
            String category
    ) {}

    public record StoreProfileRequest(
            String name,
            String description,
            String address,
            String city,
            double distanceKm,
            String phone,
            String bannerUrl,
            String logoUrl
    ) {}

    private final ProductRepository productRepo;
    private final StoreRepository storeRepo;
    private final CategoryRepository categoryRepo;
    private final OrderItemRepository orderItemRepo;
    private final UserRepository userRepo;

    private static final List<String> RESTRICTED_KEYWORDS = List.of(
            "drug", "narcotic", "contraband", "weed", "marijuana", "cannabis",
            "opioid", "prescription pill", "weapon", "explosive", "steroid", "poison"
    );

    public SellerController(ProductRepository productRepo, StoreRepository storeRepo, CategoryRepository categoryRepo, OrderItemRepository orderItemRepo, UserRepository userRepo) {
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.categoryRepo = categoryRepo;
        this.orderItemRepo = orderItemRepo;
        this.userRepo = userRepo;
    }

    private User getAuthenticatedSeller(Authentication auth) {
        if (auth == null) return null;
        return userRepo.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    @GetMapping("/store")
    public ResponseEntity<?> getMyStore(Authentication auth) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Store store = storeRepo.findBySeller(seller).orElse(null);
        Map<String, Object> resp = new HashMap<>();
        resp.put("seller", seller);
        resp.put("store", store);
        resp.put("isApproved", seller.isApproved() && (store != null && store.isApproved()));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/become-seller")
    public ResponseEntity<?> becomeSeller(Authentication auth, @RequestBody BecomeSellerRequest req) {
        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userRepo.findByEmailIgnoreCase(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        user.setRole(com.shopsphere.backend.user.Role.SELLER);
        user.setApproved(false);
        userRepo.save(user);

        Store store = storeRepo.findBySeller(user).orElse(new Store());
        store.setSeller(user);
        store.setName(req.storeName() != null && !req.storeName().isBlank() ? req.storeName() : user.getName() + " Store");
        store.setDescription(req.description());
        store.setAddress(req.address() != null ? req.address() : "Hitec City Commercial Hub");
        store.setCity(req.city() != null && !req.city().isBlank() ? req.city() : "Hyderabad");
        store.setDistanceKm(req.distanceKm() > 0 ? req.distanceKm() : 2.5);
        store.setPhone(req.phone() != null ? req.phone() : user.getPhone());
        store.setApproved(false);
        store.setVerified(false);
        storeRepo.save(store);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Seller onboarding application submitted. Your store is now under Admin compliance review.");
        resp.put("user", user);
        resp.put("store", store);
        resp.put("isApproved", false);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/store")
    public ResponseEntity<?> saveStoreProfile(Authentication auth, @RequestBody StoreProfileRequest req) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Store store = storeRepo.findBySeller(seller).orElse(new Store());
        store.setSeller(seller);
        store.setName(req.name() != null ? req.name() : "My Shop");
        store.setDescription(req.description());
        store.setAddress(req.address() != null ? req.address() : "City Center Commercial Hub");
        store.setCity(req.city() != null ? req.city() : "Metro Area");
        store.setDistanceKm(req.distanceKm() > 0 ? req.distanceKm() : 2.5);
        store.setPhone(req.phone() != null ? req.phone() : "+1 (555) 012-4567");
        if (req.bannerUrl() != null) store.setBannerUrl(req.bannerUrl());
        if (req.logoUrl() != null) store.setLogoUrl(req.logoUrl());

        // New seller store remains unapproved until admin verification if seller is unapproved
        store.setApproved(seller.isApproved());
        Store saved = storeRepo.save(store);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/products")
    public ResponseEntity<?> getMyProducts(Authentication auth) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Product> products = productRepo.findBySeller(seller);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(Authentication auth, @RequestBody ProductRequest req) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Store store = storeRepo.findBySeller(seller).orElse(null);
        if (store == null) {
            store = new Store(seller, seller.getName() + " Store", "Official store inventory", "Main Avenue", "City Center", 2.3, 4.8, 25, null, null, null, true, seller.isApproved());
            store = storeRepo.save(store);
        }

        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepo.findById(req.categoryId()).orElse(null);
        }
        if (category == null) {
            category = categoryRepo.findAll().stream().findFirst().orElse(null);
        }

        Product product = new Product();
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price() > 0 ? req.price() : 19.99);
        product.setOriginalPrice(req.originalPrice() > 0 ? req.originalPrice() : product.getPrice() * 1.25);
        product.setStock(req.stock() >= 0 ? req.stock() : 10);
        product.setImageUrl(req.imageUrl() != null && !req.imageUrl().isBlank() ? req.imageUrl() : "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80");
        product.setBrand(req.brand() != null ? req.brand() : store.getName());
        product.setCategory(category);
        product.setStore(store);
        product.setSeller(seller);
        product.setDeliveryTimeEstimate(req.deliveryTimeEstimate() != null ? req.deliveryTimeEstimate() : "Delivery in 1-2 business days");
        product.setTags(req.tags());

        // Automated safety check for restricted goods / drugs / malpractice
        String fullText = (req.name() + " " + req.description() + " " + req.tags()).toLowerCase();
        boolean hasRestricted = RESTRICTED_KEYWORDS.stream().anyMatch(fullText::contains);

        if (hasRestricted) {
            product.setRestrictedItem(true);
            product.setStatus("PENDING_REVIEW");
            product.setModerationNotes("Flagged by AI safety scanner: Prohibited/restricted keyword detected. Requires admin moderation.");
        } else if (!seller.isApproved() || !store.isApproved()) {
            product.setStatus("PENDING_REVIEW");
            product.setModerationNotes("Seller store approval pending.");
        } else {
            product.setStatus("APPROVED");
        }

        Product saved = productRepo.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(Authentication auth, @PathVariable Long id, @RequestBody ProductRequest req) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();
        if (!product.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not your product"));
        }

        if (req.name() != null) product.setName(req.name());
        if (req.description() != null) product.setDescription(req.description());
        if (req.price() > 0) product.setPrice(req.price());
        if (req.originalPrice() > 0) product.setOriginalPrice(req.originalPrice());
        product.setStock(req.stock());
        if (req.imageUrl() != null) product.setImageUrl(req.imageUrl());
        if (req.deliveryTimeEstimate() != null) product.setDeliveryTimeEstimate(req.deliveryTimeEstimate());

        Product saved = productRepo.save(product);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(Authentication auth, @PathVariable Long id) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();
        if (!product.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not your product"));
        }

        productRepo.delete(product);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getSellerOrders(Authentication auth) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Store store = storeRepo.findBySeller(seller).orElse(null);
        if (store == null) return ResponseEntity.ok(Collections.emptyList());

        List<OrderItem> items = orderItemRepo.findByStoreOrderByOrderCreatedAtDesc(store);
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateParcelStatus(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        User seller = getAuthenticatedSeller(auth);
        if (seller == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        OrderItem item = orderItemRepo.findById(id).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();

        String newStatus = body.getOrDefault("status", "PACKED");
        item.setParcelStatus(newStatus);
        orderItemRepo.save(item);
        return ResponseEntity.ok(item);
    }
}
