package com.kwanghoon.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/*
값 타입은 Getter 만 제공하고
immutable하게 설계되어야하기 때문에 생성할때만 값이 세팅되게 설계해야함
*/

@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /* Generate for JPA */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
