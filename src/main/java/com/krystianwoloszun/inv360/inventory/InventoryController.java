package com.krystianwoloszun.inv360.inventory;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krystianwoloszun.inv360.inventory.dto.AddStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.CreateInventoryRequest;
import com.krystianwoloszun.inv360.inventory.dto.InventoryResponse;
import com.krystianwoloszun.inv360.inventory.dto.RemoveStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.TransferStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.UpdateInventoryRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryservice) {
        this.inventoryService = inventoryservice;
    }

    @GetMapping
    public List<InventoryResponse> getAllInventories() {
        return inventoryService.getAllInventories();
    }

    @PostMapping
    public InventoryResponse createInventory(
            @Valid @RequestBody CreateInventoryRequest request) {

        return inventoryService.createInventory(request);
    }

    @PutMapping("/{id}")
    public InventoryResponse updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {

        return inventoryService.updateInventory(id, request);
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public InventoryResponse getInventoryByProductIdAndWarehouseId(@PathVariable Long productId,
            @PathVariable Long warehouseId) {
        return inventoryService.getInventoryByProductIdAndWarehouseId(productId, warehouseId);
    }

    @PostMapping("/add-stock")
    public InventoryResponse addStock(@Valid @RequestBody AddStockRequest request) {
        return inventoryService.addStock(request);
    }

    @PostMapping("/remove-stock")
    public InventoryResponse removeStock(@Valid @RequestBody RemoveStockRequest request) {
        return inventoryService.removeStock(request);
    }

    @PostMapping("/transfer-stock")
    public List<InventoryResponse> transferStock(@Valid @RequestBody TransferStockRequest request) {
        return inventoryService.transferStock(request);
    }

    @DeleteMapping("/{id}")
    public void deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
    }
}
