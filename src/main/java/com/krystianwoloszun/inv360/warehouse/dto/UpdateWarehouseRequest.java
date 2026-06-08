package com.krystianwoloszun.inv360.warehouse.dto;

import com.krystianwoloszun.inv360.warehouse.Address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWarehouseRequest(
                @NotBlank @Size(max = 255) String name,
                @NotBlank @Size(max = 1000) String description,
                @NotBlank Address address) {
}
