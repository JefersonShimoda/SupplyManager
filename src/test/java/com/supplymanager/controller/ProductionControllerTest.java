package com.supplymanager.controller;

import com.supplymanager.domain.dto.ProducibleProductDTO;
import com.supplymanager.domain.dto.ProductionSuggestionDTO;
import com.supplymanager.exception.GlobalExceptionHandler;
import com.supplymanager.service.ProductionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductionControllerTest {

    @Mock
    private ProductionService productionService;

    @InjectMocks
    private ProductionController productionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSuggestion_shouldReturn200WithProducts() throws Exception {
        ProducibleProductDTO product = new ProducibleProductDTO(
                1L, "P1", "Product 1", new BigDecimal("100"), 5, new BigDecimal("500"));
        ProductionSuggestionDTO suggestion = new ProductionSuggestionDTO(
                List.of(product), new BigDecimal("500"));
        when(productionService.calculateSuggestion()).thenReturn(suggestion);

        mockMvc.perform(get("/api/production/suggestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductionValue").value(500))
                .andExpect(jsonPath("$.producibleProducts[0].productCode").value("P1"))
                .andExpect(jsonPath("$.producibleProducts[0].producibleQuantity").value(5));
    }

    @Test
    void getSuggestion_shouldReturn200WithEmptyList() throws Exception {
        ProductionSuggestionDTO suggestion = new ProductionSuggestionDTO(
                Collections.emptyList(), BigDecimal.ZERO);
        when(productionService.calculateSuggestion()).thenReturn(suggestion);

        mockMvc.perform(get("/api/production/suggestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductionValue").value(0))
                .andExpect(jsonPath("$.producibleProducts").isEmpty());
    }
}
