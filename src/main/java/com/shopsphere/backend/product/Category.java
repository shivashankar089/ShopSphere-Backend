package com.shopsphere.backend.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String slug;

    private String icon;

    private String imageUrl;

    public Category(String name, String slug, String icon, String imageUrl) {
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.imageUrl = imageUrl;
    }
}
