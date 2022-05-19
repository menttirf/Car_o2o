package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppointmentQuery extends QueryObject {
    private String cmiName;
    private String cmiPhone;
    private Integer cmiStatus;
}
