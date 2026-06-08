package com.krystianwoloszun.inv360.warehouse;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.WarehouseAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;
import com.krystianwoloszun.inv360.warehouse.dto.AddressResponse;
import com.krystianwoloszun.inv360.warehouse.dto.CreateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.UpdateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.WarehouseResponse;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getDescription(),
                new AddressResponse(
                        warehouse.getAddress().getStreet(),
                        warehouse.getAddress().getBuildingNumber(),
                        warehouse.getAddress().getCity(),
                        warehouse.getAddress().getPostalCode()));
    }

    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByName(request.name())) {
            throw new WarehouseAlreadyExistsException(
                    "Warehouse with name " + request.name() + " already exists.");
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.name())
                .description(request.description())
                .address(Address.builder()
                        .street(request.address().getStreet())
                        .buildingNumber(request.address().getBuildingNumber())
                        .city(request.address().getCity())
                        .postalCode(request.address().getPostalCode())
                        .build())
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse with ID " + id + " not found."));

        return toResponse(warehouse);
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseByName(String name) {
        Warehouse warehouse = warehouseRepository.findByName(name)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse with name " + name + " not found."));
        return toResponse(warehouse);
    }

    public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request) {

        Warehouse existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse with ID " + id + " not found."));

        if (!existing.getName().equals(request.name())
                && warehouseRepository.existsByName(request.name())) {
            throw new WarehouseAlreadyExistsException(
                    "Warehouse with name " + request.name() + " already exists.");
        }

        existing.setName(request.name());
        existing.setDescription(request.description());

        existing.setAddress(
                Address.builder()
                        .street(request.address().getStreet())
                        .buildingNumber(request.address().getBuildingNumber())
                        .city(request.address().getCity())
                        .postalCode(request.address().getPostalCode())
                        .build());

        Warehouse saved = warehouseRepository.save(existing);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse with ID " + id + "not found."));
        warehouseRepository.delete(warehouse);
    }

}
