package com.krystianwoloszun.inv360.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateInventoryRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        @NotNull @PositiveOrZero Integer quantity) {
}
