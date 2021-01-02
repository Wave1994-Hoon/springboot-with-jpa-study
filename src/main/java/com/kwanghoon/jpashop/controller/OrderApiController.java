package com.kwanghoon.jpashop.controller;

import com.kwanghoon.jpashop.domain.Address;
import com.kwanghoon.jpashop.domain.Order;
import com.kwanghoon.jpashop.domain.OrderItem;
import com.kwanghoon.jpashop.domain.OrderStatus;
import com.kwanghoon.jpashop.repository.OrderRepository;
import com.kwanghoon.jpashop.repository.OrderSearch;
import com.kwanghoon.jpashop.repository.order.query.OrderFlatDto;
import com.kwanghoon.jpashop.repository.order.query.OrderItemQueryDto;
import com.kwanghoon.jpashop.repository.order.query.OrderQueryDto;
import com.kwanghoon.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;


/*
*
* 정리
* - 엔티티 조회
*   - 엔티티를 조회해서 그대로 반환: V1 엔티티 조회 후 DTO로 변환: V2 페치 조인으로 쿼리 수 최적화: V3 컬렉션 페이징과 한계 돌파: V3.1
*   - 컬렉션은 페치 조인시 페이징이 불가능
*   - ToOne 관계는 페치 조인으로 쿼리 수 최적화
*   - 컬렉션은 페치 조인 대신에 지연 로딩을 유지하고, hibernate.default_batch_fetch_size , @BatchSize 로 최적화
*
* - DTO 직접 조회
*   - JPA에서 DTO를 직접 조회: V4
*   - 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화: V5
*   - 플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환: V6
*
* - 권장 순서
*   1. 엔티티조회방식으로우선접근
*       1. 페치조인으로 쿼리 수를 최적화
*       2. 컬렉션 최적화
*           1. 페이징 필요 hibernate.default_batch_fetch_size , @BatchSize 로 최적화
*           2. 페이징 필요X -> 페치 조인 사용
*   2. 엔티티조회방식으로 해결이 안되면 DTO조회방식사용
*   3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate
*
*
* - 참고
* 엔티티 조회 방식은 페치 조인이나, hibernate.default_batch_fetch_size , @BatchSize 같이 코드를 거의 수정하지 않고, 옵션만 약간 변경해서,
* 다양한 성능 최적화를 시도할 수 있다. 반면에 DTO를 직 접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.
*
* - 참고
* 개발자는 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야 한다. 항상 그런 것은 아니지만, 보통 성능 최적화는 단순한 코드를 복잡한 코드로 몰고간다.
* 엔티티 조회 방식은 JPA가 많은 부분을 최적화 해주기 때문에, 단순한 코드를 유지하면서, 성능을 최적화 할 수 있다.
* 반면에 DTO 조회 방식은 SQL을 직접 다루는 것과 유사하기 때문에, 둘 사이에 줄타기를 해야 한다.
*
* - DTO 조회 방식의 선택지
*   - DTO로 조회하는 방법도 각각 장단이 있다. V4, V5, V6에서 단순하게 쿼리가 1번 실행된다고 V6이 항상 좋은 방법인 것은 아니다.
*   - V4는코드가단순하다. 특정주문한건만조회하면이방식을사용해도성능이잘나온다.예를들어서조회 한 Order 데이터가 1건이면 OrderItem을 찾기 위한 쿼리도 1번만 실행하면 된다.
*   - V5는 코드가 복잡하다. 여러 주문을 한꺼번에 조회하는 경우에는 V4 대신에 이것을 최적화한 V5 방식을 사 용해야 한다.
*     예를 들어서 조회한 Order 데이터가 1000건인데, V4 방식을 그대로 사용하면, 쿼리가 총 1 + 1000번 실행된다. 여기서 1은 Order 를 조회한 쿼리고,
*     1000은 조회된 Order의 row 수다. V5 방식 으로 최적화 하면 쿼리가 총 1 + 1번만 실행된다. 상황에 따라 다르겠지만 운영 환경에서 100배 이상의 성 능 차이가 날 수 있다.
*   - V6는 완전히 다른 접근방식이다. 쿼리 한번으로 최적화 되어서 상당히 좋아보이지만, Order를 기준으로 페 이징이 불가능하다. 실무에서는 이정도 데이터면 수백이나,
*     수천건 단위로 페이징 처리가 꼭 필요하므로, 이 경우 선택하기 어려운 방법이다. 그리고 데이터가 많으면 중복 전송이 증가해서 V5와 비교해서 성능 차이도 미비하다.
*
* - 참고
* 엔티티는 직접 캐싱을 하면 인된다. -> 영속성 컨텍스트에 관리가 되고 있기 때문에*
*/

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /*
    * V1
    * 엔티티를 직접 노출
    */
    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        /* 프록시 강제 초기화 */
        for (Order order : all) {
            order.getMember().getAddress();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(o -> o.getItem().getName());
        }

        return all;
    }

    /*
    * V2
    * 엔티티를 DTO로 변환 (fetch join x)
    */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        return orders
            .stream()
            .map(OrderDto::new)
            .collect(toList());
    }

    /*
     * V3
     * 페치 조인을 사용 해서 성능 최적화
     * distinct를 사용하여 중복 row 제거 (1:N inner join 시 중복 row 발생)
     *
     * 단점
     * Collection을 페치 조인할 경우 페이징은 사용 불가능
     * 엄밀하게는 페이징 기능은 수행은 되지만 DB에서 수행하지 않고 어플리케이션 메모리로 수행
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        return orders
            .stream()
            .map(OrderDto::new)
            .collect(toList());
    }

    /*
    * V3.1
    * 페치 조인(xToOne) + batch_size(xToMany)
    * batch_size 설정을 하면 xToOne도 fetch join 생략 가능, 단 쿼리는 추가 발생
    *
    * 장점
    * N+1 --> 1+1 로 최적화
    * xToMany는 fetch join을 사용하지 않기 때문에 데이터 뻥튀기 x --> DB 에서 애플리케이션으로 전송하는 데이터량 감소
    * 페이징이 가능하다 !!!
    */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders
            .stream()
            .map(OrderDto::new)
            .collect(toList());
    }

    /*
    * V4
    * JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1+NQuery)
    *
    * 장점
    * Query: 루트 1번, 컬렉션 N번 실행
    * ToOne 관계들을 먼저 조회, ToMany 관계는 각각 별도로 처리 (조안 시, row 수 증가하기 때문)
    * row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조히하고, ToMany는 별도 메서드로 조회
    * 데이터 select 양 감소
    *
    * 단점
    * 쿼리수 증가 (N + 1)
    */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    /*
    * V5
    * JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1+1Query)
    *
    * 장점
    * Query: 루트 1번, 컬렉션 1번
    * ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한꺼번에 조회
    * Map을 사용해서 매칭 성능 향상: O(1)
    * 데이터 select 양 감소
    *
    * 단점
    * 쿼리 수 증가 (1 + 1)
    */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    /*
    * V6
    * JPA에서 DTO로 바로 조회, 플랫 데이터
    *
    * 장점
    * Query: 1번
    *
    * 단점
    * 쿼리는 한 번 이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5보다 더 느릴 수 있다.
    * 애플리케이션에서 추가 작업이 크다.
    * 페이징 불가
    */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();


        return flats.stream()
            .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                    o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()), mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                o.getItemName(), o.getOrderPrice(), o.getCount()), toList()))).entrySet().stream()
            .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
            .collect(toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            /* OrderItem 을 그대로 노출하는 것이 아닌 DTO로 변환해서 전달 */
            orderItems = order
                .getOrderItems()
                .stream()
                .map(OrderItemDto::new)
                .collect(toList());;

        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName; // 상품 명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
