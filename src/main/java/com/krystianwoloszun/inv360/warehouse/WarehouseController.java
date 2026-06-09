package com.krystianwoloszun.inv360.warehouse;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krystianwoloszun.inv360.warehouse.dto.CreateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.UpdateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.WarehouseResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    @GetMapping("/{id}")
    public WarehouseResponse getWarehouseById(@PathVariable Long id) {
        return warehouseService.getWarehouseById(id);
    }

    @PostMapping
    public WarehouseResponse createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        return warehouseService.createWarehouse(request);
    }

    @PutMapping("/{id}")
    public WarehouseResponse updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        return warehouseService.updateWarehouse(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteWarehous(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
    }

}
