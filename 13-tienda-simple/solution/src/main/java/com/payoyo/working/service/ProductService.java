package com.payoyo.working.service;

import com.payoyo.working.entity.Product;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.OrderItemRepository;
import com.payoyo.working.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Por defecto solo lectura, se sobreescribe en métodos que modifican
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Crear nuevo producto
     */
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Obtener todos los productos
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Obtener producto por ID
     * Lanza ResourceNotFoundException si no existe
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con ID: " + id
                ));
    }

    /**
     * Actualizar producto existente
     */
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());

        return productRepository.save(product);
    }

    /**
     * Eliminar producto
     * Valida que no tenga pedidos asociados antes de eliminar
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);

        // Validar que no tenga pedidos asociados
        if (orderItemRepository.existsByProductId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el producto porque tiene pedidos asociados"
            );
        }

        productRepository.delete(product);
    }
}