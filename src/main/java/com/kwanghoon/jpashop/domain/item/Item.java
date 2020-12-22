package com.kwanghoon.jpashop.domain.item;

import com.kwanghoon.jpashop.exception.NotEnoughStockException;
import com.kwanghoon.jpashop.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    /* 비즈니스 로직 -> 데이터를 가지고 있는 쪽에 비즈니스 로직이 있는게 응집력(객체지향)이 있다. */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int realStock = this.stockQuantity - quantity;
        if (realStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = realStock;
    }
}
