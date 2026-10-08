package com.shopsphere.backend.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopsphere.backend.product.Product;
import com.shopsphere.backend.store.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private int quantity;

    private double price;

    private String parcelStatus = "CONFIRMED"; // CONFIRMED, PACKED, SHIPPED, DELIVERED

    public OrderItem(Order order, Product product, Store store, int quantity, double price) {
        this.order = order;
        this.product = product;
        this.store = store;
        this.quantity = quantity;
        this.price = price;
        this.parcelStatus = "CONFIRMED";
    }
}
