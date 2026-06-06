package com.krystianwoloszun.inv360.stockmovement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krystianwoloszun.inv360.common.exception.StockMovementNotFoundException;

@Service
@Transactional(readOnly = true)
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public StockMovement getStockMovementById(Long id) {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new StockMovementNotFoundException(
                        "Stock movement with ID " + id + " not found."));
    }

    public List<StockMovement> getMovementsByProduct(Long productId) {
        return stockMovementRepository.findByProductId(productId);
    }

    public List<StockMovement> getMovementsBySourceWarehouse(Long warehouseId) {
        return stockMovementRepository.findBySourceWarehouseId(warehouseId);
    }

    public List<StockMovement> getMovementsByTargetWarehouse(Long warehouseId) {
        return stockMovementRepository.findByTargetWarehouseId(warehouseId);
    }

    public List<StockMovement> getMovementsBetweenWarehouses(
            Long sourceWarehouseId,
            Long targetWarehouseId) {

        return stockMovementRepository.findBySourceWarehouseIdAndTargetWarehouseId(
                sourceWarehouseId,
                targetWarehouseId);
    }
}