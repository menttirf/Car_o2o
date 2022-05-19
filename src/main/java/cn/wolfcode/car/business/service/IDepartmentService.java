package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.Department;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface IDepartmentService {

    /**
     * 分页
     * @param qo
     * @return
     */
    //TablePageInfo<Department> query(DepartmentQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    Department get(Long id);


    /**
     * 保存
     * @param department
     */
    void save(Department department);

  
    /**
     * 更新
     * @param department
     */
    void update(Department department);

    /**
     *  批量删除
     * @param ids
     */
    void deleteBatch(String ids);

    /**
     * 查询全部
     * @return
     */
    List<Department> list();
}
