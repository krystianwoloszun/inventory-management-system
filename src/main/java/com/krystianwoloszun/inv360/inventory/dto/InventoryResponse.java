package com.krystianwoloszun.inv360.inventory.dto;

public record InventoryResponse(
            Long id,
            Long warehouseId,
            Long productId,
            int quantity) {
}
