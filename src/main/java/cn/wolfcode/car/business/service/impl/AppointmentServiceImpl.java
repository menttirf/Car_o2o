package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.alibaba.druid.sql.visitor.functions.If;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements IAppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public TablePageInfo<Appointment> query(AppointmentQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Appointment>(appointmentMapper.selectForList(qo));
    }


    //添加
    @Override
    public void save(Appointment appointment) {
        appointment.setStatus(Appointment.STATUS_APPOINTMENT);
        appointment.setCreateTime(new Date());
        appointmentMapper.insert(appointment);
    }

    @Override
    public Appointment get(Long id) {
        return appointmentMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Appointment appointment) {
        Appointment obj = appointmentMapper.selectByPrimaryKey(appointment.getId());
        if (obj == null) {
            throw new BusinessException("参数异常");
        }
        if (!Appointment.STATUS_APPOINTMENT.equals(obj.getStatus())){
            throw new BusinessException("不是预约中不能修改");
        }
        appointmentMapper.updateByPrimaryKey(appointment);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            appointmentMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<Appointment> list() {
        return appointmentMapper.selectAll();
    }

    //取消
    @Override
    public void updateByCancelId(Long id) {
        Appointment appointment = appointmentMapper.selectByPrimaryKey(id);
        if (!Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())) {
            throw new BusinessException("服务预约单只有在预约中状态才允许执行取消逻辑");
        }
        appointmentMapper.updateByCancelId(id,Appointment.STATUS_CANCEL);
    }

    //到店
    @Override
    public void updateByArrivalId(Long id) {
        Appointment appointment = appointmentMapper.selectByPrimaryKey(id);
        if (!Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())) {
            throw new BusinessException("服务预约单只有在预约中状态才允许执行到店逻辑");
        }
        appointmentMapper.updateByArrival(id,Appointment.STATUS_ARRIVAL,new Date());
    }

    @Override
    public void changeStatus(Long id, Integer statusSettle) {
        appointmentMapper.changeStatus(id,statusSettle);
    }
}
