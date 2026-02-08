package com.supplymanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplymanager.domain.dto.RawMaterialDTO;
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
class RawMaterialIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        rawMaterialRepository.deleteAll();
    }

    @Test
    void shouldCreateRawMaterial() throws Exception {
        var dto = new RawMaterialDTO(null, "MAT-001", "Madeira", new BigDecimal("100.0000"));

        mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").value("MAT-001"))
                .andExpect(jsonPath("$.name").value("Madeira"))
                .andExpect(jsonPath("$.stockQuantity").value(100.0000));
    }

    @Test
    void shouldListRawMaterials() throws Exception {
        createRawMaterial("MAT-001", "Madeira", "100.0000");
        createRawMaterial("MAT-002", "Ferro", "50.0000");

        mockMvc.perform(get("/api/raw-materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldFindRawMaterialById() throws Exception {
        String response = createRawMaterial("MAT-001", "Madeira", "100.0000");
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/raw-materials/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MAT-001"));
    }

    @Test
    void shouldUpdateRawMaterial() throws Exception {
        String response = createRawMaterial("MAT-001", "Madeira", "100.0000");
        Long id = objectMapper.readTree(response).get("id").asLong();

        var updated = new RawMaterialDTO(null, "MAT-001", "Madeira Nobre", new BigDecimal("200.0000"));

        mockMvc.perform(put("/api/raw-materials/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Madeira Nobre"))
                .andExpect(jsonPath("$.stockQuantity").value(200.0000));
    }

    @Test
    void shouldDeleteRawMaterial() throws Exception {
        String response = createRawMaterial("MAT-001", "Madeira", "100.0000");
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/raw-materials/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/raw-materials/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenDuplicateCode() throws Exception {
        createRawMaterial("MAT-001", "Madeira", "100.0000");

        var duplicate = new RawMaterialDTO(null, "MAT-001", "Outra Madeira", new BigDecimal("50.0000"));

        mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenRawMaterialNotFound() throws Exception {
        mockMvc.perform(get("/api/raw-materials/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        var invalid = new RawMaterialDTO(null, "", "", null);

        mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    private String createRawMaterial(String code, String name, String stockQuantity) throws Exception {
        var dto = new RawMaterialDTO(null, code, name, new BigDecimal(stockQuantity));
        return mockMvc.perform(post("/api/raw-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
