package com.krystianwoloszun.inv360.warehouse;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.WarehouseAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;

@Service
@Transactional
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

    @Transactional(readOnly = true)
    public Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse with ID " + id + " not found."));
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public void deleteWarehouse(Long id) {
        Warehouse warehouse = getWarehouseById(id);
        warehouseRepository.delete(warehouse);
    }

}
