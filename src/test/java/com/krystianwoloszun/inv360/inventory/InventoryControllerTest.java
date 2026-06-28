package com.krystianwoloszun.inv360.inventory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krystianwoloszun.inv360.common.exception.InsufficientStockException;
import com.krystianwoloszun.inv360.common.exception.InventoryNotFoundException;
import com.krystianwoloszun.inv360.inventory.dto.AddStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.CreateInventoryRequest;
import com.krystianwoloszun.inv360.inventory.dto.InventoryResponse;
import com.krystianwoloszun.inv360.inventory.dto.RemoveStockRequest;
import com.krystianwoloszun.inv360.inventory.dto.TransferStockRequest;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InventoryService inventoryService;

    private InventoryResponse response(int quantity) {
        return new InventoryResponse(100L, 1L, 10L, quantity);
    }

    @Test
    void getAllInventories_returnsList() throws Exception {
        when(inventoryService.getAllInventories()).thenReturn(List.of(response(5)));

        mockMvc.perform(get("/api/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    void getInventoryByProductIdAndWarehouseId_returnsInventory() throws Exception {
        when(inventoryService.getInventoryByProductIdAndWarehouseId(10L, 1L)).thenReturn(response(7));

        mockMvc.perform(get("/api/inventories/product/10/warehouse/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void createInventory_returnsCreated() throws Exception {
        CreateInventoryRequest request = new CreateInventoryRequest(10L, 1L, 5);
        when(inventoryService.createInventory(any())).thenReturn(response(5));

        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void createInventory_returns400WhenProductIdNull() throws Exception {
        CreateInventoryRequest request = new CreateInventoryRequest(null, 1L, 5);

        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_returnsUpdatedInventory() throws Exception {
        AddStockRequest request = new AddStockRequest(1L, 10L, 3);
        when(inventoryService.addStock(any())).thenReturn(response(8));

        mockMvc.perform(post("/api/inventories/add-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8));
    }

    @Test
    void addStock_returns400WhenQuantityNotPositive() throws Exception {
        AddStockRequest request = new AddStockRequest(1L, 10L, 0);

        mockMvc.perform(post("/api/inventories/add-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeStock_returns400WhenInsufficient() throws Exception {
        RemoveStockRequest request = new RemoveStockRequest(1L, 10L, 5);
        when(inventoryService.removeStock(any()))
                .thenThrow(new InsufficientStockException("Cannot remove more stock than available."));

        mockMvc.perform(post("/api/inventories/remove-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeStock_returns404WhenInventoryMissing() throws Exception {
        RemoveStockRequest request = new RemoveStockRequest(1L, 10L, 5);
        when(inventoryService.removeStock(any()))
                .thenThrow(new InventoryNotFoundException("Inventory not found."));

        mockMvc.perform(post("/api/inventories/remove-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferStock_returnsBothInventories() throws Exception {
        TransferStockRequest request = new TransferStockRequest(1L, 2L, 10L, 6);
        when(inventoryService.transferStock(any()))
                .thenReturn(List.of(new InventoryResponse(100L, 1L, 10L, 4),
                        new InventoryResponse(200L, 2L, 10L, 6)));

        mockMvc.perform(post("/api/inventories/transfer-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(4))
                .andExpect(jsonPath("$[1].quantity").value(6));
    }

    @Test
    void deleteInventory_returnsOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/inventories/1"))
                .andExpect(status().isOk());

        verify(inventoryService).deleteInventory(1L);
    }
}
