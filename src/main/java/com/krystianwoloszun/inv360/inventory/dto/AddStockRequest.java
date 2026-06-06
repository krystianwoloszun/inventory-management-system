package com.krystianwoloszun.inv360.inventory.dto;

record AddStockRequest(
        Long warehouseId,
        Long productId,
        int quantity) {
}