package com.krystianwoloszun.inv360.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddStockRequest(
                @NotNull Long warehouseId,
                @NotNull Long productId,
                @NotNull @Positive Integer quantity) {
}
