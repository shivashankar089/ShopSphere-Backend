package com.shopsphere.backend.order;

import com.shopsphere.backend.product.Product;
import com.shopsphere.backend.product.ProductRepository;
import com.shopsphere.backend.user.User;
import com.shopsphere.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record OrderItemRequest(Long productId, int quantity) {}
    public record CheckoutRequest(String shippingAddress, String contactPhone, String paymentMethod, List<OrderItemRequest> items) {}

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public OrderController(OrderRepository orderRepo, OrderItemRepository orderItemRepo, ProductRepository productRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(Authentication auth, @RequestBody CheckoutRequest req) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User must be authenticated"));
        }
        User customer = userRepo.findByEmailIgnoreCase(auth.getName()).orElse(null);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Customer account not found"));
        }
        if (req.items() == null || req.items().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Cart cannot be empty"));
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(req.shippingAddress() != null && !req.shippingAddress().isBlank() ? req.shippingAddress() : "Standard Customer Delivery Address");
        order.setContactPhone(req.contactPhone() != null ? req.contactPhone() : "+1 (555) 019-2834");
        order.setPaymentMethod(req.paymentMethod() != null ? req.paymentMethod() : "COD");
        order.setStatus("CONFIRMED");

        double total = 0.0;
        Order savedOrder = orderRepo.save(order);

        for (OrderItemRequest itemReq : req.items()) {
            Product product = productRepo.findById(itemReq.productId()).orElse(null);
            if (product != null) {
                int qty = Math.max(1, itemReq.quantity());
                double itemTotal = product.getPrice() * qty;
                total += itemTotal;

                OrderItem orderItem = new OrderItem(savedOrder, product, product.getStore(), qty, product.getPrice());
                orderItemRepo.save(orderItem);
                savedOrder.getItems().add(orderItem);

                // Reduce inventory stock
                product.setStock(Math.max(0, product.getStock() - qty));
                productRepo.save(product);
            }
        }

        savedOrder.setTotalAmount(total);
        orderRepo.save(savedOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }
        User customer = userRepo.findByEmailIgnoreCase(auth.getName()).orElse(null);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Customer not found"));
        }
        List<Order> orders = orderRepo.findByCustomerOrderByCreatedAtDesc(customer);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return orderRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
