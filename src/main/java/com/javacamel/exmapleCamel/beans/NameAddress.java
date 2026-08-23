package com.javacamel.exmapleCamel.beans;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Data
@Table(name ="NAME_ADDRESS")
@NamedQuery(name="fetchAllRows",query = "select x from NameAddress x")
public class NameAddress implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;

    @Column(name = "house_number")
    private String houseNumber;

    private String city;

    private String province;

    @Column(name = "postal_code")
    private String postalCode;

}


