package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.Cart;
import com.deliverapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Find active cart by user
    Optional<Cart> findByUserAndIsActiveTrue(User user);
    
    // Find cart by user ID
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.isActive = true")
    Optional<Cart> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);
    
    // Check if user has active cart
    boolean existsByUserAndIsActiveTrue(User user);
    
    // Find cart with cart items (eager loading)
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.id = :userId AND c.isActive = true")
    Optional<Cart> findByUserIdWithCartItems(@Param("userId") Long userId);
}