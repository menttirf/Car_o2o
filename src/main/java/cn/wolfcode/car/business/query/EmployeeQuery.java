package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
public class EmployeeQuery extends QueryObject {

    private String keyword;

    private Integer tube;

    //是否冻结
    private Integer freeze;

    private Integer dept;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date beginTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
}
