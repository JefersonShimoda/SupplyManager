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
class ProductRawMaterialIntegrationTest {

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

    private Long productId;
    private Long rawMaterialId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRawMaterialRepository.deleteAll();
        productRepository.deleteAll();
        rawMaterialRepository.deleteAll();

        String productJson = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductDTO(null, "PROD-001", "Mesa", new BigDecimal("150.00"), null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        productId = objectMapper.readTree(productJson).get("id").asLong();

        String rawMatJson = mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RawMaterialDTO(null, "MAT-001", "Madeira", new BigDecimal("100.0000")))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        rawMaterialId = objectMapper.readTree(rawMatJson).get("id").asLong();
    }

    @Test
    void shouldAddRawMaterialToProduct() throws Exception {
        var dto = new ProductRawMaterialDTO(null, rawMaterialId, null, null, new BigDecimal("2.5000"));

        mockMvc.perform(post("/api/products/{productId}/raw-materials", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.rawMaterialId").value(rawMaterialId))
                .andExpect(jsonPath("$.rawMaterialCode").value("MAT-001"))
                .andExpect(jsonPath("$.rawMaterialName").value("Madeira"))
                .andExpect(jsonPath("$.requiredQuantity").value(2.5000));
    }

    @Test
    void shouldListRawMaterialsOfProduct() throws Exception {
        addRawMaterialToProduct(productId, rawMaterialId, "2.5000");

        mockMvc.perform(get("/api/products/{productId}/raw-materials", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rawMaterialCode").value("MAT-001"));
    }

    @Test
    void shouldUpdateRequiredQuantity() throws Exception {
        addRawMaterialToProduct(productId, rawMaterialId, "2.5000");

        var updated = new ProductRawMaterialDTO(null, rawMaterialId, null, null, new BigDecimal("5.0000"));

        mockMvc.perform(put("/api/products/{productId}/raw-materials/{rawMaterialId}", productId, rawMaterialId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredQuantity").value(5.0000));
    }

    @Test
    void shouldRemoveRawMaterialFromProduct() throws Exception {
        addRawMaterialToProduct(productId, rawMaterialId, "2.5000");

        mockMvc.perform(delete("/api/products/{productId}/raw-materials/{rawMaterialId}", productId, rawMaterialId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{productId}/raw-materials", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn409WhenDuplicateAssociation() throws Exception {
        addRawMaterialToProduct(productId, rawMaterialId, "2.5000");

        var duplicate = new ProductRawMaterialDTO(null, rawMaterialId, null, null, new BigDecimal("3.0000"));

        mockMvc.perform(post("/api/products/{productId}/raw-materials", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    private void addRawMaterialToProduct(Long productId, Long rawMaterialId, String quantity) throws Exception {
        var dto = new ProductRawMaterialDTO(null, rawMaterialId, null, null, new BigDecimal(quantity));
        mockMvc.perform(post("/api/products/{productId}/raw-materials", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
