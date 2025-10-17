package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Belirli bir siparişin tüm item'larını getir
    List<OrderItem> findByOrderIdOrderByCreatedAt(Long orderId);
    
    // Kullanıcının belirli bir ürünü kaç kez sipariş ettiğini getir
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.user.id = :userId AND oi.product.id = :productId")
    Long countByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    // En çok sipariş edilen ürünleri getir
    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity) as totalQuantity FROM OrderItem oi GROUP BY oi.product.id, oi.productName ORDER BY totalQuantity DESC")
    List<Object[]> findMostOrderedProducts();
}