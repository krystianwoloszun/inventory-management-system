package com.krystianwoloszun.inv360.warehouse;

import org.springframework.stereotype.Service;

import com.krystianwoloszun.inv360.common.exception.WarehouseAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public Warehouse createWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByName(warehouse.getName())) {
            throw new WarehouseAlreadyExistsException(
                    "Warehouse with name " + warehouse.getName() + " already exists.");
        }
        return warehouseRepository.save(warehouse);
    }

    public Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse with ID " + id + " not found."));
    }

    public Warehouse getWarehouseByName(String name) {
        return warehouseRepository.findByName(name)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse with name " + name + " not found."));
    }

    public Warehouse updateWarehouse(Long id, Warehouse updatedWarehouse) {
        Warehouse existingWarehouse = getWarehouseById(id);
        if (!existingWarehouse.getName().equals(updatedWarehouse.getName())
                && warehouseRepository.existsByName(updatedWarehouse.getName())) {
            throw new WarehouseAlreadyExistsException(
                    "Warehouse with name " + updatedWarehouse.getName() + " already exists.");
        }
        existingWarehouse.setName(updatedWarehouse.getName());
        existingWarehouse.setAddress(updatedWarehouse.getAddress());
        return warehouseRepository.save(existingWarehouse);
    }

    public void deleteWarehouse(Long id) {
        Warehouse existingWarehouse = getWarehouseById(id);
        warehouseRepository.delete(existingWarehouse);
    }

}
