package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.Cart;
import com.deliverapp.backend.model.CartItem;
import com.deliverapp.backend.model.Product;
import com.deliverapp.backend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find all items in a cart
    List<CartItem> findByCartOrderByCreatedAtDesc(Cart cart);
    
    // Find cart item by cart and product
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    // Find cart item by cart, product and variant
    Optional<CartItem> findByCartAndProductAndProductVariant(Cart cart, Product product, ProductVariant productVariant);
    
    // Find cart items by cart ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId ORDER BY ci.createdAt DESC")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);
    
    // Get total quantity of items in cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalQuantityByCartId(@Param("cartId") Long cartId);
    
    // Get total amount of cart
    @Query("SELECT COALESCE(SUM(ci.subtotal), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    java.math.BigDecimal getTotalAmountByCartId(@Param("cartId") Long cartId);
    
    // Delete all items from cart
    void deleteByCart(Cart cart);
    
    // Count items in cart
    long countByCart(Cart cart);
}