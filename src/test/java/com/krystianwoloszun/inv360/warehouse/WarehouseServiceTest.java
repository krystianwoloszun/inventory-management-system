package com.krystianwoloszun.inv360.warehouse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krystianwoloszun.inv360.common.exception.WarehouseAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;
import com.krystianwoloszun.inv360.warehouse.dto.CreateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.UpdateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.WarehouseResponse;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private Address sampleAddress() {
        return Address.builder()
                .street("Main St")
                .buildingNumber("12")
                .city("Warsaw")
                .postalCode("00-001")
                .build();
    }

    private Warehouse sampleWarehouse() {
        return Warehouse.builder()
                .id(1L)
                .name("Central")
                .description("Central warehouse")
                .address(sampleAddress())
                .build();
    }

    @Test
    void createWarehouse_savesAndReturnsResponse() {
        CreateWarehouseRequest request = new CreateWarehouseRequest(
                "Central", "Central warehouse", sampleAddress());

        when(warehouseRepository.existsByName("Central")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sampleWarehouse());

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Central");
        assertThat(response.address().city()).isEqualTo("Warsaw");
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_throwsWhenNameExists() {
        CreateWarehouseRequest request = new CreateWarehouseRequest(
                "Central", "desc", sampleAddress());

        when(warehouseRepository.existsByName("Central")).thenReturn(true);

        assertThatThrownBy(() -> warehouseService.createWarehouse(request))
                .isInstanceOf(WarehouseAlreadyExistsException.class)
                .hasMessageContaining("Central");

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void getWarehouseById_returnsWarehouse() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sampleWarehouse()));

        WarehouseResponse response = warehouseService.getWarehouseById(1L);

        assertThat(response.name()).isEqualTo("Central");
        assertThat(response.address().postalCode()).isEqualTo("00-001");
    }

    @Test
    void getWarehouseById_throwsWhenMissing() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById(99L))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void getWarehouseByName_throwsWhenMissing() {
        when(warehouseRepository.findByName("Ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseByName("Ghost"))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void updateWarehouse_updatesFields() {
        Warehouse existing = sampleWarehouse();
        Address newAddress = Address.builder()
                .street("Side St").buildingNumber("5").city("Krakow").postalCode("30-001").build();
        UpdateWarehouseRequest request = new UpdateWarehouseRequest(
                "North", "North warehouse", newAddress);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.existsByName("North")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        WarehouseResponse response = warehouseService.updateWarehouse(1L, request);

        assertThat(response.name()).isEqualTo("North");
        assertThat(response.address().city()).isEqualTo("Krakow");
    }

    @Test
    void updateWarehouse_keepingSameNameDoesNotCheckUniqueness() {
        Warehouse existing = sampleWarehouse();
        UpdateWarehouseRequest request = new UpdateWarehouseRequest(
                "Central", "Updated desc", sampleAddress());

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        WarehouseResponse response = warehouseService.updateWarehouse(1L, request);

        assertThat(response.description()).isEqualTo("Updated desc");
        verify(warehouseRepository, never()).existsByName(any());
    }

    @Test
    void updateWarehouse_throwsWhenNewNameTaken() {
        Warehouse existing = sampleWarehouse();
        UpdateWarehouseRequest request = new UpdateWarehouseRequest(
                "Taken", "desc", sampleAddress());

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(warehouseRepository.existsByName("Taken")).thenReturn(true);

        assertThatThrownBy(() -> warehouseService.updateWarehouse(1L, request))
                .isInstanceOf(WarehouseAlreadyExistsException.class);

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void updateWarehouse_throwsWhenMissing() {
        when(warehouseRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.updateWarehouse(42L,
                new UpdateWarehouseRequest("x", "y", sampleAddress())))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void getAllWarehouses_mapsAll() {
        when(warehouseRepository.findAll()).thenReturn(List.of(sampleWarehouse()));

        List<WarehouseResponse> all = warehouseService.getAllWarehouses();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).name()).isEqualTo("Central");
    }

    @Test
    void deleteWarehouse_deletesExisting() {
        Warehouse existing = sampleWarehouse();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(existing));

        warehouseService.deleteWarehouse(1L);

        verify(warehouseRepository).delete(existing);
    }

    @Test
    void deleteWarehouse_throwsWhenMissing() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.deleteWarehouse(1L))
                .isInstanceOf(WarehouseNotFoundException.class);

        verify(warehouseRepository, never()).delete(any());
    }
}
