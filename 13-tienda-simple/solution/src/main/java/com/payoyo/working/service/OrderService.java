package com.payoyo.working.service;

import com.payoyo.working.entity.Order;
import com.payoyo.working.entity.OrderItem;
import com.payoyo.working.entity.enums.OrderStatus;
import com.payoyo.working.entity.Product;
import com.payoyo.working.exception.InsufficientStockException;
import com.payoyo.working.exception.InvalidOrderStateException;
import com.payoyo.working.exception.ResourceNotFoundException;
import com.payoyo.working.repository.OrderRepository;
import com.payoyo.working.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Crear pedido con validación de stock y cálculo automático de totales
     * Operación TRANSACCIONAL crítica
     */
    @Transactional
    public Order createOrder(Order order) {
        // 1. Generar orderNumber único
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);

        // 2. Procesar items: validar stock, capturar precios, calcular subtotales
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + item.getProduct().getId()
                    ));

            // VALIDACIÓN CRÍTICA: verificar stock disponible
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para producto: " + product.getName() +
                        ". Disponible: " + product.getStock() +
                        ", Requerido: " + item.getQuantity()
                );
            }

            // 3. Capturar precio actual del producto (histórico)
            item.setPriceAtPurchase(product.getPrice());
            
            // 4. Calcular subtotal del item
            item.calculateSubtotal();

            // 5. Reducir stock del producto
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            // 6. Vincular item al order (sincronización bidireccional)
            item.setOrder(order);
        }

        // 7. Calcular total del pedido (suma de subtotales)
        BigDecimal total = order.getItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        // 8. Guardar pedido (cascade guarda items automáticamente)
        return orderRepository.save(order);
    }

    /**
     * Obtener todos los pedidos
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Obtener pedido por ID
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con ID: " + id
                ));
    }

    /**
     * Actualizar estado del pedido con validación de transiciones
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);

        // No se puede cambiar estado de un pedido cancelado
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "No se puede cambiar el estado de un pedido cancelado"
            );
        }

        // Validar transiciones permitidas
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Cancelar pedido: devuelve stock y cambia estado a CANCELLED
     * Solo permitido si está PENDING o CONFIRMED
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);

        // Solo se puede cancelar si está PENDING o CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && 
            order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "No se puede cancelar un pedido en estado: " + order.getStatus()
            );
        }

        // DEVOLVER STOCK a los productos
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        // Cambiar estado a CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Generar orderNumber único: ORD-YYYYMMDD-XXXX
     */
    private String generateOrderNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        // Contar pedidos del día
        long countToday = orderRepository.countByOrderDateBetween(
                LocalDateTime.now().with(LocalTime.MIN),
                LocalDateTime.now().with(LocalTime.MAX)
        );

        return String.format("ORD-%s-%04d", dateStr, countToday + 1);
    }

    /**
     * Validar transiciones de estado permitidas
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // Estados finales
        };

        if (!isValidTransition) {
            throw new InvalidOrderStateException(
                    "Transición de estado inválida: " + currentStatus + " → " + newStatus
            );
        }
    }
}