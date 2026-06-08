package com.krystianwoloszun.inv360.warehouse.dto;

public record AddressResponse(
        String street,
        String buildingNumber,
        String city,
        String postalCode
) {
}
