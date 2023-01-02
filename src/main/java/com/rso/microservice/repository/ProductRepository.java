package com.rso.microservice.repository;

import com.rso.microservice.entity.ConcentrationUnitEnum;
import com.rso.microservice.entity.Product;
import com.rso.microservice.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p WHERE p.name = :name AND p.brand = :brand AND p.concentration = :concentration AND p.concentrationUnit = :concentrationUnit AND p.productType = :productType")
    List<Product> findByProduct(@Param("name") String name, @Param("brand") String brand,
                                @Param("concentration") BigDecimal concentration,
                                @Param("concentrationUnit") ConcentrationUnitEnum concentrationUnit,
                                @Param("productType") ProductType productType);
}
