package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class StatementServiceImpl implements IStatementService {

    @Autowired
    private StatementMapper statementMapper;

    @Autowired
    private IAppointmentService appointmentService;

    @Override
    public TablePageInfo<Statement> query(StatementQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Statement>(statementMapper.selectForList(qo));
    }


    //添加
    @Override
    public void save(Statement statement) {
        statement.setStatus(Statement.STATUS_CONSUME);
        statementMapper.insert(statement);
    }

    @Override
    public Statement get(Long id) {
        return statementMapper.selectByPrimaryKey(id);
    }


    //编辑
    @Override
    public void update(Statement statement) {
        Statement byPrimaryKey = statementMapper.selectByPrimaryKey(statement.getId());
        if (byPrimaryKey == null || !Statement.STATUS_CONSUME.equals(statement.getStatus())){
            throw new BusinessException("产数异常");
        }
        statementMapper.updateByPrimaryKey(statement);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            statementMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<Statement> list() {
        return statementMapper.selectAll();
    }

    @Override
    public Statement selectByStatementId(Long statementId) {
        return statementMapper.selectByStatementId(statementId);
    }

    @Override
    public void updateAmount(Long statementId, BigDecimal itemPrice, BigDecimal totalAmount, BigDecimal totalCount) {
        statementMapper.updateAmount(statementId,itemPrice,totalAmount,totalCount);
    }

    @Override
    public void updatePayStatement(Statement statement) {
        statementMapper.updatePayStatement(statement);
    }

    @Override
    public Statement generateStatement(Long id) {
        Appointment appointment = appointmentService.get(id);
        Statement statement = new Statement();
        //状态为到店
        if (Appointment.STATUS_ARRIVAL.equals(appointment.getStatus())){
            //生成结算单
            statement.setCustomerName(appointment.getCustomerName());
            statement.setCustomerPhone(appointment.getCustomerPhone());
            statement.setAppointmentId(id);
            statement.setLicensePlate(appointment.getLicensePlate());
            statement.setCarSeries(appointment.getCarSeries());
            statement.setServiceType((long) appointment.getServiceType());
            statement.setActualArrivalTime(appointment.getActualArrivalTime());
            statement.setStatus(Statement.STATUS_CONSUME);
            statement.setInfo(appointment.getInfo());
            statement.setCreateTime(new Date());
            //将生成的结束单添加
            statementMapper.insert(statement);
            //修改养修预约状态
            appointmentService.changeStatus(appointment.getId(),Appointment.STATUS_SETTLE);
        }else {
            //查询结算单
            statement = statementMapper.selectByAppointmentId(id);
        }
        return statement;
    }
}
