package com.krystianwoloszun.inv360.inventory.dto;

record TransferStockRequest(
                Long id,
                Long sourceWarehouseId,
                Long targetWarehouseId,
                Long productId,
                int quantity) {
}
