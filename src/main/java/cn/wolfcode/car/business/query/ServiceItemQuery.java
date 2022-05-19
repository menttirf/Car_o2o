package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceItemQuery extends QueryObject {
    private String serveName;
    private Integer serveClassify;

    //服务项
    private String name;
    private Integer carPackage;
    private Integer serviceCatalog;
}
