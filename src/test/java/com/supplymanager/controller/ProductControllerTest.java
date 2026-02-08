package com.supplymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplymanager.domain.dto.ProductDTO;
import com.supplymanager.domain.dto.ProductRawMaterialDTO;
import com.supplymanager.exception.GlobalExceptionHandler;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void findAll_shouldReturn200WithProducts() throws Exception {
        ProductDTO dto = new ProductDTO(1L, "P1", "Product 1", new BigDecimal("10.00"), Collections.emptyList());
        when(productService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("P1"))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }

    @Test
    void findById_shouldReturn200WithProduct() throws Exception {
        List<ProductRawMaterialDTO> materials = List.of(
                new ProductRawMaterialDTO(1L, 1L, "RM1", "Material 1", new BigDecimal("2.5")));
        ProductDTO dto = new ProductDTO(1L, "P1", "Product 1", new BigDecimal("10.00"), materials);
        when(productService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("P1"))
                .andExpect(jsonPath("$.rawMaterials[0].rawMaterialCode").value("RM1"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product", 99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        ProductDTO dto = new ProductDTO(null, "P1", "Product 1", new BigDecimal("10.00"), null);
        ProductDTO created = new ProductDTO(1L, "P1", "Product 1", new BigDecimal("10.00"), Collections.emptyList());
        when(productService.create(any(ProductDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("P1"));
    }

    @Test
    void create_shouldReturn400WithInvalidBody() throws Exception {
        ProductDTO dto = new ProductDTO(null, "", "", null, null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        ProductDTO dto = new ProductDTO(null, "P1-UP", "Product Updated", new BigDecimal("20.00"), null);
        ProductDTO updated = new ProductDTO(1L, "P1-UP", "Product Updated", new BigDecimal("20.00"), Collections.emptyList());
        when(productService.update(eq(1L), any(ProductDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("P1-UP"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }
}
