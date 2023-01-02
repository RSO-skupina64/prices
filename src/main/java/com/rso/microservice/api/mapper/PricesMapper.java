package com.rso.microservice.api.mapper;

import com.rso.microservice.api.dto.ProductDto;
import com.rso.microservice.api.dto.ShopDto;
import com.rso.microservice.entity.Product;
import com.rso.microservice.entity.Shop;
import com.rso.microservice.entity.ProductType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PricesMapper {

    ProductDto toModel(Product product);

    ShopDto toModel(Shop shop);

    @Mapping(source = "name", target = ".")
    String toModel(ProductType productType);
}
