package com.krystianwoloszun.inv360.stockmovement;

import java.time.LocalDateTime;

import com.krystianwoloszun.inv360.product.Product;
import com.krystianwoloszun.inv360.warehouse.Warehouse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime movementDate;

    @Column(nullable = false)
    private OperationType operationType;

}
