package com.supplymanager.repository;

import com.supplymanager.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCode(String code);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.rawMaterials prm LEFT JOIN FETCH prm.rawMaterial WHERE p.id = :id")
    Optional<Product> findByIdWithRawMaterials(Long id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.rawMaterials prm LEFT JOIN FETCH prm.rawMaterial ORDER BY p.value DESC")
    List<Product> findAllWithRawMaterialsOrderByValueDesc();
}
