package com.payoyo.working.repository;

import com.payoyo.working.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Buscar todos los items de un pedido específico
    List<OrderItem> findByOrderId(Long orderId);
    
    // Buscar todos los items que contienen un producto específico
    List<OrderItem> findByProductId(Long productId);
    
    // Query custom: verificar si un producto tiene pedidos asociados
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi WHERE oi.product.id = :productId")
    boolean existsByProductId(Long productId);
}