package com.kwanghoon.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    /* 화면에 의존성을 가지고 있음 -> 계층 분리 x */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
            "select new com.kwanghoon.jpashop.repository.order.simplequery.OrderSimpleQueryDto(" +
                "o.id, m.name, " +
                "o.orderDate, " +
                "o.status, " +
                "d.address) " +
                "from Order o " +
                "join o.member m " +
                "join o.delivery d"
            , OrderSimpleQueryDto.class).getResultList();
    }
}
