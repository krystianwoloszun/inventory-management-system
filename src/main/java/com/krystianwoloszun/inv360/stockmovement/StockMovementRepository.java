package com.krystianwoloszun.inv360.stockmovement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductId(Long productId);

    List<StockMovement> findBySourceWarehouseId(Long warehouseId);

    List<StockMovement> findByTargetWarehouseId(Long warehouseId);

}