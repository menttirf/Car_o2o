package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.Employee;
import cn.wolfcode.car.business.query.EmployeeQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface IEmployeeService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<Employee> query(EmployeeQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    Employee get(Long id);


    /**
     * 保存
     * @param employee
     */
    void save(Employee employee);

  
    /**
     * 更新
     * @param employee
     */
    void update(Employee employee);

    /**
     *  批量删除
     * @param ids
     */
    void deleteBatch(Long ids);

    /**
     * 查询全部
     * @return
     */
    List<Employee> list();


    void delete(Long id);

    void addSave(Employee employee);

}
