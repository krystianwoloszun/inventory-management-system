package com.krystianwoloszun.inv360.inventory.dto;

record InventoryResponse(
            Long id,
            Long warehouseId,
            Long productId,
            int quantity) {
}
