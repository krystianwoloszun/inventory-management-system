package com.krystianwoloszun.inv360.stockmovement;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.krystianwoloszun.inv360.common.exception.StockMovementNotFoundException;
import com.krystianwoloszun.inv360.stockmovement.dto.StockMovementResponse;

@WebMvcTest(StockMovementController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockMovementService stockMovementService;

    private StockMovementResponse sample() {
        return new StockMovementResponse(1L, 10L, "Widget", 1L, "WH-1", 2L, "WH-2",
                5, OperationType.TRANSFER, LocalDateTime.now());
    }

    @Test
    void getAllMovements_returnsList() throws Exception {
        when(stockMovementService.getAllMovements()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/stock-movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].operationType").value("TRANSFER"));
    }

    @Test
    void getStockMovementById_returnsMovement() throws Exception {
        when(stockMovementService.getStockMovementById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/stock-movements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Widget"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getStockMovementById_returns404WhenMissing() throws Exception {
        when(stockMovementService.getStockMovementById(99L))
                .thenThrow(new StockMovementNotFoundException("Stock movement with ID 99 not found."));

        mockMvc.perform(get("/api/stock-movements/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMovementsByProduct_returnsList() throws Exception {
        when(stockMovementService.getMovementsByProduct(10L)).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/stock-movements/product/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10));
    }

    @Test
    void getMovementsBetweenWarehouses_returnsList() throws Exception {
        when(stockMovementService.getMovementsBetweenWarehouses(1L, 2L)).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/stock-movements/between")
                        .param("sourceWarehouseId", "1")
                        .param("targetWarehouseId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceWarehouseId").value(1));
    }
}
