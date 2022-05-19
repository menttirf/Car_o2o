package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.StatementItemMapper;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class StatementItemServiceImpl implements IStatementItemService {

    @Autowired
    private StatementItemMapper statementItemMapper;

    @Autowired
    private IStatementService statementService;

    @Override
    public TablePageInfo<StatementItem> query(StatementQuery qo, Long statementId) {
        PageHelper.startPage(qo.getPageNum(),qo.getPageSize());
        return new TablePageInfo<>(statementItemMapper.query(qo,statementId));
    }

    //保存操作
    @Override
    public void saveItems(List<StatementItem> statementItems) {
        if (statementItems != null){
            //思路：先删除，后添加
            //获取优惠价格
            //删除最后一个优惠价格,数据封装
            StatementItem statementItem = statementItems.remove(statementItems.size() - 1);
            //删除关于id相关的数据
            statementItemMapper.deleteByPrimaryKey(statementItem.getStatementId());

            BigDecimal totalCount = new BigDecimal("0");   //计数
            BigDecimal totalAmount = new BigDecimal("0.00"); //总价

            //判断数据是否为0
            if (statementItems.size() > 0){
                //对数据进行计算处理
                for (StatementItem item : statementItems) {
                    //需求： 总价 = （单价 * 数量） * 优惠价格
                    // quantity 计算数量 (对应表total_quantity字段)
                    totalCount = totalCount.add(new BigDecimal(item.getItemQuantity()));
                    //计算总价 : (单价 * 数量） * 优惠价格 （对应表total_amount）字段
                    totalAmount = totalAmount.add(item.getItemPrice().multiply(new BigDecimal(item.getItemQuantity())));

                    //对遍历得到的数据进行添加
                    statementItemMapper.insert(item);
                }
                //对计算得到数据进行添加
                statementService.updateAmount(statementItem.getStatementId(),statementItem.getItemPrice(),totalAmount,totalCount);
            }else {
                statementService.updateAmount(statementItem.getStatementId(),new BigDecimal("0"),totalAmount,totalCount);
            }
        }
    }

    //支付
    @Override
    public void payStatement(Long statementId) {
        Statement statement = statementService.selectByStatementId(statementId);
        if (!Statement.STATUS_CONSUME.equals(statement.getStatus())){
            throw new BusinessException("不能重复支付");
        }
        //支付时间
        statement.setPayTime(new Date());
        //收款人
        statement.setPayeeId(ShiroUtils.getUserId());
        //状态为支付
        statement.setStatus(Statement.STATUS_PAID);
        statementService.updatePayStatement(statement);
    }
}
