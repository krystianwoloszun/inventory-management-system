package com.krystianwoloszun.inv360.stockmovement;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krystianwoloszun.inv360.stockmovement.dto.StockMovementResponse;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public List<StockMovementResponse> getAllMovements() {
        return stockMovementService.getAllMovements();
    }

    @GetMapping("/{id}")
    public StockMovementResponse getStockMovementById(@PathVariable Long id) {
        return stockMovementService.getStockMovementById(id);
    }

    @GetMapping("/product/{productId}")
    public List<StockMovementResponse> getMovementsByProduct(@PathVariable Long productId) {
        return stockMovementService.getMovementsByProduct(productId);
    }

    @GetMapping("/source-warehouse/{warehouseId}")
    public List<StockMovementResponse> getMovementsBySourceWarehouse(@PathVariable Long warehouseId) {
        return stockMovementService.getMovementsBySourceWarehouse(warehouseId);
    }

    @GetMapping("/target-warehouse/{warehouseId}")
    public List<StockMovementResponse> getMovementsByTargetWarehouse(@PathVariable Long warehouseId) {
        return stockMovementService.getMovementsByTargetWarehouse(warehouseId);
    }

    @GetMapping("/between")
    public List<StockMovementResponse> getMovementsBetweenWarehouses(
            @RequestParam Long sourceWarehouseId,
            @RequestParam Long targetWarehouseId) {

        return stockMovementService.getMovementsBetweenWarehouses(
                sourceWarehouseId,
                targetWarehouseId);
    }
}
