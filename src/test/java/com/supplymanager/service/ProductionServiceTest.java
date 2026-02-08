package com.supplymanager.service;

import com.supplymanager.domain.dto.ProductionSuggestionDTO;
import com.supplymanager.domain.model.Product;
import com.supplymanager.domain.model.ProductRawMaterial;
import com.supplymanager.domain.model.RawMaterial;
import com.supplymanager.repository.ProductRepository;
import com.supplymanager.repository.RawMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @InjectMocks
    private ProductionService productionService;

    private RawMaterial rm1;
    private RawMaterial rm2;

    @BeforeEach
    void setUp() {
        rm1 = new RawMaterial(1L, "RM1", "Material 1", new BigDecimal(10), new ArrayList<>());
        rm2 = new RawMaterial(2L, "RM2", "Material 2", new BigDecimal(6), new ArrayList<>());
    }

    @Test
    void shouldPrioritizeHigherValueProduct() {
        Product productA = buildProduct(1L, "PA", "Product A", "200", List.of(
                buildPrm(rm1, "5")));
        Product productB = buildProduct(2L, "PB", "Product B", "100", List.of(
                buildPrm(rm1, "3")));

        when(rawMaterialRepository.findAll()).thenReturn(List.of(rm1));
        when(productRepository.findAllWithRawMaterialsOrderByValueDesc()).thenReturn(List.of(productA, productB));

        ProductionSuggestionDTO result = productionService.calculateSuggestion();

        assertEquals(1, result.producibleProducts().size());
        assertEquals("PA", result.producibleProducts().get(0).productCode());
        assertEquals(2, result.producibleProducts().get(0).producibleQuantity());
        assertEquals(new BigDecimal("400"), result.totalProductionValue());
    }

    @Test
    void shouldHandleMultipleRawMaterials() {
        Product product = buildProduct(1L, "PA", "Product A", "300", List.of(
                buildPrm(rm1, "2"),
                buildPrm(rm2, "3")));

        when(rawMaterialRepository.findAll()).thenReturn(List.of(rm1, rm2));
        when(productRepository.findAllWithRawMaterialsOrderByValueDesc()).thenReturn(List.of(product));

        ProductionSuggestionDTO result = productionService.calculateSuggestion();

        assertEquals(1, result.producibleProducts().size());
        assertEquals(2, result.producibleProducts().get(0).producibleQuantity());
        assertEquals(new BigDecimal("600"), result.totalProductionValue());
    }

    @Test
    void shouldReturnEmptyWhenNoStock() {
        rm1.setStockQuantity(BigDecimal.ZERO);
        Product product = buildProduct(1L, "PA", "Product A", "100", List.of(
                buildPrm(rm1, "5")));

        when(rawMaterialRepository.findAll()).thenReturn(List.of(rm1));
        when(productRepository.findAllWithRawMaterialsOrderByValueDesc()).thenReturn(List.of(product));

        ProductionSuggestionDTO result = productionService.calculateSuggestion();

        assertTrue(result.producibleProducts().isEmpty());
        assertEquals(BigDecimal.ZERO, result.totalProductionValue());
    }

    @Test
    void shouldSkipProductsWithNoRequirements() {
        Product product = buildProduct(1L, "PA", "Product A", "100", List.of());

        when(rawMaterialRepository.findAll()).thenReturn(List.of(rm1));
        when(productRepository.findAllWithRawMaterialsOrderByValueDesc()).thenReturn(List.of(product));

        ProductionSuggestionDTO result = productionService.calculateSuggestion();

        assertTrue(result.producibleProducts().isEmpty());
    }

    @Test
    void shouldProduceMultipleProducts() {
        Product productA = buildProduct(1L, "PA", "Product A", "100", List.of(
                buildPrm(rm1, "2")));
        Product productB = buildProduct(2L, "PB", "Product B", "50", List.of(
                buildPrm(rm2, "2")));

        when(rawMaterialRepository.findAll()).thenReturn(List.of(rm1, rm2));
        when(productRepository.findAllWithRawMaterialsOrderByValueDesc()).thenReturn(List.of(productA, productB));

        ProductionSuggestionDTO result = productionService.calculateSuggestion();

        assertEquals(2, result.producibleProducts().size());
        assertEquals(5, result.producibleProducts().get(0).producibleQuantity());
        assertEquals(3, result.producibleProducts().get(1).producibleQuantity());
        assertEquals(new BigDecimal("650"), result.totalProductionValue());
    }

    private Product buildProduct(Long id, String code, String name, String value, List<ProductRawMaterial> materials) {
        Product p = new Product(id, code, name, new BigDecimal(value), materials);
        p.setRawMaterials(materials);
        materials.forEach(m -> m.setProduct(p));

        return p;
    }

    private ProductRawMaterial buildPrm(RawMaterial rm, String qty) {
        ProductRawMaterial prm = new ProductRawMaterial();
        prm.setRawMaterial(rm);
        prm.setRequiredQuantity(new BigDecimal(qty));

        return prm;
    }
}
