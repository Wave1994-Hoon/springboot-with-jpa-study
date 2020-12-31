package com.kwanghoon.jpashop.controller;

import com.kwanghoon.jpashop.domain.Address;
import com.kwanghoon.jpashop.domain.Order;
import com.kwanghoon.jpashop.repository.OrderSearch;
import com.kwanghoon.jpashop.domain.OrderStatus;
import com.kwanghoon.jpashop.repository.OrderRepository;
import com.kwanghoon.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import com.kwanghoon.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /*
    * V1
    * - 엔티티 직접 반환
    *
    * 문제점
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

    /*
    * V2
    * - 엔티티 직접 반환 대신 DTO 반환
    *
    * 문제점
    * 지연로딩으로 너무 많은 SQL Query 발생 (N+1)
    *
    * 참고
    * 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL을 실행한다.
    * 따라서 같은 영속성 컨텍스트에 이미 로딩한 회원 엔티티를 추가로 조회하면 SQL을 실행하지 않는다.
    */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        return orders
            .stream()
            .map(SimpleOrderDto::new)
            .collect(Collectors.toList());
    }

    /*
    * V3
    * 페치조인으로 최적화
    * 엔티티를 페치 조인을 사용하여 쿼리 한 번에 조회
    */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        return orders
            .stream()
            .map(SimpleOrderDto::new)
            .collect(Collectors.toList());
    }

    /*
    * V4
    * Select 절에서 원하는 데이터만 선택해서 조회
    * 장점: 약간의 성능 최적화 (where, join 먼저 최적화 하고 진행하는 걸 권장)
    * 단점: 재사용성 힘듬 -> 재사용성은 V3가 더 낫다.
    *
    * 정리
    * Entity를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다. 둘 중 상황에 따라서 더 나은 방법 선택
    * Entity로 조회하면 Repository 재사용성이 좋고, 개발도 단순해짐
    *
    * 쿼리 방식 선택 권장 순서
    * 1. 우선 Entity를 DTO로 변환하는 방법 선택: V2
    * 2. 필요하면 페치 조인으로 성능을 최적화 한다 -> 대부분의 성능 이슈 해결: V3
    * 3. 그래도 안된다면 DTO로 직접 조회(Select)하는 방법을 사용: V4
    * 4. 최후의 방법은 JPA가 재공하는 네이트브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용
    */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate(); // Lazy 초기화
            orderStatus = order.getStatus();
            address = order.getMember().getAddress(); // Lazy 초기화
        }
    }
}
