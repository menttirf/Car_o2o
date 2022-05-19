package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.query.AppointmentQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface AppointmentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Appointment record);

    Appointment selectByPrimaryKey(Long id);

    List<Appointment> selectAll();

    int updateByPrimaryKey(Appointment record);

    List<Appointment> selectForList(AppointmentQuery qo);


    int updateByCancelId(@Param("id") Long id, @Param("statusCancel") Integer statusCancel);

    int updateByArrival(@Param("arrival") Long id, @Param("statusArrival") Integer statusArrival, @Param("arrivalDate") Date date);

    int changeStatus(@Param("id") Long id, @Param("statusSettle") Integer statusSettle);
}