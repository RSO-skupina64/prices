package com.rso.microservice.service;

import com.rso.microservice.entity.Shop;
import com.rso.microservice.repository.ShopRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShopService {

    private ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
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

}
