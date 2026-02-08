package com.supplymanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplymanager.domain.dto.ProductDTO;
import com.supplymanager.domain.dto.ProductRawMaterialDTO;
import com.supplymanager.domain.dto.RawMaterialDTO;
import com.supplymanager.repository.ProductRawMaterialRepository;
import com.supplymanager.repository.ProductRepository;
import com.supplymanager.repository.RawMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductionSuggestionIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProductRawMaterialRepository productRawMaterialRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRawMaterialRepository.deleteAll();
        productRepository.deleteAll();
        rawMaterialRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyWhenNoData() throws Exception {
        mockMvc.perform(get("/api/production/suggestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producibleProducts", hasSize(0)))
                .andExpect(jsonPath("$.totalProductionValue").value(0));
    }

    @Test
    void shouldSuggestProductionWithGreedyAlgorithm() throws Exception {
        Long matId = createRawMaterial("MAT-001", "Madeira", "10.0000");
        Long mesaId = createProduct("PROD-001", "Mesa", "200.00");
        Long cadeiraId = createProduct("PROD-002", "Cadeira", "80.00");

        addRawMaterialToProduct(mesaId, matId, "3.0000");
        addRawMaterialToProduct(cadeiraId, matId, "2.0000");

        mockMvc.perform(get("/api/production/suggestion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producibleProducts").isArray())
                .andExpect(jsonPath("$.producibleProducts[?(@.productCode == 'PROD-001')].producibleQuantity",
                        contains(3)))
                .andExpect(jsonPath("$.producibleProducts[?(@.productCode == 'PROD-001')].totalValue",
                        contains(600.0)))
                .andExpect(jsonPath("$.totalProductionValue").value(600.0));
    }

    private Long createProduct(String code, String name, String value) throws Exception {
        var dto = new ProductDTO(null, code, name, new BigDecimal(value), null);
        String json = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private Long createRawMaterial(String code, String name, String stockQuantity) throws Exception {
        var dto = new RawMaterialDTO(null, code, name, new BigDecimal(stockQuantity));
        String json = mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private void addRawMaterialToProduct(Long productId, Long rawMaterialId, String quantity) throws Exception {
        var dto = new ProductRawMaterialDTO(null, rawMaterialId, null, null, new BigDecimal(quantity));
        mockMvc.perform(post("/api/products/{productId}/raw-materials", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
