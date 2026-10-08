package com.shopsphere.backend.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopsphere.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    private double totalAmount;

    private String shippingAddress;

    private String contactPhone;

    private String paymentMethod = "COD"; // COD, CARD, UPI

    private String status = "CONFIRMED"; // PLACED, CONFIRMED, PACKED, SHIPPED, DELIVERED, CANCELLED

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();
}
