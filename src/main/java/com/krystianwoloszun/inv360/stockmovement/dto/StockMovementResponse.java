package com.krystianwoloszun.inv360.stockmovement.dto;

import java.time.LocalDateTime;

import com.krystianwoloszun.inv360.stockmovement.OperationType;

public record StockMovementResponse(
        Long id,
        Long productId,
        String productName,
        Long sourceWarehouseId,
        String sourceWarehouseName,
        Long targetWarehouseId,
        String targetWarehouseName,
        int quantity,
        OperationType operationType,
        LocalDateTime movementDate) {
}