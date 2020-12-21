package com.kwanghoon.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") // Order Table 에 있는 member 필드에 의해서 맵핑이 된거임
    private List<Order> orders = new ArrayList<>(); /* Best practice : null 문제에서 안전
                                                    Hibernate는 persist(영속화) 할 때, 컬렉션을 감싸서 Hinbernate가 제공하는 내장 컬렉션으로 변경
                                                    */
}
