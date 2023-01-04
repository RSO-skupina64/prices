package com.rso.microservice.service;

import com.rso.microservice.entity.Product;
import com.rso.microservice.entity.ProductShop;
import com.rso.microservice.entity.Shop;
import com.rso.microservice.repository.ProductShopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductShopService {

	private ProductShopRepository productShopRepository;

	public ProductShopService(ProductShopRepository productShopRepository) {
		this.productShopRepository = productShopRepository;
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

}
