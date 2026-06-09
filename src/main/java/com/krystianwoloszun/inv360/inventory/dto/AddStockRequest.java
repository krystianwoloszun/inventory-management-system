package com.krystianwoloszun.inv360.inventory.dto;

record AddStockRequest(
                Long id,
                Long warehouseId,
                Long productId,
                int quantity) {
}