package com.kwanghoon.jpashop.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") // Order Table 에 있는 member 필드에 의해서 맵핑이 된거임
    private List<Order> orders = new ArrayList<>();
}
