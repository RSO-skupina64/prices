package com.rso.microservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageDto {

    @JsonProperty("message")
    private String message;

    public MessageDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
