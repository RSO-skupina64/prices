package com.rso.microservice.service;

import com.rso.microservice.entity.ProductShop;
import com.rso.microservice.entity.ProductShopHistory;
import com.rso.microservice.repository.ProductShopHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductShopHistoryService {

	private ProductShopHistoryRepository productShopHistoryRepository;

	public ProductShopHistoryService(ProductShopHistoryRepository productShopHistoryRepository) {
		this.productShopHistoryRepository = productShopHistoryRepository;
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
