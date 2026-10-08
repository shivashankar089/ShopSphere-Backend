package com.shopsphere.backend.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopsphere.backend.store.Store;
import com.shopsphere.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double price;

    private double originalPrice;

    @Column(nullable = false)
    private int stock = 10;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private String brand;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id")
    private User seller;

    private double rating = 4.5;

    private int reviewCount = 45;

    private String deliveryTimeEstimate = "Same-day delivery";

    private String status = "APPROVED"; // APPROVED, PENDING_REVIEW, REJECTED

    private boolean restrictedItem = false;

    private String moderationNotes;

    private String tags;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Product(String name, String description, double price, double originalPrice, int stock, String imageUrl, String brand, Category category, Store store, User seller, double rating, int reviewCount, String deliveryTimeEstimate, String status, boolean restrictedItem, String moderationNotes, String tags) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.category = category;
        this.store = store;
        this.seller = seller;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.deliveryTimeEstimate = deliveryTimeEstimate;
        this.status = status;
        this.restrictedItem = restrictedItem;
        this.moderationNotes = moderationNotes;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
    }
}
