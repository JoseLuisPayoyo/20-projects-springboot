package com.payoyo.working.repository;

import com.payoyo.working.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Query method derivado: busca productos por nombre (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Query method: verifica si existe un producto con ese nombre
    boolean existsByName(String name);
}