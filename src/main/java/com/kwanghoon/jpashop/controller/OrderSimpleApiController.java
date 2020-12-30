package com.kwanghoon.jpashop.controller;

import com.kwanghoon.jpashop.domain.Order;
import com.kwanghoon.jpashop.domain.OrderSearch;
import com.kwanghoon.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
* XToOne(ManyToOne, OneToOne)
* Order
* Order -> Member
* Order -> Delivery
*/
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;


    /*
    * 서로 toString method 호출하여 무한 루프 발생
    * 양방향 관계 문제 발생 -> @JsonIgnore
    * Hibernate5Module 모듈 등록, LAZY=null 처리
    */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        /* Eager 로 설정 시, JPQL은 SQL로 번역되기 때문에 성능 최적화가 이루어지지 않는다. N+1 문제 발생 */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // Lazy 강제 초기화
        all.forEach(order -> {
            order.getMember().getName();
            order.getDelivery().getAddress();
        });

        return all;
    }
}
