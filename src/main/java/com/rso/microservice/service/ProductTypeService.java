package com.rso.microservice.service;

import com.rso.microservice.entity.ProductType;
import com.rso.microservice.repository.ProductTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductTypeService {

	private ProductTypeRepository productTypeRepository;

	public ProductTypeService(ProductTypeRepository productTypeRepository) {
		this.productTypeRepository = productTypeRepository;
	}

	public ProductType createOrUpdateType(ProductType productType) {
		List<ProductType> productTypeList = productTypeRepository.findByName(productType.getName());
		if (productTypeList != null && productTypeList.size() > 0) {
			return productTypeList.get(0);
		}

		return productTypeRepository.save(productType);
	}

}
