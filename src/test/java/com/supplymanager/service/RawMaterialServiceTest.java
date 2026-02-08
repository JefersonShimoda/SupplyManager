package com.supplymanager.service;

import com.supplymanager.domain.dto.RawMaterialDTO;
import com.supplymanager.domain.model.RawMaterial;
import com.supplymanager.exception.DuplicateResourceException;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.repository.RawMaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawMaterialServiceTest {

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @InjectMocks
    private RawMaterialService rawMaterialService;

    @Test
    void findAll_shouldReturnListOfRawMaterials() {
        RawMaterial rm1 = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());
        RawMaterial rm2 = new RawMaterial(2L, "RM2", "Material 2", new BigDecimal("200"), new ArrayList<>());
        when(rawMaterialRepository.findAll()).thenReturn(List.of(rm1, rm2));

        List<RawMaterialDTO> result = rawMaterialService.findAll();

        assertEquals(2, result.size());
        assertEquals("RM1", result.get(0).code());
        assertEquals("RM2", result.get(1).code());
    }

    @Test
    void findAll_shouldReturnEmptyList() {
        when(rawMaterialRepository.findAll()).thenReturn(List.of());

        List<RawMaterialDTO> result = rawMaterialService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnRawMaterial() {
        RawMaterial rm = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rm));

        RawMaterialDTO result = rawMaterialService.findById(1L);

        assertEquals("RM1", result.code());
        assertEquals(new BigDecimal("100"), result.stockQuantity());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(rawMaterialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rawMaterialService.findById(99L));
    }

    @Test
    void create_shouldCreateRawMaterial() {
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1", "Material 1", new BigDecimal("100"));
        RawMaterial saved = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());

        when(rawMaterialRepository.existsByCode("RM1")).thenReturn(false);
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(saved);

        RawMaterialDTO result = rawMaterialService.create(dto);

        assertEquals(1L, result.id());
        assertEquals("RM1", result.code());
        verify(rawMaterialRepository).save(any(RawMaterial.class));
    }

    @Test
    void create_shouldThrowWhenDuplicateCode() {
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1", "Material 1", new BigDecimal("100"));
        when(rawMaterialRepository.existsByCode("RM1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> rawMaterialService.create(dto));
        verify(rawMaterialRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateRawMaterial() {
        RawMaterial existing = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1-UP", "Material Updated", new BigDecimal("200"));
        RawMaterial updated = new RawMaterial(1L, "RM1-UP", "Material Updated", new BigDecimal("200"), new ArrayList<>());

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rawMaterialRepository.existsByCode("RM1-UP")).thenReturn(false);
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(updated);

        RawMaterialDTO result = rawMaterialService.update(1L, dto);

        assertEquals("RM1-UP", result.code());
        assertEquals(new BigDecimal("200"), result.stockQuantity());
    }

    @Test
    void update_shouldAllowSameCode() {
        RawMaterial existing = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1", "Material Renamed", new BigDecimal("150"));
        RawMaterial updated = new RawMaterial(1L, "RM1", "Material Renamed", new BigDecimal("150"), new ArrayList<>());

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(updated);

        RawMaterialDTO result = rawMaterialService.update(1L, dto);

        assertEquals("Material Renamed", result.name());
        verify(rawMaterialRepository, never()).existsByCode(anyString());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM1", "Material 1", new BigDecimal("100"));
        when(rawMaterialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rawMaterialService.update(99L, dto));
    }

    @Test
    void update_shouldThrowWhenDuplicateCode() {
        RawMaterial existing = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal("100"), new ArrayList<>());
        RawMaterialDTO dto = new RawMaterialDTO(null, "RM2", "Material 1", new BigDecimal("100"));

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rawMaterialRepository.existsByCode("RM2")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> rawMaterialService.update(1L, dto));
        verify(rawMaterialRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteRawMaterial() {
        when(rawMaterialRepository.existsById(1L)).thenReturn(true);

        rawMaterialService.delete(1L);

        verify(rawMaterialRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(rawMaterialRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> rawMaterialService.delete(99L));
        verify(rawMaterialRepository, never()).deleteById(any());
    }
}
