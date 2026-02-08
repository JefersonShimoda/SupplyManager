package com.supplymanager.service;

import com.supplymanager.domain.dto.RawMaterialDTO;
import com.supplymanager.domain.model.RawMaterial;
import com.supplymanager.exception.DuplicateResourceException;
import com.supplymanager.exception.ResourceNotFoundException;
import com.supplymanager.repository.RawMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;

    public RawMaterialService(RawMaterialRepository rawMaterialRepository) {
        this.rawMaterialRepository = rawMaterialRepository;
    }

    public List<RawMaterialDTO> findAll() {
        return rawMaterialRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public RawMaterialDTO findById(Long id) {
        return toDTO(rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterial", id)));
    }

    @Transactional
    public RawMaterialDTO create(RawMaterialDTO dto) {
        if (rawMaterialRepository.existsByCode(dto.code())) {
            throw new DuplicateResourceException("RawMaterial with code '" + dto.code() + "' already exists");
        }
        RawMaterial rawMaterial = new RawMaterial();
        rawMaterial.setCode(dto.code());
        rawMaterial.setName(dto.name());
        rawMaterial.setStockQuantity(dto.stockQuantity());
        return toDTO(rawMaterialRepository.save(rawMaterial));
    }

    @Transactional
    public RawMaterialDTO update(Long id, RawMaterialDTO dto) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterial", id));
        if (!rawMaterial.getCode().equals(dto.code()) && rawMaterialRepository.existsByCode(dto.code())) {
            throw new DuplicateResourceException("RawMaterial with code '" + dto.code() + "' already exists");
        }
        rawMaterial.setCode(dto.code());
        rawMaterial.setName(dto.name());
        rawMaterial.setStockQuantity(dto.stockQuantity());
        return toDTO(rawMaterialRepository.save(rawMaterial));
    }

    @Transactional
    public void delete(Long id) {
        if (!rawMaterialRepository.existsById(id)) {
            throw new ResourceNotFoundException("RawMaterial", id);
        }
        rawMaterialRepository.deleteById(id);
    }

    private RawMaterialDTO toDTO(RawMaterial rm) {
        return new RawMaterialDTO(rm.getId(), rm.getCode(), rm.getName(), rm.getStockQuantity());
    }
}
