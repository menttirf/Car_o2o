package cn.wolfcode.car.business.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class StatementItem {
    private Long id;

    private Long statementId;

    private Long itemId;

    private String itemName;

    private BigDecimal itemPrice;

    private Long itemQuantity;
}