package com.rso.microservice.repository;

import com.rso.microservice.entity.Product;
import com.rso.microservice.entity.ProductShop;
import com.rso.microservice.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductShopRepository extends JpaRepository<ProductShop, Long> {

    List<ProductShop> findByShopAndProduct(Shop shop, Product product);
}
