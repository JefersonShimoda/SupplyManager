package com.supplymanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplymanager.domain.dto.RawMaterialDTO;
import com.supplymanager.exception.GlobalExceptionHandler;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.service.RawMaterialService;
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
class RawMaterialControllerTest {

    @Mock
    private RawMaterialService rawMaterialService;

    @InjectMocks
    private RawMaterialController rawMaterialController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rawMaterialController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void findAll_shouldReturn200WithRawMaterials() throws Exception {
        RawMaterialDTO dto = new RawMaterialDTO(1L, "RM1", "Material 1", new BigDecimal("100"));
        when(rawMaterialService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/raw-materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("RM1"))
                .andExpect(jsonPath("$[0].stockQuantity").value(100));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        RawMaterialDTO dto = new RawMaterialDTO(1L, "RM1", "Material 1", new BigDecimal("100"));
        when(rawMaterialService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/raw-materials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RM1"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(rawMaterialService.findById(99L)).thenThrow(new ResourceNotFoundException("RawMaterial", 99L));

        mockMvc.perform(get("/api/raw-materials/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1", "Material 1", new BigDecimal("100"));
        RawMaterialDTO created = new RawMaterialDTO(1L, "RM1", "Material 1", new BigDecimal("100"));
        when(rawMaterialService.create(any(RawMaterialDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("RM1"));
    }

    @Test
    void create_shouldReturn400WithInvalidBody() throws Exception {
        RawMaterialDTO dto = new RawMaterialDTO(null, "", "", null);

        mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1-UP", "Material Updated", new BigDecimal("200"));
        RawMaterialDTO updated = new RawMaterialDTO(1L, "RM1-UP", "Material Updated", new BigDecimal("200"));
        when(rawMaterialService.update(eq(1L), any(RawMaterialDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/raw-materials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RM1-UP"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(rawMaterialService).delete(1L);

        mockMvc.perform(delete("/api/raw-materials/1"))
                .andExpect(status().isNoContent());

        verify(rawMaterialService).delete(1L);
    }
}
