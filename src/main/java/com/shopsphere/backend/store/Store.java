package com.shopsphere.backend.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopsphere.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", unique = true)
    private User seller;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    private String city;

    private double distanceKm = 2.0;

    private double rating = 4.8;

    private int reviewCount = 120;

    private String bannerUrl;

    private String logoUrl;

    private String phone;

    private boolean verified = true;

    private boolean approved = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Store(User seller, String name, String description, String address, String city, double distanceKm, double rating, int reviewCount, String bannerUrl, String logoUrl, String phone, boolean verified, boolean approved) {
        this.seller = seller;
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.distanceKm = distanceKm;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.bannerUrl = bannerUrl;
        this.logoUrl = logoUrl;
        this.phone = phone;
        this.verified = verified;
        this.approved = approved;
        this.createdAt = LocalDateTime.now();
    }
}
