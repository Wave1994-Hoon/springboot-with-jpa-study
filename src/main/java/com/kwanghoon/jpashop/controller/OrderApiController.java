package com.kwanghoon.jpashop.controller;

import com.kwanghoon.jpashop.domain.Order;
import com.kwanghoon.jpashop.domain.OrderItem;
import com.kwanghoon.jpashop.repository.OrderRepository;
import com.kwanghoon.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

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
}
