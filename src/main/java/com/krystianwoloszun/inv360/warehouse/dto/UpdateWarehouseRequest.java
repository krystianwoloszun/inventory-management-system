package com.krystianwoloszun.inv360.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWarehouseRequest(
                @NotBlank @Size(max = 255) String name,
                @NotBlank @Size(max = 1000) String description,

                @NotBlank @Size(max = 100) String street,

                @NotBlank @Size(max = 20) String buildingNumber,

                @NotBlank @Size(max = 255) String city,

                @NotBlank @Size(max = 20) String postalCode) {
}
