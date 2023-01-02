package com.rso.microservice.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "PRODUCT_SHOP")
public class ProductShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PRICE_EUR", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceEUR;

    @ManyToOne(targetEntity = Shop.class)
    private Shop shop;

    @ManyToOne(targetEntity = Product.class)
    private Product product;

    @OneToMany(targetEntity = ProductShopHistory.class, mappedBy = "productShop")
    private List<ProductShopHistory> productShopHistories;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPriceEUR() {
        return priceEUR;
    }

    public void setPriceEUR(BigDecimal price) {
        this.priceEUR = price;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
