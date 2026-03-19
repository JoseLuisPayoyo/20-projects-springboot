package com.payoyo.working.repository;

import com.payoyo.working.entity.Order;
import com.payoyo.working.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Buscar pedido por orderNumber único
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Contar pedidos creados entre dos fechas (para generar orderNumber)
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Buscar pedidos de un cliente por email
    List<Order> findByCustomerEmailOrderByOrderDateDesc(String customerEmail);
    
    // Buscar pedidos por estado
    List<Order> findByStatus(OrderStatus status);
    
    // Buscar pedidos por cliente y estado
    List<Order> findByCustomerEmailAndStatus(String customerEmail, OrderStatus status);
}