package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.Order;
import com.deliverapp.backend.model.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Kullanıcının tüm siparişlerini getir (sayfalama ile) - Basic query
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Kullanıcının belirli statusdaki siparişlerini getir - Basic query
    Page<Order> findByUserIdAndOrderStatusOrderByCreatedAtDesc(Long userId, OrderStatus orderStatus, Pageable pageable);
    
    // Kullanıcının belirli bir siparişini getir - OrderItems ile birlikte
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);
    
    // Sipariş numarasına göre sipariş getir
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Sipariş numarası ve kullanıcı ID'sine göre sipariş getir
    Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);
    
    // Sipariş numarası ve kullanıcı ID'sine göre sipariş getir (orderItems ile birlikte)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH o.user WHERE o.orderNumber = :orderNumber AND o.user.id = :userId")
    Optional<Order> findByOrderNumberAndUserIdWithItems(@Param("orderNumber") String orderNumber, @Param("userId") Long userId);
    
    // Belirli ID'lerdeki siparişleri orderItems ile birlikte getir
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id IN :orderIds ORDER BY o.createdAt DESC")
    List<Order> findByIdInWithItems(@Param("orderIds") List<Long> orderIds);
    
    // Belirli tarih aralığındaki siparişleri getir
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Belirli statusdaki tüm siparişleri getir (admin için)
    List<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);
    
    // Kullanıcının aktif siparişlerini getir (PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY) - OrderItems ile birlikte
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId AND o.orderStatus IN ('PENDING', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY') ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByUserId(@Param("userId") Long userId);
    
    // Kullanıcının son 30 günlük siparişlerini getir - OrderItems ile birlikte
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId AND o.createdAt >= :thirtyDaysAgo ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    // Sipariş numarası üretmek için bugünkü sipariş sayısını getir
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :startOfNextDay")
    Long countTodaysOrders(@Param("startOfDay") LocalDateTime startOfDay, @Param("startOfNextDay") LocalDateTime startOfNextDay);
}