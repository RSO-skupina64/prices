package com.rso.microservice.repository;

import com.rso.microservice.entity.ProductShop;
import com.rso.microservice.entity.ProductShopHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductShopHistoryRepository extends JpaRepository<ProductShopHistory, Long> {

    List<ProductShopHistory> findByDateAndProductShop(LocalDateTime date, ProductShop productShop);
}
