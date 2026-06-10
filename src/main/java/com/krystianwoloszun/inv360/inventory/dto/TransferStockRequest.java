package com.krystianwoloszun.inv360.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferStockRequest(
                @NotNull Long sourceWarehouseId,
                @NotNull Long targetWarehouseId,
                @NotNull Long productId,
                @NotNull @Positive Integer quantity) {
}
