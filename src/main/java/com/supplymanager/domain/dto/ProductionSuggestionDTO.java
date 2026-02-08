package com.supplymanager.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductionSuggestionDTO(
    List<ProducibleProductDTO> producibleProducts,
    BigDecimal totalProductionValue
) {}
