package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Employee;
import cn.wolfcode.car.business.query.EmployeeQuery;

import java.util.List;

public interface EmployeeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Employee record);

    Employee selectByPrimaryKey(Long id);

    List<Employee> selectAll();

    int updateByPrimaryKey(Employee record);

    List<Employee> selectForList(EmployeeQuery qo);

    void delete(Long id);

    void addSave(Employee employee);
}