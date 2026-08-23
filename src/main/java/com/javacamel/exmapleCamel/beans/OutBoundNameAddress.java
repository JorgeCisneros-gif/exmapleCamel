package com.javacamel.exmapleCamel.beans;

import lombok.Data;

@Data
public class OutBoundNameAddress {

    private String name;
    private String address;


    public OutBoundNameAddress(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
