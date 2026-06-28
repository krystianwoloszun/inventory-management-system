package com.krystianwoloszun.inv360.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krystianwoloszun.inv360.common.exception.ProductAlreadyExistsException;
import com.krystianwoloszun.inv360.common.exception.ProductNotFoundException;
import com.krystianwoloszun.inv360.product.dto.CreateProductRequest;
import com.krystianwoloszun.inv360.product.dto.ProductResponse;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    private ProductResponse sample() {
        return new ProductResponse(1L, "Widget", "desc", "SKU-1", new BigDecimal("9.99"));
    }

    @Test
    void getAllProducts_returnsList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Widget"))
                .andExpect(jsonPath("$[0].sku").value("SKU-1"));
    }

    @Test
    void getProductById_returnsProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProductById_returns404WhenMissing() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ProductNotFoundException("Product with ID 99 not found."));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product with ID 99 not found."));
    }

    @Test
    void createProduct_returnsCreatedProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Widget", "desc", "SKU-1", new BigDecimal("9.99"));
        when(productService.createProduct(any())).thenReturn(sample());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void createProduct_returns400WhenNameBlank() throws Exception {
        CreateProductRequest request = new CreateProductRequest("", "desc", "SKU-1", new BigDecimal("9.99"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_returns400WhenPriceNotPositive() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Widget", "desc", "SKU-1", new BigDecimal("0.00"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_returns409WhenDuplicate() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Widget", "desc", "SKU-1", new BigDecimal("9.99"));
        when(productService.createProduct(any()))
                .thenThrow(new ProductAlreadyExistsException("Product with SKU SKU-1 already exists."));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteProduct_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void updateProduct_returnsUpdated() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Widget", "desc", "SKU-1", new BigDecimal("9.99"));
        when(productService.updateProduct(eq(1L), any())).thenReturn(sample());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
