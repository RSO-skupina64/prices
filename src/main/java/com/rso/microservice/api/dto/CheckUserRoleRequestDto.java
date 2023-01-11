package com.rso.microservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckUserRoleRequestDto {

    @JsonProperty("role")
    private String role;

    public CheckUserRoleRequestDto(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
