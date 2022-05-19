package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Department;
import cn.wolfcode.car.business.mapper.DepartmentMapper;
import cn.wolfcode.car.business.service.IDepartmentService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements IDepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;


    //添加
    @Override
    public void save(Department department) {
        departmentMapper.insert(department);
    }

    @Override
    public Department get(Long id) {
        return departmentMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Department department) {
        departmentMapper.updateByPrimaryKey(department);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            departmentMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<Department> list() {
        return departmentMapper.selectAll();
    }
}
