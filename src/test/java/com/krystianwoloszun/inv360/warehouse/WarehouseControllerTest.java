package com.krystianwoloszun.inv360.warehouse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.krystianwoloszun.inv360.common.exception.WarehouseAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.WarehouseNotFoundException;
import com.krystianwoloszun.inv360.warehouse.dto.AddressResponse;
import com.krystianwoloszun.inv360.warehouse.dto.CreateWarehouseRequest;
import com.krystianwoloszun.inv360.warehouse.dto.WarehouseResponse;

@WebMvcTest(WarehouseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WarehouseService warehouseService;

    private Address address() {
        return Address.builder().street("Main St").buildingNumber("1").city("Warsaw").postalCode("00-001").build();
    }

    private WarehouseResponse sample() {
        return new WarehouseResponse(1L, "Central", "desc",
                new AddressResponse("Main St", "1", "Warsaw", "00-001"));
    }

    @Test
    void getAllWarehouses_returnsList() throws Exception {
        when(warehouseService.getAllWarehouses()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Central"))
                .andExpect(jsonPath("$[0].address.city").value("Warsaw"));
    }

    @Test
    void getWarehouseById_returnsWarehouse() throws Exception {
        when(warehouseService.getWarehouseById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/warehouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getWarehouseById_returns404WhenMissing() throws Exception {
        when(warehouseService.getWarehouseById(99L))
                .thenThrow(new WarehouseNotFoundException("Warehouse with ID 99 not found."));

        mockMvc.perform(get("/api/warehouses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWarehouse_returnsCreated() throws Exception {
        CreateWarehouseRequest request = new CreateWarehouseRequest("Central", "desc", address());
        when(warehouseService.createWarehouse(any())).thenReturn(sample());

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Central"));
    }

    @Test
    void createWarehouse_returns400WhenNameBlank() throws Exception {
        CreateWarehouseRequest request = new CreateWarehouseRequest("", "desc", address());

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWarehouse_returns409WhenDuplicate() throws Exception {
        CreateWarehouseRequest request = new CreateWarehouseRequest("Central", "desc", address());
        when(warehouseService.createWarehouse(any()))
                .thenThrow(new WarehouseAlreadyExistsException("Warehouse with name Central already exists."));

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteWarehouse_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/warehouses/1"))
                .andExpect(status().isOk());

        verify(warehouseService).deleteWarehouse(1L);
    }
}
