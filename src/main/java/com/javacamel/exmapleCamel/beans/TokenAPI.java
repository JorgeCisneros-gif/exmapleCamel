package com.javacamel.exmapleCamel.beans;

import lombok.Data;

@Data
public class TokenAPI {
    private String accessToken;
    private Integer expiresIn;
    private String tokenType;
}
