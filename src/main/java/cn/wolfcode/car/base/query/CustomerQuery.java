package cn.wolfcode.car.base.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位查询对象
 */
@Setter
@Getter
public class CustomerQuery extends QueryObject {
    private String customerName;
    private String customerPhone;
}
