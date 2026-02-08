package com.supplymanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplymanager.domain.dto.ProductDTO;
import com.supplymanager.repository.ProductRepository;
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
class ProductIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        var dto = new ProductDTO(null, "PROD-001", "Mesa", new BigDecimal("150.00"), null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").value("PROD-001"))
                .andExpect(jsonPath("$.name").value("Mesa"))
                .andExpect(jsonPath("$.value").value(150.00));
    }

    @Test
    void shouldListProducts() throws Exception {
        createProduct("PROD-001", "Mesa", "150.00");
        createProduct("PROD-002", "Cadeira", "80.00");

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldFindProductById() throws Exception {
        String response = createProduct("PROD-001", "Mesa", "150.00");
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROD-001"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        String response = createProduct("PROD-001", "Mesa", "150.00");
        Long id = objectMapper.readTree(response).get("id").asLong();

        var updated = new ProductDTO(null, "PROD-001", "Mesa Grande", new BigDecimal("200.00"), null);

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mesa Grande"))
                .andExpect(jsonPath("$.value").value(200.00));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        String response = createProduct("PROD-001", "Mesa", "150.00");
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenDuplicateCode() throws Exception {
        createProduct("PROD-001", "Mesa", "150.00");

        var duplicate = new ProductDTO(null, "PROD-001", "Outra Mesa", new BigDecimal("200.00"), null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        var invalid = new ProductDTO(null, "", "", null, null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    private String createProduct(String code, String name, String value) throws Exception {
        var dto = new ProductDTO(null, code, name, new BigDecimal(value), null);
        return mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
