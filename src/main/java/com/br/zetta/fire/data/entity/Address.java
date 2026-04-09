package com.br.zetta.fire.data.entity;

import com.br.zetta.fire.data.dto.request.AddressRequestDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "address")
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idAddress;

    @Column(name = "cep", nullable = false)
    private String cep;

    @Column(name= "street", nullable = false)
    private String street;

    @Column(name="number", nullable = false)
    private String number;

    @Column(name="district",nullable = false)
    private String district;

    @Column(name="city",nullable = false)
    private String city;

    @Column(name="state", nullable = false)
    private String state;

    @Column(name="latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name="longitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal longitude;


    public Address(AddressRequestDTO addressRequestDTO) {
        this.cep = addressRequestDTO.cep();
        this.street = addressRequestDTO.street();
        this.number = addressRequestDTO.number();
        this.district = addressRequestDTO.district();
        this.city = addressRequestDTO.city();
        this.state = addressRequestDTO.state();

    }


}
