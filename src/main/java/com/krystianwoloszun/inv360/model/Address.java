package com.krystianwoloszun.inv360.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;

}
