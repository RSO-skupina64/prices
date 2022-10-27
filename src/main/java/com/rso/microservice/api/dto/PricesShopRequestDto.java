package com.rso.microservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public class PricesShopRequestDto {
    @JsonProperty("id_shop")
    @NotBlank(message = "is required")
    private Long idShop;

    public Long getIdShop() {
        return idShop;
    }

    public void setIdShop(Long idShop) {
        this.idShop = idShop;
    }
}
