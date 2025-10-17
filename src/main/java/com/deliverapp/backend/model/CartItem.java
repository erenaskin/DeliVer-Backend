package com.deliverapp.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "cart_items")
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = true)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    // JSON field for selected options (soslar, ekstralar vs.)
    @Column(name = "selected_options_json", columnDefinition = "TEXT")
    private String selectedOptionsJson = "{}";

    // Special notes or customizations
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateSubtotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateSubtotal();
    }

    // Calculate subtotal based on quantity and unit price
    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            subtotal = BigDecimal.ZERO;
        }
    }

    // Helper method to set unit price from product pricing
    public void setUnitPriceFromProduct() {
        if (product != null && !product.getPricings().isEmpty()) {
            // Get the base price from the first pricing
            ProductPricing pricing = product.getPricings().get(0);
            this.unitPrice = pricing.getBasePrice();
            
            // Add variant price modifier if exists
            if (productVariant != null && productVariant.getPriceModifier() != null) {
                this.unitPrice = this.unitPrice.add(productVariant.getPriceModifier());
            }
            
            calculateSubtotal();
        }
    }
    
    // OrderService için gerekli metodlar
    public ProductVariant getVariant() {
        return this.productVariant;
    }
    
    public BigDecimal getTotalPrice() {
        return this.subtotal;
    }
    
    public String getSelectedOptions() {
        return this.selectedOptionsJson;
    }
    
    public String getSpecialNotes() {
        return this.notes;
    }
}