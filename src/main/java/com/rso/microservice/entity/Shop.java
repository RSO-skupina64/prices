package com.rso.microservice.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "SHOP")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @OneToMany(targetEntity = ProductShop.class, mappedBy = "shop")
    List<ProductShop> productShops;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductShop> getShopProducts() {
        return productShops;
    }

    public void setShopProducts(List<ProductShop> productShops) {
        this.productShops = productShops;
    }
}
