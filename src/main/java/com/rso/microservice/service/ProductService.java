package com.rso.microservice.service;

import com.rso.microservice.entity.Product;
import com.rso.microservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

	private ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Product createOrUpdateProduct(Product product) {
		List<Product> productList = productRepository.findByProduct(product.getName(), product.getBrand(),
				product.getConcentration(), product.getConcentrationUnit(), product.getProductType());
		if (productList != null && productList.size() > 0) {
			return productList.get(0);
		}

		return productRepository.save(product);
	}
}
