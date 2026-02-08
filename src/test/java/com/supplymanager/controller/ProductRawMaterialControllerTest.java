package com.supplymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplymanager.domain.dto.ProductRawMaterialDTO;
import com.supplymanager.exception.GlobalExceptionHandler;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.service.ProductRawMaterialService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductRawMaterialControllerTest {

    @Mock
    private ProductRawMaterialService prmService;

    @InjectMocks
    private ProductRawMaterialController prmController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(prmController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void findByProductId_shouldReturn200() throws Exception {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(1L, 1L, "RM1", "Material 1", new BigDecimal("2.5"));
        when(prmService.findByProductId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/products/1/raw-materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rawMaterialCode").value("RM1"))
                .andExpect(jsonPath("$[0].requiredQuantity").value(2.5));
    }

    @Test
    void findByProductId_shouldReturn404WhenProductNotFound() throws Exception {
        when(prmService.findByProductId(99L)).thenThrow(new ResourceNotFoundException("Product", 99L));

        mockMvc.perform(get("/api/products/99/raw-materials"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void add_shouldReturn201() throws Exception {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, 1L, null, null, new BigDecimal("3.0"));
        ProductRawMaterialDTO created = new ProductRawMaterialDTO(1L, 1L, "RM1", "Material 1", new BigDecimal("3.0"));
        when(prmService.add(eq(1L), any(ProductRawMaterialDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/products/1/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rawMaterialCode").value("RM1"));
    }

    @Test
    void add_shouldReturn400WithInvalidBody() throws Exception {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, null, null, null, null);

        mockMvc.perform(post("/api/products/1/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        ProductRawMaterialDTO dto = new ProductRawMaterialDTO(null, 1L, null, null, new BigDecimal("5.0"));
        ProductRawMaterialDTO updated = new ProductRawMaterialDTO(1L, 1L, "RM1", "Material 1", new BigDecimal("5.0"));
        when(prmService.update(eq(1L), eq(1L), any(ProductRawMaterialDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/products/1/raw-materials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredQuantity").value(5.0));
    }

    @Test
    void remove_shouldReturn204() throws Exception {
        doNothing().when(prmService).remove(1L, 1L);

        mockMvc.perform(delete("/api/products/1/raw-materials/1"))
                .andExpect(status().isNoContent());

        verify(prmService).remove(1L, 1L);
    }

    @Test
    void remove_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ProductRawMaterial", 99L)).when(prmService).remove(1L, 99L);

        mockMvc.perform(delete("/api/products/1/raw-materials/99"))
                .andExpect(status().isNotFound());
    }
}
