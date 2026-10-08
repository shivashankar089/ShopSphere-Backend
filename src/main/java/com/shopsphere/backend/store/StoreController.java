package com.shopsphere.backend.store;

import com.shopsphere.backend.product.Product;
import com.shopsphere.backend.product.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreRepository storeRepo;
    private final ProductRepository productRepo;

    public StoreController(StoreRepository storeRepo, ProductRepository productRepo) {
        this.storeRepo = storeRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public ResponseEntity<List<Store>> getStores(@RequestParam(required = false) String search,
                                                 @RequestParam(required = false) Double maxDistance) {
        List<Store> stores = storeRepo.findByApprovedTrue();
        if (search != null && !search.isBlank()) {
            stores = stores.stream()
                    .filter(s -> s.getName().toLowerCase().contains(search.toLowerCase()) ||
                            (s.getDescription() != null && s.getDescription().toLowerCase().contains(search.toLowerCase())) ||
                            (s.getCity() != null && s.getCity().toLowerCase().contains(search.toLowerCase())))
                    .toList();
        }
        if (maxDistance != null && maxDistance > 0) {
            stores = stores.stream()
                    .filter(s -> s.getDistanceKm() <= maxDistance)
                    .toList();
        }
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        return storeRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<Product>> getStoreProducts(@PathVariable Long id) {
        Store store = storeRepo.findById(id).orElse(null);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        List<Product> products = productRepo.findByStoreAndStatus(store, "APPROVED");
        return ResponseEntity.ok(products);
    }
}
