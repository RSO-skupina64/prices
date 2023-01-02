package com.rso.microservice.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_SHOP_HISTORY")
public class ProductShopHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DATE", nullable = false)
    private LocalDateTime date;

    @Column(name = "PRICE_EUR", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceEUR;

    @ManyToOne(targetEntity = ProductShop.class)
    private ProductShop productShop;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getPriceEUR() {
        return priceEUR;
    }

    public void setPriceEUR(BigDecimal price) {
        this.priceEUR = price;
    }

    public ProductShop getProductShop() {
        return productShop;
    }

    public void setProductShop(ProductShop productShop) {
        this.productShop = productShop;
    }
}
