package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.util.List;

/**
 * 服务项接口
 */
public interface IStatementItemService {

    TablePageInfo<StatementItem> query(StatementQuery qo, Long statementId);

    void saveItems(List<StatementItem> statementItems);

    void payStatement(Long statementId);
}
