package com.rso.microservice.service;


import com.rso.microservice.entity.*;
import com.rso.microservice.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PricesService {

    final ProductRepository productRepository;
    final ShopRepository shopRepository;
    final ProductTypeRepository productTypeRepository;
    final ProductShopRepository productShopRepository;
    final ProductShopHistoryRepository productShopHistoryRepository;

    public PricesService(ProductRepository productRepository, ShopRepository shopRepository,
                         ProductTypeRepository productTypeRepository, ProductShopRepository productShopRepository,
                         ProductShopHistoryRepository productShopHistoryRepository) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.productTypeRepository = productTypeRepository;
        this.productShopRepository = productShopRepository;
        this.productShopHistoryRepository = productShopHistoryRepository;
    }

    public Product createOrUpdateProduct(Product product) {
        List<Product> productList = productRepository.findByProduct(product.getName(), product.getBrand(),
                product.getConcentration(), product.getConcentrationUnit(), product.getProductType());
        if (productList != null && productList.size() > 0) {
            return productList.get(0);
        }

        return productRepository.save(product);
    }

    public ProductType createOrUpdateType(ProductType productType) {
        List<ProductType> productTypeList = productTypeRepository.findByName(productType.getName());
        if (productTypeList != null && productTypeList.size() > 0) {
            return productTypeList.get(0);
        }

        return productTypeRepository.save(productType);
    }

    public Optional<Shop> findById(Long id) {
        return shopRepository.findById(id);
    }

    public Shop createOrUpdateShop(Shop shop) {
        List<Shop> shopList = shopRepository.findByName(shop.getName());
        if (shopList != null && shopList.size() > 0) {
            return shopList.get(0);
        }

        return shopRepository.save(shop);
    }

    public ProductShop findProductShop(Shop shop, Product product) {
        List<ProductShop> productShopList = productShopRepository.findByShopAndProduct(shop, product);
        if (productShopList != null && productShopList.size() > 0) {
            return productShopList.get(0);
        }

        return null;
    }

    public ProductShop createProductShop(ProductShop productShop) {
        return productShopRepository.save(productShop);
    }

    public ProductShopHistory findProductShopHistory(LocalDateTime date, ProductShop productShop) {
        List<ProductShopHistory> productShopHistoryList = productShopHistoryRepository.findByDateAndProductShop(date,
                productShop);
        if (productShopHistoryList != null && productShopHistoryList.size() > 0) {
            return productShopHistoryList.get(0);
        }

        return null;
    }

    public ProductShopHistory createProductShopHistory(ProductShopHistory productShopHistory) {
        return productShopHistoryRepository.save(productShopHistory);
    }
}
